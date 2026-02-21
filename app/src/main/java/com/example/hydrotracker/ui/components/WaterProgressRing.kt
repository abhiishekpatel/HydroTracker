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

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrotracker.ui.theme.Blue400
import com.example.hydrotracker.ui.theme.Blue500
import com.example.hydrotracker.ui.theme.Cyan400
import com.example.hydrotracker.ui.theme.Green400
import com.example.hydrotracker.ui.theme.Green500


@Composable
fun WaterProgressRing(
    currentMl: Int,
    goalMl: Int,
    modifier: Modifier = Modifier,
    ringSize: Dp = 230.dp,       // renamed from `size` to avoid shadowing DrawScope.size
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

    val arcStart = if (isGoalMet) Green400 else Blue400
    val arcEnd = if (isGoalMet) Green500 else Cyan400
    val glowColor = if (isGoalMet) Green500 else Blue500

    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {

            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f + 2.dp.toPx()
            // Use `size` from DrawScope (type Size) without ambiguity now
            val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
            val topLeft = Offset(inset, inset)

            val cx = size.width / 2f
            val cy = size.height / 2f

            // Track (full 360°)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Glow shadow arc — simulated via two wider semi-transparent arcs
            if (animatedProgress > 0.01f) {
                drawArc(
                    color = glowColor.copy(alpha = 0.12f),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx + 16.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = glowColor.copy(alpha = 0.25f),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx + 7.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Progress arc (sweep gradient)
            if (animatedProgress > 0.005f) {
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

            // Tick marks at 0 / 90 / 180 / 270°
            val radius = arcSize.width / 2f
            listOf(0f, 90f, 180f, 270f).forEach { angleDeg ->
                val rad = Math.toRadians((angleDeg - 90.0))
                val cosA = Math.cos(rad).toFloat()
                val sinA = Math.sin(rad).toFloat()
                val inner = radius - strokePx * 0.5f - 3.dp.toPx()
                val outer = radius + strokePx * 0.5f + 3.dp.toPx()
                drawLine(
                    color = trackColor.copy(alpha = 0.6f),
                    start = Offset(cx + cosA * inner, cy + sinA * inner),
                    end = Offset(cx + cosA * outer, cy + sinA * outer),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Centre text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

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

            val pillTextColor = when {
                isGoalMet -> Green400
                progress > 0.5f -> Blue400
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Text(
                text = "${(progress * 100).toInt()}%",
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
