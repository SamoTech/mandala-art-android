package com.example.data

import com.example.model.SavedArtwork
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class MandalaRepository(
    private val dao: MandalaDao,
    private val deviceActorId: String
) {
    // "" is the anonymous local-only partition; Firebase UIDs are isolated.
    private val activeOwnerUid = MutableStateFlow("")

    val currentOwnerUid: String get() = activeOwnerUid.value

    fun setOwnerUid(uid: String) {
        activeOwnerUid.value = uid
    }

    val allProgress: Flow<List<SavedArtwork>> = activeOwnerUid
        .flatMapLatest { ownerUid -> dao.getAllProgress(ownerUid) }
        .map { entities -> entities.map { it.toSavedArtwork() } }

    fun getProgress(mandalaId: String): Flow<SavedArtwork?> {
        return activeOwnerUid
            .flatMapLatest { ownerUid -> dao.getProgress(mandalaId, ownerUid) }
            .map { it?.toSavedArtwork() }
    }

    suspend fun getProgressOnce(mandalaId: String, ownerUid: String = activeOwnerUid.value): SavedArtwork? {
        return dao.getProgressOnce(mandalaId, ownerUid)
            ?.takeUnless { it.isDeleted }
            ?.toSavedArtwork()
    }

    suspend fun saveProgress(
        mandalaId: String,
        coloredRegions: Map<String, Long>,
        totalRegions: Int,
        isFavorite: Boolean? = null,
        ownerUid: String = activeOwnerUid.value
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
            actor = deviceActorId,
            ownerUid = ownerUid
        )
        if (isFavorite != null) {
            dao.updateFavoriteField(
                mandalaId = mandalaId,
                isFavorite = isFavorite,
                timestamp = System.currentTimeMillis(),
                actor = deviceActorId,
                ownerUid = ownerUid
            )
        }
    }

    suspend fun toggleFavorite(mandalaId: String, currentFav: Boolean) {
        dao.updateFavoriteField(
            mandalaId = mandalaId,
            isFavorite = !currentFav,
            timestamp = System.currentTimeMillis(),
            actor = deviceActorId,
            ownerUid = activeOwnerUid.value
        )
    }

    suspend fun deleteProgress(mandalaId: String) {
        dao.markDeleted(
            mandalaId = mandalaId,
            timestamp = System.currentTimeMillis(),
            actor = deviceActorId,
            ownerUid = activeOwnerUid.value
        )
    }

    suspend fun resetProgress(mandalaId: String, ownerUid: String = activeOwnerUid.value) {
        dao.resetProgress(
            mandalaId = mandalaId,
            timestamp = System.currentTimeMillis(),
            actor = deviceActorId,
            ownerUid = ownerUid
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
