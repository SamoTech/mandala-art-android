package com.example.ui.editor

import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.MandalaDesign
import kotlin.math.min

@Composable
fun MandalaCanvas(
    design: MandalaDesign,
    coloredRegions: Map<String, Long>,
    outlineColor: Long,
    isZoomPanMode: Boolean,
    onRegionTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    // Double tap resets zoom and pan
    fun resetZoom() {
        zoom = 1f
        pan = Offset.Zero
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(Color.White)
            .testTag("mandala_canvas_container"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("interactive_mandala_canvas")
                // Transform gestures for pinch-to-zoom and panning
                .pointerInput(Unit) {
                    detectTransformGestures { _, panDelta, zoomDelta, _ ->
                        val newZoom = (zoom * zoomDelta).coerceIn(1f, 5.0f)
                        zoom = newZoom
                        if (newZoom == 1f) {
                            pan = Offset.Zero
                        } else {
                            // Max pan bounds
                            val maxPanX = (size.width * (newZoom - 1f)) * 0.6f
                            val maxPanY = (size.height * (newZoom - 1f)) * 0.6f
                            pan = Offset(
                                (pan.x + panDelta.x).coerceIn(-maxPanX, maxPanX),
                                (pan.y + panDelta.y).coerceIn(-maxPanY, maxPanY)
                            )
                        }
                    }
                }
                // Tap gesture for coloring or resetting zoom on double-tap
                .pointerInput(isZoomPanMode, zoom, pan) {
                    detectTapGestures(
                        onDoubleTap = {
                            resetZoom()
                        },
                        onTap = { tapOffset ->
                            if (!isZoomPanMode) {
                                val canvasDim = min(size.width, size.height)
                                val originX = (size.width - canvasDim) / 2f
                                val originY = (size.height - canvasDim) / 2f

                                // Convert tapOffset to Mandala space (0..1000)
                                val mandalaX = (tapOffset.x - originX - pan.x) / (canvasDim * zoom) * 1000f
                                val mandalaY = (tapOffset.y - originY - pan.y) / (canvasDim * zoom) * 1000f

                                if (mandalaX in 0f..1000f && mandalaY in 0f..1000f) {
                                    val hitRegion = findHitRegion(design, mandalaX, mandalaY)
                                    if (hitRegion != null) {
                                        onRegionTapped(hitRegion)
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            val canvasDim = min(size.width, size.height)
            val baseScale = canvasDim / 1000f
            val originX = (size.width - canvasDim) / 2f
            val originY = (size.height - canvasDim) / 2f

            drawIntoCanvas { composeCanvas ->
                val nativeCanvas = composeCanvas.nativeCanvas
                nativeCanvas.save()
                nativeCanvas.translate(originX + pan.x, originY + pan.y)
                nativeCanvas.scale(baseScale * zoom, baseScale * zoom)

                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                }

                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2.4f / zoom.coerceAtLeast(1f)
                    color = outlineColor.toInt()
                }

                // 1. Draw base white backing
                fillPaint.color = AndroidColor.WHITE
                nativeCanvas.drawRect(0f, 0f, 1000f, 1000f, fillPaint)

                // 2. Draw all filled regions
                for (region in design.regions) {
                    val color = coloredRegions[region.id] ?: 0xFFFFFFFFL
                    fillPaint.color = color.toInt()
                    nativeCanvas.drawPath(region.androidPath, fillPaint)
                }

                // 3. Draw crisp outline boundaries over all regions
                for (region in design.regions) {
                    nativeCanvas.drawPath(region.androidPath, strokePaint)
                }

                nativeCanvas.restore()
            }
        }
    }
}

/**
 * Searches for which region contains the tapped point, with forgiving neighborhood fallback.
 */
private fun findHitRegion(design: MandalaDesign, x: Float, y: Float): String? {
    // 1. Direct hit test in reverse order (innermost/top layers first)
    for (i in design.regions.indices.reversed()) {
        val region = design.regions[i]
        if (region.containsPoint(x, y)) {
            return region.id
        }
    }

    // 2. Small neighborhood tolerance (in case user tapped on a border line)
    val deltas = floatArrayOf(-3f, 3f, -6f, 6f)
    for (dx in deltas) {
        for (dy in deltas) {
            for (i in design.regions.indices.reversed()) {
                val region = design.regions[i]
                if (region.containsPoint(x + dx, y + dy)) {
                    return region.id
                }
            }
        }
    }

    return null
}
