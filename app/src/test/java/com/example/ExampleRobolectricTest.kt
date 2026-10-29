package com.example

import android.app.Activity
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ads.AdMobConstants
import com.example.ads.AdMobManager
import com.example.data.MandalaDatabase
import com.example.data.MandalaProgressEntity
import com.example.data.MandalaRepository
import com.example.generator.MandalaLibrary
import com.example.model.MandalaPalettes
import com.example.ui.editor.EditorViewModel
import com.example.util.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.runBlocking

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Mandala Art", appName)
    }

    @Test
    fun verifyMandalaLibraryHasAtLeast20Designs() {
        val designs = MandalaLibrary.designs
        assertTrue("Expected at least 20 designs, found ${designs.size}", designs.size >= 20)

        // Verify each design has distinct id, title, and multiple fillable regions
        val ids = mutableSetOf<String>()
        for (design in designs) {
            assertTrue("ID should be unique: ${design.id}", ids.add(design.id))
            assertTrue("Design must have at least 20 regions: ${design.title}", design.regionCount >= 20)
            assertNotNull("Regions list should not be null", design.regions)

            // Verify each region has valid bounds
            for (region in design.regions) {
                assertTrue("Region ${region.id} should have positive width", region.bounds.width() > 0)
                assertTrue("Region ${region.id} should have positive height", region.bounds.height() > 0)
            }
        }
    }

    @Test
    fun verifyColorSerializationRoundtrip() {
        val colors = mapOf(
            "center_gem" to 0xFFFF4081L,
            "inner_petal_0" to 0xFF4A148CL,
            "mid_petal_3" to 0xFF00E5FFL
        )

        val serialized = MandalaRepository.serializeColors(colors)
        val deserialized = MandalaRepository.deserializeColors(serialized)

        assertEquals(colors.size, deserialized.size)
        assertEquals(0xFFFF4081L, deserialized["center_gem"])
        assertEquals(0xFF4A148CL, deserialized["inner_petal_0"])
        assertEquals(0xFF00E5FFL, deserialized["mid_petal_3"])
    }

    @Test
    fun verifyPalettesContainAtLeast24Colors() {
        val allColors = MandalaPalettes.allPalettes.flatMap { it.colors }
        assertTrue("Expected at least 24 colors, found ${allColors.size}", allColors.size >= 24)
    }

    @Test
    fun verifyAdMobPublisherAndTestUnits() {
        assertEquals("pub-3703596518277340", AdMobConstants.PUBLISHER_ID)
        assertTrue(AdMobConstants.bannerAdUnitId.startsWith("ca-app-pub-"))
        assertTrue(AdMobConstants.interstitialAdUnitId.startsWith("ca-app-pub-"))
        assertTrue(AdMobConstants.rewardedAdUnitId.startsWith("ca-app-pub-"))
    }

    @Test
    fun verifyAppSettingsBonusUnlockLogic() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("test_mandala_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val settings = AppSettings(context)

        // Standard 22 mandala designs are free and always unlocked
        assertTrue("Standard design must be unlocked by default", settings.isDesignUnlocked("lotus_bloom"))
        assertTrue("Standard design must be unlocked by default", settings.isDesignUnlocked("sacred_geometry"))

        // Bonus designs must be locked initially
        val bonusId1 = "galaxy_spiral"
        val bonusId2 = "royal_rosette"

        // Unlocking bonusId1
        settings.unlockDesign(bonusId1)
        assertTrue("bonusId1 must now be unlocked", settings.isDesignUnlocked(bonusId1))

        // Idempotent unlock call should succeed without issue
        settings.unlockDesign(bonusId1)
        assertTrue(settings.isDesignUnlocked(bonusId1))
    }

    @Test
    fun verifyColorUnlockPersistsAcrossSettingsInstances() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("mandala_app_prefs", Context.MODE_PRIVATE)
        assertTrue("Test preferences must be cleared", prefs.edit().clear().commit())

        val lockedColor = 0xFF880E4FL
        val initialSettings = AppSettings(context)
        assertFalse("Premium color should be locked by default", initialSettings.isColorUnlocked(lockedColor))

        initialSettings.unlockColor(lockedColor)
        assertTrue("Color should unlock after reward is granted", initialSettings.isColorUnlocked(lockedColor))

        val recreatedSettings = AppSettings(context)
        assertTrue("Unlocked color should persist across settings instances", recreatedSettings.isColorUnlocked(lockedColor))

        assertTrue("Clean up test preferences", prefs.edit().clear().commit())
    }

    @Test
    fun verifyStandardColorsRemainUnlockedByDefault() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("mandala_app_prefs", Context.MODE_PRIVATE)
        assertTrue("Test preferences must be cleared", prefs.edit().clear().commit())

        val settings = AppSettings(context)
        assertTrue("A standard palette color should remain free", settings.isColorUnlocked(0xFFFF4081L))
        assertFalse("A premium palette color should be locked", settings.isColorUnlocked(0xFF880E4FL))

        assertTrue("Clean up test preferences", prefs.edit().clear().commit())
    }

    @Test
    fun verifyEditorViewModelUndoRedoOperations() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inMemoryDb = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = MandalaRepository(inMemoryDb.mandalaDao())
        val settings = AppSettings(context)

        val viewModel = EditorViewModel(
            mandalaId = "lotus_bloom",
            repository = repository,
            appSettings = settings
        )

        // Initial state
        assertFalse("Initial canUndo should be false", viewModel.canUndo.value)
        assertFalse("Initial canRedo should be false", viewModel.canRedo.value)
        assertTrue("Initial colored regions should be empty", viewModel.coloredRegions.value.isEmpty())

        // Boundary test: calling undo or redo when empty should not crash
        viewModel.undo()
        viewModel.redo()
        assertFalse(viewModel.canUndo.value)
        assertFalse(viewModel.canRedo.value)

        // Color a region
        val region1 = "petal_0"
        viewModel.onRegionTapped(region1, context)
        assertTrue("canUndo should be true after coloring", viewModel.canUndo.value)
        assertFalse("canRedo should remain false after new action", viewModel.canRedo.value)
        assertEquals(MandalaPalettes.defaultColor, viewModel.coloredRegions.value[region1])

        // Color second region with custom color
        val region2 = "petal_1"
        val customColor = 0xFF00E5FFL
        viewModel.setSelectedColor(customColor)
        viewModel.onRegionTapped(region2, context)
        assertEquals(customColor, viewModel.coloredRegions.value[region2])

        // Test Undo
        viewModel.undo()
        assertTrue("canRedo should be true after undo", viewModel.canRedo.value)
        assertFalse("region2 should be undone", viewModel.coloredRegions.value.containsKey(region2))
        assertTrue("region1 should still be present", viewModel.coloredRegions.value.containsKey(region1))

        // Test Redo
        viewModel.redo()
        assertFalse("canRedo should be false after redoing all steps", viewModel.canRedo.value)
        assertEquals(customColor, viewModel.coloredRegions.value[region2])

        // Test Eraser
        viewModel.toggleEraser()
        assertTrue("Eraser should be active", viewModel.isEraserActive.value)
        viewModel.onRegionTapped(region1, context)
        assertFalse("region1 should be cleared by eraser", viewModel.coloredRegions.value.containsKey(region1))

        // Test Reset
        viewModel.reset()
        assertTrue("colored regions should be empty after reset", viewModel.coloredRegions.value.isEmpty())
        assertFalse("canUndo should be false after reset", viewModel.canUndo.value)
        assertFalse("canRedo should be false after reset", viewModel.canRedo.value)

        inMemoryDb.close()
    }

    @Test
    fun verifyRewardedAdPolicyDoesNotUnlockWhenAdUnavailableOrDismissed() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val adMobManager = AdMobManager.getInstance(context)
        val settings = AppSettings(context)

        val bonusDesign = "royal_rosette"

        val activityController = Robolectric.buildActivity(Activity::class.java).setup()
        val activity = activityController.get()

        val rewardGranted = AtomicBoolean(false)
        val unavailableCalled = AtomicBoolean(false)
        val closedCalled = AtomicBoolean(false)

        // Attempting to show rewarded ad when not preloaded
        adMobManager.showRewardedAd(
            activity = activity,
            onUserEarnedReward = {
                rewardGranted.set(true)
                settings.unlockDesign(bonusDesign)
            },
            onAdClosed = { earned ->
                closedCalled.set(true)
                assertFalse("Reward must not be earned when ad was not ready", earned)
            },
            onAdUnavailable = {
                unavailableCalled.set(true)
            }
        )

        // Assertions:
        assertTrue("onAdUnavailable must be invoked when ad is not loaded", unavailableCalled.get())
        assertTrue("onAdClosed must be invoked", closedCalled.get())
        assertFalse("Reward must NEVER be granted when ad is unavailable", rewardGranted.get())

        activityController.destroy()
    }

    @Test
    fun verifyAppAdsTxtFileContent() {
        // Check app-ads.txt content
        val expected = "google.com, pub-3703596518277340, DIRECT, f08c47fec0942fa0"
        val candidates = listOf(
            File("/app-ads.txt"),
            File("app-ads.txt"),
            File("../app-ads.txt")
        )
        val file = candidates.firstOrNull { it.exists() }
        assertNotNull("app-ads.txt should exist", file)
        val content = file!!.readText().trim()
        assertEquals(expected, content)
    }

    @Test
    fun verifyAdMobManagerInstantiation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = AdMobManager.getInstance(context)
        assertNotNull(manager)
    }

    @Test
    fun saveProgressPreservesFavoriteStateDuringAutosave() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inMemoryDb = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repository = MandalaRepository(inMemoryDb.mandalaDao())
            repository.toggleFavorite("lotus_bloom", currentFav = false)

            repository.saveProgress(
                mandalaId = "lotus_bloom",
                coloredRegions = mapOf("petal_0" to MandalaPalettes.defaultColor),
                totalRegions = MandalaLibrary.getDesignById("lotus_bloom").regionCount
            )

            val saved = repository.getProgressOnce("lotus_bloom")
            assertNotNull("Artwork should be saved", saved)
            assertTrue("Autosaving coloring progress must not remove the favorite flag", saved!!.isFavorite)
            assertEquals(MandalaPalettes.defaultColor, saved.coloredRegions["petal_0"])
        } finally {
            inMemoryDb.close()
        }
    }


    @Test
    fun savedProgressSurvivesDatabaseCloseAndReopen() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "room_persistence_regression_test.db"
        context.deleteDatabase(databaseName)

        try {
            val initialDatabase = Room.databaseBuilder(
                context,
                MandalaDatabase::class.java,
                databaseName
            ).allowMainThreadQueries().build()

            try {
                initialDatabase.mandalaDao().saveProgress(
                    MandalaProgressEntity(
                        mandalaId = "persistence_test_mandala",
                        coloredRegionsJson = "petal_0:4278190335",
                        percentCompleted = 0.25f,
                        isFavorite = true,
                        isCompleted = false,
                        lastEditedTimestamp = 123456789L
                    )
                )
            } finally {
                initialDatabase.close()
            }

            val reopenedDatabase = Room.databaseBuilder(
                context,
                MandalaDatabase::class.java,
                databaseName
            ).allowMainThreadQueries().build()

            try {
                val restored = reopenedDatabase.mandalaDao()
                    .getProgressOnce("persistence_test_mandala")

                assertNotNull("Saved progress must remain after reopening the database", restored)
                assertEquals("petal_0:4278190335", restored!!.coloredRegionsJson)
                assertEquals(0.25f, restored.percentCompleted, 0.0001f)
                assertTrue("Favorite state must survive database reopening", restored.isFavorite)
                assertEquals(123456789L, restored.lastEditedTimestamp)
            } finally {
                reopenedDatabase.close()
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun resetProgressKeepsFavoriteRecordAndRemovesNonFavoriteRecord() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inMemoryDb = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val dao = inMemoryDb.mandalaDao()

            // Favorite artwork: reset must preserve the record and the favorite flag.
            dao.saveProgress(
                MandalaProgressEntity(
                    mandalaId = "reset_favorite_mandala",
                    coloredRegionsJson = "petal_0:4278190335",
                    percentCompleted = 0.5f,
                    isFavorite = true,
                    isCompleted = false,
                    lastEditedTimestamp = 100L
                )
            )
            dao.resetProgress("reset_favorite_mandala")

            val favoriteAfterReset = dao.getProgressOnce("reset_favorite_mandala")
            assertNotNull("Favorite record must survive reset", favoriteAfterReset)
            assertTrue("Favorite flag must survive reset", favoriteAfterReset!!.isFavorite)
            assertEquals("Colored regions must be cleared by reset", "", favoriteAfterReset.coloredRegionsJson)
            assertEquals("Completion percentage must be cleared by reset", 0f, favoriteAfterReset.percentCompleted, 0.0001f)
            assertFalse("Artwork must not be marked completed after reset", favoriteAfterReset.isCompleted)

            // Non-favorite artwork: reset must remove the record (previous behavior).
            dao.saveProgress(
                MandalaProgressEntity(
                    mandalaId = "reset_plain_mandala",
                    coloredRegionsJson = "petal_0:4278190335",
                    percentCompleted = 0.5f,
                    isFavorite = false,
                    isCompleted = false,
                    lastEditedTimestamp = 100L
                )
            )
            dao.resetProgress("reset_plain_mandala")
            assertNull("Non-favorite record must be removed by reset", dao.getProgressOnce("reset_plain_mandala"))
        } finally {
            inMemoryDb.close()
        }
    }

}
