package com.example.util

import android.content.Context
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import com.example.generator.MandalaLibrary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImageSharerTest {

    @Test
    fun renderToCacheCreatesReadable1200PixelPng() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val design = MandalaLibrary.designs.first()

        val file = ImageSharer.renderToCache(
            context = context,
            design = design,
            coloredRegions = emptyMap()
        )

        try {
            assertTrue("Export file must exist", file.isFile)
            assertTrue("Export file must not be empty", file.length() > 8)
            file.inputStream().use { input ->
                assertEquals(0x89, input.read())
                assertEquals('P'.code, input.read())
                assertEquals('N'.code, input.read())
                assertEquals('G'.code, input.read())
            }

            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            assertTrue("Exported PNG must decode", bitmap != null)
            val decoded = requireNotNull(bitmap)
            try {
                assertEquals(1200, decoded.width)
                assertEquals(1200, decoded.height)
            } finally {
                decoded.recycle()
            }
        } finally {
            file.delete()
        }
    }
}
