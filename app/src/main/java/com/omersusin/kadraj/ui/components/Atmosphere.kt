package com.omersusin.kadraj.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.omersusin.kadraj.ui.theme.Kadraj
import kotlin.math.min
import kotlin.random.Random

@Composable
fun AuroraBackdrop(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "aurora")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(26000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "aurora-drift",
    )
    val dots = remember {
        val random = Random(31)
        List(82) {
            Offset(random.nextFloat(), random.nextFloat())
        }
    }

    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF19130D),
                        Kadraj.Ink,
                        Kadraj.InkDeep,
                    ),
                ),
            )
            val halo = min(size.width, size.height)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Kadraj.InkDeep.copy(alpha = 0.42f),
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = halo * 0.9f,
                ),
                radius = halo * 0.9f,
                center = Offset(size.width / 2f, size.height / 2f),
            )
        }

        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.18f
                    scaleY = 1.18f
                    translationX = (drift - 0.5f) * size.width * 0.07f
                    translationY = (drift - 0.5f) * size.height * 0.03f
                },
        ) {
            val width = size.width
            val height = size.height

            val peachCenter = Offset(width * 0.08f, height * 0.02f)
            val peachRadius = width * 1.02f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Kadraj.Peach.copy(alpha = 0.13f),
                        Kadraj.Terracotta.copy(alpha = 0.06f),
                        Color.Transparent,
                    ),
                    center = peachCenter,
                    radius = peachRadius,
                ),
                radius = peachRadius,
                center = peachCenter,
            )

            val violetCenter = Offset(width * 1.04f, height * 0.28f)
            val violetRadius = width * 0.76f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Kadraj.Violet.copy(alpha = 0.11f),
                        Color.Transparent,
                    ),
                    center = violetCenter,
                    radius = violetRadius,
                ),
                radius = violetRadius,
                center = violetCenter,
            )

            val acidCenter = Offset(width * 0.18f, height * 0.93f)
            val acidRadius = width * 0.7f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Kadraj.Acid.copy(alpha = 0.06f),
                        Color.Transparent,
                    ),
                    center = acidCenter,
                    radius = acidRadius,
                ),
                radius = acidRadius,
                center = acidCenter,
            )

            val skyCenter = Offset(width * 0.92f, height * 0.78f)
            val skyRadius = width * 0.62f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Kadraj.Sky.copy(alpha = 0.06f),
                        Color.Transparent,
                    ),
                    center = skyCenter,
                    radius = skyRadius,
                ),
                radius = skyRadius,
                center = skyCenter,
            )
        }

        Canvas(Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            drawLine(
                color = Kadraj.Line,
                start = Offset(width * 0.072f, 0f),
                end = Offset(width * 0.072f, height),
                strokeWidth = 1f,
            )
            drawLine(
                color = Kadraj.Line,
                start = Offset(width * 0.928f, 0f),
                end = Offset(width * 0.928f, height),
                strokeWidth = 1f,
            )

            dots.forEach { point ->
                drawCircle(
                    color = Kadraj.Paper.copy(alpha = 0.024f),
                    radius = 0.65.dp.toPx(),
                    center = Offset(point.x * width, point.y * height),
                )
            }
        }
    }
}
