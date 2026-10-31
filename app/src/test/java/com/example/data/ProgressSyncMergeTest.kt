package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressSyncMergeTest {
    private fun record(
        progress: String = "local",
        progressRevision: Long = 1,
        progressActor: String = "device-a",
        favorite: Boolean = false,
        favoriteRevision: Long = 1,
        favoriteActor: String = "device-a",
        deleted: Boolean = false,
        deletionRevision: Long = 0,
        deletionActor: String = "legacy",
        timestamp: Long = 1,
        restoredDeletionRevision: Long = 0,
        restoredDeletionActor: String = "legacy"
    ) = MandalaProgressEntity(
        mandalaId = "mandala-1",
        coloredRegionsJson = progress,
        percentCompleted = if (progress == "local") 0.2f else 0.8f,
        isFavorite = favorite,
        isCompleted = false,
        lastEditedTimestamp = timestamp,
        progressRevision = progressRevision,
        progressActor = progressActor,
        favoriteRevision = favoriteRevision,
        favoriteActor = favoriteActor,
        isDeleted = deleted,
        deletionRevision = deletionRevision,
        deletionActor = deletionActor,
        restoredDeletionRevision = restoredDeletionRevision,
        restoredDeletionActor = restoredDeletionActor
    )

    @Test fun favoriteChangeDoesNotOverwriteNewerProgress() {
        val local = record(progress = "new-progress", progressRevision = 8, favorite = false, favoriteRevision = 2)
        val remote = record(progress = "old-progress", progressRevision = 4, favorite = true, favoriteRevision = 9)

        val merged = ProgressSyncMerge.merge(local, remote)!!

        assertEquals("new-progress", merged.coloredRegionsJson)
        assertTrue(merged.isFavorite)
        assertEquals(8L, merged.progressRevision)
        assertEquals(9L, merged.favoriteRevision)
    }

    @Test fun newerProgressWinsIndependentlyOfFavoriteRevision() {
        val local = record(progress = "older", progressRevision = 3, favoriteRevision = 20)
        val remote = record(progress = "newer", progressRevision = 4, favoriteRevision = 2)

        val merged = ProgressSyncMerge.merge(local, remote)!!

        assertEquals("newer", merged.coloredRegionsJson)
        assertFalse(merged.isFavorite)
    }

    @Test fun tombstoneDominatesOlderFields() {
        val local = record(progress = "saved", progressRevision = 7, favoriteRevision = 6)
        val remote = record(progress = "saved", progressRevision = 7, favoriteRevision = 6,
            deleted = true, deletionRevision = 8, deletionActor = "device-b")

        val merged = ProgressSyncMerge.merge(local, remote)!!

        assertTrue(merged.isDeleted)
        assertEquals(8L, merged.deletionRevision)
    }

    @Test fun laterEditCanReviveObservedTombstone() {
        val tombstone = record(deleted = true, deletionRevision = 8, deletionActor = "device-b")
        val edited = record(progress = "after-delete", progressRevision = 9, progressActor = "device-a", restoredDeletionRevision = 8, restoredDeletionActor = "device-b")

        val merged = ProgressSyncMerge.merge(tombstone, edited)!!

        assertFalse(merged.isDeleted)
        assertEquals("after-delete", merged.coloredRegionsJson)
        assertEquals(8L, merged.deletionRevision)
    }

    @Test fun unobservedConcurrentEditCannotResurrectTombstoneEvenWithHigherCounter() {
        val tombstone = record(deleted = true, deletionRevision = 8, deletionActor = "device-b")
        val offlineEdit = record(progress = "offline-edit", progressRevision = 20, progressActor = "device-a")

        val merged = ProgressSyncMerge.merge(tombstone, offlineEdit)!!

        assertTrue("A larger counter is not proof that the device observed deletion", merged.isDeleted)
        assertEquals("offline-edit", merged.coloredRegionsJson)
    }

    @Test fun newerTombstoneOverridesRestorationOfAnOlderDeletion() {
        val restored = record(
            progress = "restored-old",
            progressRevision = 9,
            progressActor = "device-a",
            deleted = true,
            deletionRevision = 8,
            deletionActor = "device-b",
            restoredDeletionRevision = 8,
            restoredDeletionActor = "device-b"
        )
        val newerDelete = record(
            progress = "restored-old",
            progressRevision = 9,
            progressActor = "device-a",
            deleted = true,
            deletionRevision = 10,
            deletionActor = "device-c"
        )

        val merged = ProgressSyncMerge.merge(restored, newerDelete)!!
        assertTrue(merged.isDeleted)
        assertEquals(10L, merged.deletionRevision)
        assertEquals("device-c", merged.deletionActor)
    }

    @Test fun concurrentCountersUseActorIdAsDeterministicTieBreaker() {
        val a = record(progress = "from-a", progressRevision = 12, progressActor = "device-a")
        val b = record(progress = "from-b", progressRevision = 12, progressActor = "device-b")

        val ab = ProgressSyncMerge.merge(a, b)!!
        val ba = ProgressSyncMerge.merge(b, a)!!

        assertEquals("from-b", ab.coloredRegionsJson)
        assertEquals(ab, ba)
    }

    @Test fun nullSidesAreHandledWithoutChangingRecord() {
        val existing = record()

        assertEquals(existing, ProgressSyncMerge.merge(existing, null))
        assertEquals(existing, ProgressSyncMerge.merge(null, existing))
        assertNull(ProgressSyncMerge.merge(null, null))
        assertNotNull(ProgressSyncMerge.merge(existing, existing))
    }
}
