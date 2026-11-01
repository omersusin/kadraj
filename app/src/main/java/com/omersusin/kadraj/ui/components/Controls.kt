package com.omersusin.kadraj.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
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
    val shape = RoundedCornerShape(17.dp)
    val background = when (tone) {
        ButtonTone.Accent -> Brush.horizontalGradient(
            listOf(Kadraj.TerracottaSoft, Kadraj.Terracotta),
        )
        ButtonTone.Paper -> Brush.horizontalGradient(listOf(Kadraj.Paper, Kadraj.PaperDim))
        ButtonTone.Quiet -> Brush.horizontalGradient(
            listOf(Kadraj.InkSoft, Kadraj.InkSoft),
        )
    }
    val contentColor = when (tone) {
        ButtonTone.Accent, ButtonTone.Paper -> Kadraj.Ink
        ButtonTone.Quiet -> Kadraj.Paper
    }
    val borderColor = if (tone == ButtonTone.Quiet) Kadraj.LineStrong else Color.Transparent
    val hapticType = HapticFeedbackType.TextHandleMove

    Box(
        modifier = modifier
            .height(54.dp)
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
                text = label.uppercase(Locale.forLanguageTag("tr")),
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
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .size(size)
            .then(semanticsModifier)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, border, CircleShape)
            .pressable(enabled = enabled) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
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
    val background = if (selected) Kadraj.Paper else Color.Transparent
    val content = if (selected) Kadraj.Ink else Kadraj.Muted
    Box(
        modifier = modifier
            .height(30.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, if (selected) Color.Transparent else Kadraj.Line, shape)
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
    val shape = RoundedCornerShape(14.dp)
    val content = if (selected) Kadraj.Paper else Kadraj.Muted
    Column(
        modifier = modifier
            .width(66.dp)
            .height(58.dp)
            .clip(shape)
            .background(if (selected) Kadraj.InkSoft else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Kadraj.Terracotta.copy(alpha = 0.62f) else Kadraj.Line,
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
            tint = if (selected) Kadraj.TerracottaSoft else Kadraj.Muted,
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
            .clip(shape)
            .background(if (selected) Kadraj.InkSoft else Color.Transparent)
            .border(
                1.dp,
                if (selected) Kadraj.Terracotta.copy(alpha = 0.55f) else Kadraj.Line,
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
                .size(6.dp)
                .clip(CircleShape)
                .background(if (selected) Kadraj.TerracottaSoft else Kadraj.MutedDim),
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
    Column(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
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
            tint = if (selected) Kadraj.TerracottaSoft else Kadraj.MutedDim,
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
    val active = Kadraj.Terracotta
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
                        color = active,
                        start = androidx.compose.ui.geometry.Offset(startX, centerY),
                        end = androidx.compose.ui.geometry.Offset(knobX, centerY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
                drawCircle(
                    color = active.copy(alpha = 0.22f),
                    radius = knobRadiusPx + 2.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(knobX, centerY),
                )
                drawCircle(
                    color = Kadraj.Paper,
                    radius = 4.6.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(knobX, centerY),
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
                .size(5.dp)
                .clip(CircleShape)
                .background(Kadraj.Terracotta),
        )
    }
}

@Composable
fun NoticePill(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Kadraj.InkLift)
            .border(1.dp, Kadraj.Line, RoundedCornerShape(18.dp))
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
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(Kadraj.InkRaised)
                        .border(
                            width = 1.dp,
                            color = Kadraj.LineStrong,
                            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
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
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Kadraj.MutedDim.copy(alpha = 0.55f)),
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
    Template.AUTO -> "OTOMATİK"
    Template.GRID -> "IZGARA"
    Template.FLOW -> "AKIŞ"
    Template.HERO -> "VİTRİN"
    Template.STRIP -> "ŞERİT"
    Template.PILE -> "YIĞIN"
}

fun aspectLabel(aspect: AspectPreset): String = when (aspect) {
    AspectPreset.AUTO -> "Oto"
    AspectPreset.SQUARE -> "1:1"
    AspectPreset.FOUR_FIVE -> "4:5"
    AspectPreset.STORY -> "9:16"
    AspectPreset.WIDE -> "16:9"
    AspectPreset.DESKTOP -> "16:10"
    AspectPreset.CLASSIC -> "3:2"
}

fun ratioLabel(value: Float): String {
    val known = listOf(
        1f to "1:1",
        0.8f to "4:5",
        9f / 16f to "9:16",
        16f / 9f to "16:9",
        1.6f to "16:10",
        1.5f to "3:2",
        4f / 3f to "4:3",
        3f / 4f to "3:4",
    )
    return known.firstOrNull { (ratio, _) ->
        kotlin.math.abs(ratio - value) / ratio < 0.035f
    }?.second ?: String.format(Locale.US, "%.2f:1", value)
}
