package com.example.model

import androidx.compose.ui.graphics.Color

data class Palette(
    val name: String,
    val colors: List<Long>
)

object MandalaPalettes {
    val chakra = Palette(
        name = "Chakra",
        colors = listOf(
            0xFFE53935L, // Red (Root)
            0xFFFF7043L, // Coral Orange
            0xFFFF9800L, // Vibrant Orange (Sacral)
            0xFFFFCA28L, // Sun Yellow (Solar Plexus)
            0xFF66BB6AL, // Heart Green
            0xFF26A69AL, // Emerald Mint
            0xFF29B6F6L, // Sky Cyan (Throat)
            0xFF1E88E5L, // Deep Azure
            0xFF3F51B5L, // Indigo (Third Eye)
            0xFF7E57C2L, // Lavender Violet
            0xFF8E24AAL, // Crown Purple
            0xFFEC407AL  // Divine Magenta
        )
    )

    val pastel = Palette(
        name = "Pastel",
        colors = listOf(
            0xFFFFCDD2L, // Soft Pink
            0xFFF8BBD0L, // Rose Powder
            0xFFE1BEE7L, // Lilac
            0xFFD1C4E9L, // Periwinkle
            0xFFC5CAE9L, // Soft Indigo
            0xFFBBDEFBL, // Baby Blue
            0xFFB2EBF2L, // Powder Mint
            0xFFB2DFDBL, // Seafoam
            0xFFC8E6C9L, // Sage Green
            0xFFDCEDC8L, // Soft Lime
            0xFFFFF9C4L, // Buttercup
            0xFFFFE0B2L  // Peach
        )
    )

    val jewel = Palette(
        name = "Jewel",
        colors = listOf(
            0xFF880E4FL, // Deep Ruby
            0xFF4A148CL, // Royal Amethyst
            0xFF1A237EL, // Midnight Sapphire
            0xFF004D40L, // Deep Emerald
            0xFF006064L, // Dark Tourmaline
            0xFF01579BL, // Ocean Topaz
            0xFF827717L, // Raw Peridot
            0xFFE65100L, // Burnt Amber
            0xFFBF360CL, // Garnet
            0xFF3E2723L, // Smoky Quartz
            0xFFC2185BL, // Magenta Spinel
            0xFFFFB300L  // Imperial Gold
        )
    )

    val sunset = Palette(
        name = "Sunset",
        colors = listOf(
            0xFFFFF59DL, // Morning Dawn
            0xFFFFEE58L, // Golden Ray
            0xFFFFB74DL, // Warm Peach
            0xFFFFA726L, // Apricot
            0xFFFF7043L, // Tangerine
            0xFFFF5722L, // Vivid Crimson
            0xFFE91E63L, // Hot Pink
            0xFFAD1457L, // Twilight Berry
            0xFF6A1B9AL, // Dusk Violet
            0xFF4527A0L, // Night Sky
            0xFF283593L, // Starry Blue
            0xFF1A237EL  // Midnight
        )
    )

    val forest = Palette(
        name = "Nature",
        colors = listOf(
            0xFFE8F5E9L, // Dewdrop
            0xFFA5D6A7L, // Tender Sprout
            0xFF66BB6AL, // Meadow Grass
            0xFF43A047L, // Forest Canopy
            0xFF2E7D32L, // Deep Pine
            0xFF1B5E20L, // Moss Green
            0xFF80CBC4L, // Eucalyptus
            0xFF26A69AL, // Mountain Stream
            0xFF8D6E63L, // Cinnamon Bark
            0xFF5D4037L, // Cedar Wood
            0xFF4E342EL, // Wet Earth
            0xFFD7CCC8L  // River Pebble
        )
    )

    val allPalettes = listOf(chakra, pastel, jewel, sunset, forest)

    /**
     * Default selected color when opening editor: vibrant rose/pink
     */
    val defaultColor: Long = 0xFFFF4081L

    /**
     * White / Clear color for regions
     */
    const val CLEAR_COLOR: Long = 0xFFFFFFFFL
}
