package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mandala_progress")
data class MandalaProgressEntity(
    @PrimaryKey val mandalaId: String,
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
    @ColumnInfo(defaultValue = "'legacy'") val restoredDeletionActor: String = "legacy"
)
