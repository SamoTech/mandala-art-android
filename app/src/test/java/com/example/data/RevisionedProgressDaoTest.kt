package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
