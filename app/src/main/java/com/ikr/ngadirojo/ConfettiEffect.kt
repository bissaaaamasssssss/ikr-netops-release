package com.ikr.ngadirojo

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val color: Color,
    val size: Float,
    val speedX: Float,
    val speedY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val shape: Int // 0=rect, 1=circle, 2=triangle
)

@Composable
fun ConfettiEffect(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        List(120) {
            ConfettiParticle(
                startX = Random.nextFloat(),
                startY = -Random.nextFloat() * 0.5f - 0.1f,
                color = listOf(
                    Color(0xFF00E5FF),
                    Color(0xFF10B981),
                    Color(0xFFF59E0B),
                    Color(0xFFFF6B9D),
                    Color(0xFF9C27B0),
                    Color(0xFFFFFFFF)
                ).random(),
                size = Random.nextFloat() * 14f + 8f,
                speedX = (Random.nextFloat() - 0.5f) * 400f,
                speedY = Random.nextFloat() * 300f + 400f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                shape = Random.nextInt(3)
            )
        }
    }

    var animationTime by remember { mutableStateOf(0f) }

    LaunchedEffect(isActive) {
        if (isActive) {
            animationTime = 0f
            val startTime = withFrameNanos { it }
            while (animationTime < 1f) {
                withFrameNanos { now ->
                    animationTime = (now - startTime) / 3_000_000_000f // 3 detik
                }
            }
        }
    }

    if (isActive && animationTime < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            particles.forEach { p ->
                val x = p.startX * width + p.speedX * animationTime
                val y = p.startY * height + p.speedY * animationTime + 
                        0.5f * 500f * animationTime * animationTime
                
                // Skip kalau sudah keluar layar
                if (y > height + 50) return@forEach
                
                val alpha = (1f - animationTime).coerceIn(0f, 1f)
                val color = p.color.copy(alpha = alpha)
                val rot = p.rotation + p.rotationSpeed * animationTime
                
                rotate(degrees = rot, pivot = Offset(x, y)) {
                    when (p.shape) {
                        0 -> drawRect(
                            color = color,
                            topLeft = Offset(x - p.size/2, y - p.size/2),
                            size = Size(p.size, p.size * 0.6f)
                        )
                        1 -> drawCircle(
                            color = color,
                            radius = p.size / 2,
                            center = Offset(x, y)
                        )
                        2 -> {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(x, y - p.size/2)
                                lineTo(x + p.size/2, y + p.size/2)
                                lineTo(x - p.size/2, y + p.size/2)
                                close()
                            }
                            drawPath(path, color)
                        }
                    }
                }
            }
        }
    }
}
