package com.example.util

import android.content.ClipData
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
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

object ImageSharer {

    private const val CACHE_MAX_AGE_MS = 7L * 24L * 60L * 60L * 1000L

    /**
     * Renders the artwork off the main thread, then launches the Android
     * Sharesheet on the main thread. Failures are propagated to the caller.
     */
    suspend fun exportAndShare(
        context: Context,
        design: MandalaDesign,
        coloredRegions: Map<String, Long>,
        outlineColor: Long = 0xFF212121L
    ) {
        val file = withContext(Dispatchers.IO) {
            renderToCache(context.applicationContext, design, coloredRegions, outlineColor)
        }

        try {
            withContext(Dispatchers.Main.immediate) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "My Mandala Art: ${design.title}")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Look at my colored mandala: ${design.title}! Created with Mandala Art."
                    )
                    clipData = ClipData.newUri(context.contentResolver, "Mandala artwork", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(shareIntent, "Share Your Mandala Artwork")
                )
            }
        } catch (error: Exception) {
            withContext(NonCancellable + Dispatchers.IO) { file.delete() }
            throw error
        }
    }

    /**
     * Creates a PNG in the FileProvider-approved cache/artwork directory.
     * Call from an IO dispatcher; kept internal so the renderer can be tested
     * without launching an external Sharesheet activity.
     */
    internal fun renderToCache(
        context: Context,
        design: MandalaDesign,
        coloredRegions: Map<String, Long>,
        outlineColor: Long = 0xFF212121L
    ): File {
        require(design.size > 0f) { "Mandala design size must be positive" }

        val size = 1200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        var outputFile: File? = null
        try {
            val canvas = Canvas(bitmap)
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

            for (region in design.regions) {
                val colorLong = coloredRegions[region.id] ?: 0xFFFFFFFFL
                fillPaint.color = colorLong.toInt()
                val transformedPath = android.graphics.Path(region.androidPath)
                transformedPath.transform(matrix)
                canvas.drawPath(transformedPath, fillPaint)
                canvas.drawPath(transformedPath, strokePaint)
            }

            val cacheFolder = File(context.cacheDir, "artwork")
            if (!cacheFolder.exists() && !cacheFolder.mkdirs()) {
                throw IOException("Unable to create artwork cache directory")
            }
            val expirationTime = System.currentTimeMillis() - CACHE_MAX_AGE_MS
            cacheFolder.listFiles()
                ?.filter { it.isFile && it.lastModified() < expirationTime }
                ?.forEach { it.delete() }

            val file = File(cacheFolder, "mandala_${design.id}_${UUID.randomUUID()}.png")
            outputFile = file
            FileOutputStream(file).use { output ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    throw IOException("PNG compression failed")
                }
                output.flush()
            }
            return file
        } catch (error: Exception) {
            outputFile?.delete()
            throw error
        } finally {
            bitmap.recycle()
        }
    }
}
