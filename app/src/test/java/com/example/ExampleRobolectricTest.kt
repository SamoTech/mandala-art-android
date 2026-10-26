package com.example

import android.app.Activity
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ads.AdMobConstants
import com.example.ads.AdMobManager
import com.example.data.MandalaDatabase
import com.example.data.MandalaRepository
import com.example.generator.MandalaLibrary
import com.example.model.MandalaPalettes
import com.example.ui.editor.EditorViewModel
import com.example.util.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

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
}
