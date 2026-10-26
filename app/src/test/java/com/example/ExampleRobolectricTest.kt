package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ads.AdMobConstants
import com.example.data.MandalaRepository
import com.example.generator.MandalaLibrary
import com.example.model.MandalaPalettes
import com.example.util.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

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
        val settings = AppSettings(context)

        // Standard mandala design is always unlocked
        assertTrue(settings.isDesignUnlocked("lotus_bloom"))

        // Bonus design starts locked if not previously unlocked
        val bonusId = "royal_rosette"
        settings.unlockDesign(bonusId)
        assertTrue(settings.isDesignUnlocked(bonusId))
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
        val manager = com.example.ads.AdMobManager.getInstance(context)
        assertNotNull(manager)
        assertFalse(manager.isRewardedAdReady)
    }
}

