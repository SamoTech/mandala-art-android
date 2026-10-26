package com.example.model

import android.graphics.Path as AndroidPath
import android.graphics.RectF
import android.graphics.Region
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath

enum class MandalaCategory(val title: String) {
    ALL("All"),
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    FLORAL("Floral"),
    GEOMETRIC("Geometric"),
    INTRICATE("Intricate")
}

enum class Difficulty(val label: String, val color: Long) {
    EASY("Easy", 0xFF4CAF50),
    MEDIUM("Medium", 0xFFFF9800),
    HARD("Hard", 0xFFE91E63)
}

/**
 * Represents a single fillable enclosed vector region of a mandala.
 */
class MandalaRegion(
    val id: String,
    val androidPath: AndroidPath,
    val bounds: RectF
) {
    val composePath: Path by lazy {
        androidPath.asComposePath()
    }

    private val region: Region by lazy {
        val r = Region()
        val clip = Region(
            bounds.left.toInt() - 2,
            bounds.top.toInt() - 2,
            bounds.right.toInt() + 2,
            bounds.bottom.toInt() + 2
        )
        r.setPath(androidPath, clip)
        r
    }

    fun containsPoint(x: Float, y: Float): Boolean {
        if (!bounds.contains(x, y)) return false
        return region.contains(x.toInt(), y.toInt())
    }
}

/**
 * Represents a complete mandala design with all its fillable regions and outline paths.
 */
data class MandalaDesign(
    val id: String,
    val title: String,
    val category: MandalaCategory,
    val difficulty: Difficulty,
    val regions: List<MandalaRegion>,
    val outlinePaths: List<AndroidPath> = emptyList(),
    val size: Float = 1000f
) {
    val regionCount: Int get() = regions.size
}

/**
 * Saved state of coloring for a mandala
 */
data class SavedArtwork(
    val mandalaId: String,
    val coloredRegions: Map<String, Long>,
    val percentCompleted: Float,
    val isFavorite: Boolean,
    val isCompleted: Boolean,
    val lastEditedTimestamp: Long
)

/**
 * Represents an undo/redo step in coloring
 */
data class ColorStep(
    val regionId: String,
    val previousColor: Long?,
    val newColor: Long?
)
