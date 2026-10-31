package com.example.data

import com.example.model.SavedArtwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MandalaRepository(
    private val dao: MandalaDao,
    private val deviceActorId: String
) {
    val allProgress: Flow<List<SavedArtwork>> = dao.getAllProgress().map { entities ->
        entities.map { it.toSavedArtwork() }
    }

    fun getProgress(mandalaId: String): Flow<SavedArtwork?> {
        return dao.getProgress(mandalaId).map { it?.toSavedArtwork() }
    }

    suspend fun getProgressOnce(mandalaId: String): SavedArtwork? {
        return dao.getProgressOnce(mandalaId)
            ?.takeUnless { it.isDeleted }
            ?.toSavedArtwork()
    }

    suspend fun saveProgress(
        mandalaId: String,
        coloredRegions: Map<String, Long>,
        totalRegions: Int,
        isFavorite: Boolean? = null
    ) {
        val nonDefaultCount = coloredRegions.values.count { it != 0xFFFFFFFFL }
        val percent = if (totalRegions > 0) {
            (nonDefaultCount.toFloat() / totalRegions).coerceIn(0f, 1f)
        } else {
            0f
        }
        val serialized = serializeColors(coloredRegions)
        dao.updateProgressFields(
            mandalaId = mandalaId,
            coloredRegionsJson = serialized,
            percentCompleted = percent,
            isCompleted = percent >= 0.999f,
            timestamp = System.currentTimeMillis(),
            actor = deviceActorId
        )
        if (isFavorite != null) {
            dao.updateFavoriteField(
                mandalaId = mandalaId,
                isFavorite = isFavorite,
                timestamp = System.currentTimeMillis(),
                actor = deviceActorId
            )
        }
    }

    suspend fun toggleFavorite(mandalaId: String, currentFav: Boolean) {
        dao.updateFavoriteField(
            mandalaId = mandalaId,
            isFavorite = !currentFav,
            timestamp = System.currentTimeMillis(),
            actor = deviceActorId
        )
    }

    suspend fun deleteProgress(mandalaId: String) {
        dao.markDeleted(
            mandalaId = mandalaId,
            timestamp = System.currentTimeMillis(),
            actor = deviceActorId
        )
    }

    suspend fun resetProgress(mandalaId: String) {
        dao.resetProgress(
            mandalaId = mandalaId,
            timestamp = System.currentTimeMillis(),
            actor = deviceActorId
        )
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
                    if (color != null) result[id] = color
                }
            }
            return result
        }
    }
}

private fun MandalaProgressEntity.toSavedArtwork(): SavedArtwork {
    return SavedArtwork(
        mandalaId = mandalaId,
        coloredRegions = MandalaRepository.deserializeColors(coloredRegionsJson),
        percentCompleted = percentCompleted,
        isFavorite = isFavorite,
        isCompleted = isCompleted,
        lastEditedTimestamp = lastEditedTimestamp
    )
}
