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
        targetValue = if (pressed && enabled) 0.978f else 1f,
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
    val shape = Kadraj.Shape.Button
    val background = when (tone) {
        ButtonTone.Accent -> Brush.horizontalGradient(
            listOf(Kadraj.Peach, Kadraj.TerracottaSoft),
        )
        ButtonTone.Paper -> Brush.horizontalGradient(
            if (Kadraj.isPaper) {
                listOf(Kadraj.InkRaised, Kadraj.InkSoft)
            } else {
                listOf(Color.White, Kadraj.Paper)
            },
        )
        ButtonTone.Quiet -> Brush.horizontalGradient(
            listOf(Kadraj.InkLift, Kadraj.InkSoft),
        )
    }
    val contentColor = when (tone) {
        ButtonTone.Accent -> Kadraj.Ink
        ButtonTone.Paper -> if (Kadraj.isPaper) Kadraj.Paper else Kadraj.Ink
        ButtonTone.Quiet -> Kadraj.Paper
    }
    val borderColor = when (tone) {
        ButtonTone.Quiet -> Kadraj.LineStrong
        else -> Kadraj.AccentBorder
    }
    val hapticType = HapticFeedbackType.TextHandleMove

    Box(
        modifier = modifier
            .height(52.dp)
            .shadow(
                elevation = if (
                    tone == ButtonTone.Accent && !Kadraj.isPaper && !Kadraj.isContact
                ) {
                    12.dp
                } else {
                    0.dp
                },
                shape = shape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.35f),
                spotColor = Kadraj.Terracotta.copy(alpha = 0.22f),
            )
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .pressable(enabled = enabled) {
                haptics.performHapticFeedback(hapticType)
                onClick()
            }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
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
                text = if (Kadraj.isContact) label.uppercase(Locale.ROOT) else label,
                style = Kadraj.Type.Button.copy(color = contentColor),
            )
            if (trailingGlyph != null) {
                Spacer(Modifier.size(1.dp))
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
            .clip(Kadraj.Shape.Icon)
            .background(background)
            .border(1.dp, border, Kadraj.Shape.Icon)
            .pressable(enabled = enabled) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        GlyphIcon(
            glyph = glyph,
            modifier = Modifier.size(size * 0.4f),
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
    val shape = Kadraj.Shape.Pill
    val content = if (selected) Kadraj.Ink else Kadraj.PaperDim
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(shape)
            .background(if (selected) Kadraj.Paper else Kadraj.InkLift.copy(alpha = 0.62f))
            .pressable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = Kadraj.Type.Chip.copy(color = content),
        )
    }
}

@Composable
fun TemplateChip(
    template: Template,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    preview: (@Composable () -> Unit)? = null,
) {
    val haptics = LocalHapticFeedback.current
    val shape = Kadraj.Shape.Control
    val content = if (selected) Kadraj.Paper else Kadraj.Muted
    Column(
        modifier = modifier
            .width(76.dp)
            .height(80.dp)
            .clip(shape)
            .background(
                if (selected) {
                    Kadraj.Peach.copy(alpha = 0.16f)
                } else {
                    Kadraj.InkLift.copy(alpha = 0.55f)
                },
            )
            .border(
                width = 1.dp,
                color = if (selected) Kadraj.Peach.copy(alpha = 0.72f) else Color.Transparent,
                shape = shape,
            )
            .pressable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (preview != null) {
            preview()
        } else {
            TemplateMiniature(
                template = template,
                tint = if (selected) Kadraj.Peach else Kadraj.MutedDim,
            )
        }
        Spacer(Modifier.height(6.dp))
        BasicText(
            text = templateLabel(template),
            style = Kadraj.Type.LabelSmall.copy(
                color = content,
                fontSize = 9.5.sp,
                letterSpacing = 0.3.sp,
            ),
        )
    }
}

@Composable
private fun TemplateMiniature(template: Template, tint: Color) {
    Canvas(Modifier.width(36.dp).height(26.dp)) {
        val gap = 2.dp.toPx()
        val radius = 1.7.dp.toPx()
        fun block(left: Float, top: Float, width: Float, height: Float) {
            drawRoundRect(
                color = tint.copy(alpha = 0.86f),
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(width, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius),
            )
        }
        val w = size.width
        val h = size.height
        when (template) {
            Template.AUTO -> {
                block(0f, h * 0.08f, w * 0.56f, h * 0.84f)
                block(w * 0.61f, h * 0.08f, w * 0.39f, h * 0.38f)
                block(w * 0.61f, h * 0.54f, w * 0.39f, h * 0.38f)
            }
            Template.GRID -> {
                block(0f, 0f, (w - gap) / 2f, (h - gap) / 2f)
                block((w + gap) / 2f, 0f, (w - gap) / 2f, (h - gap) / 2f)
                block(0f, (h + gap) / 2f, (w - gap) / 2f, (h - gap) / 2f)
                block((w + gap) / 2f, (h + gap) / 2f, (w - gap) / 2f, (h - gap) / 2f)
            }
            Template.FLOW -> {
                block(0f, 0f, w * 0.29f, h * 0.72f)
                block(w * 0.33f, h * 0.15f, w * 0.25f, h * 0.85f)
                block(w * 0.62f, 0f, w * 0.18f, h * 0.56f)
                block(w * 0.84f, h * 0.2f, w * 0.16f, h * 0.8f)
            }
            Template.HERO -> {
                block(0f, 0f, w * 0.62f, h)
                block(w * 0.67f, 0f, w * 0.33f, h * 0.46f)
                block(w * 0.67f, h * 0.54f, w * 0.33f, h * 0.46f)
            }
            Template.STRIP -> {
                val cell = (w - gap * 2f) / 3f
                block(0f, 0f, cell, h)
                block(cell + gap, 0f, cell, h)
                block((cell + gap) * 2f, 0f, cell, h)
            }
            Template.PILE -> {
                block(0f, h * 0.18f, w * 0.72f, h * 0.66f)
                block(w * 0.2f, h * 0.08f, w * 0.66f, h * 0.58f)
                block(w * 0.32f, h * 0.38f, w * 0.68f, h * 0.62f)
            }
        }
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
    val shape = Kadraj.Shape.Pill
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(shape)
            .background(
                if (selected) {
                    Kadraj.Peach.copy(alpha = 0.2f)
                } else {
                    Kadraj.InkLift.copy(alpha = 0.62f)
                },
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
    val shape = Kadraj.Shape.Control
    Column(
        modifier = modifier
            .height(50.dp)
            .padding(horizontal = 2.dp)
            .clip(shape)
            .background(
                if (selected) Kadraj.Peach else Color.Transparent,
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
            tint = if (selected) Kadraj.Ink else Kadraj.Muted,
            strokeWidth = if (selected) 1.85.dp else 1.55.dp,
        )
        Spacer(Modifier.height(4.dp))
        BasicText(
            text = if (Kadraj.isContact) label.uppercase(Locale.ROOT) else label,
            style = Kadraj.Type.LabelSmall.copy(
                color = if (selected) Kadraj.Ink else Kadraj.Muted,
                fontSize = 10.sp,
                letterSpacing = 0.35.sp,
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
    val knobRadius = 6.dp
    val knobRadiusPx = with(LocalDensity.current) { knobRadius.toPx() }
    val active = Kadraj.Peach
    val track = Kadraj.LineStrong
    val currentOnChange by rememberUpdatedState(onValueChange)

    Row(
        modifier = modifier.fillMaxWidth().height(34.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = label,
            modifier = Modifier.width(66.dp),
            style = Kadraj.Type.BodySmall.copy(color = Kadraj.Muted, fontSize = 12.sp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(34.dp)
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
                    strokeWidth = 2.5.dp.toPx(),
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
                    color = active.copy(alpha = 0.14f),
                    radius = knobRadiusPx + 3.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(knobX, centerY),
                )
                drawCircle(
                    color = Kadraj.Paper,
                    radius = 4.4.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(knobX, centerY),
                )
                drawCircle(
                    color = active,
                    radius = 4.4.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(knobX, centerY),
                    style = Stroke(width = 1.2.dp.toPx()),
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
fun Wordmark(
    modifier: Modifier = Modifier,
    onPhoto: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        BasicText(
            text = if (Kadraj.isContact) "KADRAJ" else "Kadraj",
            style = Kadraj.Type.Wordmark.copy(
                color = if (onPhoto) Kadraj.FixedPaper else Kadraj.Paper,
            ),
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(if (Kadraj.isContact) RoundedCornerShape(0.dp) else CircleShape)
                .background(
                    if (onPhoto && !Kadraj.isContact) Kadraj.FixedPeach else Kadraj.Peach,
                ),
        )
    }
}

@Composable
fun NoticePill(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(Kadraj.Shape.Pill)
            .background(Kadraj.InkLift)
            .border(1.dp, Kadraj.LineStrong, Kadraj.Shape.Pill)
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
                            elevation = if (Kadraj.isPaper || Kadraj.isContact) 0.dp else 28.dp,
                            shape = Kadraj.Shape.Sheet,
                            clip = false,
                            ambientColor = Color.Black,
                            spotColor = Color.Black,
                        )
                        .clip(Kadraj.Shape.Sheet)
                        .background(
                            Brush.verticalGradient(
                                listOf(Kadraj.InkRaised, Kadraj.Ink),
                            ),
                        )
                        .border(
                            width = 1.dp,
                            color = Kadraj.Line,
                            shape = Kadraj.Shape.Sheet,
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
                            .clip(Kadraj.Shape.Pill)
                            .background(Kadraj.LineStrong),
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
    Template.AUTO -> "Auto"
    Template.GRID -> "Grid"
    Template.FLOW -> "Flow"
    Template.HERO -> "Hero"
    Template.STRIP -> "Strip"
    Template.PILE -> "Stack"
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
