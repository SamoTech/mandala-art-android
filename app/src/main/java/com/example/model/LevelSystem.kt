package com.example.model

/**
 * Level Packs: free designs organized into progressive levels.
 *
 * Level 1 is always unlocked. Level N unlocks once the player completes
 * [REQUIRED_COMPLETIONS_TO_UNLOCK] designs in level N - 1. Completion is
 * derived from saved artwork progress, so no extra storage or migration
 * is needed and cloud saves keep working unchanged.
 *
 * Level designs are free content. The rewarded-ad premium unlock system
 * for bonus designs is not affected by this progression.
 */
object LevelSystem {

    /** Completed designs in a level required to unlock the next one. */
    const val REQUIRED_COMPLETIONS_TO_UNLOCK = 4

    /** Number of free designs per level. */
    const val DESIGNS_PER_LEVEL = 6

    /** Number of level packs. */
    const val LEVEL_COUNT = 3

    /** Design ids grouped by level, in gallery and unlock order. */
    val LEVEL_DESIGN_IDS: Map<Int, List<String>> = (1..LEVEL_COUNT).associateWith { level ->
        (1..DESIGNS_PER_LEVEL).map { index -> "level${level}_mandala_$index" }
    }

    /** All design ids that belong to the level packs. */
    val ALL_LEVEL_DESIGN_IDS: Set<String> = LEVEL_DESIGN_IDS.values.flatten().toSet()

    /** Returns the level a design belongs to, or null for non-level designs. */
    fun levelOf(designId: String): Int? {
        val level = Regex("""^level(\d+)_mandala_\d+$""").find(designId)?.groupValues?.get(1)?.toIntOrNull() ?: return null
        return if (level in 1..LEVEL_COUNT) level else null
    }

    /** Number of completed designs in the given level. */
    fun completedCountInLevel(level: Int, savedArtworks: Map<String, SavedArtwork>): Int =
        LEVEL_DESIGN_IDS[level].orEmpty().count { id -> savedArtworks[id]?.isCompleted == true }

    /** A level is unlocked if it is the first one, or the previous level has enough completions. */
    fun isLevelUnlocked(level: Int, savedArtworks: Map<String, SavedArtwork>): Boolean {
        if (level <= 1) return true
        return completedCountInLevel(level - 1, savedArtworks) >= REQUIRED_COMPLETIONS_TO_UNLOCK
    }

    /** How many more completed designs in the previous level are still needed to unlock this level. */
    fun remainingToUnlock(level: Int, savedArtworks: Map<String, SavedArtwork>): Int {
        if (isLevelUnlocked(level, savedArtworks)) return 0
        return (REQUIRED_COMPLETIONS_TO_UNLOCK - completedCountInLevel(level - 1, savedArtworks))
            .coerceAtLeast(0)
    }

    /** Progression gate for a single design; non-level designs are always unlocked. */
    fun isDesignUnlockedByProgress(designId: String, savedArtworks: Map<String, SavedArtwork>): Boolean {
        val level = levelOf(designId) ?: return true
        return isLevelUnlocked(level, savedArtworks)
    }
}
