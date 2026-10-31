package com.example.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppSettingsLanguageTest {
    private lateinit var context: Context

    @Before
    fun clearPreferences() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("mandala_app_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun languageSelectionPersistsAcrossSettingsInstances() {
        val firstInstance = AppSettings(context)

        assertEquals("", firstInstance.language.value)

        firstInstance.setLanguage("ar")
        assertEquals("ar", AppSettings(context).language.value)

        firstInstance.setLanguage("en")
        assertEquals("en", AppSettings(context).language.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun unsupportedLanguageIsRejected() {
        AppSettings(context).setLanguage("fr")
    }
}
