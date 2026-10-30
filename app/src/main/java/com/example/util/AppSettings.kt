package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mandala_app_prefs", Context.MODE_PRIVATE)

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND, true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC, true))
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _outlineColor = MutableStateFlow(prefs.getLong(KEY_OUTLINE, 0xFF212121L))
    val outlineColor: StateFlow<Long> = _outlineColor.asStateFlow()

    private val _unlockedColors = MutableStateFlow(prefs.getStringSet(KEY_UNLOCKED_COLORS, emptySet()) ?: emptySet())
    val unlockedColors: StateFlow<Set<String>> = _unlockedColors.asStateFlow()

    private val _unlockedDesigns = MutableStateFlow(
        prefs.getStringSet(KEY_UNLOCKED_DESIGNS, emptySet()) ?: emptySet()
    )
    val unlockedDesigns: StateFlow<Set<String>> = _unlockedDesigns.asStateFlow()

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _hapticEnabled.value = enabled
    }

    fun setOutlineColor(color: Long) {
        prefs.edit().putLong(KEY_OUTLINE, color).apply()
        _outlineColor.value = color
    }

    fun isColorUnlocked(color: Long): Boolean = color.toString(16) !in LOCKED_COLORS || _unlockedColors.value.contains(color.toString(16))

    fun unlockColor(color: Long) {
        val updated = _unlockedColors.value.toMutableSet().apply { add(color.toString(16)) }
        prefs.edit().putStringSet(KEY_UNLOCKED_COLORS, updated).apply()
        _unlockedColors.value = updated
    }

    fun isDesignUnlocked(designId: String): Boolean {
        // Only special bonus designs require unlock; all non-bonus designs are free by default
        if (designId !in BONUS_DESIGNS) return true
        return _unlockedDesigns.value.contains(designId)
    }

    fun unlockDesign(designId: String) {
        val updated = _unlockedDesigns.value.toMutableSet().apply { add(designId) }
        prefs.edit().putStringSet(KEY_UNLOCKED_DESIGNS, updated).apply()
        _unlockedDesigns.value = updated
    }

    companion object {
        private const val KEY_SOUND = "key_sound"
        private const val KEY_HAPTIC = "key_haptic"
        private const val KEY_OUTLINE = "key_outline"
        private const val KEY_UNLOCKED_DESIGNS = "key_unlocked_designs"
        private const val KEY_UNLOCKED_COLORS = "key_unlocked_colors"
        val LOCKED_COLORS = setOf("ff880e4f", "ff4a148c", "ff1a237e", "ff004d40", "ff006064", "ff01579b", "ff827717", "ffe65100", "ffbf360c", "ff3e2723", "ffc2185b", "ffffb300")

        // Bonus designs that can be unlocked via rewarded ad
        val BONUS_DESIGNS = setOf(
            "galaxy_spiral", "royal_rosette",
            "premium_mandala_1", "premium_mandala_2", "premium_mandala_3", "premium_mandala_4", "premium_mandala_5",
            "premium_mandala_6", "premium_mandala_7", "premium_mandala_8", "premium_mandala_9", "premium_mandala_10",
            "premium_mandala_11", "premium_mandala_12", "premium_mandala_13", "premium_mandala_14", "premium_mandala_15",
            "premium_mandala_16", "premium_mandala_17", "premium_mandala_18", "premium_mandala_19", "premium_mandala_20",
            "premium_mandala_21", "premium_mandala_22", "premium_mandala_23", "premium_mandala_24", "premium_mandala_25",
            "premium_mandala_26", "premium_mandala_27", "premium_mandala_28", "premium_mandala_29", "premium_mandala_30",
            "premium_mandala_31", "premium_mandala_32", "premium_mandala_33", "premium_mandala_34", "premium_mandala_35",
            "premium_mandala_36", "premium_mandala_37", "premium_mandala_38", "premium_mandala_39", "premium_mandala_40",
            "premium_mandala_41", "premium_mandala_42", "premium_mandala_43", "premium_mandala_44", "premium_mandala_45",
            "premium_mandala_46", "premium_mandala_47", "premium_mandala_48", "premium_mandala_49", "premium_mandala_50"
        )
    }
}
