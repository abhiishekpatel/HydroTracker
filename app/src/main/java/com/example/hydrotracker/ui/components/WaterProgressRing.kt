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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrotracker.ui.theme.Cyan400
import com.example.hydrotracker.ui.theme.Blue400
import com.example.hydrotracker.ui.theme.Blue500
import com.example.hydrotracker.ui.theme.Green400
import com.example.hydrotracker.ui.theme.Green500
import com.example.hydrotracker.ui.theme.Slate700

@Composable
fun WaterProgressRing(
    currentMl: Int,
    goalMl: Int,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    strokeWidth: Dp = 16.dp
) {
    val progress = (currentMl.toFloat() / goalMl.coerceAtLeast(1)).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ringProgress"
    )

    val isGoalMet = currentMl >= goalMl

    // Gradient colours for the arc
    val arcStart = if (isGoalMet) Green400 else Blue400
    val arcEnd = if (isGoalMet) Green500 else Cyan400
    val glowColor = if (isGoalMet) Green500 else Blue500

    // Track colour
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {

        // ── Canvas ring ────────────────────────────────────────────────────────
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f + 2.dp.toPx()      // keep stroke inside bounds
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            // ── Track (full 360°) ─────────────────────────────────────────────
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // ── Glow shadow arc (drawn slightly wider, offset behind) ──────────
            if (animatedProgress > 0.01f) {
                drawIntoCanvas { canvas ->
                    val paint = androidx.compose.ui.graphics.Paint().apply {
                        asFrameworkPaint().apply {
                            isAntiAlias = true
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = strokePx + 8.dp.toPx()
                            strokeCap = android.graphics.Paint.Cap.ROUND
                            color = android.graphics.Color.TRANSPARENT
                            setShadowLayer(
                                14.dp.toPx(),   // blur radius
                                0f, 0f,
                                glowColor.copy(alpha = 0.55f).toArgb()
                            )
                        }
                    }
                    val oval = android.graphics.RectF(
                        topLeft.x, topLeft.y,
                        topLeft.x + arcSize.width,
                        topLeft.y + arcSize.height
                    )
                    canvas.nativeCanvas.drawArc(
                        oval,
                        -90f,
                        animatedProgress * 360f,
                        false,
                        paint.asFrameworkPaint()
                    )
                }
            }

            // ── Progress arc (sweep gradient via sweepGradient brush) ─────────
            if (animatedProgress > 0.005f) {
                val cx = this.size.width / 2f
                val cy = this.size.height / 2f
                drawArc(
                    brush = Brush.sweepGradient(
                        colorStops = arrayOf(
                            0.00f to arcStart.copy(alpha = 0.85f),
                            0.50f to arcEnd,
                            1.00f to arcStart.copy(alpha = 0.90f)
                        ),
                        center = Offset(cx, cy)
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            // ── Tick marks at 25 / 50 / 75 % ─────────────────────────────────
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val radius = arcSize.width / 2f
            listOf(0f, 90f, 180f, 270f).forEach { angleDeg ->
                val rad = Math.toRadians((angleDeg - 90.0))
                val cos = Math.cos(rad).toFloat()
                val sin = Math.sin(rad).toFloat()
                val inner = radius - strokePx * 0.5f - 3.dp.toPx()
                val outer = radius + strokePx * 0.5f + 3.dp.toPx()
                drawLine(
                    color = trackColor.copy(alpha = 0.6f),
                    start = Offset(cx + cos * inner, cy + sin * inner),
                    end = Offset(cx + cos * outer, cy + sin * outer),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // ── Centre text ────────────────────────────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Large formatted amount (e.g. "2.4L")
            Text(
                text = formatAmount(currentMl),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 42.sp,
                    letterSpacing = (-1.5).sp
                ),
                color = when {
                    isGoalMet -> Green400
                    currentMl > 0 -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Sub-label "of X.XL goal"
            Text(
                text = "of ${formatAmount(goalMl)}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    letterSpacing = 0.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Percentage pill
            val pctText = "${(progress * 100).toInt()}%"
            val pillColor = when {
                isGoalMet -> Green500.copy(alpha = 0.18f)
                progress > 0.5f -> Blue500.copy(alpha = 0.14f)
                else -> Slate700.copy(alpha = 0.40f)
            }
            val pillTextColor = when {
                isGoalMet -> Green400
                progress > 0.5f -> Blue400
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Text(
                text = pctText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.6.sp
                ),
                color = pillTextColor
            )
        }
    }
}

private fun formatAmount(ml: Int): String =
    if (ml >= 1000) String.format("%.1fL", ml / 1000f) else "${ml}ml"
