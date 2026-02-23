package com.example.hydrotracker.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrotracker.ui.theme.Crystal400
import com.example.hydrotracker.ui.theme.Crystal500
import com.example.hydrotracker.ui.theme.IceBlue400
import com.example.hydrotracker.ui.theme.Violet400
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

// ─────────────────────────────────────────────────────────────────────────────
//  Physics constants
// ─────────────────────────────────────────────────────────────────────────────

private const val ORB_N = 120      // surface sample columns — more = smoother curve
private const val ORB_K = 0.030f   // spring tension between neighbours
private const val ORB_DAMP = 0.020f   // velocity damping per tick
private const val ORB_MAX_DT = 0.032f // timestep cap

// ─────────────────────────────────────────────────────────────────────────────
//  Pure-spring wave (zero equilibrium — tilt slope lives ONLY in drawing)
// ─────────────────────────────────────────────────────────────────────────────

private class OrbWave(val n: Int = ORB_N) {
    val h = FloatArray(n)   // height offsets (px); positive = surface moves DOWN in canvas
    val v = FloatArray(n)   // velocities (px / s)

    /** Advance one physics tick toward flat equilibrium (h = 0 everywhere). */
    fun step(dt: Float) {
        for (i in 0 until n) {
            val left = if (i > 0) h[i - 1] else h[0]
            val right = if (i < n - 1) h[i + 1] else h[n - 1]
            // a = k*(L + R − 2h) − damp*v
            val accel = ORB_K * (left + right - 2f * h[i]) - ORB_DAMP * v[i]
            v[i] += accel
            h[i] += v[i]
        }
    }

    /**
     * Inject a velocity impulse centred at normalised position [pos] ∈ 0..1.
     * Neighbours receive 60 % of the force so ripples spread naturally.
     */
    fun splash(pos: Float, force: Float) {
        val idx = (pos * (n - 1)).toInt().coerceIn(0, n - 1)
        v[idx] += force
        if (idx > 0) v[idx - 1] += force * 0.60f
        if (idx < n - 1) v[idx + 1] += force * 0.60f
        // second ring
        if (idx > 1) v[idx - 2] += force * 0.25f
        if (idx < n - 2) v[idx + 2] += force * 0.25f
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun WaterProgressRing(
    currentMl: Int,
    goalMl: Int,
    modifier: Modifier = Modifier,
    ringSize: Dp = 260.dp,
    gyroTilt: GyroTilt = GyroTilt(),
) {
    val progress = (currentMl.toFloat() / goalMl.coerceAtLeast(1)).coerceIn(0f, 1f)
    val isGoalMet = currentMl >= goalMl

    // Smoothly animated fill — LowBouncy so adding water feels satisfying
    val animFill by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow,
        ),
        label = "orbFill",
    )

    // Smooth tilt for the DRAWING baseline — glides without jitter
    val animTilt by animateFloatAsState(
        targetValue = gyroTilt.x,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "orbTilt",
    )

    val wave = remember { OrbWave(ORB_N) }
    var frameTick by remember { mutableLongStateOf(0L) }

    // ── Physics loop (display-rate) ─────────────────────────────────────────
    LaunchedEffect(Unit) {
        var lastMs = withFrameMillis { it }
        var prevRaw = gyroTilt.rawX

        while (isActive) {
            val nowMs = withFrameMillis { it }
            val dt = ((nowMs - lastMs) / 1000f).coerceAtMost(ORB_MAX_DT)
            lastMs = nowMs

            // Splash driven by the FAST rawX channel — fires before the spring
            // animation even starts to move, so the slosh feels instant.
            val rawNow = gyroTilt.rawX
            val delta = rawNow - prevRaw
            if (abs(delta) > 0.20f) {
                // Sign convention: positive x = tilted LEFT → water goes LEFT
                //   → splash lands on the LEFT side  (pos ≈ 0.15)
                // Negative x = tilted RIGHT → water goes RIGHT
                //   → splash lands on the RIGHT side (pos ≈ 0.85)
                val pos = if (delta > 0f) 0.15f else 0.85f
                val force = abs(delta) * 11f
                wave.splash(pos, force)
            }
            prevRaw = rawNow

            wave.step(dt)
            frameTick = nowMs
        }
    }

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val r = size.width / 2f
            val cx = size.width / 2f
            val cy = size.height / 2f

            // ── 1. Outer ambient glow (brightens with fill level) ─────────────
            val glowColor = if (isGoalMet) Crystal400 else IceBlue400
            val glowAlpha = 0.08f + animFill * 0.16f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = glowAlpha), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = r * 1.40f,
                ),
                radius = r * 1.40f,
                center = Offset(cx, cy),
            )

            // ── 2. Everything inside the orb circle ───────────────────────────
            val orbClip = Path().apply {
                addOval(Rect(0f, 0f, size.width, size.height))
            }

            clipPath(orbClip) {

                // ── 2a. Dark orb background ───────────────────────────────────
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0C1628), Color(0xFF060D1A)),
                        center = Offset(cx, cy),
                        radius = r,
                    ),
                    radius = r,
                    center = Offset(cx, cy),
                )

                // ── 2b. Physics water fill ────────────────────────────────────
                if (animFill > 0.005f) {

                    @Suppress("UNUSED_EXPRESSION") frameTick   // force redraw each frame

                    // Water surface centre Y from top of orb
                    val baselineY = size.height * (1f - animFill)

                    // ── Slope (FIXED sign) ─────────────────────────────────────
                    // GyroTilt sign convention: positive x = phone tilted LEFT
                    //   → water accumulates on the LEFT side
                    //   → left surface is LOWER in canvas (larger Y) … wait —
                    //     water depth on the left increases, so the surface rises
                    //     on the LEFT, meaning surface Y is SMALLER on the left.
                    //
                    // Formula:  surfaceY(t) = baselineY + slope * (t − 0.5)
                    //   t = 0 (left edge),  t = 1 (right edge)
                    //   When tiltDeg > 0 (tilted LEFT), slope > 0:
                    //     left  (t=0): baselineY − slope/2  → smaller Y → surface HIGHER ✓
                    //     right (t=1): baselineY + slope/2  → larger  Y → surface LOWER  ✓
                    val maxTilt = 35f
                    val clamped = animTilt.coerceIn(-maxTilt, maxTilt)
                    val rawSlope = tan((clamped * PI / 180.0).toFloat()) * (size.width / 2f)
                    val maxSlope = r * 0.62f
                    val slope = rawSlope.coerceIn(-maxSlope, maxSlope)

                    fun surfaceY(t: Float) = baselineY + slope * (t - 0.5f)

                    // Edge-fade: amplitude → 0 when nearly empty or full
                    val edgeFade = (animFill * (1f - animFill) * 5f).coerceIn(0f, 1f)

                    val n = wave.n

                    // ── Back water layer (deeper, slower phase) ───────────────
                    val backPath = Path()
                    for (i in 0 until n) {
                        val t = i.toFloat() / (n - 1)
                        val px = t * size.width
                        val src = ((i + n / 3) % n)
                        val wy = surfaceY(t) + wave.h[src] * 0.50f * edgeFade
                        if (i == 0) backPath.moveTo(px, wy) else backPath.lineTo(px, wy)
                    }
                    backPath.lineTo(size.width, size.height)
                    backPath.lineTo(0f, size.height)
                    backPath.close()

                    drawPath(
                        path = backPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0284C7).copy(alpha = 0.50f),
                                Color(0xFF075985).copy(alpha = 0.70f),
                                Color(0xFF0C4A6E).copy(alpha = 0.88f),
                            ),
                            startY = (baselineY - r * 0.08f).coerceAtLeast(0f),
                            endY = size.height,
                        ),
                    )

                    // ── Front water layer (brighter, full physics heights) ─────
                    val frontPath = Path()
                    for (i in 0 until n) {
                        val t = i.toFloat() / (n - 1)
                        val px = t * size.width
                        val wy = surfaceY(t) + wave.h[i] * edgeFade
                        if (i == 0) frontPath.moveTo(px, wy) else frontPath.lineTo(px, wy)
                    }
                    frontPath.lineTo(size.width, size.height)
                    frontPath.lineTo(0f, size.height)
                    frontPath.close()

                    drawPath(
                        path = frontPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF38BDF8).copy(alpha = 0.40f),
                                Color(0xFF0EA5E9).copy(alpha = 0.58f),
                                Color(0xFF0369A1).copy(alpha = 0.78f),
                            ),
                            startY = (baselineY - r * 0.04f).coerceAtLeast(0f),
                            endY = size.height,
                        ),
                    )

                    // ── Surface shimmer (third phase) ─────────────────────────
                    val shimPath = Path()
                    for (i in 0 until n) {
                        val t = i.toFloat() / (n - 1)
                        val px = t * size.width
                        val src = ((i + n * 2 / 3) % n)
                        val wy = surfaceY(t) + wave.h[src] * 0.28f * edgeFade
                        if (i == 0) shimPath.moveTo(px, wy) else shimPath.lineTo(px, wy)
                    }
                    shimPath.lineTo(size.width, size.height)
                    shimPath.lineTo(0f, size.height)
                    shimPath.close()

                    drawPath(shimPath, Color(0xFF7DD3FC).copy(alpha = 0.14f))

                    // ── Caustic light bands inside the water ──────────────────
                    // Soft diagonal highlight bands that give a "light refracting
                    // through water" feel without being garish.
                    val causticAlpha = (animFill * 0.09f).coerceAtMost(0.09f)
                    val band1X = cx * 0.55f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = causticAlpha),
                                Color.Transparent,
                            ),
                            center = Offset(band1X, cy + r * 0.15f),
                            radius = r * 0.45f,
                        ),
                        radius = r,
                        center = Offset(cx, cy),
                    )
                }

                // ── 2c. Inner vignette — darkens the rim for depth ────────────
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.42f)),
                        center = Offset(cx, cy),
                        radius = r,
                    ),
                    radius = r,
                    center = Offset(cx, cy),
                )

                // ── 2d. Top specular highlight (glass lens look) ──────────────
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                        center = Offset(cx * 0.70f, cy * 0.52f),
                        radius = r * 0.52f,
                    ),
                    radius = r,
                    center = Offset(cx, cy),
                )
            }

            // ── 3. Progress ring border ───────────────────────────────────────
            val strokeW = 2.dp.toPx()
            val halfStroke = strokeW / 2f

            // Dim track ring
            drawCircle(
                color = Color.White.copy(alpha = 0.07f),
                radius = r - halfStroke,
                center = Offset(cx, cy),
                style = Stroke(width = strokeW),
            )

            // Glowing progress arc
            if (animFill > 0.005f) {
                val arc1 = if (isGoalMet) Crystal400 else IceBlue400
                val arc2 = if (isGoalMet) Crystal500 else Violet400

                // Soft glow halo behind the arc
                drawArc(
                    color = arc1.copy(alpha = 0.18f),
                    startAngle = -90f,
                    sweepAngle = animFill * 360f,
                    useCenter = false,
                    topLeft = Offset(halfStroke - 5.dp.toPx(), halfStroke - 5.dp.toPx()),
                    size = Size(
                        size.width - (halfStroke - 5.dp.toPx()) * 2f,
                        size.height - (halfStroke - 5.dp.toPx()) * 2f,
                    ),
                    style = Stroke(width = strokeW + 9.dp.toPx(), cap = StrokeCap.Round),
                )

                // Crisp arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colorStops = arrayOf(
                            0.00f to arc2.copy(alpha = 0.80f),
                            0.50f to arc1,
                            1.00f to arc2.copy(alpha = 0.85f),
                        ),
                        center = Offset(cx, cy),
                    ),
                    startAngle = -90f,
                    sweepAngle = animFill * 360f,
                    useCenter = false,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(size.width - halfStroke * 2f, size.height - halfStroke * 2f),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                )

                // Bright leading-edge dot
                val leadRad = ((-90f + animFill * 360f) * PI / 180.0).toFloat()
                val dotX = cx + (r - halfStroke) * cos(leadRad)
                val dotY = cy + (r - halfStroke) * sin(leadRad)
                drawCircle(color = arc1, radius = 4.dp.toPx(), center = Offset(dotX, dotY))
                drawCircle(color = Color.White.copy(alpha = 0.95f), radius = 1.8.dp.toPx(), center = Offset(dotX, dotY))
            }
        }

        // ── 4. Centre text overlay ────────────────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = orbFormat(currentMl),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (currentMl >= 10000) 30.sp else 36.sp,
                    letterSpacing = (-2).sp,
                ),
                color = when {
                    isGoalMet -> Crystal400
                    currentMl > 0 -> Color.White.copy(alpha = 0.93f)
                    else -> Color.White.copy(alpha = 0.25f)
                },
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "of ${orbFormat(goalMl)}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    letterSpacing = 0.3.sp,
                ),
                color = Color.White.copy(alpha = 0.35f),
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val pctColor = when {
                isGoalMet -> Crystal400
                progress > 0.5f -> IceBlue400
                else -> Color.White.copy(alpha = 0.45f)
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.6.sp,
                ),
                color = pctColor,
            )
        }
    }
}

private fun orbFormat(ml: Int): String =
    if (ml >= 1000) String.format("%.1fL", ml / 1000f) else "${ml}ml"
