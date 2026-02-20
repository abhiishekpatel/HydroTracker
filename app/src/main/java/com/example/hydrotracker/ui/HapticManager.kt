package com.example.hydrotracker.ui

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

enum class HapticType {
    TICK,       // Ultra-light — UI acknowledgement (nav tap, toggle)
    CLICK,      // Light crisp — standard button press
    MEDIUM,     // Medium — adding water, confirming action
    HEAVY,      // Strong — reset, destructive action warning
    SUCCESS     // Rich pattern — goal reached celebration
}

/**
 * Resolve the system [Vibrator] in a forward-compatible way.
 */
private fun getVibrator(context: Context): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
            ?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
}

/**
 * Perform haptic feedback.
 *
 * - API 34+ (Android 14): uses [VibrationEffect.createPredefined] for TICK/CLICK so the
 *   system plays the device's native haptic primitives (much crisper on Pixel / Samsung).
 * - API 26–33: uses [VibrationEffect.createOneShot] / [VibrationEffect.createWaveform]
 *   with validated amplitude values (1–255).
 * - API < 26: legacy [Vibrator.vibrate] fallback (duration only, no amplitude control).
 *
 * @param context  Any [Context]; ApplicationContext is fine.
 * @param type     The semantic haptic type to play.
 * @param enabled  When false the call is a no-op (respects the in-app toggle).
 */
fun performHaptic(
    context: Context,
    type: HapticType = HapticType.CLICK,
    enabled: Boolean = true
) {
    if (!enabled) return

    val vibrator = getVibrator(context) ?: return
    if (!vibrator.hasVibrator()) return

    when {
        // ── Android 14+ — use predefined effects for TICK / CLICK ─────────
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
            val effect: VibrationEffect = when (type) {
                HapticType.TICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                HapticType.CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                HapticType.MEDIUM -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                HapticType.HEAVY -> VibrationEffect.createWaveform(
                    longArrayOf(0L, 40L, 30L, 60L),
                    intArrayOf(0, 180, 0, 220),
                    -1
                )

                HapticType.SUCCESS -> VibrationEffect.createWaveform(
                    longArrayOf(0L, 30L, 40L, 50L, 40L, 80L),
                    intArrayOf(0, 120, 0, 180, 0, 255),
                    -1
                )
            }
            val attrs = VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_TOUCH)
                .build()
            vibrator.vibrate(effect, attrs)
        }

        // ── Android 8–13 — VibrationEffect with amplitude ─────────────────
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            // Amplitude must be 1–255 or VibrationEffect.DEFAULT_AMPLITUDE (-1).
            val effect: VibrationEffect = when (type) {
                HapticType.TICK -> VibrationEffect.createOneShot(20L, 80)
                HapticType.CLICK -> VibrationEffect.createOneShot(35L, 120)
                HapticType.MEDIUM -> VibrationEffect.createOneShot(55L, 170)
                HapticType.HEAVY -> VibrationEffect.createWaveform(
                    longArrayOf(0L, 50L, 30L, 70L),
                    intArrayOf(0, 180, 0, 230),
                    -1
                )

                HapticType.SUCCESS -> VibrationEffect.createWaveform(
                    longArrayOf(0L, 30L, 40L, 55L, 40L, 90L),
                    intArrayOf(0, 110, 0, 175, 0, 255),
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
        }

        // ── Android < 8 — legacy duration-only vibration ──────────────────
        else -> {
            @Suppress("DEPRECATION")
            val duration: Long = when (type) {
                HapticType.TICK -> 18L
                HapticType.CLICK -> 30L
                HapticType.MEDIUM -> 50L
                HapticType.HEAVY -> 90L
                HapticType.SUCCESS -> 200L
            }
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
}

/**
 * Convenience extension to perform [View]-level haptic feedback constants
 * (e.g. for things already attached to the window like Compose root views).
 *
 * This is separate from [performHaptic] and uses Android's built-in
 * [View.performHapticFeedback] which requires no VIBRATE permission.
 */
fun View.performViewHaptic(type: HapticType) {
    val constant = when (type) {
        HapticType.TICK -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            HapticFeedbackConstants.CLOCK_TICK else HapticFeedbackConstants.VIRTUAL_KEY

        HapticType.CLICK -> HapticFeedbackConstants.VIRTUAL_KEY
        HapticType.MEDIUM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
            HapticFeedbackConstants.TEXT_HANDLE_MOVE else HapticFeedbackConstants.VIRTUAL_KEY

        HapticType.HEAVY -> HapticFeedbackConstants.LONG_PRESS
        HapticType.SUCCESS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY
    }
    performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
}
