package com.example.data

/**
 * Deterministic, field-level merge for offline-first progress records.
 *
 * Revisions are Lamport counters scoped to a field. The actor ID breaks ties
 * between concurrent writes without relying on device wall clocks.
 */
data class SyncRevision(
    val counter: Long,
    val actor: String
) : Comparable<SyncRevision> {
    override fun compareTo(other: SyncRevision): Int {
        val counterOrder = counter.compareTo(other.counter)
        return if (counterOrder != 0) counterOrder else actor.compareTo(other.actor)
    }
}

object ProgressSyncMerge {
    private data class ProgressFields(
        val colorsJson: String,
        val percentCompleted: Float,
        val isCompleted: Boolean
    )

    private data class RestorationProof(
        val deletedRevision: Long,
        val deletedActor: String,
        val eventRevision: Long,
        val eventActor: String
    )

    fun merge(
        local: MandalaProgressEntity?,
        remote: MandalaProgressEntity?
    ): MandalaProgressEntity? {
        if (local == null) return remote
        if (remote == null) return local
        require(local.mandalaId == remote.mandalaId) {
            "Cannot merge different mandala IDs."
        }

        val localProgressRevision = local.progressRevision.toRevision(local.progressActor)
        val remoteProgressRevision = remote.progressRevision.toRevision(remote.progressActor)
        val localFavoriteRevision = local.favoriteRevision.toRevision(local.favoriteActor)
        val remoteFavoriteRevision = remote.favoriteRevision.toRevision(remote.favoriteActor)
        val localDeletionRevision = local.deletionRevision.toRevision(local.deletionActor)
        val remoteDeletionRevision = remote.deletionRevision.toRevision(remote.deletionActor)

        val progress = choose(
            ProgressFields(local.coloredRegionsJson, local.percentCompleted, local.isCompleted) to localProgressRevision,
            ProgressFields(remote.coloredRegionsJson, remote.percentCompleted, remote.isCompleted) to remoteProgressRevision
        )
        val favorite = choose(
            local.isFavorite to localFavoriteRevision,
            remote.isFavorite to remoteFavoriteRevision
        )
        val deletionOrder = localDeletionRevision.compareTo(remoteDeletionRevision)
        val deletion = when {
            deletionOrder > 0 -> local.isDeleted to localDeletionRevision
            deletionOrder < 0 -> remote.isDeleted to remoteDeletionRevision
            else -> (local.isDeleted || remote.isDeleted) to localDeletionRevision
        }

        val localRestoreProof = RestorationProof(
            local.restoredDeletionRevision,
            local.restoredDeletionActor,
            local.restorationEventRevision,
            local.restorationEventActor
        )
        val remoteRestoreProof = RestorationProof(
            remote.restoredDeletionRevision,
            remote.restoredDeletionActor,
            remote.restorationEventRevision,
            remote.restorationEventActor
        )
        val restoreCandidates = listOf(
            localRestoreProof to local.restorationEventRevision.toRevision(local.restorationEventActor),
            remoteRestoreProof to remote.restorationEventRevision.toRevision(remote.restorationEventActor)
        )
        // Prefer a proof for the currently selected tombstone. A very high
        // Lamport counter acknowledging an older deletion must not mask a
        // lower-counter proof for a newer deletion.
        val matchingRestoreCandidates = restoreCandidates.filter { (proof, _) ->
            proof.deletedRevision == deletion.second.counter &&
                proof.deletedActor == deletion.second.actor
        }
        val restore = choose(
            matchingRestoreCandidates.ifEmpty { restoreCandidates }
                .reduce { best, candidate -> choose(best, candidate) }
        ).first

        // A Lamport counter alone is not causal evidence. The proof explicitly
        // names the exact tombstone observed by the restoring device, and its
        // event revision must be later than that tombstone.
        val restoreEventRevision = restore.eventRevision.toRevision(restore.eventActor)
        val observedDeletion = restore.deletedRevision == deletion.second.counter &&
            restore.deletedActor == deletion.second.actor
        val postDeletionRestoreEvent = restoreEventRevision.compareTo(deletion.second) > 0
        val deleted = deletion.first && !(observedDeletion && postDeletionRestoreEvent)

        return MandalaProgressEntity(
            mandalaId = local.mandalaId,
            coloredRegionsJson = progress.first.colorsJson,
            percentCompleted = progress.first.percentCompleted,
            isFavorite = favorite.first,
            isCompleted = progress.first.isCompleted,
            lastEditedTimestamp = maxOf(local.lastEditedTimestamp, remote.lastEditedTimestamp),
            progressRevision = progress.second.counter,
            progressActor = progress.second.actor,
            favoriteRevision = favorite.second.counter,
            favoriteActor = favorite.second.actor,
            isDeleted = deleted,
            deletionRevision = deletion.second.counter,
            deletionActor = deletion.second.actor,
            restoredDeletionRevision = restore.deletedRevision,
            restoredDeletionActor = restore.deletedActor,
            restorationEventRevision = restore.eventRevision,
            restorationEventActor = restore.eventActor
        )
    }

    private fun Long.toRevision(actor: String) = SyncRevision(this.coerceAtLeast(0L), actor)

    private fun <T> choose(
        local: Pair<T, SyncRevision>,
        remote: Pair<T, SyncRevision>
    ): Pair<T, SyncRevision> {
        val revisionOrder = local.second.compareTo(remote.second)
        if (revisionOrder > 0) return local
        if (revisionOrder < 0) return remote

        // Equal revision tuples should represent the same write. This stable
        // fallback also makes malformed/duplicated tuples converge.
        return if (local.first.toString() >= remote.first.toString()) local else remote
    }
}
