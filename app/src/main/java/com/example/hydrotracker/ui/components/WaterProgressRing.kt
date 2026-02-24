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

private const val ORB_N = 120       // surface sample columns — more = smoother
private const val ORB_K = 0.026f    // spring tension — slightly softer, more fluid
private const val ORB_DAMP = 0.022f // velocity damping per tick
private const val ORB_MAX_DT = 0.032f

// ─────────────────────────────────────────────────────────────────────────────
//  Pure-spring wave
// ─────────────────────────────────────────────────────────────────────────────

private class OrbWave(val n: Int = ORB_N) {
    val h = FloatArray(n)
    val v = FloatArray(n)

    fun step(dt: Float) {
        for (i in 0 until n) {
            val left = if (i > 0) h[i - 1] else h[0]
            val right = if (i < n - 1) h[i + 1] else h[n - 1]
            val accel = ORB_K * (left + right - 2f * h[i]) - ORB_DAMP * v[i]
            v[i] += accel
            h[i] += v[i]
        }
    }

    fun splash(pos: Float, force: Float) {
        val idx = (pos * (n - 1)).toInt().coerceIn(0, n - 1)
        v[idx] += force
        if (idx > 0) v[idx - 1] += force * 0.60f
        if (idx < n - 1) v[idx + 1] += force * 0.60f
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

    val animFill by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow,
        ),
        label = "orbFill",
    )

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

    LaunchedEffect(Unit) {
        var lastMs = withFrameMillis { it }
        var prevRaw = gyroTilt.rawX

        while (isActive) {
            val nowMs = withFrameMillis { it }
            val dt = ((nowMs - lastMs) / 1000f).coerceAtMost(ORB_MAX_DT)
            lastMs = nowMs

            val rawNow = gyroTilt.rawX
            val delta = rawNow - prevRaw
            if (abs(delta) > 0.20f) {
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

            // ── 1. Outer ambient glow ─────────────────────────────────────────
            val glowColor = if (isGoalMet) Crystal400 else IceBlue400
            val glowAlpha = 0.10f + animFill * 0.18f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = glowAlpha), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = r * 1.40f,
                ),
                radius = r * 1.40f,
                center = Offset(cx, cy),
            )

            // ── 2. Everything inside the orb ──────────────────────────────────
            val orbClip = Path().apply {
                addOval(Rect(0f, 0f, size.width, size.height))
            }

            clipPath(orbClip) {

                // ── 2a. Orb background — visible dark blue, not pitch black ───
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0F2444), Color(0xFF0A1830)),
                        center = Offset(cx, cy),
                        radius = r,
                    ),
                    radius = r,
                    center = Offset(cx, cy),
                )

                // ── 2b. Physics water fill ─────────────────────────────────────
                if (animFill > 0.005f) {

                    @Suppress("UNUSED_EXPRESSION") frameTick

                    val baselineY = size.height * (1f - animFill)

                    // Tilt slope
                    val maxTilt = 35f
                    val clamped = animTilt.coerceIn(-maxTilt, maxTilt)
                    val rawSlope = tan((clamped * PI / 180.0).toFloat()) * (size.width / 2f)
                    val maxSlope = r * 0.62f
                    val slope = rawSlope.coerceIn(-maxSlope, maxSlope)
                    fun surfaceY(t: Float) = baselineY + slope * (t - 0.5f)

                    val edgeFade = (animFill * (1f - animFill) * 5f).coerceIn(0f, 1f)
                    val n = wave.n
                    val timeSec = frameTick.toFloat() / 1000f

                    // ── Back water layer ───────────────────────────────────────
                    // Slower ambient phase, deeper colour — gives depth impression
                    val backPhase = timeSec * 0.20f * 2f * PI.toFloat()
                    val backXCoords = FloatArray(n) { i -> i.toFloat() * size.width / (n - 1) }
                    val backYCoords = FloatArray(n) { i ->
                        val t = i.toFloat() / (n - 1)
                        val src = ((i + n / 3) % n)
                        val ambient = sin(t * 2.8f * PI.toFloat() + backPhase) * 3f * edgeFade
                        surfaceY(t) + wave.h[src] * 0.50f * edgeFade + ambient
                    }

                    val backPath = Path()
                    backPath.moveTo(backXCoords[0], backYCoords[0])
                    for (i in 0 until n - 1) {
                        val midX = (backXCoords[i] + backXCoords[i + 1]) / 2f
                        val midY = (backYCoords[i] + backYCoords[i + 1]) / 2f
                        backPath.quadraticTo(backXCoords[i], backYCoords[i], midX, midY)
                    }
                    backPath.lineTo(backXCoords[n - 1], backYCoords[n - 1])
                    backPath.lineTo(size.width, size.height)
                    backPath.lineTo(0f, size.height)
                    backPath.close()

                    drawPath(
                        path = backPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E8ED4).copy(alpha = 0.55f),
                                Color(0xFF0E6FAA).copy(alpha = 0.72f),
                                Color(0xFF0A5588).copy(alpha = 0.90f),
                            ),
                            startY = (baselineY - r * 0.08f).coerceAtLeast(0f),
                            endY = size.height,
                        ),
                    )

                    // ── Front water layer ──────────────────────────────────────
                    // Faster ambient phase, lighter colour — sits "on top" of water
                    val frontPhase = timeSec * 0.28f * 2f * PI.toFloat() + 1.2f
                    val frontXCoords = FloatArray(n) { i -> i.toFloat() * size.width / (n - 1) }
                    val frontYCoords = FloatArray(n) { i ->
                        val t = i.toFloat() / (n - 1)
                        val ambient = sin(t * 3.5f * PI.toFloat() + frontPhase) * 2.5f * edgeFade
                        surfaceY(t) + wave.h[i] * edgeFade + ambient
                    }

                    val frontPath = Path()
                    frontPath.moveTo(frontXCoords[0], frontYCoords[0])
                    for (i in 0 until n - 1) {
                        val midX = (frontXCoords[i] + frontXCoords[i + 1]) / 2f
                        val midY = (frontYCoords[i] + frontYCoords[i + 1]) / 2f
                        frontPath.quadraticTo(frontXCoords[i], frontYCoords[i], midX, midY)
                    }
                    frontPath.lineTo(frontXCoords[n - 1], frontYCoords[n - 1])
                    frontPath.lineTo(size.width, size.height)
                    frontPath.lineTo(0f, size.height)
                    frontPath.close()

                    drawPath(
                        path = frontPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF50C8F0).copy(alpha = 0.48f),
                                Color(0xFF28A8E0).copy(alpha = 0.62f),
                                Color(0xFF1080BC).copy(alpha = 0.80f),
                            ),
                            startY = (baselineY - r * 0.04f).coerceAtLeast(0f),
                            endY = size.height,
                        ),
                    )

                    // ── Surface shimmer ────────────────────────────────────────
                    val shimPhase = timeSec * 0.35f * 2f * PI.toFloat() + 2.5f
                    val shimPath = Path()
                    shimPath.moveTo(frontXCoords[0], frontYCoords[0])
                    for (i in 0 until n - 1) {
                        val src = ((i + n * 2 / 3) % n)
                        // Shimmer follows the front surface closely with slight offset
                        val shimY = frontYCoords[i] +
                                sin(i.toFloat() / n * 4f * PI.toFloat() + shimPhase) * 1.5f * edgeFade
                        val nextSrc = ((i + 1 + n * 2 / 3) % n)
                        val nextShimY = frontYCoords[i + 1] +
                                sin((i + 1).toFloat() / n * 4f * PI.toFloat() + shimPhase) * 1.5f * edgeFade
                        val midX = (frontXCoords[i] + frontXCoords[i + 1]) / 2f
                        val midY = (shimY + nextShimY) / 2f
                        if (i == 0) shimPath.moveTo(frontXCoords[0], shimY)
                        shimPath.quadraticTo(frontXCoords[i], shimY, midX, midY)
                        @Suppress("UNUSED_VARIABLE") src
                        @Suppress("UNUSED_VARIABLE") nextSrc
                    }
                    shimPath.lineTo(frontXCoords[n - 1], frontYCoords[n - 1])
                    shimPath.lineTo(size.width, size.height)
                    shimPath.lineTo(0f, size.height)
                    shimPath.close()
                    drawPath(shimPath, Color(0xFF90D9FC).copy(alpha = 0.14f))

                    // ── Soft surface foam line ─────────────────────────────────
                    // A thin highlight stroke at the waterline gives real "surface
                    // tension" look — like light catching the water's edge.
                    if (edgeFade > 0.08f) {
                        val foamPath = Path()
                        foamPath.moveTo(frontXCoords[0], frontYCoords[0])
                        for (i in 0 until n - 1) {
                            val midX = (frontXCoords[i] + frontXCoords[i + 1]) / 2f
                            val midY = (frontYCoords[i] + frontYCoords[i + 1]) / 2f
                            foamPath.quadraticTo(
                                frontXCoords[i], frontYCoords[i], midX, midY,
                            )
                        }
                        foamPath.lineTo(frontXCoords[n - 1], frontYCoords[n - 1])
                        drawPath(
                            path = foamPath,
                            color = Color.White.copy(alpha = 0.22f * edgeFade),
                            style = Stroke(width = 1.5.dp.toPx()),
                        )
                    }

                    // ── Caustic light bands inside the water ───────────────────
                    val causticAlpha = (animFill * 0.10f).coerceAtMost(0.10f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = causticAlpha),
                                Color.Transparent,
                            ),
                            center = Offset(cx * 0.55f, cy + r * 0.15f),
                            radius = r * 0.45f,
                        ),
                        radius = r,
                        center = Offset(cx, cy),
                    )
                }

                // ── 2c. Inner vignette — gentle rim darkening for depth ────────
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                        center = Offset(cx, cy),
                        radius = r,
                    ),
                    radius = r,
                    center = Offset(cx, cy),
                )

                // ── 2d. Top specular highlight (glass lens) ────────────────────
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.07f),
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

            drawCircle(
                color = Color.White.copy(alpha = 0.09f),
                radius = r - halfStroke,
                center = Offset(cx, cy),
                style = Stroke(width = strokeW),
            )

            if (animFill > 0.005f) {
                val arc1 = if (isGoalMet) Crystal400 else IceBlue400
                val arc2 = if (isGoalMet) Crystal500 else Violet400

                // Soft glow halo behind the arc
                drawArc(
                    color = arc1.copy(alpha = 0.20f),
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
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f),
                    radius = 1.8.dp.toPx(),
                    center = Offset(dotX, dotY),
                )
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
                    else -> Color.White.copy(alpha = 0.30f)
                },
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "of ${orbFormat(goalMl)}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    letterSpacing = 0.3.sp,
                ),
                color = Color.White.copy(alpha = 0.42f),
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val pctColor = when {
                isGoalMet -> Crystal400
                progress > 0.5f -> IceBlue400
                else -> Color.White.copy(alpha = 0.50f)
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
