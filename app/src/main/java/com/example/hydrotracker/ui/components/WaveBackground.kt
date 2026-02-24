package com.example.hydrotracker.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.tan

// ─────────────────────────────────────────────────────────────────────────────
//  Physics constants
// ─────────────────────────────────────────────────────────────────────────────

private const val BG_N = 80          // wave sample columns
private const val BG_K = 0.022f      // spring tension — slightly softer for fluid feel
private const val BG_DAMP = 0.018f   // velocity damping per tick
private const val BG_MAX_DT = 0.032f // timestep cap (~30 fps floor)

// ─────────────────────────────────────────────────────────────────────────────
//  Pure-spring wave state
// ─────────────────────────────────────────────────────────────────────────────

private class BgWave(val n: Int = BG_N) {
    val h = FloatArray(n)
    val v = FloatArray(n)

    fun step(dt: Float) {
        for (i in 0 until n) {
            val left = if (i > 0) h[i - 1] else h[0]
            val right = if (i < n - 1) h[i + 1] else h[n - 1]
            val accel = BG_K * (left + right - 2f * h[i]) - BG_DAMP * v[i]
            v[i] += accel
            h[i] += v[i]
        }
    }

    fun splash(pos: Float, force: Float) {
        val idx = (pos * (n - 1)).toInt().coerceIn(0, n - 1)
        v[idx] += force
        if (idx > 0) v[idx - 1] += force * 0.55f
        if (idx < n - 1) v[idx + 1] += force * 0.55f
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Composable
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-screen water-fill background driven by real spring-mass physics.
 *
 * Waves use smooth quadratic-bezier paths so the surface looks organic rather
 * than polygonal.  An ambient sine oscillation keeps the water alive even when
 * the device is perfectly still.
 */
@Composable
fun WaveBackground(
    progress: Float,
    modifier: Modifier = Modifier,
    gyroTilt: GyroTilt = GyroTilt(),
) {
    val animFill by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow,
        ),
        label = "bgFill",
    )

    val animTilt by animateFloatAsState(
        targetValue = gyroTilt.x,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "bgTilt",
    )

    val wave = remember { BgWave(BG_N) }
    var tick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        var lastMs = withFrameMillis { it }
        var prevRaw = gyroTilt.rawX

        while (isActive) {
            val nowMs = withFrameMillis { it }
            val dt = ((nowMs - lastMs) / 1000f).coerceAtMost(BG_MAX_DT)
            lastMs = nowMs

            val rawNow = gyroTilt.rawX
            val delta = rawNow - prevRaw
            if (abs(delta) > 0.25f) {
                val pos = if (delta < 0f) 0.82f else 0.18f
                val force = abs(delta) * 9f
                wave.splash(pos, force)
            }
            prevRaw = rawNow

            wave.step(dt)
            tick = nowMs
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // ── Background: warm deep-blue gradient, not pitch black ───────────
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0C2440),
                    Color(0xFF071828),
                ),
                startY = 0f,
                endY = h,
            ),
        )

        // ── Soft ambient top glow (moonlight on water) ─────────────────────
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1E6FA8).copy(alpha = 0.22f),
                    Color.Transparent,
                ),
                center = Offset(w * 0.5f, 0f),
                radius = w * 0.85f,
            ),
            radius = w * 0.85f,
            center = Offset(w * 0.5f, 0f),
        )

        val centreY = h * (1f - animFill)
        val edgeFade = (animFill * (1f - animFill) * 5.5f).coerceIn(0f, 1f)

        // ── Back wave (deeper tone, slower ambient phase) ──────────────────
        drawBgWave(
            wave = wave,
            w = w, h = h,
            centreY = centreY,
            tiltDeg = animTilt,
            edgeFade = edgeFade,
            heightScale = 0.62f,
            phaseShift = BG_N / 3,
            color = Color(0xFF1178B8).copy(alpha = 0.24f),
            ambientFreqHz = 0.18f,
            ambientAmpPx = 5.5f,
            ambientSpatialCycles = 2.8f,
            ambientPhaseOffset = 0f,
            tick = tick,
        )

        // ── Front wave (brighter, full physics) ────────────────────────────
        drawBgWave(
            wave = wave,
            w = w, h = h,
            centreY = centreY,
            tiltDeg = animTilt,
            edgeFade = edgeFade,
            heightScale = 1.0f,
            phaseShift = 0,
            color = Color(0xFF3EB8F0).copy(alpha = 0.20f),
            ambientFreqHz = 0.25f,
            ambientAmpPx = 4f,
            ambientSpatialCycles = 3.5f,
            ambientPhaseOffset = 1.2f,
            tick = tick,
        )

        // ── Shimmer layer (lightest, different phase) ──────────────────────
        if (animFill > 0.03f) {
            drawBgWave(
                wave = wave,
                w = w, h = h,
                centreY = centreY,
                tiltDeg = animTilt,
                edgeFade = edgeFade,
                heightScale = 0.38f,
                phaseShift = BG_N * 2 / 3,
                color = Color(0xFF90D9FB).copy(alpha = 0.10f),
                ambientFreqHz = 0.32f,
                ambientAmpPx = 2.5f,
                ambientSpatialCycles = 5f,
                ambientPhaseOffset = 2.5f,
                tick = tick,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Private drawing helper
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawBgWave(
    wave: BgWave,
    w: Float,
    h: Float,
    centreY: Float,
    tiltDeg: Float,
    edgeFade: Float,
    heightScale: Float,
    phaseShift: Int,
    color: Color,
    ambientFreqHz: Float,        // temporal frequency (Hz) — how fast the wave rolls
    ambientAmpPx: Float,         // peak amplitude of idle oscillation in pixels
    ambientSpatialCycles: Float, // spatial cycles across full width
    ambientPhaseOffset: Float,   // per-layer phase offset (radians) for variety
    tick: Long,                  // read here so Compose redraws every frame
) {
    @Suppress("UNUSED_EXPRESSION") tick

    val n = wave.n
    if (edgeFade < 0.005f) return

    // ── Tilt slope ─────────────────────────────────────────────────────────
    val maxTilt = 35f
    val clamped = tiltDeg.coerceIn(-maxTilt, maxTilt)
    val halfW = w / 2f
    val slope = tan((clamped * PI / 180.0).toFloat()) * halfW
    val maxSlope = h * 0.26f
    val s = slope.coerceIn(-maxSlope, maxSlope)

    fun baseY(t: Float) = centreY + s * (t - 0.5f)

    val step = w / (n - 1)

    // Ambient temporal phase — creates gentle rolling motion when device is still
    val timeSec = tick.toFloat() / 1000f
    val timePhase = timeSec * ambientFreqHz * 2f * PI.toFloat() + ambientPhaseOffset

    // ── Compute y values per column (physics + ambient) ────────────────────
    val yCoords = FloatArray(n) { i ->
        val t = i.toFloat() / (n - 1)
        val src = ((i + phaseShift) % n + n) % n
        val physics = wave.h[src] * heightScale * edgeFade
        val ambient = sin(t * ambientSpatialCycles * PI.toFloat() + timePhase) * ambientAmpPx * edgeFade
        baseY(t) + physics + ambient
    }
    val xCoords = FloatArray(n) { i -> i * step }

    // ── Smooth wave path via quadratic bezier through midpoints ────────────
    // Using control=P[i], endpoint=midpoint(P[i], P[i+1]) for every segment
    // makes a C1-continuous curve that passes smoothly through all data points.
    val path = Path()
    path.moveTo(xCoords[0], yCoords[0])
    for (i in 0 until n - 1) {
        val midX = (xCoords[i] + xCoords[i + 1]) / 2f
        val midY = (yCoords[i] + yCoords[i + 1]) / 2f
        path.quadraticTo(xCoords[i], yCoords[i], midX, midY)
    }
    path.lineTo(xCoords[n - 1], yCoords[n - 1])
    path.lineTo(w, h)
    path.lineTo(0f, h)
    path.close()

    drawPath(path, color)
}
