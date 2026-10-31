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
    fun installationActorIdIsStableAndStoredOutsideBackupData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val first = DeviceIdentity.getOrCreate(context)
        val second = DeviceIdentity.getOrCreate(context)

        assertEquals(first, second)
        assertTrue(first.isNotBlank())
        assertTrue(context.noBackupFilesDir.resolve("mandala_sync_actor_id").isFile)
    }
}
