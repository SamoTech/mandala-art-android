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
        restoredDeletionActor: String = "legacy",
        restorationEventRevision: Long = 0,
        restorationEventActor: String = "legacy"
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
        restoredDeletionActor = restoredDeletionActor,
        restorationEventRevision = restorationEventRevision,
        restorationEventActor = restorationEventActor
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

    @Test fun conflictingEqualRevisionProgressPayloadIsChosenAtomically() {
        val local = record(
            progress = "aaa",
            progressRevision = 5,
            progressActor = "same-device"
        ).copy(percentCompleted = 0.1f, isCompleted = false)
        val remote = record(
            progress = "zzz",
            progressRevision = 5,
            progressActor = "same-device"
        ).copy(percentCompleted = 0.9f, isCompleted = true)

        val merged = ProgressSyncMerge.merge(local, remote)!!

        assertEquals("zzz", merged.coloredRegionsJson)
        assertEquals(0.9f, merged.percentCompleted, 0.0001f)
        assertTrue(merged.isCompleted)
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
        val edited = record(progress = "after-delete", progressRevision = 9, progressActor = "device-a", restoredDeletionRevision = 8, restoredDeletionActor = "device-b", restorationEventRevision = 9, restorationEventActor = "device-a")

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

    @Test fun explicitRestoreProofSurvivesHigherConcurrentForeignEdit() {
        val observedTombstone = record(deleted = true, deletionRevision = 8, deletionActor = "device-b")
        val restoreEvent = record(
            progress = "restored",
            progressRevision = 9,
            progressActor = "device-a",
            restoredDeletionRevision = 8,
            restoredDeletionActor = "device-b",
            restorationEventRevision = 9,
            restorationEventActor = "device-a"
        )
        val unobservedForeignEdit = record(
            progress = "offline-newer",
            progressRevision = 20,
            progressActor = "device-c"
        )

        val merged = ProgressSyncMerge.merge(
            ProgressSyncMerge.merge(observedTombstone, restoreEvent),
            unobservedForeignEdit
        )!!

        assertFalse("A real restore event remains valid even when a foreign field edit wins", merged.isDeleted)
        assertEquals("offline-newer", merged.coloredRegionsJson)
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
            restoredDeletionActor = "device-b",
            restorationEventRevision = 9,
            restorationEventActor = "device-a"
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

    @Test fun proofForNewerDeletionIsNotMaskedByOlderHighCounterProof() {
        val olderRestoreProof = record(
            progress = "older-restored",
            progressRevision = 100,
            progressActor = "device-a",
            restoredDeletionRevision = 8,
            restoredDeletionActor = "device-b",
            restorationEventRevision = 100,
            restorationEventActor = "device-a"
        )
        val newerTombstone = record(
            progress = "before-new-delete",
            progressRevision = 11,
            progressActor = "device-c",
            deleted = true,
            deletionRevision = 12,
            deletionActor = "device-c"
        )
        val restoreNewerDeletion = record(
            progress = "newly-restored",
            progressRevision = 13,
            progressActor = "device-d",
            deleted = false,
            deletionRevision = 12,
            deletionActor = "device-c",
            restoredDeletionRevision = 12,
            restoredDeletionActor = "device-c",
            restorationEventRevision = 13,
            restorationEventActor = "device-d"
        )

        val firstMerge = ProgressSyncMerge.merge(olderRestoreProof, newerTombstone)!!
        val finalMerge = ProgressSyncMerge.merge(firstMerge, restoreNewerDeletion)!!

        assertFalse(finalMerge.isDeleted)
        assertEquals(12L, finalMerge.deletionRevision)
        assertEquals(12L, finalMerge.restoredDeletionRevision)
        assertEquals("device-c", finalMerge.restoredDeletionActor)
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

    @Test
    fun concurrentLocalEditIsUploadedBeforeReconciliationCompletes() = kotlinx.coroutines.runBlocking {
        var local = record(progress = "before-sync", progressRevision = 1)
        var cloud: MandalaProgressEntity? = null
        var attempts = 0

        ProgressSyncReconciler.reconcile(
            readLocal = { local },
            syncRemote = { candidate ->
                attempts++
                val merged = ProgressSyncMerge.merge(candidate, cloud)
                cloud = merged
                if (attempts == 1) {
                    // Simulate an editor save after the sync read but before
                    // the first cloud transaction has completed.
                    local = record(
                        progress = "edited-during-sync",
                        progressRevision = 2,
                        timestamp = 2
                    )
                }
                merged
            },
            applyRemote = { incoming ->
                local = ProgressSyncMerge.merge(local, incoming)!!
            }
        )

        assertEquals(2, attempts)
        assertEquals("edited-during-sync", cloud!!.coloredRegionsJson)
        assertEquals(2L, cloud!!.progressRevision)
        assertEquals(cloud, local)
    }

    @Test
    fun continuouslyChangingLocalRowFailsInsteadOfReportingSyncSuccess() = kotlinx.coroutines.runBlocking {
        var local = record(progress = "edit-0", progressRevision = 1)
        var attempts = 0

        val error = runCatching {
            ProgressSyncReconciler.reconcile(
                readLocal = { local },
                syncRemote = { candidate ->
                    attempts++
                    val merged = candidate
                    local = record(
                        progress = "edit-$attempts",
                        progressRevision = (attempts + 1).toLong(),
                        timestamp = (attempts + 1).toLong()
                    )
                    merged
                },
                applyRemote = {},
                maxAttempts = 3
            )
        }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertEquals(3, attempts)
    }

}
