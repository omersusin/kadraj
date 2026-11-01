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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omersusin.kadraj.BuildConfig
import com.omersusin.kadraj.ui.components.ButtonTone
import com.omersusin.kadraj.ui.components.CircleIconButton
import com.omersusin.kadraj.ui.components.Glyph
import com.omersusin.kadraj.ui.components.GlyphIcon
import com.omersusin.kadraj.ui.components.Hairline
import com.omersusin.kadraj.ui.components.KadrajButton
import com.omersusin.kadraj.ui.components.KadrajSpinner
import com.omersusin.kadraj.ui.theme.Kadraj

@Composable
fun InfoSheet(
    hasImages: Boolean,
    onClose: () -> Unit,
    onNewProject: () -> Unit,
) {
    var confirming by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        BasicText(
            text = "ABOUT KADRAJ",
            style = Kadraj.Type.Label.copy(
                color = Kadraj.TerracottaSoft,
                letterSpacing = 2.0.sp,
            ),
        )
        Spacer(Modifier.weight(1f))
        CircleIconButton(
            glyph = Glyph.Close,
            onClick = onClose,
            size = 36.dp,
            tint = Kadraj.Muted,
            background = Color.Transparent,
            border = Kadraj.Line,
            contentDescription = "Close",
        )
    }
    Spacer(Modifier.height(18.dp))
    BasicText(
        text = "A pause between",
        style = Kadraj.Type.Display.copy(
            color = Kadraj.Paper,
            fontSize = 32.sp,
            lineHeight = 35.sp,
        ),
    )
    BasicText(
        text = "frames.",
        style = Kadraj.Type.HeroItalic.copy(
            color = Kadraj.TerracottaSoft,
            fontSize = 32.sp,
            lineHeight = 35.sp,
        ),
    )
    Spacer(Modifier.height(14.dp))
    BasicText(
        text = "Kadraj brings phone and desktop captures into one editorial composition. " +
            "It reads ratios, balances negative space, and leaves the final call to you. " +
            "No account, no cloud, no upload.",
        style = Kadraj.Type.Body.copy(color = Kadraj.Muted),
    )
    Spacer(Modifier.height(20.dp))
    Hairline()
    Spacer(Modifier.height(18.dp))

    InfoTip(
        number = "01",
        title = "Choose a ratio, let go.",
        body = "Auto reads portrait, landscape, and mixed selections differently. " +
            "Switch to grid, flow, hero, strip, or stack whenever you want.",
    )
    Spacer(Modifier.height(17.dp))
    InfoTip(
        number = "02",
        title = "Touch the canvas.",
        body = "Tap a photo to select it, then drag to move the crop. Double-tap to switch " +
            "between fill and fit. The Photo tab adds looks, zoom, tilt, and flips.",
    )
    Spacer(Modifier.height(17.dp))
    InfoTip(
        number = "03",
        title = "Quietly capable.",
        body = "Textured palettes, soft shadows, captions, and 4K output. Everything " +
            "finishes on your device; your photos never leave it.",
    )

    Spacer(Modifier.height(22.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InfoStat(value = "12", label = "photos", modifier = Modifier.weight(1f))
        InfoStat(value = "7", label = "looks", modifier = Modifier.weight(1f))
        InfoStat(value = "4K", label = "output", modifier = Modifier.weight(1f))
        InfoStat(value = "100%", label = "local", modifier = Modifier.weight(1f))
    }

    if (hasImages) {
        Spacer(Modifier.height(22.dp))
        Hairline()
        Spacer(Modifier.height(17.dp))
        if (!confirming) {
            KadrajButton(
                label = "Start a new collage",
                onClick = { confirming = true },
                modifier = Modifier.fillMaxWidth(),
                tone = ButtonTone.Quiet,
                leadingGlyph = Glyph.Plus,
            )
        } else {
            BasicText(
                text = "The current collage will close. Continue?",
                style = Kadraj.Type.BodySmall.copy(color = Kadraj.TerracottaSoft),
            )
            Spacer(Modifier.height(11.dp))
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
                    label = "Start",
                    onClick = onNewProject,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    Spacer(Modifier.height(20.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = "VERSION ${BuildConfig.VERSION_NAME}",
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.MutedDim,
                letterSpacing = 1.3.sp,
            ),
        )
        Spacer(Modifier.weight(1f))
        BasicText(
            text = "FRAUNCES & MANROPE · OFL",
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.MutedDim,
                fontSize = 8.sp,
                letterSpacing = 1.0.sp,
            ),
        )
    }
}

@Composable
private fun InfoTip(
    number: String,
    title: String,
    body: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Kadraj.InkSoft)
                .border(1.dp, Kadraj.LineStrong, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = number,
                style = Kadraj.Type.Numeric.copy(
                    color = Kadraj.TerracottaSoft,
                    fontSize = 9.sp,
                ),
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
            Spacer(Modifier.height(4.dp))
            BasicText(
                text = body,
                style = Kadraj.Type.BodySmall.copy(color = Kadraj.Muted),
            )
        }
    }
}

@Composable
private fun InfoStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(Kadraj.InkSoft)
            .border(1.dp, Kadraj.Line, RoundedCornerShape(13.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(
            text = value,
            style = Kadraj.Type.DisplaySmall.copy(
                color = Kadraj.Paper,
                fontSize = 19.sp,
                textAlign = TextAlign.Center,
            ),
        )
        Spacer(Modifier.height(2.dp))
        BasicText(
            text = label,
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.MutedDim,
                fontSize = 8.sp,
                letterSpacing = 1.0.sp,
            ),
        )
    }
}
