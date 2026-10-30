package com.example.model

import com.example.generator.MandalaLibrary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LevelSystemTest {

    private fun completed(vararg ids: String): Map<String, SavedArtwork> =
        ids.associateWith { id ->
            SavedArtwork(
                mandalaId = id,
                coloredRegions = emptyMap(),
                percentCompleted = 1f,
                isFavorite = false,
                isCompleted = true,
                lastEditedTimestamp = 0L
            )
        }

    @Test
    fun levelPacksContain18FreeDesignsAcrossThreeLevels() {
        assertEquals(18, LevelSystem.ALL_LEVEL_DESIGN_IDS.size)
        assertEquals(3, LevelSystem.LEVEL_COUNT)
        (1..3).forEach { level ->
            assertEquals(LevelSystem.DESIGNS_PER_LEVEL, LevelSystem.LEVEL_DESIGN_IDS[level].orEmpty().size)
        }
    }

    @Test
    fun levelOfParsesLevelDesignIdsOnly() {
        assertEquals(1, LevelSystem.levelOf("level1_mandala_3"))
        assertEquals(3, LevelSystem.levelOf("level3_mandala_6"))
        assertNull(LevelSystem.levelOf("lotus_bloom"))
        assertNull(LevelSystem.levelOf("premium_mandala_12"))
        assertNull(LevelSystem.levelOf("level1_mandala"))
        assertNull(LevelSystem.levelOf("level99_mandala_1"))
    }

    @Test
    fun level1AlwaysUnlockedAndHigherLevelsStartLocked() {
        val noProgress = emptyMap<String, SavedArtwork>()
        assertTrue(LevelSystem.isLevelUnlocked(1, noProgress))
        assertFalse(LevelSystem.isLevelUnlocked(2, noProgress))
        assertFalse(LevelSystem.isLevelUnlocked(3, noProgress))
        assertTrue(LevelSystem.isDesignUnlockedByProgress("level1_mandala_1", noProgress))
        assertFalse(LevelSystem.isDesignUnlockedByProgress("level2_mandala_1", noProgress))
        assertFalse(LevelSystem.isDesignUnlockedByProgress("level3_mandala_6", noProgress))
    }

    @Test
    fun completingFourDesignsUnlocksNextLevel() {
        val three = completed("level1_mandala_1", "level1_mandala_2", "level1_mandala_3")
        assertFalse(LevelSystem.isLevelUnlocked(2, three))
        assertEquals(1, LevelSystem.remainingToUnlock(2, three))

        val four = completed("level1_mandala_1", "level1_mandala_2", "level1_mandala_3", "level1_mandala_4")
        assertTrue(LevelSystem.isLevelUnlocked(2, four))
        assertEquals(0, LevelSystem.remainingToUnlock(2, four))
        assertFalse("Level 3 must still be locked", LevelSystem.isLevelUnlocked(3, four))

        val fourInLevel2 = four + completed("level2_mandala_1", "level2_mandala_2", "level2_mandala_3", "level2_mandala_4")
        assertTrue(LevelSystem.isLevelUnlocked(3, fourInLevel2))
    }

    @Test
    fun inProgressDesignsDoNotCountAsCompleted() {
        val inProgress = mapOf(
            "level1_mandala_1" to SavedArtwork(
                mandalaId = "level1_mandala_1",
                coloredRegions = emptyMap(),
                percentCompleted = 0.5f,
                isFavorite = true,
                isCompleted = false,
                lastEditedTimestamp = 0L
            )
        )
        assertEquals(0, LevelSystem.completedCountInLevel(1, inProgress))
        assertFalse(LevelSystem.isLevelUnlocked(2, inProgress))
        assertEquals(4, LevelSystem.remainingToUnlock(2, inProgress))
    }

    @Test
    fun nonLevelDesignsAreAlwaysUnlocked() {
        val noProgress = emptyMap<String, SavedArtwork>()
        assertTrue(LevelSystem.isDesignUnlockedByProgress("moonlit_lotus", noProgress))
        assertTrue(LevelSystem.isDesignUnlockedByProgress("premium_mandala_12", noProgress))
    }

    @Test
    fun levelDesignsExistInLibraryWithStableIds() {
        val libraryIds = MandalaLibrary.designs.map { it.id }.toSet()
        LevelSystem.ALL_LEVEL_DESIGN_IDS.forEach { id ->
            assertTrue("Library must contain level design $id", libraryIds.contains(id))
        }
        val levelDesigns = MandalaLibrary.designs.filter { it.id in LevelSystem.ALL_LEVEL_DESIGN_IDS }
        assertEquals(18, levelDesigns.size)
        levelDesigns.forEach { design ->
            assertTrue("Design must have at least 20 regions: ${design.title}", design.regionCount >= 20)
        }
    }
}
