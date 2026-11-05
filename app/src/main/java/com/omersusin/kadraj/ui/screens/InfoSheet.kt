package com.omersusin.kadraj.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omersusin.kadraj.BuildConfig
import com.omersusin.kadraj.EditorViewModel
import com.omersusin.kadraj.ui.components.ButtonTone
import com.omersusin.kadraj.ui.components.CircleIconButton
import com.omersusin.kadraj.ui.components.Glyph
import com.omersusin.kadraj.ui.components.GlyphIcon
import com.omersusin.kadraj.ui.components.Hairline
import com.omersusin.kadraj.ui.components.KadrajButton
import com.omersusin.kadraj.ui.components.pressable
import com.omersusin.kadraj.ui.theme.Kadraj
import com.omersusin.kadraj.ui.theme.ThemeMode

@Composable
fun InfoSheet(
    hasImages: Boolean,
    onClose: () -> Unit,
    onNewProject: () -> Unit,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
) {
    var confirming by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = "Settings",
            style = Kadraj.Type.Title.copy(color = Kadraj.Paper),
        )
        Spacer(Modifier.weight(1f))
        CircleIconButton(
            glyph = Glyph.Close,
            onClick = onClose,
            size = 34.dp,
            tint = Kadraj.Muted,
            background = Kadraj.InkSoft,
            border = Kadraj.Line,
            contentDescription = "Close",
        )
    }
    Spacer(Modifier.height(18.dp))
    BasicText(
        text = "Appearance",
        style = Kadraj.Type.Sticker.copy(color = Kadraj.Peach),
    )
    Spacer(Modifier.height(8.dp))
    ThemePicker(
        selected = themeMode,
        onSelect = onThemeChange,
    )
    Spacer(Modifier.height(20.dp))
    Hairline()
    Spacer(Modifier.height(16.dp))
    BasicText(
        text = "About Kadraj",
        style = Kadraj.Type.Sticker.copy(color = Kadraj.MutedDim),
    )
    Spacer(Modifier.height(8.dp))
    BasicText(
        text = "A quieter way to compose.",
        style = Kadraj.Type.Display.copy(
            color = Kadraj.Paper,
            fontSize = 24.sp,
            lineHeight = 28.sp,
        ),
    )
    Spacer(Modifier.height(8.dp))
    BasicText(
        text = "Kadraj brings phone and desktop captures into one considered frame. " +
            "It reads ratios, balances negative space, and leaves the final call to you.",
        style = Kadraj.Type.Body.copy(color = Kadraj.Muted),
    )
    Spacer(Modifier.height(14.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Pill("On-device")
        Pill("${EditorViewModel.MAX_IMAGES} photos")
        Pill("7 looks")
        Pill("4K")
    }
    Spacer(Modifier.height(20.dp))
    Hairline()
    Spacer(Modifier.height(18.dp))

    InfoTip(
        glyph = Glyph.Grid,
        title = "Choose a rhythm",
        body = "Auto reads portrait, landscape, and mixed selections differently. " +
            "Switch to grid, flow, hero, strip, or stack whenever you want.",
    )
    Spacer(Modifier.height(16.dp))
    InfoTip(
        glyph = Glyph.Image,
        title = "Shape every frame",
        body = "Tap a photo, then drag the canvas to move the crop. Double-tap to switch " +
            "between fill and fit. Photo adds looks, light, color, warmth, zoom, and tilt.",
    )
    Spacer(Modifier.height(16.dp))
    InfoTip(
        glyph = Glyph.Sparkle,
        title = "Let it read the light",
        body = "Smart enhance samples each photo and balances brightness, contrast, color, " +
            "and warmth. Smart crop finds the busiest part of the frame and centres the crop.",
    )
    Spacer(Modifier.height(16.dp))
    InfoTip(
        glyph = Glyph.Eye,
        title = "Check the original",
        body = "The eye button on the stage shows the untouched photo treatment, so you can " +
            "compare before and after without losing your place.",
    )
    Spacer(Modifier.height(16.dp))
    InfoTip(
        glyph = Glyph.Lock,
        title = "Private by design",
        body = "Palettes, captions, and 4K output finish on your device. No account, " +
            "no cloud, no upload.",
    )

    if (hasImages) {
        Spacer(Modifier.height(20.dp))
        Hairline()
        Spacer(Modifier.height(16.dp))
        if (!confirming) {
            KadrajButton(
                label = "Start a new collage",
                onClick = { confirming = true },
                modifier = Modifier.fillMaxWidth(),
                tone = ButtonTone.Quiet,
                leadingGlyph = Glyph.Plus,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Kadraj.Shape.Control)
                    .background(Kadraj.Terracotta.copy(alpha = 0.09f))
                    .border(1.dp, Kadraj.Terracotta.copy(alpha = 0.22f), Kadraj.Shape.Control)
                    .padding(14.dp),
            ) {
                BasicText(
                    text = "Close this collage?",
                    style = Kadraj.Type.Title.copy(color = Kadraj.Paper),
                )
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = "The current edits will be cleared. This cannot be undone.",
                    style = Kadraj.Type.BodySmall.copy(color = Kadraj.Muted),
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KadrajButton(
                        label = "Cancel",
                        onClick = { confirming = false },
                        modifier = Modifier.weight(1f),
                        tone = ButtonTone.Quiet,
                    )
                    KadrajButton(
                        label = "New collage",
                        onClick = onNewProject,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(18.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = "Kadraj ${BuildConfig.VERSION_NAME}",
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.MutedDim,
                fontSize = 9.5.sp,
                letterSpacing = 0.4.sp,
            ),
        )
        Spacer(Modifier.weight(1f))
        BasicText(
            text = "Fraunces & Manrope · OFL",
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.MutedDim,
                fontSize = 9.5.sp,
                letterSpacing = 0.3.sp,
            ),
        )
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun ThemePicker(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ThemeMode.entries.forEach { mode ->
            val active = mode == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Kadraj.Shape.Card)
                    .background(
                        if (active) {
                            Kadraj.Peach.copy(alpha = 0.12f)
                        } else {
                            Kadraj.InkLift.copy(alpha = 0.55f)
                        },
                    )
                    .border(
                        width = 1.dp,
                        color = if (active) {
                            Kadraj.Peach.copy(alpha = 0.6f)
                        } else {
                            Kadraj.Line
                        },
                        shape = Kadraj.Shape.Card,
                    )
                    .pressable { onSelect(mode) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                ThemeSwatch(mode)
                Column(Modifier.weight(1f)) {
                    BasicText(
                        text = mode.label,
                        style = Kadraj.Type.Title.copy(color = Kadraj.Paper),
                    )
                    Spacer(Modifier.height(2.dp))
                    BasicText(
                        text = mode.blurb,
                        style = Kadraj.Type.BodySmall.copy(
                            color = Kadraj.Muted,
                            fontSize = 11.5.sp,
                        ),
                    )
                }
                if (active) {
                    Box(
                        modifier = Modifier
                            .size(19.dp)
                            .clip(Kadraj.Shape.Icon)
                            .background(Kadraj.Peach),
                        contentAlignment = Alignment.Center,
                    ) {
                        GlyphIcon(
                            glyph = Glyph.Check,
                            modifier = Modifier.size(11.dp),
                            tint = Kadraj.Ink,
                            strokeWidth = 1.8.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSwatch(mode: ThemeMode) {
    val background = when (mode) {
        ThemeMode.CINEMA -> Color(0xFF0B0B0D)
        ThemeMode.PAPER -> Color(0xFFF2EBDF)
        ThemeMode.CONTACT -> Color(0xFF050505)
    }
    val foreground = when (mode) {
        ThemeMode.CINEMA -> Color(0xFFF7F2EA)
        ThemeMode.PAPER -> Color(0xFF1B1814)
        ThemeMode.CONTACT -> Color(0xFFF4F4EF)
    }
    val accent = when (mode) {
        ThemeMode.CINEMA -> Color(0xFFFFB18A)
        ThemeMode.PAPER -> Color(0xFFAD5132)
        ThemeMode.CONTACT -> Color(0xFFFF4B36)
    }
    val shape = when (mode) {
        ThemeMode.CINEMA -> RoundedCornerShape(12.dp)
        ThemeMode.PAPER -> RoundedCornerShape(1.dp)
        ThemeMode.CONTACT -> RoundedCornerShape(0.dp)
    }
    val family = when (mode) {
        ThemeMode.CONTACT -> FontFamily.Monospace
        else -> Kadraj.Fraunces
    }

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, foreground.copy(alpha = 0.2f), shape),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = "Aa",
            style = Kadraj.Type.Display.copy(
                color = foreground,
                fontFamily = family,
                fontSize = 17.sp,
                letterSpacing = if (mode == ThemeMode.CONTACT) 0.5.sp else (-0.5).sp,
            ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(5.dp)
                .width(13.dp)
                .height(3.dp)
                .background(accent),
        )
    }
}

@Composable
private fun Pill(text: String) {
    Box(
        modifier = Modifier
            .clip(Kadraj.Shape.Pill)
            .background(Kadraj.InkLift.copy(alpha = 0.7f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        BasicText(
            text = text,
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.PaperDim,
                fontSize = 10.sp,
                letterSpacing = 0.1.sp,
            ),
        )
    }
}

@Composable
private fun InfoTip(
    glyph: Glyph,
    title: String,
    body: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(Kadraj.Shape.Tile)
                .background(Kadraj.Peach.copy(alpha = 0.1f))
                .border(1.dp, Kadraj.Peach.copy(alpha = 0.2f), Kadraj.Shape.Tile),
            contentAlignment = Alignment.Center,
        ) {
            GlyphIcon(
                glyph = glyph,
                modifier = Modifier.size(16.dp),
                tint = Kadraj.Peach,
                strokeWidth = 1.5.dp,
            )
        }
        Column(Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = Kadraj.Type.Title.copy(
                    color = Kadraj.Paper,
                    fontSize = 14.sp,
                ),
            )
            Spacer(Modifier.height(3.dp))
            BasicText(
                text = body,
                style = Kadraj.Type.BodySmall.copy(color = Kadraj.Muted),
            )
        }
    }
}
