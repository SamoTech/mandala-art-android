package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RevisionedProgressDaoTest {
    @Test
    fun progressAndFavoriteMutationsUseIndependentRevisions() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java).build()
        try {
            val dao = database.mandalaDao()
            val repository = MandalaRepository(dao, "device-local")

            repository.toggleFavorite("lotus", currentFav = false)
            repository.saveProgress("lotus", mapOf("petal" to 123L), totalRegions = 10)

            val saved = dao.getProgressOnce("lotus")!!
            assertTrue(saved.isFavorite)
            assertEquals(1L, saved.favoriteRevision)
            assertEquals(2L, saved.progressRevision)
            assertEquals("device-local", saved.favoriteActor)
            assertEquals("device-local", saved.progressActor)
        } finally {
            database.close()
        }
    }

    @Test
    fun staleCloudFavoriteAndProgressCannotOverwriteIndependentNewerFields() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java).build()
        try {
            val dao = database.mandalaDao()
            val local = MandalaProgressEntity(
                mandalaId = "lotus",
                coloredRegionsJson = "new-color",
                percentCompleted = 0.7f,
                isFavorite = false,
                isCompleted = false,
                progressRevision = 9,
                progressActor = "device-a",
                favoriteRevision = 2,
                favoriteActor = "device-a"
            )
            dao.upsertProgress(local)
            dao.mergeProgress(
                MandalaProgressEntity(
                    mandalaId = "lotus",
                    coloredRegionsJson = "old-color",
                    percentCompleted = 0.1f,
                    isFavorite = true,
                    isCompleted = false,
                    progressRevision = 3,
                    progressActor = "device-b",
                    favoriteRevision = 8,
                    favoriteActor = "device-b"
                )
            )

            val merged = dao.getProgressOnce("lotus")!!
            assertEquals("new-color", merged.coloredRegionsJson)
            assertTrue(merged.isFavorite)
            assertEquals(9L, merged.progressRevision)
            assertEquals(8L, merged.favoriteRevision)
        } finally {
            database.close()
        }
    }

    @Test
    fun deletionTombstoneSurvivesStaleCloudCopyAndCanBeRevivedByLaterEdit() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java).build()
        try {
            val dao = database.mandalaDao()
            val repository = MandalaRepository(dao, "device-local")
            repository.saveProgress("lotus", mapOf("petal" to 123L), totalRegions = 10)
            repository.deleteProgress("lotus")

            val tombstone = dao.getProgressOnce("lotus")!!
            assertTrue(tombstone.isDeleted)
            assertNull(repository.getProgressOnce("lotus"))

            dao.mergeProgress(tombstone.copy(isDeleted = false, deletionRevision = 0, deletionActor = "legacy"))
            assertTrue("A stale cloud copy must not resurrect a deleted row", dao.getProgressOnce("lotus")!!.isDeleted)

            repository.saveProgress("lotus", mapOf("petal" to 456L), totalRegions = 10)
            val revived = dao.getProgressOnce("lotus")!!
            assertFalse(revived.isDeleted)
            assertTrue(revived.progressRevision > revived.deletionRevision)
            assertEquals(tombstone.deletionRevision, revived.restoredDeletionRevision)
            assertEquals(tombstone.deletionActor, revived.restoredDeletionActor)
            assertEquals(revived.progressRevision, revived.restorationEventRevision)
            assertEquals("device-local", revived.restorationEventActor)
        } finally {
            database.close()
        }
    }

    @Test
    fun deletingUncachedArtworkCreatesSyncTombstone() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java).build()
        try {
            val repository = MandalaRepository(database.mandalaDao(), "device-local")
            repository.deleteProgress("cloud-only-artwork")

            val tombstone = database.mandalaDao().getProgressOnce("cloud-only-artwork")
            assertNotNull(tombstone)
            assertTrue(tombstone!!.isDeleted)
            assertEquals(1L, tombstone.deletionRevision)
            assertEquals("device-local", tombstone.deletionActor)
        } finally {
            database.close()
        }
    }


    @Test
    fun identicalMandalaIdsRemainIsolatedAcrossAnonymousAndFirebaseAccounts() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java).build()
        try {
            val dao = database.mandalaDao()
            val repository = MandalaRepository(dao, "device-local")

            repository.saveProgress("lotus", mapOf("petal" to 111L), totalRegions = 10)
            repository.setOwnerUid("firebase-user-a")
            repository.saveProgress("lotus", mapOf("petal" to 222L), totalRegions = 10)

            repository.setOwnerUid("firebase-user-b")
            assertNull("A different account must not read user A's progress", repository.getProgressOnce("lotus"))
            repository.saveProgress("lotus", mapOf("petal" to 333L), totalRegions = 10)

            repository.setOwnerUid("firebase-user-a")
            assertEquals(222L, repository.getProgressOnce("lotus")!!.coloredRegions["petal"])
            repository.setOwnerUid("firebase-user-b")
            assertEquals(333L, repository.getProgressOnce("lotus")!!.coloredRegions["petal"])

            assertEquals(111L, dao.getProgressOnce("lotus", "")!!.let {
                MandalaRepository.deserializeColors(it.coloredRegionsJson)["petal"]
            })
            assertEquals(222L, dao.getProgressOnce("lotus", "firebase-user-a")!!.let {
                MandalaRepository.deserializeColors(it.coloredRegionsJson)["petal"]
            })
            assertEquals(333L, dao.getProgressOnce("lotus", "firebase-user-b")!!.let {
                MandalaRepository.deserializeColors(it.coloredRegionsJson)["petal"]
            })
        } finally {
            database.close()
        }
    }

    @Test
    fun deletingOneAccountPartitionDoesNotDeleteAnotherAccountsProgress() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java).build()
        try {
            val dao = database.mandalaDao()
            dao.upsertProgress(
                MandalaProgressEntity(
                    mandalaId = "lotus",
                    ownerUid = "firebase-user-a",
                    coloredRegionsJson = "petal:123",
                    percentCompleted = 0.1f,
                    isFavorite = false,
                    isCompleted = false
                )
            )
            dao.upsertProgress(
                MandalaProgressEntity(
                    mandalaId = "lotus",
                    ownerUid = "firebase-user-b",
                    coloredRegionsJson = "petal:456",
                    percentCompleted = 0.2f,
                    isFavorite = false,
                    isCompleted = false
                )
            )

            dao.markDeleted("lotus", 10L, "device-a", "firebase-user-a")

            assertTrue(dao.getProgressOnce("lotus", "firebase-user-a")!!.isDeleted)
            assertEquals("petal:456", dao.getProgressOnce("lotus", "firebase-user-b")!!.coloredRegionsJson)
        } finally {
            database.close()
        }
    }


    @Test
    fun pendingEditorWriteStaysInTheAccountThatOpenedTheEditor() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java).build()
        try {
            val repository = MandalaRepository(database.mandalaDao(), "device-local")
            repository.setOwnerUid("firebase-user-a")
            val editorOwnerUid = repository.currentOwnerUid

            repository.setOwnerUid("firebase-user-b")
            repository.saveProgress(
                "lotus",
                mapOf("petal" to 777L),
                totalRegions = 10,
                ownerUid = editorOwnerUid
            )

            assertNull(repository.getProgressOnce("lotus"))
            assertEquals(
                "firebase-user-a",
                database.mandalaDao().getProgressOnce("lotus", "firebase-user-a")!!.ownerUid
            )
            assertNull(database.mandalaDao().getProgressOnce("lotus", "firebase-user-b"))
        } finally {
            database.close()
        }
    }

    @Test
    fun cloudMergeKeepsAuthenticatedOwnerPartition() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java).build()
        try {
            val dao = database.mandalaDao()
            val owner = "firebase-user-a"
            val row = MandalaProgressEntity(
                mandalaId = "lotus",
                ownerUid = owner,
                coloredRegionsJson = "petal:111",
                percentCompleted = 0.1f,
                isFavorite = false,
                isCompleted = false,
                progressRevision = 1L,
                progressActor = "device-a"
            )
            dao.upsertProgress(row)
            dao.mergeProgress(row.copy(coloredRegionsJson = "petal:222", progressRevision = 2L))

            assertEquals(owner, dao.getProgressOnce("lotus", owner)!!.ownerUid)
            assertNull(dao.getProgressOnce("lotus", ""))
        } finally {
            database.close()
        }
    }

    @Test
    fun deletingOneAccountLocalPartitionPreservesAnonymousAndOtherAccounts() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java).build()
        try {
            val dao = database.mandalaDao()
            listOf("", "firebase-user-a", "firebase-user-b").forEachIndexed { index, owner ->
                dao.upsertProgress(
                    MandalaProgressEntity(
                        mandalaId = "lotus",
                        ownerUid = owner,
                        coloredRegionsJson = "petal:$index",
                        percentCompleted = 0.1f,
                        isFavorite = false,
                        isCompleted = false
                    )
                )
            }

            dao.deleteAllProgress("firebase-user-a")

            assertNull(dao.getProgressOnce("lotus", "firebase-user-a"))
            assertEquals("petal:0", dao.getProgressOnce("lotus", "")!!.coloredRegionsJson)
            assertEquals("petal:2", dao.getProgressOnce("lotus", "firebase-user-b")!!.coloredRegionsJson)
        } finally {
            database.close()
        }
    }

    @Test
    fun installationActorIdIsStableAndStoredOutsideBackupData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val first = DeviceIdentity.getOrCreate(context)
        val second = DeviceIdentity.getOrCreate(context)

        assertEquals(first, second)
        assertTrue(first.isNotBlank())
        assertTrue(context.noBackupFilesDir.resolve("mandala_sync_actor_id").isFile)
    }

    @Test
    fun favoriteMutationPersistsTimestampAndPreservesIndependentProgressSnapshot() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java).build()
        try {
            val dao = database.mandalaDao()
            val original = MandalaProgressEntity(
                mandalaId = "favorite-timestamp",
                coloredRegionsJson = "petal:123;center:456",
                percentCompleted = 0.65f,
                isFavorite = false,
                isCompleted = false,
                lastEditedTimestamp = 100L,
                progressRevision = 4L,
                progressActor = "progress-device",
                favoriteRevision = 2L,
                favoriteActor = "old-favorite-device"
            )
            dao.upsertProgress(original)

            dao.updateFavoriteField(
                mandalaId = original.mandalaId,
                isFavorite = true,
                timestamp = 200L,
                actor = "favorite-device"
            )

            val favorited = dao.getProgressOnce(original.mandalaId)!!
            assertTrue(favorited.isFavorite)
            assertEquals(200L, favorited.lastEditedTimestamp)
            assertEquals(5L, favorited.favoriteRevision)
            assertEquals("favorite-device", favorited.favoriteActor)
            assertEquals(original.coloredRegionsJson, favorited.coloredRegionsJson)
            assertEquals(original.percentCompleted, favorited.percentCompleted, 0.0001f)
            assertEquals(original.isCompleted, favorited.isCompleted)
            assertEquals(original.progressRevision, favorited.progressRevision)
            assertEquals(original.progressActor, favorited.progressActor)

            dao.updateFavoriteField(
                mandalaId = original.mandalaId,
                isFavorite = false,
                timestamp = 300L,
                actor = "favorite-device"
            )

            val unfavorited = dao.getProgressOnce(original.mandalaId)!!
            assertFalse(unfavorited.isFavorite)
            assertEquals(300L, unfavorited.lastEditedTimestamp)
            assertEquals(6L, unfavorited.favoriteRevision)
            assertEquals(original.progressRevision, unfavorited.progressRevision)
            assertEquals(original.coloredRegionsJson, unfavorited.coloredRegionsJson)
        } finally {
            database.close()
        }
    }

    @Test
    fun persistedRestoreProofCannotOverrideANewerTombstone() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java).build()
        try {
            val dao = database.mandalaDao()
            val tombstone = MandalaProgressEntity(
                mandalaId = "restore-race",
                coloredRegionsJson = "before-delete",
                percentCompleted = 0.4f,
                isFavorite = false,
                isCompleted = false,
                isDeleted = true,
                deletionRevision = 8L,
                deletionActor = "device-b"
            )
            val restore = tombstone.copy(
                coloredRegionsJson = "restored",
                percentCompleted = 0.8f,
                progressRevision = 9L,
                progressActor = "device-a",
                isDeleted = false,
                restoredDeletionRevision = 8L,
                restoredDeletionActor = "device-b",
                restorationEventRevision = 9L,
                restorationEventActor = "device-a"
            )
            dao.upsertProgress(tombstone)
            dao.mergeProgress(restore)

            val restored = dao.getProgressOnce("restore-race")!!
            assertFalse(restored.isDeleted)
            assertEquals("restored", restored.coloredRegionsJson)
            assertEquals(8L, restored.restoredDeletionRevision)
            assertEquals("device-b", restored.restoredDeletionActor)
            assertEquals(9L, restored.restorationEventRevision)

            val newerTombstone = restored.copy(
                isDeleted = true,
                deletionRevision = 10L,
                deletionActor = "device-c"
            )
            dao.mergeProgress(newerTombstone)
            // A delayed copy of the old restore must not resurrect the newer deletion.
            dao.mergeProgress(restore)

            val finalRow = dao.getProgressOnce("restore-race")!!
            assertTrue(finalRow.isDeleted)
            assertEquals(10L, finalRow.deletionRevision)
            assertEquals("device-c", finalRow.deletionActor)
        } finally {
            database.close()
        }
    }

}
