package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mandala_progress")
data class MandalaProgressEntity(
    @PrimaryKey val mandalaId: String,
    val coloredRegionsJson: String, // format: "regionId:colorLongValue;regionId2:colorLongValue2" (see MandalaRepository.serializeColors)
    val percentCompleted: Float,
    val isFavorite: Boolean,
    val isCompleted: Boolean,
    val lastEditedTimestamp: Long = System.currentTimeMillis()
)
