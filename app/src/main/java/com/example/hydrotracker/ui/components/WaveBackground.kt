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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.tan

// ─────────────────────────────────────────────────────────────────────────────
//  Physics constants
// ─────────────────────────────────────────────────────────────────────────────

private const val BG_N = 80      // wave sample columns
private const val BG_K = 0.026f  // spring tension between neighbours
private const val BG_DAMP = 0.016f  // velocity damping per tick
private const val BG_MAX_DT = 0.032f  // timestep cap (~30 fps floor)

// ─────────────────────────────────────────────────────────────────────────────
//  Pure-spring wave state  (zero equilibrium — slope lives only in drawing)
// ─────────────────────────────────────────────────────────────────────────────

private class BgWave(val n: Int = BG_N) {
    val h = FloatArray(n)   // surface height offsets (px); + = surface moves DOWN
    val v = FloatArray(n)   // velocities (px / s)

    /**
     * Advance one frame.  The equilibrium is intentionally FLAT (zero) so that
     * the drawing layer's baseline slope is the only tilt applied — preventing
     * the "double-slope" fight that made the water look wrong.
     */
    fun step(dt: Float) {
        for (i in 0 until n) {
            val left = if (i > 0) h[i - 1] else h[0]
            val right = if (i < n - 1) h[i + 1] else h[n - 1]
            // a = k*(L + R - 2h) - damp*v        (restores toward h=0)
            val accel = BG_K * (left + right - 2f * h[i]) - BG_DAMP * v[i]
            v[i] += accel
            h[i] += v[i]
        }
    }

    /**
     * Inject a velocity impulse at normalised position [pos] ∈ 0..1.
     * Neighbours receive 55 % of the impulse so the disturbance spreads
     * naturally rather than spiking at a single column.
     */
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
 * Tilt handling — two-layer approach that avoids the double-slope bug:
 *  1. **Drawing baseline** uses [gyroTilt].x (spring-animated) to slope the
 *     water surface visually.  The formula `baseY(t) = centreY + slope*(t-0.5)`
 *     correctly puts MORE water on whichever side gravity pulls it toward.
 *  2. **Physics impulses** use [gyroTilt].rawX (fast) to detect sudden wrist
 *     flicks and inject a splash, making the surface ripple convincingly.
 *     The physics otherwise restores to FLAT — it never fights the drawing slope.
 */
@Composable
fun WaveBackground(
    progress: Float,
    modifier: Modifier = Modifier,
    gyroTilt: GyroTilt = GyroTilt(),
) {
    // ── Animated fill level (spring — adds feel organic on sudden +intake) ──
    val animFill by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow,
        ),
        label = "bgFill",
    )

    // ── Animated tilt for the drawing baseline — smooth glide, no jitter ───
    val animTilt by animateFloatAsState(
        targetValue = gyroTilt.x,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "bgTilt",
    )

    // ── Physics state (plain arrays — zero GC pressure per frame) ──────────
    val wave = remember { BgWave(BG_N) }

    // Single Long updated every frame; Canvas reads it to force a redraw.
    var tick by remember { mutableLongStateOf(0L) }

    // ── Physics loop — display-rate via withFrameMillis ─────────────────────
    LaunchedEffect(Unit) {
        var lastMs = withFrameMillis { it }
        var prevRaw = gyroTilt.rawX

        while (isActive) {
            val nowMs = withFrameMillis { it }
            val dt = ((nowMs - lastMs) / 1000f).coerceAtMost(BG_MAX_DT)
            lastMs = nowMs

            // Detect rapid tilt change using the FAST (rawX) channel so the
            // splash fires even before the spring animation catches up.
            val rawNow = gyroTilt.rawX
            val delta = rawNow - prevRaw
            if (abs(delta) > 0.25f) {
                // When tilting RIGHT (more negative rawX), water rushes RIGHT →
                // splash lands on the right side (pos ≈ 0.85) and vice-versa.
                val pos = if (delta < 0f) 0.82f else 0.18f
                val force = abs(delta) * 9f
                wave.splash(pos, force)
            }
            prevRaw = rawNow

            wave.step(dt)
            tick = nowMs
        }
    }

    // ── Draw ─────────────────────────────────────────────────────────────────
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Water-surface centre Y (from top).  fill=0 → surface at bottom.
        val centreY = h * (1f - animFill)

        // Edge-fade: amplitude → 0 when nearly empty or nearly full so the
        // surface doesn't poke outside the screen edges.
        val edgeFade = (animFill * (1f - animFill) * 5.5f).coerceIn(0f, 1f)

        // ── Back wave (slower, lighter) ─────────────────────────────────────
        drawBgWave(
            wave = wave,
            w = w, h = h,
            centreY = centreY,
            tiltDeg = animTilt,
            edgeFade = edgeFade,
            heightScale = 0.62f,
            phaseShift = BG_N / 3,
            color = Color(0xFF0EA5E9).copy(alpha = 0.13f),
            tick = tick,
        )

        // ── Front wave (faster, stronger) ───────────────────────────────────
        drawBgWave(
            wave = wave,
            w = w, h = h,
            centreY = centreY,
            tiltDeg = animTilt,
            edgeFade = edgeFade,
            heightScale = 1.0f,
            phaseShift = 0,
            color = Color(0xFF38BDF8).copy(alpha = 0.10f),
            tick = tick,
        )

        // ── Shimmer layer (very faint, different phase) ──────────────────────
        if (animFill > 0.03f) {
            drawBgWave(
                wave = wave,
                w = w, h = h,
                centreY = centreY,
                tiltDeg = animTilt,
                edgeFade = edgeFade,
                heightScale = 0.38f,
                phaseShift = BG_N * 2 / 3,
                color = Color(0xFF7DD3FC).copy(alpha = 0.055f),
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
    tick: Long,    // read so Compose redraws every frame
) {
    @Suppress("UNUSED_EXPRESSION") tick

    val n = wave.n
    if (edgeFade < 0.005f) return

    // ── Slope (FIXED sign) ──────────────────────────────────────────────────
    // Sign convention from GyroTilt: positive x = phone tilted LEFT
    //   → water accumulates on the LEFT → left side surface is LOWER in y
    //     (closer to bottom) which in canvas coords means HIGHER y value.
    // Formula: baseY(t) = centreY + slope*(t - 0.5)
    //   t=0 (left):  centreY - slope/2   when slope>0 (tilted left) → smaller y → surface HIGHER ✓
    //   t=1 (right): centreY + slope/2                               → larger  y → surface LOWER  ✓
    val maxTilt = 35f
    val clamped = tiltDeg.coerceIn(-maxTilt, maxTilt)
    val halfW = w / 2f
    val slope = tan((clamped * PI / 180.0).toFloat()) * halfW
    val maxSlope = h * 0.26f
    val s = slope.coerceIn(-maxSlope, maxSlope)

    fun baseY(t: Float) = centreY + s * (t - 0.5f)

    // ── Build wave path ─────────────────────────────────────────────────────
    val path = Path()
    val step = w / (n - 1)

    for (i in 0 until n) {
        val t = i.toFloat() / (n - 1)
        val px = i * step
        val src = ((i + phaseShift) % n + n) % n
        val wy = baseY(t) + wave.h[src] * heightScale * edgeFade
        if (i == 0) path.moveTo(px, wy) else path.lineTo(px, wy)
    }

    path.lineTo(w, h)
    path.lineTo(0f, h)
    path.close()

    drawPath(path, color)
}
