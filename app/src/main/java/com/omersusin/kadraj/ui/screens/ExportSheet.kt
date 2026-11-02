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
import androidx.compose.foundation.shape.RoundedCornerShape
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
                text = "EXPORT",
                style = Kadraj.Type.Label.copy(
                    color = Kadraj.TerracottaSoft,
                    letterSpacing = 2.0.sp,
                ),
            )
            Spacer(Modifier.height(8.dp))
            BasicText(
                text = when (viewModel.exportState) {
                    is EditorViewModel.ExportState.Saved -> "Your Kadraj is ready."
                    else -> "Make your Kadraj."
                },
                style = Kadraj.Type.Display.copy(
                    color = Kadraj.Paper,
                    fontSize = 31.sp,
                    lineHeight = 34.sp,
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
                text = "Your composition is rendered on-device at full resolution and " +
                    "saved to the Kadraj album in Photos.",
                style = Kadraj.Type.Body.copy(color = Kadraj.Muted),
            )
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    text = "PREVIEW",
                    style = Kadraj.Type.LabelSmall.copy(
                        color = Kadraj.MutedDim,
                        letterSpacing = 1.5.sp,
                    ),
                )
                Spacer(Modifier.weight(1f))
                BasicText(
                    text = if (viewModel.template == Template.FLOW) {
                        "FREE"
                    } else {
                        ratioLabel(layout.aspect)
                    },
                    style = Kadraj.Type.LabelSmall.copy(
                        color = Kadraj.PaperDim,
                        letterSpacing = 1.1.sp,
                    ),
                )
            }
            Spacer(Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(18.dp),
                        clip = false,
                        ambientColor = Kadraj.Terracotta.copy(alpha = 0.32f),
                        spotColor = Kadraj.Violet.copy(alpha = 0.2f),
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(Kadraj.Ink)
                    .border(1.dp, Kadraj.LineStrong, RoundedCornerShape(18.dp))
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
                text = "SIZE",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    letterSpacing = 1.5.sp,
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
                text = "FORMAT",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    letterSpacing = 1.5.sp,
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
                    "${viewModel.exportFormat.label}  ·  Processed on device",
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
                        .height(54.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(Kadraj.InkSoft)
                        .border(1.dp, Kadraj.LineStrong, RoundedCornerShape(17.dp)),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    KadrajSpinner(diameter = 17.dp)
                    Spacer(Modifier.width(10.dp))
                    BasicText(
                        text = "RENDERING",
                        style = Kadraj.Type.Button.copy(
                            color = Kadraj.PaperDim,
                            letterSpacing = 1.3.sp,
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
    val shape = RoundedCornerShape(13.dp)
    Column(
        modifier = modifier
            .height(54.dp)
            .shadow(
                elevation = if (selected) 9.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.4f),
                spotColor = Kadraj.Terracotta.copy(alpha = 0.28f),
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
                color = if (selected) Kadraj.Peach.copy(alpha = 0.7f) else Kadraj.Line,
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
                color = Kadraj.MutedDim,
                fontSize = 8.sp,
                letterSpacing = 0.6.sp,
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
                .size(78.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = Kadraj.Terracotta.copy(alpha = 0.7f),
                    spotColor = Kadraj.Violet.copy(alpha = 0.3f),
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Kadraj.Peach.copy(alpha = 0.26f),
                            Kadraj.InkSoft,
                        ),
                    ),
                )
                .border(1.dp, Kadraj.Peach.copy(alpha = 0.7f), CircleShape),
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
            text = "Saved to Photos.",
            style = Kadraj.Type.DisplaySmall.copy(
                color = Kadraj.Paper,
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
                fontSize = 8.5.sp,
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
                .clip(RoundedCornerShape(50))
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
                text = "NEW COLLAGE",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.Muted,
                    letterSpacing = 1.3.sp,
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
