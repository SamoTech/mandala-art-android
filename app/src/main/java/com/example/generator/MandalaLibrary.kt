package com.example.generator

import com.example.model.Difficulty
import com.example.model.MandalaCategory
import com.example.model.MandalaDesign
import com.example.model.MandalaRegion
import kotlin.math.PI

object MandalaLibrary {

    val designs: List<MandalaDesign> by lazy {
        listOf(
            createLotusBloom(),
            createSunburstHarmony(),
            createZenWheel(),
            createFourSeasonsPetal(),
            createMysticDaisy(),
            createSolarChakra(),
            createStarlightBlossom(),
            createDiamondRadiance(),
            createCosmicOrbit(),
            createSacredGeometry(),
            createCelestialDahlia(),
            createMandalaOfWisdom(),
            createEternalSwirl(),
            createKaleidoscopeDream(),
            createGoldenPeacock(),
            createTempleOfPeace(),
            createInfinityLattice(),
            createArabesqueRosette(),
            createSanskritWheel(),
            createUniverseEye(),
            createCrownChakra(),
            createMandalaOfSerenity(),
            createGalaxySpiral(),
            createRoyalRosette()
        ) + createAdditionalDesigns()
    }

    /**
     * Additional locally generated designs. Each uses a distinct radial recipe,
     * stable region IDs, and the same vector-region engine as the original library.
     */
    private fun createAdditionalDesigns(): List<MandalaDesign> {
        data class Recipe(
            val id: String,
            val title: String,
            val category: MandalaCategory,
            val difficulty: Difficulty,
            val symmetry: Int,
            val style: Int
        )

        val recipes = listOf(
            Recipe("moonlit_lotus", "Moonlit Lotus", MandalaCategory.FLORAL, Difficulty.EASY, 10, 0),
            Recipe("rose_compass", "Rose Compass", MandalaCategory.FLORAL, Difficulty.MEDIUM, 12, 1),
            Recipe("ocean_tide", "Ocean Tide", MandalaCategory.GEOMETRIC, Difficulty.EASY, 14, 2),
            Recipe("desert_sun", "Desert Sun", MandalaCategory.GEOMETRIC, Difficulty.MEDIUM, 16, 3),
            Recipe("butterfly_garden", "Butterfly Garden", MandalaCategory.FLORAL, Difficulty.MEDIUM, 12, 4),
            Recipe("crystal_bloom", "Crystal Bloom", MandalaCategory.INTRICATE, Difficulty.HARD, 18, 5),
            Recipe("night_sky", "Night Sky", MandalaCategory.GEOMETRIC, Difficulty.MEDIUM, 16, 6),
            Recipe("arabesque_dream", "Arabesque Dream", MandalaCategory.INTRICATE, Difficulty.HARD, 20, 7),
            Recipe("fern_and_flower", "Fern and Flower", MandalaCategory.FLORAL, Difficulty.MEDIUM, 14, 8),
            Recipe("celestial_crown", "Celestial Crown", MandalaCategory.INTRICATE, Difficulty.HARD, 18, 9),
            Recipe("prism_waves", "Prism Waves", MandalaCategory.GEOMETRIC, Difficulty.HARD, 22, 10),
            Recipe("infinite_garden", "Infinite Garden", MandalaCategory.FLORAL, Difficulty.HARD, 24, 11)
        )

        val premiumRecipes = listOf(
            Recipe("premium_mandala_1", "Amethyst Petals", MandalaCategory.FLORAL, Difficulty.MEDIUM, 10, 0),
            Recipe("premium_mandala_2", "Golden Mandala", MandalaCategory.GEOMETRIC, Difficulty.HARD, 11, 1),
            Recipe("premium_mandala_3", "Sapphire Bloom", MandalaCategory.GEOMETRIC, Difficulty.EASY, 12, 2),
            Recipe("premium_mandala_4", "Coral Reverie", MandalaCategory.FLORAL, Difficulty.MEDIUM, 13, 3),
            Recipe("premium_mandala_5", "Emerald Lace", MandalaCategory.GEOMETRIC, Difficulty.HARD, 14, 4),
            Recipe("premium_mandala_6", "Starlit Garden", MandalaCategory.GEOMETRIC, Difficulty.EASY, 15, 5),
            Recipe("premium_mandala_7", "Royal Sunflower", MandalaCategory.FLORAL, Difficulty.MEDIUM, 16, 6),
            Recipe("premium_mandala_8", "Oceanic Geometry", MandalaCategory.GEOMETRIC, Difficulty.HARD, 17, 7),
            Recipe("premium_mandala_9", "Velvet Rose", MandalaCategory.GEOMETRIC, Difficulty.EASY, 18, 8),
            Recipe("premium_mandala_10", "Aurora Wheel", MandalaCategory.FLORAL, Difficulty.MEDIUM, 19, 9),
            Recipe("premium_mandala_11", "Lotus Sanctuary", MandalaCategory.GEOMETRIC, Difficulty.HARD, 20, 10),
            Recipe("premium_mandala_12", "Sacred Wings", MandalaCategory.GEOMETRIC, Difficulty.EASY, 21, 11),
            Recipe("premium_mandala_13", "Midnight Blossom", MandalaCategory.FLORAL, Difficulty.MEDIUM, 22, 0),
            Recipe("premium_mandala_14", "Desert Mirage", MandalaCategory.GEOMETRIC, Difficulty.HARD, 23, 1),
            Recipe("premium_mandala_15", "Crystal Lotus", MandalaCategory.GEOMETRIC, Difficulty.EASY, 24, 2),
            Recipe("premium_mandala_16", "Floral Mosaic", MandalaCategory.FLORAL, Difficulty.MEDIUM, 10, 3),
            Recipe("premium_mandala_17", "Celestial Waves", MandalaCategory.GEOMETRIC, Difficulty.HARD, 11, 4),
            Recipe("premium_mandala_18", "Moon Garden", MandalaCategory.GEOMETRIC, Difficulty.EASY, 12, 5),
            Recipe("premium_mandala_19", "Radiant Compass", MandalaCategory.FLORAL, Difficulty.MEDIUM, 13, 6),
            Recipe("premium_mandala_20", "Pearl Mandala", MandalaCategory.GEOMETRIC, Difficulty.HARD, 14, 7),
            Recipe("premium_mandala_21", "Wildflower Halo", MandalaCategory.GEOMETRIC, Difficulty.EASY, 15, 8),
            Recipe("premium_mandala_22", "Opal Dream", MandalaCategory.FLORAL, Difficulty.MEDIUM, 16, 9),
            Recipe("premium_mandala_23", "Sunset Filigree", MandalaCategory.GEOMETRIC, Difficulty.HARD, 17, 10),
            Recipe("premium_mandala_24", "Zen Garden", MandalaCategory.GEOMETRIC, Difficulty.EASY, 18, 11),
            Recipe("premium_mandala_25", "Prismatic Lotus", MandalaCategory.FLORAL, Difficulty.MEDIUM, 19, 0),
            Recipe("premium_mandala_26", "Silver Fern", MandalaCategory.GEOMETRIC, Difficulty.HARD, 20, 1),
            Recipe("premium_mandala_27", "Cosmic Petals", MandalaCategory.GEOMETRIC, Difficulty.EASY, 21, 2),
            Recipe("premium_mandala_28", "Royal Arabesque", MandalaCategory.FLORAL, Difficulty.MEDIUM, 22, 3),
            Recipe("premium_mandala_29", "Dawn Star", MandalaCategory.GEOMETRIC, Difficulty.HARD, 23, 4),
            Recipe("premium_mandala_30", "Enchanted Dahlia", MandalaCategory.GEOMETRIC, Difficulty.EASY, 24, 5),
            Recipe("premium_mandala_31", "Diamond Garden", MandalaCategory.FLORAL, Difficulty.MEDIUM, 10, 6),
            Recipe("premium_mandala_32", "Northern Lights", MandalaCategory.GEOMETRIC, Difficulty.HARD, 11, 7),
            Recipe("premium_mandala_33", "Flame Blossom", MandalaCategory.GEOMETRIC, Difficulty.EASY, 12, 8),
            Recipe("premium_mandala_34", "Lotus Crown", MandalaCategory.FLORAL, Difficulty.MEDIUM, 13, 9),
            Recipe("premium_mandala_35", "Eternal Petals", MandalaCategory.GEOMETRIC, Difficulty.HARD, 14, 10),
            Recipe("premium_mandala_36", "Jasmine Circle", MandalaCategory.GEOMETRIC, Difficulty.EASY, 15, 11),
            Recipe("premium_mandala_37", "Sapphire Star", MandalaCategory.FLORAL, Difficulty.MEDIUM, 16, 0),
            Recipe("premium_mandala_38", "Harmony Wheel", MandalaCategory.GEOMETRIC, Difficulty.HARD, 17, 1),
            Recipe("premium_mandala_39", "Rosewater Mandala", MandalaCategory.GEOMETRIC, Difficulty.EASY, 18, 2),
            Recipe("premium_mandala_40", "Golden Fern", MandalaCategory.FLORAL, Difficulty.MEDIUM, 19, 3),
            Recipe("premium_mandala_41", "Celestial Garden", MandalaCategory.GEOMETRIC, Difficulty.HARD, 20, 4),
            Recipe("premium_mandala_42", "Blooming Spiral", MandalaCategory.GEOMETRIC, Difficulty.EASY, 21, 5),
            Recipe("premium_mandala_43", "Mystic Peacock", MandalaCategory.FLORAL, Difficulty.MEDIUM, 22, 6),
            Recipe("premium_mandala_44", "Starlight Crown", MandalaCategory.GEOMETRIC, Difficulty.HARD, 23, 7),
            Recipe("premium_mandala_45", "Ocean Jewel", MandalaCategory.GEOMETRIC, Difficulty.EASY, 24, 8),
            Recipe("premium_mandala_46", "Radiant Arabesque", MandalaCategory.FLORAL, Difficulty.MEDIUM, 10, 9),
            Recipe("premium_mandala_47", "Lavender Halo", MandalaCategory.GEOMETRIC, Difficulty.HARD, 11, 10),
            Recipe("premium_mandala_48", "Infinite Blossom", MandalaCategory.GEOMETRIC, Difficulty.EASY, 12, 11),
            Recipe("premium_mandala_49", "Solar Garden", MandalaCategory.FLORAL, Difficulty.MEDIUM, 13, 0),
            Recipe("premium_mandala_50", "Dreamcatcher Mandala", MandalaCategory.GEOMETRIC, Difficulty.HARD, 14, 1)
        )

        val levelRecipes = listOf(
            // Level 1 - Blossom Path (Easy)
            Recipe("level1_mandala_1", "Dawn Blossom", MandalaCategory.FLORAL, Difficulty.EASY, 10, 0),
            Recipe("level1_mandala_2", "Simple Harmony", MandalaCategory.GEOMETRIC, Difficulty.EASY, 12, 1),
            Recipe("level1_mandala_3", "Spring Petals", MandalaCategory.FLORAL, Difficulty.EASY, 10, 2),
            Recipe("level1_mandala_4", "Copper Wheel", MandalaCategory.GEOMETRIC, Difficulty.EASY, 12, 3),
            Recipe("level1_mandala_5", "Morning Star", MandalaCategory.GEOMETRIC, Difficulty.EASY, 14, 4),
            Recipe("level1_mandala_6", "Gentle Bloom", MandalaCategory.FLORAL, Difficulty.EASY, 10, 5),
            // Level 2 - Golden Flow (Medium)
            Recipe("level2_mandala_1", "Sunrise Mosaic", MandalaCategory.GEOMETRIC, Difficulty.MEDIUM, 14, 6),
            Recipe("level2_mandala_2", "Desert Bloom", MandalaCategory.FLORAL, Difficulty.MEDIUM, 16, 7),
            Recipe("level2_mandala_3", "Twilight Wheel", MandalaCategory.GEOMETRIC, Difficulty.MEDIUM, 14, 8),
            Recipe("level2_mandala_4", "Sapphire Garden", MandalaCategory.FLORAL, Difficulty.MEDIUM, 16, 9),
            Recipe("level2_mandala_5", "Amber Lotus", MandalaCategory.INTRICATE, Difficulty.MEDIUM, 15, 10),
            Recipe("level2_mandala_6", "Silver Compass", MandalaCategory.GEOMETRIC, Difficulty.MEDIUM, 18, 11),
            // Level 3 - Master Circle (Hard)
            Recipe("level3_mandala_1", "Cosmic Tapestry", MandalaCategory.INTRICATE, Difficulty.HARD, 18, 0),
            Recipe("level3_mandala_2", "Royal Garden", MandalaCategory.FLORAL, Difficulty.HARD, 20, 1),
            Recipe("level3_mandala_3", "Infinity Bloom", MandalaCategory.INTRICATE, Difficulty.HARD, 22, 2),
            Recipe("level3_mandala_4", "Celestial Web", MandalaCategory.GEOMETRIC, Difficulty.HARD, 20, 3),
            Recipe("level3_mandala_5", "Phoenix Wheel", MandalaCategory.INTRICATE, Difficulty.HARD, 24, 4),
            Recipe("level3_mandala_6", "Emerald Crown", MandalaCategory.FLORAL, Difficulty.HARD, 22, 5)
        )

        return (recipes + premiumRecipes + levelRecipes).map { recipe ->
            val regions = mutableListOf<MandalaRegion>()
            regions.add(MandalaShapes.circle("${recipe.id}_core", radius = if (recipe.difficulty == Difficulty.HARD) 28f else 38f))
            val n = recipe.symmetry
            val step = 360f / n
            val halfWidth = (PI / (n * if (recipe.style % 2 == 0) 1.15 else 1.35)).toFloat()

            // Four balanced rings keep the artwork legible and fully fillable.
            for (i in 0 until n) {
                val a = (i * 2 * PI / n).toFloat()
                val deg = i * step
                when (recipe.style % 4) {
                    0 -> regions.add(MandalaShapes.petal("${recipe.id}_inner_$i", 42f, 132f, a, halfWidth))
                    1 -> regions.add(MandalaShapes.annularSector("${recipe.id}_inner_$i", 45f, 125f, deg, step))
                    2 -> regions.add(MandalaShapes.diamond("${recipe.id}_inner_$i", 42f, 135f, 88f, a, halfWidth))
                    else -> regions.add(MandalaShapes.triangleRay("${recipe.id}_inner_$i", 45f, 135f, a - halfWidth, a, a + halfWidth))
                }
            }
            for (i in 0 until n) {
                val a = ((i + 0.5) * 2 * PI / n).toFloat()
                val deg = (i + 0.5f) * step
                when (recipe.style % 4) {
                    0 -> regions.add(MandalaShapes.diamond("${recipe.id}_mid_$i", 125f, 245f, 185f, a, halfWidth))
                    1 -> regions.add(MandalaShapes.petal("${recipe.id}_mid_$i", 120f, 250f, a, halfWidth))
                    2 -> regions.add(MandalaShapes.annularSector("${recipe.id}_mid_$i", 140f, 235f, deg - step * 0.5f, step))
                    else -> regions.add(MandalaShapes.petal("${recipe.id}_mid_$i", 130f, 250f, a, halfWidth * 0.9f))
                }
            }
            for (i in 0 until n) {
                val a = (i * 2 * PI / n).toFloat()
                val deg = i * step
                when ((recipe.style + 1) % 4) {
                    0 -> regions.add(MandalaShapes.petal("${recipe.id}_outer_$i", 235f, 355f, a, halfWidth))
                    1 -> regions.add(MandalaShapes.diamond("${recipe.id}_outer_$i", 230f, 355f, 292f, a, halfWidth))
                    2 -> regions.add(MandalaShapes.annularSector("${recipe.id}_outer_$i", 245f, 350f, deg, step))
                    else -> regions.add(MandalaShapes.triangleRay("${recipe.id}_outer_$i", 245f, 365f, a - halfWidth, a, a + halfWidth))
                }
            }
            for (i in 0 until n) {
                val a = ((i + 0.5) * 2 * PI / n).toFloat()
                val deg = (i + 0.5f) * step
                when (recipe.style % 3) {
                    0 -> regions.add(MandalaShapes.bead("${recipe.id}_bead_$i", 405f, if (n >= 20) 8f else 11f, a))
                    1 -> regions.add(MandalaShapes.annularSector("${recipe.id}_rim_$i", 365f, 430f, deg - step * 0.5f, step))
                    else -> regions.add(MandalaShapes.petal("${recipe.id}_rim_$i", 355f, 435f, a, halfWidth * 0.8f))
                }
            }

            MandalaDesign(
                id = recipe.id,
                title = recipe.title,
                category = recipe.category,
                difficulty = recipe.difficulty,
                regions = regions
            )
        }
    }

    fun getDesignById(id: String): MandalaDesign {
        return designs.find { it.id == id } ?: designs.first()
    }

    // 1. Lotus Bloom (Easy / Floral - 25 regions)
    private fun createLotusBloom(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("center_gem", radius = 45f))

        // Inner 8 petals
        val symmetry = 8
        for (i in 0 until symmetry) {
            val angle = (i * 2 * PI / symmetry).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "inner_petal_$i",
                    rBase = 50f,
                    rTip = 160f,
                    centerAngleRad = angle,
                    halfWidthRad = (PI / (symmetry * 1.3)).toFloat()
                )
            )
        }

        // Mid 8 petals staggered
        for (i in 0 until symmetry) {
            val angle = ((i + 0.5) * 2 * PI / symmetry).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "mid_petal_$i",
                    rBase = 120f,
                    rTip = 290f,
                    centerAngleRad = angle,
                    halfWidthRad = (PI / (symmetry * 1.2)).toFloat()
                )
            )
        }

        // Outer 8 large lotus crowns
        for (i in 0 until symmetry) {
            val angle = (i * 2 * PI / symmetry).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "outer_petal_$i",
                    rBase = 220f,
                    rTip = 440f,
                    centerAngleRad = angle,
                    halfWidthRad = (PI / (symmetry * 1.1)).toFloat()
                )
            )
        }

        return MandalaDesign(
            id = "lotus_bloom",
            title = "Lotus Bloom",
            category = MandalaCategory.FLORAL,
            difficulty = Difficulty.EASY,
            regions = regions
        )
    }

    // 2. Sunburst Harmony (Easy / Geometric - 33 regions)
    private fun createSunburstHarmony(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("core_sun", radius = 55f))

        val count = 16
        // Ring of 16 inner sectors
        for (i in 0 until count) {
            val deg = i * (360f / count)
            regions.add(
                MandalaShapes.annularSector(
                    id = "inner_ring_$i",
                    rInner = 60f,
                    rOuter = 150f,
                    startDeg = deg,
                    sweepDeg = 360f / count
                )
            )
        }

        // 16 Radiating sunburst rays
        for (i in 0 until count) {
            val angle = (i * 2 * PI / count).toFloat()
            val step = (2 * PI / count).toFloat()
            regions.add(
                MandalaShapes.triangleRay(
                    id = "sun_ray_$i",
                    rBase = 160f,
                    rTip = 420f,
                    startRad = angle - step * 0.45f,
                    tipRad = angle,
                    endRad = angle + step * 0.45f
                )
            )
        }

        return MandalaDesign(
            id = "sunburst_harmony",
            title = "Sunburst Harmony",
            category = MandalaCategory.GEOMETRIC,
            difficulty = Difficulty.EASY,
            regions = regions
        )
    }

    // 3. Zen Wheel (Easy / Geometric - 29 regions)
    private fun createZenWheel(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("zen_hub", radius = 50f))
        regions.add(MandalaShapes.circle("zen_center_bead", radius = 22f))

        val spokes = 12
        // 12 Inner Dharma spokes
        for (i in 0 until spokes) {
            val deg = i * (360f / spokes)
            regions.add(
                MandalaShapes.annularSector(
                    id = "dharma_spoke_$i",
                    rInner = 55f,
                    rOuter = 210f,
                    startDeg = deg,
                    sweepDeg = 360f / spokes
                )
            )
        }

        // 12 Outer rim arches
        for (i in 0 until spokes) {
            val deg = i * (360f / spokes)
            regions.add(
                MandalaShapes.arch(
                    id = "outer_arch_$i",
                    rBase = 220f,
                    rPeak = 420f,
                    startDeg = deg,
                    sweepDeg = 360f / spokes
                )
            )
        }

        // Outer rim border sectors
        for (i in 0 until 3) {
            val deg = i * 120f
            regions.add(
                MandalaShapes.annularSector(
                    id = "zen_rim_$i",
                    rInner = 425f,
                    rOuter = 460f,
                    startDeg = deg,
                    sweepDeg = 120f
                )
            )
        }

        return MandalaDesign(
            id = "zen_wheel",
            title = "Zen Wheel",
            category = MandalaCategory.GEOMETRIC,
            difficulty = Difficulty.EASY,
            regions = regions
        )
    }

    // 4. Four Seasons Petal (Easy / Floral - 37 regions)
    private fun createFourSeasonsPetal(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("seasons_core", radius = 40f))

        val quadrants = 4
        // 4 Seasons main large petals
        for (i in 0 until quadrants) {
            val angle = (i * 2 * PI / quadrants).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "season_core_petal_$i",
                    rBase = 45f,
                    rTip = 200f,
                    centerAngleRad = angle,
                    halfWidthRad = (PI / 5).toFloat()
                )
            )
        }

        // 8 Secondary leaf petals
        for (i in 0 until 8) {
            val angle = (i * 2 * PI / 8).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "leaf_petal_$i",
                    rBase = 170f,
                    rTip = 330f,
                    centerAngleRad = angle,
                    halfWidthRad = (PI / 10).toFloat()
                )
            )
        }

        // 12 Outer blossom petals
        for (i in 0 until 12) {
            val angle = (i * 2 * PI / 12).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "outer_blossom_$i",
                    rBase = 280f,
                    rTip = 445f,
                    centerAngleRad = angle,
                    halfWidthRad = (PI / 15).toFloat()
                )
            )
        }

        // 12 Corner beads
        for (i in 0 until 12) {
            val angle = ((i + 0.5) * 2 * PI / 12).toFloat()
            regions.add(
                MandalaShapes.bead(
                    id = "corner_bead_$i",
                    rDistance = 370f,
                    radius = 16f,
                    angleRad = angle
                )
            )
        }

        return MandalaDesign(
            id = "four_seasons",
            title = "Four Seasons",
            category = MandalaCategory.FLORAL,
            difficulty = Difficulty.EASY,
            regions = regions
        )
    }

    // 5. Mystic Daisy (Medium / Floral - 49 regions)
    private fun createMysticDaisy(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("daisy_center", radius = 45f))

        // 16 inner petals
        val count = 16
        for (i in 0 until count) {
            val angle = (i * 2 * PI / count).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "inner_daisy_$i",
                    rBase = 50f,
                    rTip = 180f,
                    centerAngleRad = angle,
                    halfWidthRad = (PI / (count * 1.2)).toFloat()
                )
            )
        }

        // 16 mid diamonds
        for (i in 0 until count) {
            val angle = ((i + 0.5) * 2 * PI / count).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "daisy_diamond_$i",
                    rInner = 160f,
                    rOuter = 300f,
                    rMid = 230f,
                    centerAngleRad = angle,
                    halfWidthRad = (PI / (count * 1.5)).toFloat()
                )
            )
        }

        // 16 outer flare petals
        for (i in 0 until count) {
            val angle = (i * 2 * PI / count).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "outer_daisy_$i",
                    rBase = 260f,
                    rTip = 440f,
                    centerAngleRad = angle,
                    halfWidthRad = (PI / (count * 1.3)).toFloat()
                )
            )
        }

        return MandalaDesign(
            id = "mystic_daisy",
            title = "Mystic Daisy",
            category = MandalaCategory.FLORAL,
            difficulty = Difficulty.MEDIUM,
            regions = regions
        )
    }

    // 6. Solar Chakra (Medium / Geometric - 57 regions)
    private fun createSolarChakra(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("chakra_center_pearl", radius = 35f))

        val n = 14
        // 14 inner chakra flames
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "chakra_flame_$i",
                    rBase = 40f,
                    rTip = 160f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.1)).toFloat()
                )
            )
        }

        // 14 diamond stars
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "chakra_gem_$i",
                    rInner = 140f,
                    rOuter = 270f,
                    rMid = 205f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.3)).toFloat()
                )
            )
        }

        // 14 outer lotus rays
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.triangleRay(
                    id = "chakra_ray_$i",
                    rBase = 250f,
                    rTip = 420f,
                    startRad = a - (PI / n * 0.45).toFloat(),
                    tipRad = a,
                    endRad = a + (PI / n * 0.45).toFloat()
                )
            )
        }

        // 14 crown beads
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.bead(
                    id = "crown_bead_$i",
                    rDistance = 370f,
                    radius = 20f,
                    angleRad = a
                )
            )
        }

        return MandalaDesign(
            id = "solar_chakra",
            title = "Solar Chakra",
            category = MandalaCategory.GEOMETRIC,
            difficulty = Difficulty.MEDIUM,
            regions = regions
        )
    }

    // 7. Starlight Blossom (Medium / Floral - 65 regions)
    private fun createStarlightBlossom(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("star_core", radius = 40f))

        val symmetry = 16
        // 16 innermost petals
        for (i in 0 until symmetry) {
            val a = (i * 2 * PI / symmetry).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "tier1_petal_$i",
                    rBase = 45f,
                    rTip = 140f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (symmetry * 1.2)).toFloat()
                )
            )
        }

        // 16 tier 2 petals
        for (i in 0 until symmetry) {
            val a = ((i + 0.5) * 2 * PI / symmetry).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "tier2_petal_$i",
                    rBase = 120f,
                    rTip = 240f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (symmetry * 1.2)).toFloat()
                )
            )
        }

        // 16 tier 3 diamonds
        for (i in 0 until symmetry) {
            val a = (i * 2 * PI / symmetry).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "tier3_diamond_$i",
                    rInner = 220f,
                    rOuter = 340f,
                    rMid = 280f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (symmetry * 1.3)).toFloat()
                )
            )
        }

        // 16 star tips
        for (i in 0 until symmetry) {
            val a = ((i + 0.5) * 2 * PI / symmetry).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "tier4_tip_$i",
                    rBase = 310f,
                    rTip = 440f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (symmetry * 1.2)).toFloat()
                )
            )
        }

        return MandalaDesign(
            id = "starlight_blossom",
            title = "Starlight Blossom",
            category = MandalaCategory.FLORAL,
            difficulty = Difficulty.MEDIUM,
            regions = regions
        )
    }

    // 8. Diamond Radiance (Medium / Geometric - 65 regions)
    private fun createDiamondRadiance(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("rad_core", radius = 35f))

        val count = 16
        // Ring 1 diamonds
        for (i in 0 until count) {
            val a = (i * 2 * PI / count).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "rad_d1_$i",
                    rInner = 40f,
                    rOuter = 160f,
                    rMid = 100f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / count * 0.9).toFloat()
                )
            )
        }

        // Ring 2 diamonds
        for (i in 0 until count) {
            val a = ((i + 0.5) * 2 * PI / count).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "rad_d2_$i",
                    rInner = 140f,
                    rOuter = 260f,
                    rMid = 200f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / count * 0.9).toFloat()
                )
            )
        }

        // Ring 3 diamonds
        for (i in 0 until count) {
            val a = (i * 2 * PI / count).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "rad_d3_$i",
                    rInner = 240f,
                    rOuter = 360f,
                    rMid = 300f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / count * 0.9).toFloat()
                )
            )
        }

        // Ring 4 outer diamonds
        for (i in 0 until count) {
            val a = ((i + 0.5) * 2 * PI / count).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "rad_d4_$i",
                    rInner = 340f,
                    rOuter = 450f,
                    rMid = 395f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / count * 0.9).toFloat()
                )
            )
        }

        return MandalaDesign(
            id = "diamond_radiance",
            title = "Diamond Radiance",
            category = MandalaCategory.GEOMETRIC,
            difficulty = Difficulty.MEDIUM,
            regions = regions
        )
    }

    // 9. Cosmic Orbit (Medium / Intricate - 73 regions)
    private fun createCosmicOrbit(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("cosmic_sun", radius = 50f))

        val n = 18
        // Inner planetary sectors
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(
                MandalaShapes.annularSector(
                    id = "orbit_inner_$i",
                    rInner = 55f,
                    rOuter = 160f,
                    startDeg = deg,
                    sweepDeg = 360f / n
                )
            )
        }

        // Mid orbital teardrops
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "orbit_drop_$i",
                    rBase = 150f,
                    rTip = 280f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.3)).toFloat()
                )
            )
        }

        // Outer planetary sectors
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(
                MandalaShapes.annularSector(
                    id = "orbit_mid_$i",
                    rInner = 270f,
                    rOuter = 370f,
                    startDeg = deg,
                    sweepDeg = 360f / n
                )
            )
        }

        // Outer solar flare tips
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "orbit_flare_$i",
                    rBase = 350f,
                    rTip = 445f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        return MandalaDesign(
            id = "cosmic_orbit",
            title = "Cosmic Orbit",
            category = MandalaCategory.INTRICATE,
            difficulty = Difficulty.MEDIUM,
            regions = regions
        )
    }

    // 10. Sacred Geometry (Medium / Geometric - 81 regions)
    private fun createSacredGeometry(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("sacred_center", radius = 40f))

        val count = 20
        // Layer 1: 20 sectors
        for (i in 0 until count) {
            val deg = i * (360f / count)
            regions.add(
                MandalaShapes.annularSector(
                    id = "geo_l1_$i",
                    rInner = 45f,
                    rOuter = 130f,
                    startDeg = deg,
                    sweepDeg = 360f / count
                )
            )
        }

        // Layer 2: 20 diamonds
        for (i in 0 until count) {
            val a = ((i + 0.5) * 2 * PI / count).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "geo_l2_$i",
                    rInner = 120f,
                    rOuter = 230f,
                    rMid = 175f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (count * 1.2)).toFloat()
                )
            )
        }

        // Layer 3: 20 arches
        for (i in 0 until count) {
            val deg = i * (360f / count)
            regions.add(
                MandalaShapes.arch(
                    id = "geo_l3_$i",
                    rBase = 220f,
                    rPeak = 330f,
                    startDeg = deg,
                    sweepDeg = 360f / count
                )
            )
        }

        // Layer 4: 20 outer star rays
        for (i in 0 until count) {
            val a = ((i + 0.5) * 2 * PI / count).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "geo_l4_$i",
                    rBase = 310f,
                    rTip = 445f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (count * 1.2)).toFloat()
                )
            )
        }

        return MandalaDesign(
            id = "sacred_geometry",
            title = "Sacred Geometry",
            category = MandalaCategory.GEOMETRIC,
            difficulty = Difficulty.MEDIUM,
            regions = regions
        )
    }

    // 11. Celestial Dahlia (Hard / Floral - 97 regions)
    private fun createCelestialDahlia(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("dahlia_seed", radius = 35f))

        val n = 16
        // Tier 1 petals
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "dahlia_t1_$i",
                    rBase = 40f,
                    rTip = 120f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Tier 2 petals
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "dahlia_t2_$i",
                    rBase = 100f,
                    rTip = 190f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Tier 3 petals
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "dahlia_t3_$i",
                    rBase = 170f,
                    rTip = 270f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Tier 4 petals
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "dahlia_t4_$i",
                    rBase = 250f,
                    rTip = 350f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Tier 5 petals
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "dahlia_t5_$i",
                    rBase = 330f,
                    rTip = 425f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Tier 6 outer beads
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.bead(
                    id = "dahlia_bead_$i",
                    rDistance = 425f,
                    radius = 16f,
                    angleRad = a
                )
            )
        }

        return MandalaDesign(
            id = "celestial_dahlia",
            title = "Celestial Dahlia",
            category = MandalaCategory.FLORAL,
            difficulty = Difficulty.HARD,
            regions = regions
        )
    }

    // 12. Mandala of Wisdom (Hard / Intricate - 113 regions)
    private fun createMandalaOfWisdom(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("wisdom_core", radius = 30f))

        val n = 16
        // Layer 1
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "wis_l1_$i",
                    rBase = 35f,
                    rTip = 100f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Layer 2: Ring sectors
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(
                MandalaShapes.annularSector(
                    id = "wis_l2_$i",
                    rInner = 95f,
                    rOuter = 160f,
                    startDeg = deg,
                    sweepDeg = 360f / n
                )
            )
        }

        // Layer 3: Diamonds
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "wis_l3_$i",
                    rInner = 150f,
                    rOuter = 230f,
                    rMid = 190f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.3)).toFloat()
                )
            )
        }

        // Layer 4: Arches
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(
                MandalaShapes.arch(
                    id = "wis_l4_$i",
                    rBase = 220f,
                    rPeak = 290f,
                    startDeg = deg,
                    sweepDeg = 360f / n
                )
            )
        }

        // Layer 5: Petals
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "wis_l5_$i",
                    rBase = 280f,
                    rTip = 360f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Layer 6: Rays
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            val step = (2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.triangleRay(
                    id = "wis_l6_$i",
                    rBase = 350f,
                    rTip = 430f,
                    startRad = a - step * 0.45f,
                    tipRad = a,
                    endRad = a + step * 0.45f
                )
            )
        }

        // Layer 7: Beads
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.bead(
                    id = "wis_bead_$i",
                    rDistance = 425f,
                    radius = 14f,
                    angleRad = a
                )
            )
        }

        return MandalaDesign(
            id = "mandala_wisdom",
            title = "Mandala of Wisdom",
            category = MandalaCategory.INTRICATE,
            difficulty = Difficulty.HARD,
            regions = regions
        )
    }

    // 13. Eternal Swirl (Hard / Intricate - 121 regions)
    private fun createEternalSwirl(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("swirl_hub", radius = 35f))

        val n = 20
        // Swirl spokes tier 1
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.swirlSpoke(
                    id = "swirl_t1_$i",
                    rInner = 40f,
                    rOuter = 140f,
                    startAngleRad = a,
                    twistRad = (PI / n * 1.5).toFloat(),
                    widthRad = (PI / n * 0.9).toFloat()
                )
            )
        }

        // Swirl spokes tier 2
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.swirlSpoke(
                    id = "swirl_t2_$i",
                    rInner = 140f,
                    rOuter = 240f,
                    startAngleRad = a,
                    twistRad = -(PI / n * 1.5).toFloat(),
                    widthRad = (PI / n * 0.9).toFloat()
                )
            )
        }

        // Swirl spokes tier 3
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.swirlSpoke(
                    id = "swirl_t3_$i",
                    rInner = 240f,
                    rOuter = 340f,
                    startAngleRad = a,
                    twistRad = (PI / n * 1.5).toFloat(),
                    widthRad = (PI / n * 0.9).toFloat()
                )
            )
        }

        // Tier 4: Diamonds
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "swirl_d_$i",
                    rInner = 330f,
                    rOuter = 410f,
                    rMid = 370f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.3)).toFloat()
                )
            )
        }

        // Tier 5: Outer beads
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.bead(
                    id = "swirl_bead_$i",
                    rDistance = 425f,
                    radius = 14f,
                    angleRad = a
                )
            )
        }

        // Outer crown arc sectors
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(
                MandalaShapes.annularSector(
                    id = "swirl_rim_$i",
                    rInner = 430f,
                    rOuter = 455f,
                    startDeg = deg,
                    sweepDeg = 360f / n
                )
            )
        }

        return MandalaDesign(
            id = "eternal_swirl",
            title = "Eternal Swirl",
            category = MandalaCategory.INTRICATE,
            difficulty = Difficulty.HARD,
            regions = regions
        )
    }

    // 14. Kaleidoscope Dream (Hard / Geometric - 105 regions)
    private fun createKaleidoscopeDream(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("kaleido_eye", radius = 30f))

        val n = 16
        // Ring 1 triangles
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            val step = (2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.triangleRay(
                    id = "kaleido_r1_$i",
                    rBase = 35f,
                    rTip = 110f,
                    startRad = a - step * 0.45f,
                    tipRad = a,
                    endRad = a + step * 0.45f
                )
            )
        }

        // Ring 2 diamonds
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "kaleido_d1_$i",
                    rInner = 100f,
                    rOuter = 180f,
                    rMid = 140f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.3)).toFloat()
                )
            )
        }

        // Ring 3 diamonds
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "kaleido_d2_$i",
                    rInner = 170f,
                    rOuter = 260f,
                    rMid = 215f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.3)).toFloat()
                )
            )
        }

        // Ring 4 diamonds
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "kaleido_d3_$i",
                    rInner = 250f,
                    rOuter = 340f,
                    rMid = 295f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.3)).toFloat()
                )
            )
        }

        // Ring 5 petals
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "kaleido_p_$i",
                    rBase = 320f,
                    rTip = 410f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Outer rim sectors
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(
                MandalaShapes.annularSector(
                    id = "kaleido_rim_$i",
                    rInner = 415f,
                    rOuter = 450f,
                    startDeg = deg,
                    sweepDeg = 360f / n
                )
            )
        }

        return MandalaDesign(
            id = "kaleidoscope_dream",
            title = "Kaleidoscope Dream",
            category = MandalaCategory.GEOMETRIC,
            difficulty = Difficulty.HARD,
            regions = regions
        )
    }

    // 15. Golden Peacock (Hard / Floral - 129 regions)
    private fun createGoldenPeacock(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("peacock_heart", radius = 32f))

        val n = 16
        // Layer 1: inner eyes
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "pea_eye1_$i",
                    rBase = 36f,
                    rTip = 110f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Layer 2: teardrop eyes
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "pea_eye2_$i",
                    rBase = 100f,
                    rTip = 180f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Layer 3: mid feather fan
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "pea_fan_$i",
                    rBase = 170f,
                    rTip = 260f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Layer 4: feather eye outer ring (bead pupil)
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.bead(
                    id = "pea_pupil_$i",
                    rDistance = 215f,
                    radius = 16f,
                    angleRad = a
                )
            )
        }

        // Layer 5: outer plumes
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "pea_plume_$i",
                    rBase = 245f,
                    rTip = 350f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Layer 6: outer eye beads
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.bead(
                    id = "pea_outer_eye_$i",
                    rDistance = 305f,
                    radius = 18f,
                    angleRad = a
                )
            )
        }

        // Layer 7: crown feather tips
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "pea_crown_$i",
                    rBase = 340f,
                    rTip = 445f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Layer 8: outer crest beads
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.bead(
                    id = "pea_crest_bead_$i",
                    rDistance = 410f,
                    radius = 15f,
                    angleRad = a
                )
            )
        }

        return MandalaDesign(
            id = "golden_peacock",
            title = "Golden Peacock",
            category = MandalaCategory.FLORAL,
            difficulty = Difficulty.HARD,
            regions = regions
        )
    }

    // 16. Temple of Peace (Intricate / Hard - 145 regions)
    private fun createTempleOfPeace(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("temple_altar", radius = 30f))

        val n = 18
        // Tier 1: petals
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "tmp_p1_$i",
                    rBase = 35f,
                    rTip = 95f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Tier 2: sectors
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(
                MandalaShapes.annularSector(
                    id = "tmp_s1_$i",
                    rInner = 90f,
                    rOuter = 145f,
                    startDeg = deg,
                    sweepDeg = 360f / n
                )
            )
        }

        // Tier 3: diamonds
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.diamond(
                    id = "tmp_d1_$i",
                    rInner = 140f,
                    rOuter = 210f,
                    rMid = 175f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.3)).toFloat()
                )
            )
        }

        // Tier 4: pagoda arches
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(
                MandalaShapes.arch(
                    id = "tmp_arch_$i",
                    rBase = 205f,
                    rPeak = 275f,
                    startDeg = deg,
                    sweepDeg = 360f / n
                )
            )
        }

        // Tier 5: mid beads
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.bead(
                    id = "tmp_bead1_$i",
                    rDistance = 250f,
                    radius = 14f,
                    angleRad = a
                )
            )
        }

        // Tier 6: lotus crown
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.petal(
                    id = "tmp_lotus_$i",
                    rBase = 270f,
                    rTip = 350f,
                    centerAngleRad = a,
                    halfWidthRad = (PI / (n * 1.2)).toFloat()
                )
            )
        }

        // Tier 7: outer arches
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(
                MandalaShapes.arch(
                    id = "tmp_oarch_$i",
                    rBase = 345f,
                    rPeak = 415f,
                    startDeg = deg,
                    sweepDeg = 360f / n
                )
            )
        }

        // Tier 8: outer rim beads
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(
                MandalaShapes.bead(
                    id = "tmp_bead2_$i",
                    rDistance = 425f,
                    radius = 16f,
                    angleRad = a
                )
            )
        }

        return MandalaDesign(
            id = "temple_of_peace",
            title = "Temple of Peace",
            category = MandalaCategory.INTRICATE,
            difficulty = Difficulty.HARD,
            regions = regions
        )
    }

    // 17. Infinity Lattice (Intricate / Hard - 161 regions)
    private fun createInfinityLattice(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("inf_center", radius = 30f))

        val n = 20
        // 8 rings of 20 alternating elements = 160 + 1 = 161 regions
        for (ring in 0 until 8) {
            val r0 = 35f + ring * 52f
            val r1 = r0 + 52f
            for (i in 0 until n) {
                if (ring % 2 == 0) {
                    val deg = (i + (ring * 0.5f)) * (360f / n)
                    regions.add(
                        MandalaShapes.annularSector(
                            id = "inf_s_${ring}_$i",
                            rInner = r0,
                            rOuter = r1,
                            startDeg = deg,
                            sweepDeg = 360f / n
                        )
                    )
                } else {
                    val a = ((i + (ring * 0.5f)) * 2 * PI / n).toFloat()
                    regions.add(
                        MandalaShapes.diamond(
                            id = "inf_d_${ring}_$i",
                            rInner = r0 - 6f,
                            rOuter = r1 + 6f,
                            rMid = (r0 + r1) * 0.5f,
                            centerAngleRad = a,
                            halfWidthRad = (PI / (n * 1.3)).toFloat()
                        )
                    )
                }
            }
        }

        return MandalaDesign(
            id = "infinity_lattice",
            title = "Infinity Lattice",
            category = MandalaCategory.INTRICATE,
            difficulty = Difficulty.HARD,
            regions = regions
        )
    }

    // 18. Arabesque Rosette (Intricate / Floral - 153 regions)
    private fun createArabesqueRosette(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("ara_rose_core", radius = 32f))

        val n = 19
        // 8 tiers of 19 petals, diamonds, and beads = 1 + 19 * 8 = 153
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("ara_p1_$i", 35f, 95f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("ara_p2_$i", 90f, 150f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.diamond("ara_d1_$i", 145f, 215f, 180f, a, (PI / (n * 1.3)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("ara_p3_$i", 205f, 275f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.bead("ara_b1_$i", 255f, 15f, a))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("ara_p4_$i", 270f, 350f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.diamond("ara_d2_$i", 340f, 415f, 375f, a, (PI / (n * 1.3)).toFloat()))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.bead("ara_b2_$i", 425f, 16f, a))
        }

        return MandalaDesign(
            id = "arabesque_rosette",
            title = "Arabesque Rosette",
            category = MandalaCategory.FLORAL,
            difficulty = Difficulty.HARD,
            regions = regions
        )
    }

    // 19. Sanskrit Wheel (Intricate / Intricate - 169 regions)
    private fun createSanskritWheel(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("san_hub", radius = 30f))

        val n = 21
        // 8 tiers of 21 elements = 1 + 21 * 8 = 169 regions
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(MandalaShapes.annularSector("san_s1_$i", 35f, 85f, deg, 360f / n))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("san_p1_$i", 80f, 140f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(MandalaShapes.annularSector("san_s2_$i", 135f, 195f, deg, 360f / n))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.diamond("san_d1_$i", 190f, 255f, 222f, a, (PI / (n * 1.3)).toFloat()))
        }
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(MandalaShapes.arch("san_a1_$i", 250f, 310f, deg, 360f / n))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("san_p2_$i", 305f, 375f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.diamond("san_d2_$i", 370f, 430f, 400f, a, (PI / (n * 1.3)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.bead("san_b1_$i", 425f, 14f, a))
        }

        return MandalaDesign(
            id = "sanskrit_wheel",
            title = "Sanskrit Wheel",
            category = MandalaCategory.INTRICATE,
            difficulty = Difficulty.HARD,
            regions = regions
        )
    }

    // 20. Universe Eye (Intricate / Geometric - 185 regions)
    private fun createUniverseEye(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("uni_pupil", radius = 25f))

        val n = 23
        // 8 tiers of 23 = 1 + 23 * 8 = 185
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(MandalaShapes.annularSector("uni_iris_$i", 30f, 80f, deg, 360f / n))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.diamond("uni_gem1_$i", 75f, 135f, 105f, a, (PI / (n * 1.3)).toFloat()))
        }
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(MandalaShapes.annularSector("uni_ring2_$i", 130f, 185f, deg, 360f / n))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("uni_ray1_$i", 180f, 250f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.diamond("uni_gem2_$i", 245f, 315f, 280f, a, (PI / (n * 1.3)).toFloat()))
        }
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(MandalaShapes.arch("uni_arch_$i", 310f, 370f, deg, 360f / n))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("uni_ray2_$i", 365f, 435f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.bead("uni_star_$i", 425f, 13f, a))
        }

        return MandalaDesign(
            id = "universe_eye",
            title = "Universe Eye",
            category = MandalaCategory.GEOMETRIC,
            difficulty = Difficulty.HARD,
            regions = regions
        )
    }

    // 21. Crown Chakra (Floral / Intricate - 193 regions)
    private fun createCrownChakra(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("crown_pearl", radius = 28f))

        val n = 24
        // 8 tiers of 24 = 1 + 24 * 8 = 193
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("cr_p1_$i", 32f, 85f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("cr_p2_$i", 80f, 135f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.diamond("cr_d1_$i", 130f, 190f, 160f, a, (PI / (n * 1.3)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("cr_p3_$i", 185f, 250f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.diamond("cr_d2_$i", 245f, 310f, 277f, a, (PI / (n * 1.3)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("cr_p4_$i", 305f, 375f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("cr_p5_$i", 370f, 438f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.bead("cr_bead_$i", 425f, 12f, a))
        }

        return MandalaDesign(
            id = "crown_chakra",
            title = "Crown Chakra",
            category = MandalaCategory.FLORAL,
            difficulty = Difficulty.HARD,
            regions = regions
        )
    }

    // 22. Mandala of Serenity (Medium / Floral - 77 regions)
    private fun createMandalaOfSerenity(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("ser_core", radius = 40f))

        val n = 19
        // 4 tiers of 19 = 76 + 1 = 77
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("ser_p1_$i", 45f, 140f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.diamond("ser_d1_$i", 130f, 240f, 185f, a, (PI / (n * 1.3)).toFloat()))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("ser_p2_$i", 230f, 345f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("ser_p3_$i", 335f, 440f, a, (PI / (n * 1.2)).toFloat()))
        }

        return MandalaDesign(
            id = "mandala_serenity",
            title = "Mandala of Serenity",
            category = MandalaCategory.FLORAL,
            difficulty = Difficulty.MEDIUM,
            regions = regions
        )
    }

    // 23. Galaxy Spiral (Hard / Geometric - 137 regions)
    private fun createGalaxySpiral(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("galaxy_core", radius = 35f))

        val n = 17
        // 8 tiers of 17 = 136 + 1 = 137
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(MandalaShapes.annularSector("gal_s1_$i", 40f, 95f, deg, 360f / n))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("gal_p1_$i", 90f, 150f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(MandalaShapes.annularSector("gal_s2_$i", 145f, 205f, deg, 360f / n))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.diamond("gal_d1_$i", 200f, 265f, 232f, a, (PI / (n * 1.3)).toFloat()))
        }
        for (i in 0 until n) {
            val deg = i * (360f / n)
            regions.add(MandalaShapes.annularSector("gal_s3_$i", 260f, 320f, deg, 360f / n))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("gal_p2_$i", 315f, 380f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.diamond("gal_d2_$i", 375f, 440f, 407f, a, (PI / (n * 1.3)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.bead("gal_b1_$i", 425f, 14f, a))
        }

        return MandalaDesign(
            id = "galaxy_spiral",
            title = "Galaxy Spiral",
            category = MandalaCategory.GEOMETRIC,
            difficulty = Difficulty.HARD,
            regions = regions
        )
    }

    // 24. Royal Rosette (Intricate / Floral - 201 regions)
    private fun createRoyalRosette(): MandalaDesign {
        val regions = mutableListOf<MandalaRegion>()
        regions.add(MandalaShapes.circle("royal_core", radius = 25f))

        val n = 25
        // 8 tiers of 25 = 200 + 1 = 201 regions
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("roy_p1_$i", 28f, 75f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("roy_p2_$i", 70f, 125f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.diamond("roy_d1_$i", 120f, 180f, 150f, a, (PI / (n * 1.3)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("roy_p3_$i", 175f, 240f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.diamond("roy_d2_$i", 235f, 300f, 267f, a, (PI / (n * 1.3)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("roy_p4_$i", 295f, 365f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = (i * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.petal("roy_p5_$i", 360f, 435f, a, (PI / (n * 1.2)).toFloat()))
        }
        for (i in 0 until n) {
            val a = ((i + 0.5) * 2 * PI / n).toFloat()
            regions.add(MandalaShapes.bead("roy_bead_$i", 422f, 11f, a))
        }

        return MandalaDesign(
            id = "royal_rosette",
            title = "Royal Rosette",
            category = MandalaCategory.FLORAL,
            difficulty = Difficulty.HARD,
            regions = regions
        )
    }
}
