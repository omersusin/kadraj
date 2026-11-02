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

    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF111113),
                        Kadraj.Ink,
                        Kadraj.InkDeep,
                    ),
                ),
            )
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
            val warmCenter = Offset(size.width * 0.06f, -size.height * 0.06f)
            val warmRadius = size.width * 0.95f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Kadraj.Peach.copy(alpha = 0.11f),
                        Kadraj.Terracotta.copy(alpha = 0.035f),
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
                        Color.White.copy(alpha = 0.035f),
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
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.36f),
                    ),
                    center = Offset(size.width / 2f, size.height * 0.44f),
                    radius = size.maxDimension * 0.72f,
                ),
            )
            grain.forEach { point ->
                drawCircle(
                    color = Kadraj.Paper.copy(alpha = 0.018f),
                    radius = 0.7.dp.toPx(),
                    center = Offset(point.x * size.width, point.y * size.height),
                )
            }
        }
    }
}
