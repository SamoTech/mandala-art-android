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

    fun isDesignUnlocked(designId: String): Boolean {
        // Only special bonus designs require unlock; standard 22 designs are free by default
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

        // 2 bonus intricate designs that can be unlocked via rewarded ad
        val BONUS_DESIGNS = setOf("galaxy_spiral", "royal_rosette")
    }
}
