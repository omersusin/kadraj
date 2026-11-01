package com.omersusin.kadraj.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omersusin.kadraj.BuildConfig
import com.omersusin.kadraj.EditorViewModel
import com.omersusin.kadraj.ui.components.CircleIconButton
import com.omersusin.kadraj.ui.components.Glyph
import com.omersusin.kadraj.ui.components.GlyphIcon
import com.omersusin.kadraj.ui.components.KadrajButton
import com.omersusin.kadraj.ui.components.Wordmark
import com.omersusin.kadraj.ui.components.pressable
import com.omersusin.kadraj.ui.theme.Kadraj
import kotlin.math.min

@Composable
fun EmptyState(
    onPickImages: () -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
) {
    val haptics = LocalHapticFeedback.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val compact = maxHeight < 700.dp
        val heroSize = if (maxWidth < 360.dp) 39.sp else 45.sp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp),
        ) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Wordmark()
                Spacer(Modifier.weight(1f))
                BasicText(
                    text = "ISSUE 01",
                    style = Kadraj.Type.LabelSmall.copy(
                        color = Kadraj.MutedDim,
                        letterSpacing = 1.8.sp,
                    ),
                )
                Spacer(Modifier.width(12.dp))
                CircleIconButton(
                    glyph = Glyph.Info,
                    onClick = onInfo,
                    size = 36.dp,
                    tint = Kadraj.Muted,
                    background = Color.Transparent,
                    border = Kadraj.Line,
                    contentDescription = "About Kadraj",
                )
            }

            Spacer(Modifier.height(if (compact) 8.dp else 18.dp))
            CollageArtwork(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 240.dp else 294.dp),
            )
            Spacer(Modifier.height(if (compact) 12.dp else 20.dp))

            BasicText(
                text = "SMART COLLAGE STUDIO",
                style = Kadraj.Type.Label.copy(
                    color = Kadraj.TerracottaSoft,
                    letterSpacing = 2.1.sp,
                ),
            )
            Spacer(Modifier.height(12.dp))
            BasicText(
                text = "Your frames,",
                style = Kadraj.Type.Hero.copy(
                    color = Kadraj.Paper,
                    fontSize = heroSize,
                    lineHeight = (heroSize.value + 2).sp,
                ),
            )
            BasicText(
                text = "in one frame.",
                style = Kadraj.Type.HeroItalic.copy(
                    color = Kadraj.TerracottaSoft,
                    fontSize = heroSize,
                    lineHeight = (heroSize.value + 2).sp,
                ),
            )
            Spacer(Modifier.height(13.dp))
            BasicText(
                text = "Choose phone and desktop captures. Kadraj reads every frame's ratio " +
                    "and builds the composition around it; you add the final touch.",
                modifier = Modifier.fillMaxWidth(0.96f),
                style = Kadraj.Type.Body.copy(color = Kadraj.Muted),
            )
            Spacer(Modifier.height(22.dp))

            KadrajButton(
                label = "Choose photos",
                onClick = onPickImages,
                modifier = Modifier.fillMaxWidth(),
                leadingGlyph = Glyph.Image,
                trailingGlyph = Glyph.ArrowRight,
            )
            Spacer(Modifier.height(11.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlyphIcon(
                    glyph = Glyph.Lock,
                    modifier = Modifier.size(11.dp),
                    tint = Kadraj.MutedDim,
                    strokeWidth = 1.3.dp,
                )
                Spacer(Modifier.width(6.dp))
                BasicText(
                    text = "Processed on device  ·  Up to ${EditorViewModel.MAX_IMAGES} photos",
                    style = Kadraj.Type.LabelSmall.copy(
                        color = Kadraj.MutedDim,
                        letterSpacing = 0.8.sp,
                    ),
                )
            }
            if (canUndo || canRedo) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (canUndo) {
                        HistoryAction(
                            glyph = Glyph.Undo,
                            label = "UNDO",
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onUndo()
                            },
                        )
                    }
                    if (canRedo) {
                        HistoryAction(
                            glyph = Glyph.Redo,
                            label = "REDO",
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onRedo()
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Feature(
                    glyph = Glyph.Phone,
                    title = "RATIO",
                    detail = "aware",
                    modifier = Modifier.weight(1f),
                )
                Feature(
                    glyph = Glyph.Sparkle,
                    title = "TEXTURED",
                    detail = "aesthetic",
                    modifier = Modifier.weight(1f),
                )
                Feature(
                    glyph = Glyph.Download,
                    title = "4K",
                    detail = "export",
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(28.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Kadraj.Line))
            Spacer(Modifier.height(13.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = "KADRAJ ${BuildConfig.VERSION_NAME}",
                    style = Kadraj.Type.LabelSmall.copy(
                        color = Kadraj.MutedDim,
                        letterSpacing = 1.5.sp,
                    ),
                )
                Spacer(Modifier.weight(1f))
                BasicText(
                    text = "ENGLISH  ·  ON-DEVICE",
                    style = Kadraj.Type.LabelSmall.copy(
                        color = Kadraj.MutedDim,
                        letterSpacing = 1.5.sp,
                    ),
                )
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun HistoryAction(
    glyph: Glyph,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .pressable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        GlyphIcon(
            glyph = glyph,
            modifier = Modifier.size(12.dp),
            tint = Kadraj.Muted,
            strokeWidth = 1.6.dp,
        )
        BasicText(
            text = label,
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.Muted,
                letterSpacing = 1.1.sp,
            ),
        )
    }
}

@Composable
private fun Feature(
    glyph: Glyph,
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        GlyphIcon(
            glyph = glyph,
            modifier = Modifier.size(17.dp),
            tint = Kadraj.TerracottaSoft,
            strokeWidth = 1.45.dp,
        )
        Spacer(Modifier.height(8.dp))
        BasicText(
            text = title,
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.PaperDim,
                letterSpacing = 1.5.sp,
            ),
        )
        BasicText(
            text = detail,
            style = Kadraj.Type.BodySmall.copy(
                color = Kadraj.MutedDim,
                fontSize = 11.5.sp,
            ),
        )
    }
}

@Composable
private fun CollageArtwork(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "cover-float")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cover-float-value",
    )

    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f) return@Canvas
            val bob = (drift - 0.5f) * 10.dp.toPx()
            val center = Offset(width * 0.52f, height * 0.54f)
            val glowRadius = min(width, height) * 0.5f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Kadraj.Terracotta.copy(alpha = 0.18f),
                        Kadraj.Terracotta.copy(alpha = 0.05f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = glowRadius,
                ),
                radius = glowRadius,
                center = center,
            )
            drawCircle(
                color = Kadraj.Paper.copy(alpha = 0.075f),
                radius = min(width, height) * 0.42f,
                center = center,
                style = Stroke(width = 1f),
            )
            drawCircle(
                color = Kadraj.Terracotta.copy(alpha = 0.22f),
                radius = min(width, height) * 0.47f,
                center = center,
                style = Stroke(width = 1f),
            )

            drawSpark(
                center = Offset(width * 0.84f, height * 0.17f),
                radius = min(width, height) * 0.035f,
                color = Kadraj.TerracottaSoft.copy(alpha = 0.8f),
            )
            drawSpark(
                center = Offset(width * 0.13f, height * 0.62f),
                radius = min(width, height) * 0.018f,
                color = Kadraj.Paper.copy(alpha = 0.45f),
            )

            val phoneWidth = min(width * 0.44f, height * 0.52f)
            val phoneHeight = phoneWidth * 1.48f
            val phoneCenter = Offset(width * 0.35f, height * 0.46f + bob)
            drawPhoneCard(
                center = phoneCenter,
                width = phoneWidth,
                height = phoneHeight,
                rotation = -9.5f,
            )

            val desktopWidth = min(width * 0.76f, height * 0.82f)
            val desktopHeight = desktopWidth * 0.64f
            drawDesktopCard(
                center = Offset(width * 0.55f, height * 0.64f + bob * 0.55f),
                width = desktopWidth,
                height = desktopHeight,
                rotation = 3.4f,
            )

            val swatchWidth = min(width * 0.17f, height * 0.25f)
            drawSwatchCard(
                center = Offset(width * 0.8f, height * 0.34f - bob * 0.7f),
                width = swatchWidth,
                height = swatchWidth * 1.32f,
                rotation = 9f,
            )
        }

        BasicText(
            text = "01 / PHONE",
            modifier = Modifier.align(Alignment.TopStart).padding(start = 6.dp, top = 18.dp),
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.MutedDim,
                letterSpacing = 1.4.sp,
            ),
        )
        BasicText(
            text = "02 / DESKTOP",
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 14.dp),
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.MutedDim,
                letterSpacing = 1.4.sp,
            ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 0.dp, top = 30.dp)
                .width(18.dp)
                .height(1.dp)
                .background(Kadraj.Terracotta.copy(alpha = 0.6f)),
        )
    }
}

private fun DrawScope.drawSpark(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticBezierTo(
            center.x + radius * 0.18f,
            center.y - radius * 0.18f,
            center.x + radius,
            center.y,
        )
        quadraticBezierTo(
            center.x + radius * 0.18f,
            center.y + radius * 0.18f,
            center.x,
            center.y + radius,
        )
        quadraticBezierTo(
            center.x - radius * 0.18f,
            center.y + radius * 0.18f,
            center.x - radius,
            center.y,
        )
        quadraticBezierTo(
            center.x - radius * 0.18f,
            center.y - radius * 0.18f,
            center.x,
            center.y - radius,
        )
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawPhoneCard(
    center: Offset,
    width: Float,
    height: Float,
    rotation: Float,
) {
    val left = center.x - width / 2f
    val top = center.y - height / 2f
    val radius = width * 0.095f

    rotate(rotation, pivot = center) {
        drawCardShadow(left, top, width, height, radius)
        val rect = Rect(left, top, left + width, top + height)
        val clip = Path().apply {
            addRoundRect(RoundRect(rect, CornerRadius(radius)))
        }
        clipPath(clip) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFF1E9DB), Color(0xFFD9C9B0)),
                    start = Offset(left, top),
                    end = Offset(left + width, top + height),
                ),
                topLeft = Offset(left, top),
                size = Size(width, height),
            )

            val pad = width * 0.075f
            drawRoundRect(
                color = Kadraj.Ink.copy(alpha = 0.12f),
                topLeft = Offset(left + pad, top + pad),
                size = Size(width * 0.23f, height * 0.008f),
                cornerRadius = CornerRadius(2f),
            )
            drawCircle(
                color = Kadraj.Terracotta.copy(alpha = 0.9f),
                radius = width * 0.018f,
                center = Offset(left + width - pad, top + pad),
            )

            val photoTop = top + height * 0.11f
            val photoHeight = height * 0.5f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFB76549), Color(0xFF6E3A2B)),
                    startY = photoTop,
                    endY = photoTop + photoHeight,
                ),
                topLeft = Offset(left + pad, photoTop),
                size = Size(width - pad * 2f, photoHeight),
                cornerRadius = CornerRadius(width * 0.035f),
            )
            drawCircle(
                color = Color(0xFFE9C79A).copy(alpha = 0.9f),
                radius = width * 0.075f,
                center = Offset(left + width * 0.63f, photoTop + photoHeight * 0.34f),
            )
            val hills = Path().apply {
                moveTo(left + pad, photoTop + photoHeight * 0.8f)
                lineTo(left + width * 0.38f, photoTop + photoHeight * 0.47f)
                lineTo(left + width * 0.55f, photoTop + photoHeight * 0.74f)
                lineTo(left + width * 0.7f, photoTop + photoHeight * 0.56f)
                lineTo(left + width - pad, photoTop + photoHeight * 0.82f)
                lineTo(left + width - pad, photoTop + photoHeight)
                lineTo(left + pad, photoTop + photoHeight)
                close()
            }
            drawPath(hills, Color(0xFF3F2A22).copy(alpha = 0.75f))

            var lineY = top + height * 0.68f
            repeat(3) { index ->
                drawRoundRect(
                    color = Kadraj.Ink.copy(alpha = if (index == 0) 0.35f else 0.18f),
                    topLeft = Offset(left + pad, lineY),
                    size = Size(
                        width * when (index) {
                            0 -> 0.54f
                            1 -> 0.72f
                            else -> 0.4f
                        },
                        2.2f.dp.toPx(),
                    ),
                    cornerRadius = CornerRadius(2f),
                )
                lineY += height * 0.048f
            }

            drawCircle(
                color = Kadraj.Ink.copy(alpha = 0.14f),
                radius = width * 0.026f,
                center = Offset(left + width / 2f, top + height * 0.94f),
            )
        }
        drawRoundRect(
            color = Kadraj.Paper.copy(alpha = 0.32f),
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(radius),
            style = Stroke(width = 1f),
        )
    }
}

private fun DrawScope.drawDesktopCard(
    center: Offset,
    width: Float,
    height: Float,
    rotation: Float,
) {
    val left = center.x - width / 2f
    val top = center.y - height / 2f
    val radius = width * 0.045f

    rotate(rotation, pivot = center) {
        drawCardShadow(left, top, width, height, radius)
        val rect = Rect(left, top, left + width, top + height)
        val clip = Path().apply {
            addRoundRect(RoundRect(rect, CornerRadius(radius)))
        }
        clipPath(clip) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF211C16), Color(0xFF100E0B)),
                    start = Offset(left, top),
                    end = Offset(left + width, top + height),
                ),
                topLeft = Offset(left, top),
                size = Size(width, height),
            )

            val pad = width * 0.038f
            val toolbarHeight = height * 0.16f
            drawRoundRect(
                color = Kadraj.Paper.copy(alpha = 0.06f),
                topLeft = Offset(left + pad, top + pad),
                size = Size(width - pad * 2f, toolbarHeight),
                cornerRadius = CornerRadius(toolbarHeight * 0.35f),
            )
            val dotRadius = width * 0.009f
            listOf(Kadraj.Terracotta, Kadraj.Gold, Kadraj.Moss).forEachIndexed { index, color ->
                drawCircle(
                    color = color.copy(alpha = 0.9f),
                    radius = dotRadius,
                    center = Offset(
                        left + pad + toolbarHeight * 0.42f + index * dotRadius * 3.2f,
                        top + pad + toolbarHeight / 2f,
                    ),
                )
            }
            drawRoundRect(
                color = Kadraj.Paper.copy(alpha = 0.15f),
                topLeft = Offset(left + width * 0.42f, top + pad + toolbarHeight * 0.38f),
                size = Size(width * 0.22f, 1.5f.dp.toPx()),
                cornerRadius = CornerRadius(2f),
            )

            val contentTop = top + pad + toolbarHeight + pad
            val contentHeight = height - (contentTop - top) - pad
            val bigWidth = (width - pad * 3f) * 0.58f
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFD8C7AC), Color(0xFFB6A186)),
                    start = Offset(left + pad, contentTop),
                    end = Offset(left + pad + bigWidth, contentTop + contentHeight),
                ),
                topLeft = Offset(left + pad, contentTop),
                size = Size(bigWidth, contentHeight),
                cornerRadius = CornerRadius(width * 0.018f),
            )
            drawCircle(
                color = Color(0xFF7B4636).copy(alpha = 0.8f),
                radius = width * 0.04f,
                center = Offset(
                    left + pad + bigWidth * 0.34f,
                    contentTop + contentHeight * 0.3f,
                ),
            )
            val desktopHills = Path().apply {
                moveTo(left + pad, contentTop + contentHeight)
                lineTo(left + pad, contentTop + contentHeight * 0.67f)
                lineTo(left + pad + bigWidth * 0.34f, contentTop + contentHeight * 0.45f)
                lineTo(left + pad + bigWidth * 0.62f, contentTop + contentHeight * 0.72f)
                lineTo(left + pad + bigWidth, contentTop + contentHeight * 0.55f)
                lineTo(left + pad + bigWidth, contentTop + contentHeight)
                close()
            }
            drawPath(desktopHills, Color(0xFF4A3328).copy(alpha = 0.78f))

            val smallLeft = left + pad * 2f + bigWidth
            val smallWidth = width - (smallLeft - left) - pad
            drawRoundRect(
                color = Kadraj.Terracotta.copy(alpha = 0.88f),
                topLeft = Offset(smallLeft, contentTop),
                size = Size(smallWidth, contentHeight * 0.46f),
                cornerRadius = CornerRadius(width * 0.018f),
            )
            drawRoundRect(
                brush = Brush.linearGradient(
                    listOf(Kadraj.Moss.copy(alpha = 0.85f), Color(0xFF3E4A38)),
                ),
                topLeft = Offset(smallLeft, contentTop + contentHeight * 0.53f),
                size = Size(smallWidth, contentHeight * 0.47f),
                cornerRadius = CornerRadius(width * 0.018f),
            )
        }
        drawRoundRect(
            color = Kadraj.Paper.copy(alpha = 0.35f),
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(radius),
            style = Stroke(width = 1f),
        )
    }
}

private fun DrawScope.drawSwatchCard(
    center: Offset,
    width: Float,
    height: Float,
    rotation: Float,
) {
    val left = center.x - width / 2f
    val top = center.y - height / 2f
    val radius = width * 0.12f
    rotate(rotation, pivot = center) {
        drawCardShadow(left, top, width, height, radius)
        val rect = Rect(left, top, left + width, top + height)
        val clip = Path().apply {
            addRoundRect(RoundRect(rect, CornerRadius(radius)))
        }
        clipPath(clip) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF2A241C), Color(0xFF15120E)),
                ),
                topLeft = Offset(left, top),
                size = Size(width, height),
            )
            val swatchSize = width * 0.28f
            listOf(
                Kadraj.Terracotta,
                Kadraj.Gold,
                Kadraj.Moss,
                Kadraj.PaperDim,
            ).forEachIndexed { index, color ->
                val x = left + width * 0.18f +
                    (index % 2) * (swatchSize + width * 0.1f)
                val y = top + height * 0.15f +
                    (index / 2) * (swatchSize + height * 0.07f)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(swatchSize, swatchSize),
                    cornerRadius = CornerRadius(swatchSize * 0.25f),
                )
            }
        }
        drawRoundRect(
            color = Kadraj.Paper.copy(alpha = 0.35f),
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(radius),
            style = Stroke(width = 1f),
        )
    }
}

private fun DrawScope.drawCardShadow(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    radius: Float,
) {
    val offset = 9.dp.toPx()
    for (layer in 3 downTo 1) {
        val grow = layer * 2.5.dp.toPx()
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.14f / layer),
            topLeft = Offset(left - grow, top - grow + offset),
            size = Size(width + grow * 2f, height + grow * 2f),
            cornerRadius = CornerRadius(radius + grow),
        )
    }
}
