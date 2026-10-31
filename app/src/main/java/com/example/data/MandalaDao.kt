package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MandalaDao {
    @Query("SELECT * FROM mandala_progress WHERE ownerUid = :ownerUid AND isDeleted = 0 ORDER BY lastEditedTimestamp DESC")
    fun getAllProgress(ownerUid: String = ""): Flow<List<MandalaProgressEntity>>

    @Query("SELECT * FROM mandala_progress WHERE mandalaId = :mandalaId AND ownerUid = :ownerUid AND isDeleted = 0 LIMIT 1")
    fun getProgress(mandalaId: String, ownerUid: String = ""): Flow<MandalaProgressEntity?>

    // Includes tombstones; every read is scoped to one local account partition.
    @Query("SELECT * FROM mandala_progress WHERE mandalaId = :mandalaId AND ownerUid = :ownerUid LIMIT 1")
    suspend fun getProgressOnce(mandalaId: String, ownerUid: String = ""): MandalaProgressEntity?

    @Query("SELECT * FROM mandala_progress WHERE ownerUid = :ownerUid ORDER BY lastEditedTimestamp DESC")
    suspend fun getAllProgressOnce(ownerUid: String = ""): List<MandalaProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(entity: MandalaProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(entity: MandalaProgressEntity)

    /** Apply a remote candidate without replacing a newer local edit. */
    @Transaction
    suspend fun mergeProgress(incoming: MandalaProgressEntity) {
        val current = getProgressOnce(incoming.mandalaId, incoming.ownerUid)
        upsertProgress(ProgressSyncMerge.merge(current, incoming) ?: incoming)
    }

    @Transaction
    suspend fun updateProgressFields(
        mandalaId: String,
        coloredRegionsJson: String,
        percentCompleted: Float,
        isCompleted: Boolean,
        timestamp: Long,
        actor: String,
        ownerUid: String = ""
    ) {
        val existing = getProgressOnce(mandalaId, ownerUid)
        val revision = existing.nextLocalRevision()
        val updated = if (existing == null) {
            MandalaProgressEntity(
                mandalaId = mandalaId,
                ownerUid = ownerUid,
                coloredRegionsJson = coloredRegionsJson,
                percentCompleted = percentCompleted,
                isFavorite = false,
                isCompleted = isCompleted,
                lastEditedTimestamp = timestamp,
                progressRevision = revision,
                progressActor = actor
            )
        } else {
            existing.copy(
                coloredRegionsJson = coloredRegionsJson,
                percentCompleted = percentCompleted,
                isCompleted = isCompleted,
                lastEditedTimestamp = timestamp,
                progressRevision = revision,
                progressActor = actor,
                isDeleted = false,
                restoredDeletionRevision = if (existing.deletionRevision > 0L) existing.deletionRevision else existing.restoredDeletionRevision,
                restoredDeletionActor = if (existing.deletionRevision > 0L) existing.deletionActor else existing.restoredDeletionActor,
                restorationEventRevision = if (existing.deletionRevision > 0L) revision else existing.restorationEventRevision,
                restorationEventActor = if (existing.deletionRevision > 0L) actor else existing.restorationEventActor
            )
        }
        upsertProgress(updated)
    }

    @Transaction
    suspend fun updateFavoriteField(
        mandalaId: String,
        isFavorite: Boolean,
        timestamp: Long,
        actor: String,
        ownerUid: String = ""
    ) {
        val existing = getProgressOnce(mandalaId, ownerUid)
        val revision = existing.nextLocalRevision()
        val updated = if (existing == null) {
            MandalaProgressEntity(
                mandalaId = mandalaId,
                ownerUid = ownerUid,
                coloredRegionsJson = "",
                percentCompleted = 0f,
                isFavorite = isFavorite,
                isCompleted = false,
                lastEditedTimestamp = timestamp,
                favoriteRevision = revision,
                favoriteActor = actor
            )
        } else {
            existing.copy(
                coloredRegionsJson = if (existing.isDeleted) "" else existing.coloredRegionsJson,
                percentCompleted = if (existing.isDeleted) 0f else existing.percentCompleted,
                isCompleted = if (existing.isDeleted) false else existing.isCompleted,
                isFavorite = isFavorite,
                lastEditedTimestamp = timestamp,
                favoriteRevision = revision,
                favoriteActor = actor,
                isDeleted = false,
                restoredDeletionRevision = if (existing.deletionRevision > 0L) existing.deletionRevision else existing.restoredDeletionRevision,
                restoredDeletionActor = if (existing.deletionRevision > 0L) existing.deletionActor else existing.restoredDeletionActor,
                restorationEventRevision = if (existing.deletionRevision > 0L) revision else existing.restorationEventRevision,
                restorationEventActor = if (existing.deletionRevision > 0L) actor else existing.restorationEventActor
            )
        }
        upsertProgress(updated)
    }

    @Transaction
    suspend fun markDeleted(mandalaId: String, timestamp: Long, actor: String, ownerUid: String = "") {
        val existing = getProgressOnce(mandalaId, ownerUid)
        val revision = existing.nextLocalRevision()
        val tombstone = if (existing == null) {
            MandalaProgressEntity(
                mandalaId = mandalaId,
                ownerUid = ownerUid,
                coloredRegionsJson = "",
                percentCompleted = 0f,
                isFavorite = false,
                isCompleted = false,
                lastEditedTimestamp = timestamp,
                isDeleted = true,
                deletionRevision = revision,
                deletionActor = actor
            )
        } else {
            existing.copy(
                isDeleted = true,
                deletionRevision = revision,
                deletionActor = actor,
                lastEditedTimestamp = timestamp
            )
        }
        upsertProgress(tombstone)
    }

    @Transaction
    suspend fun resetProgress(mandalaId: String, timestamp: Long, actor: String, ownerUid: String = "") {
        val existing = getProgressOnce(mandalaId, ownerUid) ?: return
        val revision = existing.nextLocalRevision()
        if (existing.isFavorite) {
            upsertProgress(
                existing.copy(
                    coloredRegionsJson = "",
                    percentCompleted = 0f,
                    isCompleted = false,
                    progressRevision = revision,
                    progressActor = actor,
                    lastEditedTimestamp = timestamp,
                    isDeleted = false,
                    restoredDeletionRevision = if (existing.isDeleted) existing.deletionRevision else existing.restoredDeletionRevision,
                    restoredDeletionActor = if (existing.deletionRevision > 0L) existing.deletionActor else existing.restoredDeletionActor,
                    restorationEventRevision = if (existing.deletionRevision > 0L) revision else existing.restorationEventRevision,
                    restorationEventActor = if (existing.deletionRevision > 0L) actor else existing.restorationEventActor
                )
            )
        } else {
            upsertProgress(
                existing.copy(
                    isDeleted = true,
                    deletionRevision = revision,
                    deletionActor = actor,
                    lastEditedTimestamp = timestamp
                )
            )
        }
    }

    @Query("DELETE FROM mandala_progress WHERE mandalaId = :mandalaId AND ownerUid = :ownerUid")
    suspend fun deleteProgress(mandalaId: String, ownerUid: String = "")
}

private fun MandalaProgressEntity?.nextLocalRevision(): Long {
    val maxRevision = this?.let {
        maxOf(it.progressRevision, it.favoriteRevision, it.deletionRevision)
    } ?: 0L
    return Math.addExact(maxRevision.coerceAtLeast(0L), 1L)
}
