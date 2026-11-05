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
import com.omersusin.kadraj.ui.theme.ThemeMode
import kotlin.random.Random

@Composable
fun AuroraBackdrop(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "backdrop")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "backdrop-drift",
    )
    val grain = remember {
        val random = Random(17)
        List(96) { Offset(random.nextFloat(), random.nextFloat()) }
    }

    val mode = Kadraj.mode
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Kadraj.InkRaised,
                        Kadraj.Ink,
                        Kadraj.InkDeep,
                    ),
                ),
            )
            if (mode == ThemeMode.CONTACT) {
                val step = 42.dp.toPx()
                val line = Kadraj.Line.copy(alpha = 0.5f)
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = line,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 0.6.dp.toPx(),
                    )
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = line,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.6.dp.toPx(),
                    )
                    y += step
                }
            }
        }

        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.08f
                    scaleY = 1.08f
                    translationX = (drift - 0.5f) * size.width * 0.035f
                    translationY = (drift - 0.5f) * size.height * 0.02f
                },
        ) {
            val warmCenter = when (mode) {
                ThemeMode.CONTACT -> Offset(size.width * 0.92f, size.height * 0.04f)
                else -> Offset(size.width * 0.06f, -size.height * 0.06f)
            }
            val warmRadius = size.width * 0.95f
            val warmAlpha = if (mode == ThemeMode.PAPER) 0.2f else 0.11f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Kadraj.Peach.copy(alpha = warmAlpha),
                        Kadraj.Terracotta.copy(alpha = 0.04f),
                        Color.Transparent,
                    ),
                    center = warmCenter,
                    radius = warmRadius,
                ),
                radius = warmRadius,
                center = warmCenter,
            )

            val coolCenter = Offset(size.width * 1.02f, size.height * 0.72f)
            val coolRadius = size.width * 0.72f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(
                            alpha = if (mode == ThemeMode.PAPER) 0.22f else 0.035f,
                        ),
                        Color.Transparent,
                    ),
                    center = coolCenter,
                    radius = coolRadius,
                ),
                radius = coolRadius,
                center = coolCenter,
            )
        }

        Canvas(Modifier.fillMaxSize()) {
            val vignette = when (mode) {
                ThemeMode.PAPER -> Kadraj.MutedDim.copy(alpha = 0.12f)
                ThemeMode.CONTACT -> Color.Black.copy(alpha = 0.5f)
                ThemeMode.CINEMA -> Color.Black.copy(alpha = 0.36f)
            }
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        vignette,
                    ),
                    center = Offset(size.width / 2f, size.height * 0.44f),
                    radius = size.maxDimension * 0.72f,
                ),
            )
            grain.forEach { point ->
                drawCircle(
                    color = Kadraj.Paper.copy(
                        alpha = if (mode == ThemeMode.CONTACT) 0.028f else 0.018f,
                    ),
                    radius = 0.7.dp.toPx(),
                    center = Offset(point.x * size.width, point.y * size.height),
                )
            }
        }
    }
}
