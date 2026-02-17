package com.example.hydrotracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.example.hydrotracker.ui.theme.WaterBlue
import com.example.hydrotracker.ui.theme.WaterBlueLight
import com.example.hydrotracker.ui.theme.WaterBlueTransparent
import kotlin.math.sin

@Composable
fun WaveBackground(
    progress: Float,
    modifier: Modifier = Modifier,
    waveColor: Color = WaterBlue,
    waveColorLight: Color = WaterBlueLight,
    waveColorTransparent: Color = WaterBlueTransparent
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "waveProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val waterLevel = height * (1f - animatedProgress)
        val waveAmplitude = 12f

        // Back wave (lighter, slower)
        val backPath = Path().apply {
            moveTo(0f, waterLevel)
            for (x in 0..width.toInt() step 4) {
                val angle = Math.toRadians((x * 0.8f + waveOffset * 0.7f).toDouble())
                val y = waterLevel + (sin(angle) * waveAmplitude * 0.7f).toFloat()
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(backPath, waveColorTransparent)

        // Front wave
        val frontPath = Path().apply {
            moveTo(0f, waterLevel)
            for (x in 0..width.toInt() step 4) {
                val angle = Math.toRadians((x * 1.2f + waveOffset).toDouble())
                val y = waterLevel + (sin(angle) * waveAmplitude).toFloat()
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(frontPath, waveColor.copy(alpha = 0.15f))
    }
}
