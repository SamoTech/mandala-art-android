package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MandalaDao {
    @Query("SELECT * FROM mandala_progress ORDER BY lastEditedTimestamp DESC")
    fun getAllProgress(): Flow<List<MandalaProgressEntity>>

    @Query("SELECT * FROM mandala_progress WHERE mandalaId = :mandalaId LIMIT 1")
    fun getProgress(mandalaId: String): Flow<MandalaProgressEntity?>

    @Query("SELECT * FROM mandala_progress WHERE mandalaId = :mandalaId LIMIT 1")
    suspend fun getProgressOnce(mandalaId: String): MandalaProgressEntity?

    @Query("SELECT * FROM mandala_progress ORDER BY lastEditedTimestamp DESC")
    suspend fun getAllProgressOnce(): List<MandalaProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(entity: MandalaProgressEntity)

    @Query("""
        UPDATE mandala_progress
        SET coloredRegionsJson = :coloredRegionsJson,
            percentCompleted = :percentCompleted,
            isCompleted = :isCompleted,
            lastEditedTimestamp = :lastEditedTimestamp
        WHERE mandalaId = :mandalaId
    """)
    suspend fun updateProgressPreservingFavorite(
        mandalaId: String,
        coloredRegionsJson: String,
        percentCompleted: Float,
        isCompleted: Boolean,
        lastEditedTimestamp: Long
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(entity: MandalaProgressEntity)

    @Query("UPDATE mandala_progress SET isFavorite = :isFav, lastEditedTimestamp = :timestamp WHERE mandalaId = :mandalaId")
    suspend fun updateFavorite(mandalaId: String, isFav: Boolean, timestamp: Long)

    @Query("DELETE FROM mandala_progress WHERE mandalaId = :mandalaId")
    suspend fun deleteProgress(mandalaId: String)

    /**
     * Reset an artwork's saved progress without losing a favorite record.
     *
     * For favorite artworks the progress fields are cleared and the row is kept
     * (so the favorite flag and the My Art entry survive a reset); for
     * non-favorite artworks the row is removed, matching previous behavior.
     */
    @Transaction
    suspend fun resetProgress(mandalaId: String) {
        val existing = getProgressOnce(mandalaId) ?: return
        if (existing.isFavorite) {
            updateProgressPreservingFavorite(
                mandalaId = mandalaId,
                coloredRegionsJson = "",
                percentCompleted = 0f,
                isCompleted = false,
                lastEditedTimestamp = System.currentTimeMillis()
            )
        } else {
            deleteProgress(mandalaId)
        }
    }
}
