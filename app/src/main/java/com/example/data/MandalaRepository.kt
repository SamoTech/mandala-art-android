package com.example.data

import com.example.model.SavedArtwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MandalaRepository(private val dao: MandalaDao) {

    val allProgress: Flow<List<SavedArtwork>> = dao.getAllProgress().map { entities ->
        entities.map { it.toSavedArtwork() }
    }

    fun getProgress(mandalaId: String): Flow<SavedArtwork?> {
        return dao.getProgress(mandalaId).map { it?.toSavedArtwork() }
    }

    suspend fun getProgressOnce(mandalaId: String): SavedArtwork? {
        return dao.getProgressOnce(mandalaId)?.toSavedArtwork()
    }

    suspend fun saveProgress(
        mandalaId: String,
        coloredRegions: Map<String, Long>,
        totalRegions: Int,
        isFavorite: Boolean? = null
    ) {
        val nonDefaultCount = coloredRegions.values.count { it != 0xFFFFFFFFL }
        val percent = if (totalRegions > 0) (nonDefaultCount.toFloat() / totalRegions).coerceIn(0f, 1f) else 0f
        val isCompleted = percent >= 0.999f

        val serialized = serializeColors(coloredRegions)
        val timestamp = System.currentTimeMillis()
        if (isFavorite == null) {
            // Update progress fields atomically without overwriting a concurrent favorite toggle.
            val updatedRows = dao.updateProgressPreservingFavorite(
                mandalaId = mandalaId,
                coloredRegionsJson = serialized,
                percentCompleted = percent,
                isCompleted = isCompleted,
                lastEditedTimestamp = timestamp
            )
            if (updatedRows > 0) return
        }

        // New artwork starts as not favorite unless the caller explicitly sets the state.
        dao.saveProgress(
            MandalaProgressEntity(
                mandalaId = mandalaId,
                coloredRegionsJson = serialized,
                percentCompleted = percent,
                isFavorite = isFavorite ?: false,
                isCompleted = isCompleted,
                lastEditedTimestamp = timestamp
            )
        )
    }

    suspend fun toggleFavorite(mandalaId: String, currentFav: Boolean) {
        val existing = dao.getProgressOnce(mandalaId)
        if (existing != null) {
            dao.updateFavorite(mandalaId, !currentFav, System.currentTimeMillis())
        } else {
            // Create initial entry with favorite flagged
            dao.saveProgress(
                MandalaProgressEntity(
                    mandalaId = mandalaId,
                    coloredRegionsJson = "",
                    percentCompleted = 0f,
                    isFavorite = !currentFav,
                    isCompleted = false,
                    lastEditedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteProgress(mandalaId: String) {
        dao.deleteProgress(mandalaId)
    }

    companion object {
        fun serializeColors(map: Map<String, Long>): String {
            if (map.isEmpty()) return ""
            return map.entries.joinToString(";") { "${it.key}:${it.value}" }
        }

        fun deserializeColors(raw: String): Map<String, Long> {
            if (raw.isBlank()) return emptyMap()
            val result = mutableMapOf<String, Long>()
            raw.split(";").forEach { pair ->
                val parts = pair.split(":")
                if (parts.size == 2) {
                    val id = parts[0]
                    val color = parts[1].toLongOrNull()
                    if (color != null) {
                        result[id] = color
                    }
                }
            }
            return result
        }
    }
}

private fun MandalaProgressEntity.toSavedArtwork(): SavedArtwork {
    return SavedArtwork(
        mandalaId = this.mandalaId,
        coloredRegions = MandalaRepository.deserializeColors(this.coloredRegionsJson),
        percentCompleted = this.percentCompleted,
        isFavorite = this.isFavorite,
        isCompleted = this.isCompleted,
        lastEditedTimestamp = this.lastEditedTimestamp
    )
}
