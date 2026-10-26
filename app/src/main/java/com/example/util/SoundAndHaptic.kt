package com.example.util

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object SoundAndHaptic {

    fun performColorTap(context: Context, soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (soundEnabled) {
            try {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                am?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.4f)
            } catch (_: Exception) {}
        }

        if (hapticEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator?.vibrate(
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v?.vibrate(18)
                    }
                }
            } catch (_: Exception) {}
        }
    }
}
