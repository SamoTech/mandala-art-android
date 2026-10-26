package com.example

import com.example.ads.AdMobConstants
import com.example.data.MandalaRepository
import com.example.generator.MandalaLibrary
import com.example.model.MandalaPalettes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit & Component Tests for Core Business Logic, Data Serialization,
 * AdMob Test Configurations, Mandala Design Library, and Palette Models.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun verifyAdMobConstantsTestModeAndTestAdUnits() {
        // Must always be running in test ad mode during development/testing
        assertTrue("USE_TEST_ADS must be true in development", AdMobConstants.USE_TEST_ADS)

        // Verify standard official Google Mobile Ads test IDs
        assertEquals("ca-app-pub-3940256099942544/9214589741", AdMobConstants.bannerAdUnitId)
        assertEquals("ca-app-pub-3940256099942544/1033173712", AdMobConstants.interstitialAdUnitId)
        assertEquals("ca-app-pub-3940256099942544/5224354917", AdMobConstants.rewardedAdUnitId)

        // Official publisher ID
        assertEquals("pub-3703596518277340", AdMobConstants.PUBLISHER_ID)
    }

    @Test
    fun verifyMandalaLibraryCompletenessAndIntegrity() {
        val designs = MandalaLibrary.designs
        assertTrue("Library should contain at least 22 designs", designs.size >= 22)

        val ids = mutableSetOf<String>()
        for (design in designs) {
            assertTrue("Design ID cannot be blank", design.id.isNotBlank())
            assertTrue("Duplicate design ID found: ${design.id}", ids.add(design.id))
            assertTrue("Design title cannot be blank for ${design.id}", design.title.isNotBlank())
            assertNotNull("Category cannot be null for ${design.id}", design.category)
            assertTrue("Design ${design.id} must have >= 20 regions", design.regionCount >= 20)
            assertEquals("Reported regionCount must match regions list size", design.regions.size, design.regionCount)

            val regionIds = mutableSetOf<String>()
            for (region in design.regions) {
                assertTrue("Region ID cannot be blank in ${design.id}", region.id.isNotBlank())
                assertTrue("Duplicate region ID ${region.id} in ${design.id}", regionIds.add(region.id))
                assertTrue("Region ${region.id} in ${design.id} must have width > 0", region.bounds.width() > 0)
                assertTrue("Region ${region.id} in ${design.id} must have height > 0", region.bounds.height() > 0)
            }
        }
    }

    @Test
    fun verifyBonusDesignsExistInLibrary() {
        val galaxy = MandalaLibrary.getDesignById("galaxy_spiral")
        assertNotNull("galaxy_spiral must exist in library", galaxy)
        assertEquals("Galaxy Spiral", galaxy.title)

        val rosette = MandalaLibrary.getDesignById("royal_rosette")
        assertNotNull("royal_rosette must exist in library", rosette)
        assertEquals("Royal Rosette", rosette.title)
    }

    @Test
    fun verifyColorSerializationAndDeserialization() {
        // Test empty map
        assertEquals("", MandalaRepository.serializeColors(emptyMap()))
        assertTrue(MandalaRepository.deserializeColors("").isEmpty())
        assertTrue(MandalaRepository.deserializeColors("   ").isEmpty())

        // Test normal map
        val input = mapOf(
            "r1" to 0xFFFF4081L,
            "r2" to 0xFF1E88E5L,
            "petal_center" to 0xFF4CAF50L
        )
        val serialized = MandalaRepository.serializeColors(input)
        val output = MandalaRepository.deserializeColors(serialized)

        assertEquals(input.size, output.size)
        assertEquals(0xFFFF4081L, output["r1"])
        assertEquals(0xFF1E88E5L, output["r2"])
        assertEquals(0xFF4CAF50L, output["petal_center"])

        // Test resilient handling of corrupted / partial tokens
        val corrupted = "r1:12345;invalid_token;r2:not_a_number;r3:67890"
        val parsedCorrupted = MandalaRepository.deserializeColors(corrupted)
        assertEquals(2, parsedCorrupted.size)
        assertEquals(12345L, parsedCorrupted["r1"])
        assertEquals(67890L, parsedCorrupted["r3"])
    }

    @Test
    fun verifyPaletteConfigurations() {
        val palettes = MandalaPalettes.allPalettes
        assertTrue("Must have multiple palettes", palettes.size >= 4)

        for (palette in palettes) {
            assertTrue("Palette name cannot be blank", palette.name.isNotBlank())
            assertTrue("Palette ${palette.name} must contain at least 8 colors", palette.colors.size >= 8)
            for (color in palette.colors) {
                // Must be valid non-zero ARGB
                assertTrue("Color $color in ${palette.name} must be valid", color != 0L)
            }
        }

        assertTrue("Default color must be valid", MandalaPalettes.defaultColor != 0L)
        assertEquals(0xFFFFFFFFL, MandalaPalettes.CLEAR_COLOR)
    }
}
