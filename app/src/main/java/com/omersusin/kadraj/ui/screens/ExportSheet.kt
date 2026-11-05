package com.omersusin.kadraj.ui.screens

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omersusin.kadraj.EditorViewModel
import com.omersusin.kadraj.collage.Template
import com.omersusin.kadraj.export.ExportFormat
import com.omersusin.kadraj.export.ExportResult
import com.omersusin.kadraj.export.ExportSize
import com.omersusin.kadraj.ui.components.CircleIconButton
import com.omersusin.kadraj.ui.components.CollagePreview
import com.omersusin.kadraj.ui.components.Glyph
import com.omersusin.kadraj.ui.components.GlyphIcon
import com.omersusin.kadraj.ui.components.Hairline
import com.omersusin.kadraj.ui.components.KadrajButton
import com.omersusin.kadraj.ui.components.KadrajSpinner
import com.omersusin.kadraj.ui.components.pressable
import com.omersusin.kadraj.ui.components.ratioLabel
import com.omersusin.kadraj.ui.theme.Kadraj
import kotlin.math.roundToInt

@Composable
fun ExportSheet(
    viewModel: EditorViewModel,
    onClose: () -> Unit,
    onNewProject: () -> Unit,
) {
    val layoutKey = remember(viewModel.images) {
        viewModel.images.map { it.id to it.aspect }
    }
    val layout = remember(
        layoutKey,
        viewModel.style.gap,
        viewModel.style.margin,
        viewModel.style.radius,
        viewModel.style.mat,
        viewModel.style.shadow,
        viewModel.style.texture,
        viewModel.style.palette,
        viewModel.template,
        viewModel.aspect,
    ) {
        viewModel.buildLayout()
    }
    val dimensions = remember(layout.aspect, viewModel.exportSize) {
        exportDimensions(layout.aspect, viewModel.exportSize.longSide)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            BasicText(
                text = "Export",
                style = Kadraj.Type.Display.copy(
                    color = Kadraj.Paper,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                ),
            )
            Spacer(Modifier.height(3.dp))
            BasicText(
                text = when (viewModel.exportState) {
                    is EditorViewModel.ExportState.Saved -> "Your collage is ready."
                    else -> "Choose a size, then save."
                },
                style = Kadraj.Type.BodySmall.copy(
                    color = Kadraj.Muted,
                    fontSize = 11.5.sp,
                ),
            )
        }
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

    when (val state = viewModel.exportState) {
        is EditorViewModel.ExportState.Saved -> {
            ExportSuccess(
                result = state.result,
                onClose = onClose,
                onNewProject = onNewProject,
            )
        }
        else -> {
            Spacer(Modifier.height(10.dp))
            BasicText(
                text = "Rendered on-device at full resolution and saved to the Kadraj " +
                    "album in Photos.",
                style = Kadraj.Type.Body.copy(color = Kadraj.Muted),
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    text = "Preview",
                    style = Kadraj.Type.BodySmall.copy(
                        color = Kadraj.MutedDim,
                        fontSize = 11.sp,
                    ),
                )
                Spacer(Modifier.weight(1f))
                BasicText(
                    text = if (viewModel.template == Template.FLOW) {
                        "Free flow"
                    } else {
                        ratioLabel(layout.aspect)
                    },
                    style = Kadraj.Type.LabelSmall.copy(
                        color = Kadraj.PaperDim,
                        fontSize = 10.sp,
                        letterSpacing = 0.3.sp,
                    ),
                )
            }
            Spacer(Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(146.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = Kadraj.Shape.Card,
                        clip = false,
                        ambientColor = Color.Black,
                        spotColor = Color.Black,
                    )
                    .clip(Kadraj.Shape.Card)
                    .background(Kadraj.Ink)
                    .border(1.dp, Kadraj.LineStrong, Kadraj.Shape.Card)
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                CollagePreview(
                    layout = layout,
                    style = viewModel.style,
                    images = viewModel.images,
                    loader = viewModel.imageLoader,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.height(17.dp))

            BasicText(
                text = "Size",
                style = Kadraj.Type.BodySmall.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 11.sp,
                ),
            )
            Spacer(Modifier.height(7.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                ExportSize.entries.forEach { size ->
                    ExportOption(
                        label = size.label,
                        detail = "${exportDimensions(layout.aspect, size.longSide).first} px",
                        selected = viewModel.exportSize == size,
                        onClick = { viewModel.updateExportSize(size) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(13.dp))

            BasicText(
                text = "Format",
                style = Kadraj.Type.BodySmall.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 11.sp,
                ),
            )
            Spacer(Modifier.height(7.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                ExportFormat.entries.forEach { format ->
                    ExportOption(
                        label = format.label,
                        detail = if (format == ExportFormat.PNG) "lossless" else "compact",
                        selected = viewModel.exportFormat == format,
                        onClick = { viewModel.updateExportFormat(format) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(13.dp))
            BasicText(
                text = "${dimensions.first} × ${dimensions.second} px  ·  " +
                    "${viewModel.exportFormat.label}  ·  On-device",
                style = Kadraj.Type.BodySmall.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 10.5.sp,
                ),
            )
            Spacer(Modifier.height(17.dp))

            if (state is EditorViewModel.ExportState.Failed) {
                BasicText(
                    text = state.message,
                    style = Kadraj.Type.BodySmall.copy(color = Kadraj.TerracottaSoft),
                )
                Spacer(Modifier.height(10.dp))
            }

            if (viewModel.exportState is EditorViewModel.ExportState.Rendering) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(Kadraj.Shape.Card)
                        .background(Kadraj.InkSoft)
                        .border(1.dp, Kadraj.LineStrong, Kadraj.Shape.Card),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    KadrajSpinner(diameter = 17.dp)
                    Spacer(Modifier.width(10.dp))
                    BasicText(
                        text = "Rendering",
                        style = Kadraj.Type.Button.copy(
                            color = Kadraj.PaperDim,
                            letterSpacing = 0.2.sp,
                        ),
                    )
                }
            } else {
                KadrajButton(
                    label = "Save to Photos",
                    onClick = viewModel::export,
                    modifier = Modifier.fillMaxWidth(),
                    leadingGlyph = Glyph.Download,
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ExportOption(
    label: String,
    detail: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val shape = Kadraj.Shape.Control
    Column(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(
                if (selected) Kadraj.Peach.copy(alpha = 0.13f) else Kadraj.InkSoft,
            )
            .border(
                width = 1.dp,
                color = if (selected) Kadraj.Peach.copy(alpha = 0.45f) else Kadraj.Line,
                shape = shape,
            )
            .pressable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        BasicText(
            text = label,
            style = Kadraj.Type.Title.copy(
                color = if (selected) Kadraj.Paper else Kadraj.PaperDim,
                fontSize = 14.sp,
                lineHeight = 17.sp,
            ),
        )
        Spacer(Modifier.height(2.dp))
        BasicText(
            text = detail,
            style = Kadraj.Type.LabelSmall.copy(
                color = if (selected) Kadraj.Peach else Kadraj.MutedDim,
                fontSize = 9.5.sp,
                letterSpacing = 0.3.sp,
            ),
        )
    }
}

@Composable
private fun ExportSuccess(
    result: ExportResult,
    onClose: () -> Unit,
    onNewProject: () -> Unit,
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Kadraj.Peach.copy(alpha = 0.12f))
                .border(1.dp, Kadraj.Peach.copy(alpha = 0.34f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            GlyphIcon(
                glyph = Glyph.Check,
                modifier = Modifier.size(30.dp),
                tint = Kadraj.TerracottaSoft,
                strokeWidth = 1.7.dp,
            )
        }
        Spacer(Modifier.height(16.dp))
        BasicText(
            text = "Saved to Photos",
            style = Kadraj.Type.Display.copy(
                color = Kadraj.Paper,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                textAlign = TextAlign.Center,
            ),
        )
        Spacer(Modifier.height(6.dp))
        BasicText(
            text = "${result.width} × ${result.height} px  ·  ${result.format.label}",
            style = Kadraj.Type.BodySmall.copy(color = Kadraj.Muted),
        )
        Spacer(Modifier.height(4.dp))
        BasicText(
            text = result.fileName,
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.MutedDim,
                fontSize = 9.5.sp,
                letterSpacing = 0.8.sp,
            ),
        )
        Spacer(Modifier.height(22.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KadrajButton(
                label = "Share",
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = result.format.mimeType
                        putExtra(Intent.EXTRA_STREAM, result.uri)
                        clipData = ClipData.newRawUri("Kadraj", result.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Kadraj"))
                },
                modifier = Modifier.weight(1f),
                tone = com.omersusin.kadraj.ui.components.ButtonTone.Paper,
                leadingGlyph = Glyph.Share,
            )
            KadrajButton(
                label = "Close",
                onClick = onClose,
                modifier = Modifier.weight(1f),
                tone = com.omersusin.kadraj.ui.components.ButtonTone.Quiet,
            )
        }
        Spacer(Modifier.height(17.dp))
        Row(
            modifier = Modifier
                .clip(Kadraj.Shape.Pill)
                .pressable {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNewProject()
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            GlyphIcon(
                glyph = Glyph.Plus,
                modifier = Modifier.size(12.dp),
                tint = Kadraj.Muted,
                strokeWidth = 1.6.dp,
            )
            BasicText(
                text = "New collage",
                style = Kadraj.Type.BodySmall.copy(
                    color = Kadraj.Muted,
                    fontSize = 11.sp,
                ),
            )
        }
    }
}

private fun exportDimensions(aspect: Float, longSide: Int): Pair<Int, Int> {
    val safeAspect = aspect.coerceIn(0.2f, 4.5f)
    return if (safeAspect >= 1f) {
        val width = longSide
        width to (longSide / safeAspect).roundToInt().coerceAtLeast(2)
    } else {
        val height = longSide
        (longSide * safeAspect).roundToInt().coerceAtLeast(2) to height
    }
}
