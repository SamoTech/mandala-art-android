package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Matrix
import android.graphics.Paint
import androidx.core.content.FileProvider
import com.example.model.MandalaDesign
import java.io.File
import java.io.FileOutputStream

object ImageSharer {

    fun exportAndShare(
        context: Context,
        design: MandalaDesign,
        coloredRegions: Map<String, Long>,
        outlineColor: Long = 0xFF212121L
    ) {
        try {
            val size = 1200
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Draw clean background
            canvas.drawColor(AndroidColor.WHITE)

            val scale = size / design.size
            val matrix = Matrix().apply { postScale(scale, scale) }

            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }

            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3.0f
                color = outlineColor.toInt()
            }

            // Draw filled regions
            for (region in design.regions) {
                val colorLong = coloredRegions[region.id] ?: 0xFFFFFFFFL
                fillPaint.color = colorLong.toInt()
                val transformedPath = android.graphics.Path(region.androidPath)
                transformedPath.transform(matrix)
                canvas.drawPath(transformedPath, fillPaint)
                canvas.drawPath(transformedPath, strokePaint)
            }

            // Save to cache
            val cacheFolder = File(context.cacheDir, "artwork").apply { mkdirs() }
            val file = File(cacheFolder, "mandala_${design.id}_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // Share via FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "My Mandala Art: ${design.title}")
                putExtra(Intent.EXTRA_TEXT, "Look at my colored mandala: ${design.title}! Created with Mandala Art.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Your Mandala Artwork"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
