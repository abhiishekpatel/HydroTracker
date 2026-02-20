package com.example.hydrotracker.ui

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class HapticType {
    LIGHT,
    MEDIUM,
    STRONG,
    SUCCESS
}

fun performHaptic(context: Context, type: HapticType = HapticType.STRONG, enabled: Boolean = true) {
    if (!enabled) return

    val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            ?: return
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    }

    if (!vibrator.hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // API 26+: use VibrationEffect
        val effect = when (type) {
            HapticType.LIGHT -> VibrationEffect.createOneShot(80, 200)
            HapticType.MEDIUM -> VibrationEffect.createOneShot(130, 230)
            HapticType.STRONG -> VibrationEffect.createWaveform(
                longArrayOf(0, 150, 50, 100),
                intArrayOf(0, 255, 0, 255),
                -1
            )
            HapticType.SUCCESS -> VibrationEffect.createWaveform(
                longArrayOf(0, 80, 40, 130, 40, 180),
                intArrayOf(0, 180, 0, 230, 0, 255),
                -1
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val attrs = VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_TOUCH)
                .build()
            vibrator.vibrate(effect, attrs)
        } else {
            vibrator.vibrate(effect)
        }
    } else {
        // API 21-25: legacy vibrate(long) fallback
        @Suppress("DEPRECATION")
        val duration = when (type) {
            HapticType.LIGHT -> 80L
            HapticType.MEDIUM -> 130L
            HapticType.STRONG -> 200L
            HapticType.SUCCESS -> 300L
        }
        @Suppress("DEPRECATION")
        vibrator.vibrate(duration)
    }
}
