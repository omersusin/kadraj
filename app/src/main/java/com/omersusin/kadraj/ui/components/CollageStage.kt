package com.omersusin.kadraj.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omersusin.kadraj.CollageImage
import com.omersusin.kadraj.collage.CollageCell
import com.omersusin.kadraj.collage.CollageStyle
import com.omersusin.kadraj.collage.LayoutResult
import com.omersusin.kadraj.image.ImageLoader
import com.omersusin.kadraj.render.CaptionTypefaces
import com.omersusin.kadraj.render.CollagePainter
import com.omersusin.kadraj.render.ImageProvider
import com.omersusin.kadraj.render.ImageRenderState
import com.omersusin.kadraj.render.ProvidedImage
import com.omersusin.kadraj.ui.theme.Kadraj

@Composable
fun CollageStage(
    layout: LayoutResult,
    style: CollageStyle,
    images: List<CollageImage>,
    selectedId: Long?,
    loader: ImageLoader,
    onSelect: (Long?) -> Unit,
    onFocus: (Long, Float, Float) -> Unit,
    onToggleFit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    guides: Boolean = false,
) {
    val previews = rememberPreviewLoadStates(loader, images)
    val revision = previews.revision.value
    val loading = previews.loading.value
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val captionTypeface = remember(context, style.caption.font) {
        CaptionTypefaces.load(context, style.caption.font)
    }
    val transition = rememberInfiniteTransition(label = "stage-aura")
    val auraRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "stage-aura-rotation",
    )
    val guideAlpha by animateFloatAsState(
        targetValue = if (guides) 1f else 0f,
        animationSpec = tween(220),
        label = "guide-alpha",
    )

    val currentLayout by rememberUpdatedState(layout)
    val currentImages by rememberUpdatedState(images)
    val currentSelect by rememberUpdatedState(onSelect)
    val currentFocus by rememberUpdatedState(onFocus)
    val currentToggleFit by rememberUpdatedState(onToggleFit)

    val imageKey = remember(images) {
        images.map { it.id to it.uri }
    }
    val uriById = remember(imageKey) { imageKey.toMap() }
    val imageStates = remember(images) {
        images.associate {
            it.id to ImageRenderState(
                fit = it.fit,
                focusX = it.focusX,
                focusY = it.focusY,
                look = it.look,
                lookAmount = it.lookAmount,
                zoom = it.zoom,
                tilt = it.tilt,
                flipX = it.flipX,
                flipY = it.flipY,
                brightness = it.brightness,
                contrast = it.contrast,
                saturation = it.saturation,
                warmth = it.warmth,
            )
        }
    }
    val provider = remember(uriById) {
        ImageProvider { imageId, _, _ ->
            uriById[imageId]?.let { uri ->
                loader.cached(uri, COLLAGE_PREVIEW_DIMENSION)?.let { bitmap ->
                    ProvidedImage(bitmap, recycleAfterUse = false)
                }
            }
        }
    }
    val shape = RoundedCornerShape(3.dp)

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val safeAspect = layout.aspect.coerceIn(0.25f, 4f)
        var canvasWidth = maxWidth
        var canvasHeight = canvasWidth / safeAspect
        if (canvasHeight > maxHeight) {
            canvasHeight = maxHeight
            canvasWidth = canvasHeight * safeAspect
        }

        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = auraRotation },
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val glowRadius = size.minDimension * 0.7f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Kadraj.Peach.copy(alpha = 0.13f),
                        Kadraj.Terracotta.copy(alpha = 0.07f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = glowRadius,
                ),
                radius = glowRadius,
                center = center,
            )

            val orbitRadius = size.minDimension * 0.46f
            val orbitTopLeft = Offset(
                center.x - orbitRadius,
                center.y - orbitRadius,
            )
            val orbitSize = androidx.compose.ui.geometry.Size(orbitRadius * 2f, orbitRadius * 2f)
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        Color.Transparent,
                        Kadraj.Peach.copy(alpha = 0.32f),
                        Kadraj.Violet.copy(alpha = 0.24f),
                        Color.Transparent,
                    ),
                    center = center,
                ),
                startAngle = 10f,
                sweepAngle = 118f,
                useCenter = false,
                topLeft = orbitTopLeft,
                size = orbitSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 1.3.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                ),
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        Color.Transparent,
                        Kadraj.Acid.copy(alpha = 0.26f),
                        Color.Transparent,
                    ),
                    center = center,
                ),
                startAngle = 196f,
                sweepAngle = 72f,
                useCenter = false,
                topLeft = orbitTopLeft,
                size = orbitSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 1.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                ),
            )

            val dotCenter = Offset(center.x + orbitRadius, center.y)
            drawCircle(
                color = Kadraj.Peach.copy(alpha = 0.55f),
                radius = 2.2.dp.toPx(),
                center = dotCenter,
            )
            drawCircle(
                color = Kadraj.Peach.copy(alpha = 0.12f),
                radius = 7.dp.toPx(),
                center = dotCenter,
            )
        }

        if (canvasWidth > 0.dp && canvasHeight > 0.dp) {
            Box(
                modifier = Modifier
                    .size(canvasWidth, canvasHeight)
                    .shadow(
                        elevation = 22.dp,
                        shape = shape,
                        clip = false,
                        ambientColor = Color.Black,
                        spotColor = Color.Black,
                    )
                    .clip(shape)
                    .background(Color.Black)
                    .border(1.dp, Kadraj.Line, shape)
                    .semantics { contentDescription = "Collage preview" },
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(layout) {
                            fun hit(offset: Offset): CollageCell? {
                                if (size.width <= 0 || size.height <= 0) return null
                                val nx = offset.x / size.width.toFloat()
                                val ny = offset.y / size.height.toFloat()
                                return currentLayout.cells
                                    .sortedByDescending { it.z }
                                    .firstOrNull { cell ->
                                        nx >= cell.rect.left &&
                                            nx <= cell.rect.right &&
                                            ny >= cell.rect.top &&
                                            ny <= cell.rect.bottom
                                    }
                            }

                            detectTapGestures(
                                onTap = { offset ->
                                    currentSelect(hit(offset)?.imageId)
                                },
                                onDoubleTap = { offset ->
                                    hit(offset)?.let { cell ->
                                        haptics.performHapticFeedback(
                                            HapticFeedbackType.TextHandleMove,
                                        )
                                        currentSelect(cell.imageId)
                                        currentToggleFit(cell.imageId)
                                    }
                                },
                            )
                        }
                        .pointerInput(layout) {
                            fun hit(offset: Offset): CollageCell? {
                                if (size.width <= 0 || size.height <= 0) return null
                                val nx = offset.x / size.width.toFloat()
                                val ny = offset.y / size.height.toFloat()
                                return currentLayout.cells
                                    .sortedByDescending { it.z }
                                    .firstOrNull { cell ->
                                        nx >= cell.rect.left &&
                                            nx <= cell.rect.right &&
                                            ny >= cell.rect.top &&
                                            ny <= cell.rect.bottom
                                    }
                            }

                            var activeId: Long? = null
                            var focusX = 0.5f
                            var focusY = 0.5f
                            var activeWidth = 1f
                            var activeHeight = 1f

                            detectDragGestures(
                                onDragStart = { offset ->
                                    val cell = hit(offset)
                                    activeId = cell?.imageId
                                    if (cell != null) {
                                        currentSelect(cell.imageId)
                                        val image = currentImages.firstOrNull {
                                            it.id == cell.imageId
                                        }
                                        focusX = image?.focusX ?: 0.5f
                                        focusY = image?.focusY ?: 0.5f
                                        activeWidth = (cell.rect.width * size.width)
                                            .coerceAtLeast(1f)
                                        activeHeight = (cell.rect.height * size.height)
                                            .coerceAtLeast(1f)
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    val id = activeId
                                    if (id != null) {
                                        change.consume()
                                        focusX = (focusX - dragAmount.x / activeWidth)
                                            .coerceIn(0f, 1f)
                                        focusY = (focusY - dragAmount.y / activeHeight)
                                            .coerceIn(0f, 1f)
                                        currentFocus(id, focusX, focusY)
                                    }
                                },
                                onDragEnd = { activeId = null },
                                onDragCancel = { activeId = null },
                            )
                        },
                ) {
                    @Suppress("UNUSED_EXPRESSION")
                    revision

                    drawIntoCanvas { canvas ->
                        CollagePainter.draw(
                            canvas = canvas.nativeCanvas,
                            width = size.width.toInt(),
                            height = size.height.toInt(),
                            layout = layout,
                            style = style,
                            provider = provider,
                            imageStates = imageStates,
                            selectedId = selectedId,
                            selectionColor = 0xFFD17252.toInt(),
                            captionTypeface = captionTypeface,
                        )
                    }

                    if (guideAlpha > 0.01f) {
                        val third = 1f / 3f
                        val lineColor = Color.White.copy(alpha = 0.34f * guideAlpha)
                        val shadowColor = Color.Black.copy(alpha = 0.22f * guideAlpha)
                        listOf(third, third * 2f).forEach { fraction ->
                            val x = size.width * fraction
                            drawLine(
                                color = shadowColor,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 2.5f,
                            )
                            drawLine(
                                color = lineColor,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1f,
                            )
                            val y = size.height * fraction
                            drawLine(
                                color = shadowColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 2.5f,
                            )
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f,
                            )
                        }
                        drawCircle(
                            color = lineColor,
                            radius = 3.5.dp.toPx(),
                            center = Offset(size.width / 2f, size.height / 2f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 1f,
                            ),
                        )
                    }
                }

                if (loading) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Kadraj.Ink.copy(alpha = 0.86f))
                            .border(1.dp, Kadraj.Line, RoundedCornerShape(50))
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        KadrajSpinner(diameter = 13.dp)
                        BasicText(
                            text = "PREPARING PHOTOS",
                            style = Kadraj.Type.LabelSmall.copy(
                                color = Kadraj.PaperDim,
                                fontSize = 8.sp,
                                letterSpacing = 1.1.sp,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CollagePreview(
    layout: LayoutResult,
    style: CollageStyle,
    images: List<CollageImage>,
    loader: ImageLoader,
    modifier: Modifier = Modifier,
) {
    val previews = rememberPreviewLoadStates(loader, images)
    val revision = previews.revision.value
    val imageKey = remember(images) { images.map { it.id to it.uri } }
    val uriById = remember(imageKey) { imageKey.toMap() }
    val context = LocalContext.current
    val captionTypeface = remember(context, style.caption.font) {
        CaptionTypefaces.load(context, style.caption.font)
    }
    val imageStates = remember(images) {
        images.associate {
            it.id to ImageRenderState(
                fit = it.fit,
                focusX = it.focusX,
                focusY = it.focusY,
                look = it.look,
                lookAmount = it.lookAmount,
                zoom = it.zoom,
                tilt = it.tilt,
                flipX = it.flipX,
                flipY = it.flipY,
                brightness = it.brightness,
                contrast = it.contrast,
                saturation = it.saturation,
                warmth = it.warmth,
            )
        }
    }
    val provider = remember(uriById) {
        ImageProvider { imageId, _, _ ->
            uriById[imageId]?.let { uri ->
                loader.cached(uri, COLLAGE_PREVIEW_DIMENSION)?.let { bitmap ->
                    ProvidedImage(bitmap, recycleAfterUse = false)
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val safeAspect = layout.aspect.coerceIn(0.3f, 3.2f)
        var canvasWidth = maxWidth
        var canvasHeight = canvasWidth / safeAspect
        if (canvasHeight > maxHeight) {
            canvasHeight = maxHeight
            canvasWidth = canvasHeight * safeAspect
        }
        if (canvasWidth > 0.dp && canvasHeight > 0.dp) {
            Canvas(
                modifier = Modifier
                    .size(canvasWidth, canvasHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .border(1.dp, Kadraj.Line, RoundedCornerShape(3.dp)),
            ) {
                @Suppress("UNUSED_EXPRESSION")
                revision
                drawIntoCanvas { canvas ->
                    CollagePainter.draw(
                        canvas = canvas.nativeCanvas,
                        width = size.width.toInt(),
                        height = size.height.toInt(),
                        layout = layout,
                        style = style,
                        provider = provider,
                        imageStates = imageStates,
                        captionTypeface = captionTypeface,
                    )
                }
            }
        }
    }
}
