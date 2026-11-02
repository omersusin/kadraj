package com.omersusin.kadraj.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omersusin.kadraj.collage.AspectPreset
import com.omersusin.kadraj.collage.Template
import com.omersusin.kadraj.ui.theme.Kadraj
import java.util.Locale
import kotlin.math.roundToInt

fun Modifier.pressable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "press-scale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
        alpha = if (enabled) 1f else 0.36f
    }.clickable(
        interactionSource = interaction,
        indication = null,
        enabled = enabled,
        role = Role.Button,
        onClick = onClick,
    )
}

enum class ButtonTone {
    Accent,
    Paper,
    Quiet,
}

@Composable
fun KadrajButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: ButtonTone = ButtonTone.Accent,
    leadingGlyph: Glyph? = null,
    trailingGlyph: Glyph? = null,
    enabled: Boolean = true,
) {
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(19.dp)
    val background = when (tone) {
        ButtonTone.Accent -> Brush.linearGradient(
            listOf(Kadraj.Peach, Kadraj.TerracottaSoft, Kadraj.Terracotta),
        )
        ButtonTone.Paper -> Brush.linearGradient(
            listOf(Color(0xFFFFFFFF), Kadraj.Paper, Kadraj.PaperDim),
        )
        ButtonTone.Quiet -> Brush.verticalGradient(
            listOf(Kadraj.InkLift, Kadraj.InkSoft),
        )
    }
    val contentColor = when (tone) {
        ButtonTone.Accent, ButtonTone.Paper -> Kadraj.Ink
        ButtonTone.Quiet -> Kadraj.Paper
    }
    val borderColor = when (tone) {
        ButtonTone.Quiet -> Kadraj.LineStrong
        else -> Color.White.copy(alpha = 0.2f)
    }
    val hapticType = HapticFeedbackType.TextHandleMove

    Box(
        modifier = modifier
            .height(54.dp)
            .shadow(
                elevation = if (tone == ButtonTone.Accent) 18.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.65f),
                spotColor = Kadraj.Terracotta.copy(alpha = 0.45f),
            )
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .pressable(enabled = enabled) {
                haptics.performHapticFeedback(hapticType)
                onClick()
            }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (tone == ButtonTone.Accent) 0.2f else 0.08f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (leadingGlyph != null) {
                GlyphIcon(
                    glyph = leadingGlyph,
                    modifier = Modifier.size(17.dp),
                    tint = contentColor,
                    strokeWidth = 1.7.dp,
                )
            }
            BasicText(
                text = label.uppercase(Locale.ENGLISH),
                style = Kadraj.Type.Button.copy(color = contentColor),
            )
            if (trailingGlyph != null) {
                Spacer(Modifier.size(2.dp))
                GlyphIcon(
                    glyph = trailingGlyph,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor,
                    strokeWidth = 1.7.dp,
                )
            }
        }
    }
}

@Composable
fun CircleIconButton(
    glyph: Glyph,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    tint: Color = Kadraj.Paper,
    background: Color = Kadraj.InkSoft,
    border: Color = Kadraj.Line,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    val haptics = LocalHapticFeedback.current
    val hasFill = background.alpha > 0.04f
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .size(size)
            .then(semanticsModifier)
            .shadow(
                elevation = if (hasFill) 9.dp else 0.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.38f),
                spotColor = Kadraj.Terracotta.copy(alpha = 0.28f),
            )
            .clip(CircleShape)
            .background(background)
            .border(1.dp, border, CircleShape)
            .pressable(enabled = enabled) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        if (hasFill) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.13f), Color.Transparent),
                        ),
                    ),
            )
        }
        GlyphIcon(
            glyph = glyph,
            modifier = Modifier.size(size * 0.42f),
            tint = tint,
            strokeWidth = 1.6.dp,
        )
    }
}

@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(50)
    val background = if (selected) {
        Brush.horizontalGradient(listOf(Color(0xFFFFFFFF), Kadraj.Paper, Kadraj.PaperDim))
    } else {
        Brush.horizontalGradient(listOf(Kadraj.Glass, Color.Transparent))
    }
    val content = if (selected) Kadraj.Ink else Kadraj.PaperDim
    Box(
        modifier = modifier
            .height(30.dp)
            .shadow(
                elevation = if (selected) 10.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.5f),
                spotColor = Kadraj.Terracotta.copy(alpha = 0.35f),
            )
            .clip(shape)
            .background(background)
            .border(
                1.dp,
                if (selected) Color.White.copy(alpha = 0.38f) else Kadraj.Line,
                shape,
            )
            .pressable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = Kadraj.Type.Chip.copy(color = content, fontSize = 11.5.sp),
        )
    }
}

@Composable
fun TemplateChip(
    template: Template,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(15.dp)
    val content = if (selected) Kadraj.Paper else Kadraj.Muted
    Column(
        modifier = modifier
            .width(66.dp)
            .height(58.dp)
            .shadow(
                elevation = if (selected) 12.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.42f),
                spotColor = Kadraj.Terracotta.copy(alpha = 0.3f),
            )
            .clip(shape)
            .background(
                if (selected) {
                    Brush.verticalGradient(listOf(Kadraj.InkLift, Kadraj.InkSoft))
                } else {
                    Brush.verticalGradient(listOf(Kadraj.Glass, Color.Transparent))
                },
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    Kadraj.TerracottaSoft.copy(alpha = 0.72f)
                } else {
                    Kadraj.Line
                },
                shape = shape,
            )
            .pressable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        GlyphIcon(
            glyph = glyphForTemplate(template),
            modifier = Modifier.size(19.dp),
            tint = if (selected) Kadraj.Peach else Kadraj.Muted,
            strokeWidth = 1.55.dp,
        )
        Spacer(Modifier.height(6.dp))
        BasicText(
            text = templateLabel(template),
            style = Kadraj.Type.LabelSmall.copy(
                color = content,
                fontSize = 8.5.sp,
                letterSpacing = 0.7.sp,
            ),
        )
    }
}

@Composable
fun ToggleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .height(30.dp)
            .shadow(
                elevation = if (selected) 8.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.45f),
                spotColor = Kadraj.Terracotta.copy(alpha = 0.3f),
            )
            .clip(shape)
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        listOf(Kadraj.Terracotta.copy(alpha = 0.24f), Kadraj.InkSoft),
                    )
                } else {
                    Brush.horizontalGradient(listOf(Kadraj.Glass, Color.Transparent))
                },
            )
            .border(
                1.dp,
                if (selected) Kadraj.TerracottaSoft.copy(alpha = 0.62f) else Kadraj.Line,
                shape,
            )
            .pressable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(
                    if (selected) Kadraj.Peach else Kadraj.MutedDim.copy(alpha = 0.7f),
                ),
        )
        BasicText(
            text = label,
            style = Kadraj.Type.Chip.copy(
                color = if (selected) Kadraj.Paper else Kadraj.Muted,
                fontSize = 11.sp,
            ),
        )
    }
}

@Composable
fun DockTab(
    glyph: Glyph,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(17.dp)
    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) 12.dp else 0.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "dock-indicator",
    )
    Column(
        modifier = modifier
            .height(54.dp)
            .shadow(
                elevation = if (selected) 12.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.5f),
                spotColor = Kadraj.Terracotta.copy(alpha = 0.34f),
            )
            .clip(shape)
            .background(
                if (selected) {
                    Brush.verticalGradient(
                        listOf(Kadraj.Terracotta.copy(alpha = 0.26f), Kadraj.InkSoft.copy(alpha = 0.12f)),
                    )
                } else {
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                },
            )
            .pressable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        GlyphIcon(
            glyph = glyph,
            modifier = Modifier.size(18.dp),
            tint = if (selected) Kadraj.Peach else Kadraj.MutedDim,
            strokeWidth = 1.55.dp,
        )
        Spacer(Modifier.height(5.dp))
        BasicText(
            text = label,
            style = Kadraj.Type.LabelSmall.copy(
                color = if (selected) Kadraj.Paper else Kadraj.MutedDim,
                fontSize = 9.sp,
                letterSpacing = 1.2.sp,
            ),
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(2.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    if (selected) {
                        Brush.horizontalGradient(listOf(Kadraj.Peach, Kadraj.Violet))
                    } else {
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    },
                ),
        )
    }
}

@Composable
fun KadrajSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueLabel: String = "${(value * 100f).roundToInt()}%",
) {
    val knobRadius = 7.dp
    val knobRadiusPx = with(LocalDensity.current) { knobRadius.toPx() }
    val active = Kadraj.TerracottaSoft
    val track = Kadraj.LineStrong
    val currentOnChange by rememberUpdatedState(onValueChange)

    Row(
        modifier = modifier.fillMaxWidth().height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = label,
            modifier = Modifier.width(64.dp),
            style = Kadraj.Type.BodySmall.copy(color = Kadraj.Muted, fontSize = 11.5.sp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(30.dp)
                .pointerInput(valueRange) {
                    detectTapGestures { change ->
                        val usable = (size.width - knobRadiusPx * 2f).coerceAtLeast(1f)
                        val fraction = ((change.x - knobRadiusPx) / usable).coerceIn(0f, 1f)
                        currentOnChange(
                            valueRange.start + fraction * (valueRange.endInclusive - valueRange.start),
                        )
                    }
                }
                .pointerInput(valueRange) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val usable = (size.width - knobRadiusPx * 2f).coerceAtLeast(1f)
                        val fraction = ((change.position.x - knobRadiusPx) / usable)
                            .coerceIn(0f, 1f)
                        currentOnChange(
                            valueRange.start + fraction * (valueRange.endInclusive - valueRange.start),
                        )
                    }
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val centerY = size.height / 2f
                val startX = knobRadiusPx
                val endX = size.width - knobRadiusPx
                val fraction = ((value - valueRange.start) /
                    (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
                val knobX = startX + (endX - startX) * fraction
                drawLine(
                    color = track,
                    start = androidx.compose.ui.geometry.Offset(startX, centerY),
                    end = androidx.compose.ui.geometry.Offset(endX, centerY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                if (knobX > startX) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Kadraj.TerracottaSoft, Kadraj.Peach),
                            startX = startX,
                            endX = endX,
                        ),
                        start = androidx.compose.ui.geometry.Offset(startX, centerY),
                        end = androidx.compose.ui.geometry.Offset(knobX, centerY),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
                drawCircle(
                    color = active.copy(alpha = 0.16f),
                    radius = knobRadiusPx + 3.5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(knobX, centerY),
                )
                drawCircle(
                    color = Kadraj.Paper,
                    radius = 4.8.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(knobX, centerY),
                )
                drawCircle(
                    color = active,
                    radius = 4.8.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(knobX, centerY),
                    style = Stroke(width = 1.4.dp.toPx()),
                )
            }
        }
        BasicText(
            text = valueLabel,
            modifier = Modifier.width(40.dp),
            style = Kadraj.Type.Numeric.copy(
                color = Kadraj.PaperDim,
                fontSize = 10.sp,
                textAlign = TextAlign.End,
            ),
        )
    }
}

@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = Kadraj.Line) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}

@Composable
fun Wordmark(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        BasicText(
            text = "Kadraj",
            style = Kadraj.Type.Wordmark.copy(color = Kadraj.Paper),
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = Kadraj.Terracotta,
                    spotColor = Kadraj.Violet,
                )
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Kadraj.Peach, Kadraj.Terracotta, Kadraj.Violet)))
                .border(0.5.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        )
    }
}

@Composable
fun NoticePill(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(19.dp),
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.32f),
                spotColor = Kadraj.Violet.copy(alpha = 0.22f),
            )
            .clip(RoundedCornerShape(19.dp))
            .background(Brush.horizontalGradient(listOf(Kadraj.InkLift, Kadraj.InkRaised)))
            .border(1.dp, Kadraj.LineStrong, RoundedCornerShape(19.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        GlyphIcon(
            glyph = Glyph.Info,
            modifier = Modifier.size(15.dp),
            tint = Kadraj.TerracottaSoft,
            strokeWidth = 1.5.dp,
        )
        BasicText(
            text = message,
            style = Kadraj.Type.BodySmall.copy(color = Kadraj.Paper),
        )
    }
}

@Composable
fun KadrajSpinner(
    modifier: Modifier = Modifier,
    diameter: Dp = 18.dp,
    tint: Color = Kadraj.TerracottaSoft,
) {
    val transition = rememberInfiniteTransition(label = "spinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spinner-angle",
    )
    Canvas(modifier.size(diameter)) {
        val stroke = 1.8.dp.toPx()
        drawArc(
            color = tint,
            startAngle = angle,
            sweepAngle = 255f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            topLeft = androidx.compose.ui.geometry.Offset(stroke / 2f, stroke / 2f),
            size = androidx.compose.ui.geometry.Size(
                size.width - stroke,
                size.height - stroke,
            ),
        )
    }
}

@Composable
fun ModalSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(190)),
            exit = fadeOut(tween(140)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Kadraj.Scrim)
                    .pointerInput(Unit) {
                        detectTapGestures { onDismiss() }
                    },
            )
        }

        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                initialOffsetY = { it },
            ) + fadeIn(tween(190)),
            exit = slideOutVertically(
                animationSpec = tween(210, easing = FastOutSlowInEasing),
                targetOffsetY = { it },
            ) + fadeOut(tween(130)),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 620.dp)
                        .shadow(
                            elevation = 34.dp,
                            shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
                            clip = false,
                            ambientColor = Kadraj.Terracotta.copy(alpha = 0.28f),
                            spotColor = Kadraj.Violet.copy(alpha = 0.2f),
                        )
                        .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF241B12), Kadraj.InkRaised, Color(0xFF110E0A)),
                            ),
                        )
                        .border(
                            width = 1.dp,
                            color = Kadraj.LineStrong,
                            shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
                        )
                        .pointerInput(Unit) { detectTapGestures { } }
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(top = 10.dp, bottom = 26.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(42.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(listOf(Kadraj.Peach, Kadraj.Violet))),
                    )
                    Spacer(Modifier.height(22.dp))
                    content()
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

fun templateLabel(template: Template): String = when (template) {
    Template.AUTO -> "AUTO"
    Template.GRID -> "GRID"
    Template.FLOW -> "FLOW"
    Template.HERO -> "HERO"
    Template.STRIP -> "STRIP"
    Template.PILE -> "STACK"
}

fun aspectLabel(aspect: AspectPreset): String = when (aspect) {
    AspectPreset.AUTO -> "Auto"
    AspectPreset.SQUARE -> "1:1"
    AspectPreset.PORTRAIT -> "3:4"
    AspectPreset.FOUR_FIVE -> "4:5"
    AspectPreset.STORY -> "9:16"
    AspectPreset.TALL -> "2:3"
    AspectPreset.CLASSIC -> "3:2"
    AspectPreset.WIDE -> "16:9"
    AspectPreset.DESKTOP -> "16:10"
    AspectPreset.BANNER -> "2:1"
}

fun ratioLabel(value: Float): String {
    val known = listOf(
        1f to "1:1",
        0.8f to "4:5",
        9f / 16f to "9:16",
        2f / 3f to "2:3",
        16f / 9f to "16:9",
        1.6f to "16:10",
        2f to "2:1",
        1.5f to "3:2",
        4f / 3f to "4:3",
        3f / 4f to "3:4",
    )
    return known.firstOrNull { (ratio, _) ->
        kotlin.math.abs(ratio - value) / ratio < 0.035f
    }?.second ?: String.format(Locale.US, "%.2f:1", value)
}
