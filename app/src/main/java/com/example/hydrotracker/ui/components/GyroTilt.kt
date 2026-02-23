package com.example.hydrotracker.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs

/**
 * Tilt values from the device gravity / accelerometer sensor.
 *
 * Two pairs are exposed:
 *  • [x] / [y]       — heavily smoothed (alpha = 0.08).  Feed into Compose
 *                        animateFloatAsState springs for UI elements so they
 *                        glide without jitter.
 *  • [rawX] / [rawY] — lightly smoothed (alpha = 0.30).  Feed directly into
 *                        physics loops for splash impulse detection; much more
 *                        responsive to quick wrist flicks.
 *
 * Sign convention (portrait mode):
 *   positive x  →  phone tilted LEFT   (water accumulates on LEFT side)
 *   negative x  →  phone tilted RIGHT  (water accumulates on RIGHT side)
 *
 * The sign matches the direction water flows under gravity, so callers can
 * use the value directly without additional negation.
 */
data class GyroTilt(
    val x: Float = 0f,
    val y: Float = 0f,
    val rawX: Float = 0f,
    val rawY: Float = 0f,
)

@Composable
fun rememberGyroTilt(): State<GyroTilt> {
    val context = LocalContext.current
    val state = remember { mutableStateOf(GyroTilt()) }

    DisposableEffect(Unit) {
        val sensorManager =
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Prefer the OS-fused GRAVITY sensor (already band-limited);
        // fall back to raw ACCELEROMETER when unavailable (older devices).
        val sensor: Sensor? =
            sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
                ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // All mutable state lives inside the listener closure — zero
        // allocation on the hot path, no Compose recomposition cost.
        var smoothX = 0f
        var smoothY = 0f
        var fastX = 0f
        var fastY = 0f

        val alphaSmooth = 0.08f   // for UI / Compose springs  (silky)
        val alphaFast = 0.30f   // for physics impulses       (responsive)
        val maxTilt = 35f     // degrees; beyond this water is at the rim
        val deadzone = 0.35f   // suppress sub-threshold noise when flat

        val listener = object : SensorEventListener {
            override fun onAccuracyChanged(s: Sensor?, accuracy: Int) = Unit

            override fun onSensorChanged(event: SensorEvent) {
                // Gravity sensor axes (device-space, portrait mode):
                //   values[0] = Gx  →  positive when phone tilted LEFT
                //   values[1] = Gy  →  positive when phone tilted BACK (top away)
                //
                // We divide by g (9.81) to get a –1..1 ratio, then scale to
                // degrees.  No sign flip needed: positive Gx → positive tiltX
                // → water flows LEFT, which matches our sign convention above.
                val gx = (event.values[0] / SensorManager.GRAVITY_EARTH) * maxTilt
                val gy = (event.values[1] / SensorManager.GRAVITY_EARTH) * maxTilt

                // Heavy low-pass for UI springs
                smoothX = alphaSmooth * gx + (1f - alphaSmooth) * smoothX
                smoothY = alphaSmooth * gy + (1f - alphaSmooth) * smoothY

                // Light low-pass for physics impulses
                fastX = alphaFast * gx + (1f - alphaFast) * fastX
                fastY = alphaFast * gy + (1f - alphaFast) * fastY

                // Deadzone: snap to zero when the phone is nearly flat so the
                // water surface doesn't drift around a resting device.
                val finalX = if (abs(smoothX) < deadzone) 0f else smoothX.coerceIn(-maxTilt, maxTilt)
                val finalY = if (abs(smoothY) < deadzone) 0f else smoothY.coerceIn(-maxTilt, maxTilt)
                val finalRawX = if (abs(fastX) < deadzone) 0f else fastX.coerceIn(-maxTilt, maxTilt)
                val finalRawY = if (abs(fastY) < deadzone) 0f else fastY.coerceIn(-maxTilt, maxTilt)

                state.value = GyroTilt(
                    x = finalX,
                    y = finalY,
                    rawX = finalRawX,
                    rawY = finalRawY,
                )
            }
        }

        if (sensor != null) {
            sensorManager.registerListener(
                listener,
                sensor,
                // SENSOR_DELAY_GAME ≈ 50 Hz — smooth without hammering the CPU.
                SensorManager.SENSOR_DELAY_GAME,
            )
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return state
}
