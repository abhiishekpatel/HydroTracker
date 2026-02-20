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

    // Raw waveform patterns — max amplitude, long enough to feel physical
    val effect = when (type) {
        // Single solid thud: 80ms at full power
        HapticType.LIGHT -> VibrationEffect.createOneShot(80, 255)

        // Longer thud: 130ms at full power
        HapticType.MEDIUM -> VibrationEffect.createOneShot(130, 255)

        // Double-punch: 150ms BANG + 50ms gap + 100ms follow-through, both at 255
        HapticType.STRONG -> VibrationEffect.createWaveform(
            longArrayOf(0, 150, 50, 100),
            intArrayOf(0, 255, 0, 255),
            -1
        )

        // Triple rising pulse: escalating celebration
        HapticType.SUCCESS -> VibrationEffect.createWaveform(
            longArrayOf(0, 80, 40, 130, 40, 180),
            intArrayOf(0, 180, 0, 230, 0, 255),
            -1
        )
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // API 31+: VibratorManager + VibrationAttributes (USAGE_TOUCH bypasses DND suppression)
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator
        if (!vibrator.hasVibrator()) return
        val attrs = VibrationAttributes.Builder()
            .setUsage(VibrationAttributes.USAGE_TOUCH)
            .build()
        vibrator.vibrate(effect, attrs)
    } else {
        // API 26–30: direct Vibrator service
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(effect)
    }
}
