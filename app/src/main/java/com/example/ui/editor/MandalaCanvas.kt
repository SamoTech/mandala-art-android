package com.example.ui.editor

import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.os.Trace
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
import com.example.BuildConfig
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
                // Double-tap zoom reset lives in its own gesture detector so
                // single taps do not wait for the double-tap timeout (~300ms).
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            resetZoom()
                        }
                    )
                }
                // Tap gesture for coloring; fires immediately on touch-up
                .pointerInput(isZoomPanMode, zoom, pan) {
                    detectTapGestures(
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
                val tracingEnabled = BuildConfig.DEBUG
                if (tracingEnabled) Trace.beginSection("MandalaCanvas.draw")
                nativeCanvas.save()
                try {
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
                    if (tracingEnabled) Trace.beginSection("MandalaCanvas.fillRegions")
                    try {
                        for (region in design.regions) {
                            val color = coloredRegions[region.id] ?: 0xFFFFFFFFL
                            fillPaint.color = color.toInt()
                            nativeCanvas.drawPath(region.androidPath, fillPaint)
                        }
                    } finally {
                        if (tracingEnabled) Trace.endSection()
                    }

                    // 3. Draw crisp outline boundaries over all regions
                    if (tracingEnabled) Trace.beginSection("MandalaCanvas.strokeRegions")
                    try {
                        for (region in design.regions) {
                            nativeCanvas.drawPath(region.androidPath, strokePaint)
                        }
                    } finally {
                        if (tracingEnabled) Trace.endSection()
                    }
                } finally {
                    nativeCanvas.restore()
                    if (tracingEnabled) Trace.endSection()
                }
            }
        }
    }
}

/**
 * Searches for which region contains the tapped point, with forgiving neighborhood fallback.
 */
private fun findHitRegion(design: MandalaDesign, x: Float, y: Float): String? {
    val tracingEnabled = BuildConfig.DEBUG
    if (tracingEnabled) Trace.beginSection("MandalaCanvas.hitTest")
    try {
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
    } finally {
        if (tracingEnabled) Trace.endSection()
    }
}
