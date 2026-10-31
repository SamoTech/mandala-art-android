package com.example

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import android.view.View
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ArabicLocaleRtlTest {

    @Test
    fun manifestEnablesRtlAndArabicConfigurationLoadsArabicResources() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()

        assertTrue(
            "Android manifest must opt in to RTL support",
            baseContext.applicationInfo.flags and ApplicationInfo.FLAG_SUPPORTS_RTL != 0
        )

        val arabicConfiguration = Configuration(baseContext.resources.configuration).apply {
            setLocale(Locale.forLanguageTag("ar"))
        }
        val arabicContext = baseContext.createConfigurationContext(arabicConfiguration)

        assertEquals(
            "Arabic locale must resolve to RTL layout direction",
            View.LAYOUT_DIRECTION_RTL,
            arabicContext.resources.configuration.layoutDirection
        )
        assertEquals("ابدأ التلوين", arabicContext.getString(R.string.home_play_now))
        assertEquals("الإعدادات", arabicContext.getString(R.string.settings_title))
        assertEquals("رجوع", arabicContext.getString(R.string.navigation_back))
        assertEquals("Mandala Art", arabicContext.getString(R.string.app_name))
    }

    @Test
    fun englishConfigurationRemainsLtrAndKeepsEnglishResources() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val englishConfiguration = Configuration(baseContext.resources.configuration).apply {
            setLocale(Locale.ENGLISH)
        }
        val englishContext = baseContext.createConfigurationContext(englishConfiguration)

        assertEquals(
            "English locale must resolve to LTR layout direction",
            View.LAYOUT_DIRECTION_LTR,
            englishContext.resources.configuration.layoutDirection
        )
        assertEquals("Color Now", englishContext.getString(R.string.home_play_now))
        assertEquals("Settings", englishContext.getString(R.string.settings_title))
        assertEquals("Back", englishContext.getString(R.string.navigation_back))
        assertEquals("Mandala Art", englishContext.getString(R.string.app_name))
    }
}
