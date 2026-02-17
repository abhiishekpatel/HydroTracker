package com.example.hydrotracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.hydrotracker.ui.theme.Amber400
import com.example.hydrotracker.ui.theme.Blue400
import com.example.hydrotracker.ui.theme.Cyan400
import com.example.hydrotracker.ui.theme.Green400
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val color: Color,
    val size: Float
)

@Composable
fun ConfettiEffect(
    show: Boolean,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!show) return

    val progress = remember { Animatable(0f) }
    val particles = remember {
        val colors = listOf(Blue400, Cyan400, Green400, Amber400, Color(0xFF8B5CF6))
        List(60) {
            val angle = Random.nextFloat() * 360f
            val speed = Random.nextFloat() * 800f + 200f
            ConfettiParticle(
                startX = 0.5f,
                startY = 0.3f,
                velocityX = cos(Math.toRadians(angle.toDouble())).toFloat() * speed,
                velocityY = sin(Math.toRadians(angle.toDouble())).toFloat() * speed - 400f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 720f - 360f,
                color = colors[Random.nextInt(colors.size)],
                size = Random.nextFloat() * 8f + 4f
            )
        }
    }

    LaunchedEffect(show) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2000, easing = LinearEasing)
        )
        onComplete()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        val gravity = 1200f

        particles.forEach { particle ->
            val x = size.width * particle.startX + particle.velocityX * t
            val y = size.height * particle.startY + particle.velocityY * t + 0.5f * gravity * t * t
            val alpha = (1f - t).coerceIn(0f, 1f)
            val rotation = particle.rotation + particle.rotationSpeed * t

            if (y < size.height && y > 0 && x > 0 && x < size.width) {
                rotate(rotation, pivot = Offset(x, y)) {
                    drawRect(
                        color = particle.color.copy(alpha = alpha),
                        topLeft = Offset(x - particle.size / 2, y - particle.size / 2),
                        size = Size(particle.size, particle.size * 0.6f)
                    )
                }
            }
        }
    }
}
