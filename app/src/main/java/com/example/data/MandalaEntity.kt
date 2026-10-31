package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "mandala_progress", primaryKeys = ["mandalaId", "ownerUid"])
data class MandalaProgressEntity(
    val mandalaId: String,
    // Empty string is the anonymous/local-only partition. Authenticated saves
    // are always stored under the Firebase UID that owns them.
    @ColumnInfo(defaultValue = "''") val ownerUid: String = "",
    val coloredRegionsJson: String, // format: "regionId:colorLongValue;regionId2:colorLongValue2" (see MandalaRepository.serializeColors)
    val percentCompleted: Float,
    val isFavorite: Boolean,
    val isCompleted: Boolean,
    val lastEditedTimestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0") val progressRevision: Long = 0L,
    @ColumnInfo(defaultValue = "'legacy'") val progressActor: String = "legacy",
    @ColumnInfo(defaultValue = "0") val favoriteRevision: Long = 0L,
    @ColumnInfo(defaultValue = "'legacy'") val favoriteActor: String = "legacy",
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false,
    @ColumnInfo(defaultValue = "0") val deletionRevision: Long = 0L,
    @ColumnInfo(defaultValue = "'legacy'") val deletionActor: String = "legacy",
    @ColumnInfo(defaultValue = "0") val restoredDeletionRevision: Long = 0L,
    @ColumnInfo(defaultValue = "'legacy'") val restoredDeletionActor: String = "legacy",
    @ColumnInfo(defaultValue = "0") val restorationEventRevision: Long = 0L,
    @ColumnInfo(defaultValue = "'legacy'") val restorationEventActor: String = "legacy"
)
