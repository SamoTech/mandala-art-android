package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Difficulty
import com.example.model.MandalaCategory
import com.example.model.LevelSystem
import com.example.model.MandalaDesign
import com.example.model.SavedArtwork
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.glassStroke
import kotlin.math.min

@Composable
fun MandalaCard(
    design: MandalaDesign,
    savedArtwork: SavedArtwork?,
    outlineColor: Long,
    onSelect: () -> Unit,
    onToggleFavorite: (Boolean) -> Unit,
    isLocked: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isFav = savedArtwork?.isFavorite == true
    val percent = savedArtwork?.percentCompleted ?: 0f
    val coloredRegions = savedArtwork?.coloredRegions ?: emptyMap()
    val isCompleted = savedArtwork?.isCompleted == true

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onSelect() }
            .testTag("mandala_card_${design.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, glassStroke()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.White)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasDim = min(size.width, size.height)
                    val baseScale = canvasDim / 1000f
                    val originX = (size.width - canvasDim) / 2f
                    val originY = (size.height - canvasDim) / 2f

                    drawIntoCanvas { composeCanvas ->
                        val nativeCanvas = composeCanvas.nativeCanvas
                        nativeCanvas.save()
                        nativeCanvas.translate(originX, originY)
                        nativeCanvas.scale(baseScale, baseScale)

                        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.FILL
                        }

                        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.STROKE
                            strokeWidth = 3f
                            color = outlineColor.toInt()
                        }

                        for (region in design.regions) {
                            val color = coloredRegions[region.id] ?: 0xFFFFFFFFL
                            fillPaint.color = color.toInt()
                            nativeCanvas.drawPath(region.androidPath, fillPaint)
                        }

                        for (region in design.regions) {
                            nativeCanvas.drawPath(region.androidPath, strokePaint)
                        }

                        nativeCanvas.restore()
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, glassStroke()),
                    shadowElevation = 2.dp
                ) {
                    IconButton(
                        onClick = { onToggleFavorite(isFav) },
                        modifier = Modifier.testTag("fav_btn_${design.id}")
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.mandala_favorite),
                            tint = if (isFav) PinkAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (isLocked) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomStart),
                        shape = RoundedCornerShape(50.dp),
                        color = GoldAccent
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(R.string.mandala_locked),
                                tint = Color(0xFF3F2A00),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (LevelSystem.levelOf(design.id) != null) {
                                    stringResource(R.string.mandala_level)
                                } else {
                                    stringResource(R.string.mandala_bonus)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF3F2A00)
                            )
                        }
                    }
                } else if (percent > 0f) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomStart),
                        shape = RoundedCornerShape(50.dp),
                        color = if (isCompleted) Color(0xFF16C779) else PinkAccent
                    ) {
                        Text(
                            text = if (isCompleted) {
                                stringResource(R.string.mandala_completed)
                            } else {
                                stringResource(R.string.mandala_progress_percent, (percent * 100).toInt())
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = design.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(difficultyLabelResource(design.difficulty)) + " • " + stringResource(categoryLabelResource(design.category)),
                        fontSize = 12.sp,
                        color = Color(design.difficulty.color),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = pluralStringResource(
                            R.plurals.mandala_region_count,
                            design.regionCount,
                            design.regionCount
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (percent > 0f) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { percent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PinkAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}


private fun difficultyLabelResource(difficulty: Difficulty): Int = when (difficulty) {
    Difficulty.EASY -> R.string.category_easy
    Difficulty.MEDIUM -> R.string.category_medium
    Difficulty.HARD -> R.string.category_hard
}

private fun categoryLabelResource(category: MandalaCategory): Int = when (category) {
    MandalaCategory.ALL -> R.string.category_all
    MandalaCategory.EASY -> R.string.category_easy
    MandalaCategory.MEDIUM -> R.string.category_medium
    MandalaCategory.HARD -> R.string.category_hard
    MandalaCategory.FLORAL -> R.string.category_floral
    MandalaCategory.GEOMETRIC -> R.string.category_geometric
    MandalaCategory.INTRICATE -> R.string.category_intricate
}
