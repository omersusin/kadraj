package com.omersusin.kadraj.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omersusin.kadraj.CollageImage
import com.omersusin.kadraj.EditorViewModel
import com.omersusin.kadraj.collage.AspectPreset
import com.omersusin.kadraj.collage.FitMode
import com.omersusin.kadraj.collage.Palette
import com.omersusin.kadraj.collage.Palettes
import com.omersusin.kadraj.collage.Template
import com.omersusin.kadraj.ui.components.AddImageTile
import com.omersusin.kadraj.ui.components.ChoiceChip
import com.omersusin.kadraj.ui.components.CircleIconButton
import com.omersusin.kadraj.ui.components.CollageStage
import com.omersusin.kadraj.ui.components.DockTab
import com.omersusin.kadraj.ui.components.Glyph
import com.omersusin.kadraj.ui.components.GlyphIcon
import com.omersusin.kadraj.ui.components.Hairline
import com.omersusin.kadraj.ui.components.ImageThumb
import com.omersusin.kadraj.ui.components.KadrajSlider
import com.omersusin.kadraj.ui.components.TemplateChip
import com.omersusin.kadraj.ui.components.ToggleChip
import com.omersusin.kadraj.ui.components.Wordmark
import com.omersusin.kadraj.ui.components.aspectLabel
import com.omersusin.kadraj.ui.components.pressable
import com.omersusin.kadraj.ui.components.ratioLabel
import com.omersusin.kadraj.ui.components.templateLabel
import com.omersusin.kadraj.ui.theme.Kadraj
import java.util.Locale

private enum class EditorTab {
    Layout,
    Style,
    Images,
}

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onPickImages: () -> Unit,
    onExport: () -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by rememberSaveable { mutableStateOf(EditorTab.Layout) }

    val images = viewModel.images
    val selectedImage = images.firstOrNull { it.id == viewModel.selectedId }
    val layoutKey = remember(images) { images.map { it.id to it.aspect } }
    val layout = remember(
        layoutKey,
        viewModel.style.gap,
        viewModel.style.margin,
        viewModel.template,
        viewModel.aspect,
    ) {
        viewModel.buildLayout()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        EditorTopBar(
            imageCount = images.size,
            template = viewModel.template,
            onExport = onExport,
            onInfo = onInfo,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(top = 6.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "KOMPOZİSYON",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    letterSpacing = 1.5.sp,
                ),
            )
            Spacer(Modifier.width(9.dp))
            Box(Modifier.width(14.dp).height(1.dp).background(Kadraj.LineStrong))
            Spacer(Modifier.width(9.dp))
            BasicText(
                text = if (viewModel.template == Template.FLOW) {
                    "SERBEST"
                } else {
                    ratioLabel(layout.aspect)
                },
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.PaperDim,
                    letterSpacing = 1.2.sp,
                ),
            )
            Spacer(Modifier.weight(1f))
            BasicText(
                text = "${images.size} / ${EditorViewModel.MAX_IMAGES}",
                style = Kadraj.Type.Numeric.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 9.5.sp,
                ),
            )
        }

        CollageStage(
            layout = layout,
            style = viewModel.style,
            images = images,
            selectedId = viewModel.selectedId,
            loader = viewModel.imageLoader,
            onSelect = { id ->
                viewModel.selectImage(id)
                if (id != null) tab = EditorTab.Images
            },
            onFocus = viewModel::updateFocus,
            onToggleFit = viewModel::toggleFit,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 8.dp),
        )

        EditorDock(
            tab = tab,
            onTabChange = { tab = it },
            viewModel = viewModel,
            selectedImage = selectedImage,
            onPickImages = onPickImages,
        )
    }
}

@Composable
private fun EditorTopBar(
    imageCount: Int,
    template: Template,
    onExport: () -> Unit,
    onInfo: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Wordmark()
            Spacer(Modifier.height(2.dp))
            BasicText(
                text = "$imageCount GÖRSEL  ·  ${templateLabel(template)}",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 8.sp,
                    letterSpacing = 1.0.sp,
                ),
            )
        }
        Spacer(Modifier.weight(1f))
        CircleIconButton(
            glyph = Glyph.Info,
            onClick = onInfo,
            size = 38.dp,
            tint = Kadraj.Muted,
            background = Color.Transparent,
            border = Kadraj.Line,
            contentDescription = "Kadraj hakkında",
        )
        Spacer(Modifier.width(8.dp))
        ExportPill(onClick = onExport)
    }
}

@Composable
private fun ExportPill(onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(
                    listOf(Kadraj.TerracottaSoft, Kadraj.Terracotta),
                ),
            )
            .pressable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        GlyphIcon(
            glyph = Glyph.Export,
            modifier = Modifier.size(14.dp),
            tint = Kadraj.Ink,
            strokeWidth = 1.8.dp,
        )
        BasicText(
            text = "DIŞA",
            style = Kadraj.Type.Button.copy(
                color = Kadraj.Ink,
                fontSize = 10.5.sp,
                letterSpacing = 1.3.sp,
            ),
        )
    }
}

@Composable
private fun EditorDock(
    tab: EditorTab,
    onTabChange: (EditorTab) -> Unit,
    viewModel: EditorViewModel,
    selectedImage: CollageImage?,
    onPickImages: () -> Unit,
) {
    val shape = RoundedCornerShape(27.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp)
            .padding(bottom = 9.dp)
            .shadow(
                elevation = 24.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black,
                spotColor = Color.Black,
            )
            .clip(shape)
            .background(Kadraj.InkRaised.copy(alpha = 0.97f))
            .border(1.dp, Kadraj.LineStrong, shape)
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            DockTab(
                glyph = Glyph.Grid,
                label = "DÜZEN",
                selected = tab == EditorTab.Layout,
                onClick = { onTabChange(EditorTab.Layout) },
                modifier = Modifier.weight(1f),
            )
            DockTab(
                glyph = Glyph.Sliders,
                label = "STİL",
                selected = tab == EditorTab.Style,
                onClick = { onTabChange(EditorTab.Style) },
                modifier = Modifier.weight(1f),
            )
            DockTab(
                glyph = Glyph.Image,
                label = "GÖRSEL",
                selected = tab == EditorTab.Images,
                onClick = { onTabChange(EditorTab.Images) },
                modifier = Modifier.weight(1f),
            )
        }
        Hairline()
        when (tab) {
            EditorTab.Layout -> LayoutControls(viewModel)
            EditorTab.Style -> StyleControls(viewModel)
            EditorTab.Images -> ImageControls(
                viewModel = viewModel,
                selectedImage = selectedImage,
                onPickImages = onPickImages,
            )
        }
    }
}

@Composable
private fun LayoutControls(viewModel: EditorViewModel) {
    Column(Modifier.padding(horizontal = 16.dp).padding(top = 10.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                text = "ŞABLON",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    letterSpacing = 1.5.sp,
                ),
            )
            Spacer(Modifier.weight(1f))
            CircleIconButton(
                glyph = Glyph.Shuffle,
                onClick = viewModel::shuffle,
                size = 28.dp,
                tint = Kadraj.Muted,
                background = Color.Transparent,
                border = Kadraj.Line,
                enabled = viewModel.images.size > 1,
                contentDescription = "Görselleri karıştır",
            )
        }
        Spacer(Modifier.height(7.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Template.entries.forEach { template ->
                TemplateChip(
                    template = template,
                    selected = viewModel.template == template,
                    onClick = { viewModel.setTemplate(template) },
                )
            }
        }
        Spacer(Modifier.height(11.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                text = "ORAN",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    letterSpacing = 1.5.sp,
                ),
            )
            Spacer(Modifier.weight(1f))
            if (viewModel.template == Template.FLOW) {
                BasicText(
                    text = "KENDİ ORANINI KURAR",
                    style = Kadraj.Type.LabelSmall.copy(
                        color = Kadraj.TerracottaSoft,
                        fontSize = 8.sp,
                        letterSpacing = 1.0.sp,
                    ),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            AspectPreset.entries.forEach { aspect ->
                ChoiceChip(
                    label = aspectLabel(aspect),
                    selected = viewModel.aspect == aspect,
                    onClick = { viewModel.setAspect(aspect) },
                )
            }
        }
    }
}

@Composable
private fun StyleControls(viewModel: EditorViewModel) {
    Column(Modifier.padding(horizontal = 16.dp).padding(top = 10.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                text = "PALET",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    letterSpacing = 1.5.sp,
                ),
            )
            Spacer(Modifier.weight(1f))
            BasicText(
                text = viewModel.style.palette.name.uppercase(Locale.forLanguageTag("tr")),
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.TerracottaSoft,
                    letterSpacing = 1.2.sp,
                ),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Palettes.all.forEach { palette ->
                PaletteSwatch(
                    palette = palette,
                    selected = viewModel.style.palette.id == palette.id,
                    onClick = { viewModel.setPalette(palette) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        KadrajSlider(
            label = "Aralık",
            value = viewModel.style.gap,
            valueRange = 0f..0.06f,
            onValueChange = viewModel::setGap,
        )
        KadrajSlider(
            label = "Kenar",
            value = viewModel.style.margin,
            valueRange = 0f..0.12f,
            onValueChange = viewModel::setMargin,
        )
        KadrajSlider(
            label = "Köşe",
            value = viewModel.style.radius,
            valueRange = 0f..0.1f,
            onValueChange = viewModel::setRadius,
        )
        Spacer(Modifier.height(5.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            ToggleChip(
                label = "Paspartu",
                selected = viewModel.style.mat > 0.002f,
                onClick = viewModel::toggleMat,
            )
            ToggleChip(
                label = "Gölge",
                selected = viewModel.style.shadow,
                onClick = viewModel::toggleShadow,
            )
            ToggleChip(
                label = "Doku",
                selected = viewModel.style.texture,
                onClick = viewModel::toggleTexture,
            )
        }
    }
}

@Composable
private fun ImageControls(
    viewModel: EditorViewModel,
    selectedImage: CollageImage?,
    onPickImages: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val images = viewModel.images
    val index = selectedImage?.let { images.indexOfFirst { item -> item.id == it.id } } ?: -1

    Column(Modifier.padding(horizontal = 16.dp).padding(top = 10.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                text = "GÖRSELLER",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    letterSpacing = 1.5.sp,
                ),
            )
            Spacer(Modifier.width(7.dp))
            BasicText(
                text = "${images.size}/${EditorViewModel.MAX_IMAGES}",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 8.sp,
                    letterSpacing = 0.8.sp,
                ),
            )
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .height(28.dp)
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, Kadraj.LineStrong, RoundedCornerShape(50))
                    .pressable(enabled = images.size < EditorViewModel.MAX_IMAGES) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPickImages()
                    }
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                GlyphIcon(
                    glyph = Glyph.Plus,
                    modifier = Modifier.size(11.dp),
                    tint = Kadraj.TerracottaSoft,
                    strokeWidth = 1.8.dp,
                )
                BasicText(
                    text = "EKLE",
                    style = Kadraj.Type.LabelSmall.copy(
                        color = Kadraj.PaperDim,
                        fontSize = 8.sp,
                        letterSpacing = 1.0.sp,
                    ),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            images.forEachIndexed { imageIndex, image ->
                ImageThumb(
                    image = image,
                    loader = viewModel.imageLoader,
                    selected = image.id == viewModel.selectedId,
                    onClick = { viewModel.selectImage(image.id) },
                    label = "Görsel ${imageIndex + 1}",
                )
            }
            AddImageTile(
                onClick = onPickImages,
                enabled = images.size < EditorViewModel.MAX_IMAGES,
            )
        }
        Spacer(Modifier.height(9.dp))
        if (selectedImage != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                FitSwitch(
                    fit = selectedImage.fit,
                    onSelect = { fit -> viewModel.setFit(selectedImage.id, fit) },
                )
                Spacer(Modifier.weight(1f))
                CircleIconButton(
                    glyph = Glyph.ChevronLeft,
                    onClick = { viewModel.moveImage(selectedImage.id, -1) },
                    size = 34.dp,
                    tint = if (index > 0) Kadraj.Paper else Kadraj.MutedDim,
                    background = Kadraj.InkSoft,
                    border = Kadraj.Line,
                    enabled = index > 0,
                    contentDescription = "Sırada öne al",
                )
                CircleIconButton(
                    glyph = Glyph.ChevronRight,
                    onClick = { viewModel.moveImage(selectedImage.id, 1) },
                    size = 34.dp,
                    tint = if (index in 0 until images.lastIndex) Kadraj.Paper else Kadraj.MutedDim,
                    background = Kadraj.InkSoft,
                    border = Kadraj.Line,
                    enabled = index in 0 until images.lastIndex,
                    contentDescription = "Sırada geriye al",
                )
                CircleIconButton(
                    glyph = Glyph.Trash,
                    onClick = { viewModel.removeImage(selectedImage.id) },
                    size = 34.dp,
                    tint = Kadraj.TerracottaSoft,
                    background = Kadraj.InkSoft,
                    border = Kadraj.Line,
                    contentDescription = "Görseli kaldır",
                )
            }
            Spacer(Modifier.height(5.dp))
            BasicText(
                text = "Sürükle: kadrajı kaydır  ·  Çift dokun: doldur / sığdır",
                style = Kadraj.Type.BodySmall.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 10.sp,
                ),
            )
        } else {
            BasicText(
                text = "Sahneden bir görsel seçin; kadrajı ve sırasını buradan yönetin.",
                style = Kadraj.Type.BodySmall.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 10.5.sp,
                ),
            )
        }
    }
}

@Composable
private fun FitSwitch(
    fit: FitMode,
    onSelect: (FitMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(50))
            .background(Kadraj.InkSoft)
            .border(1.dp, Kadraj.Line, RoundedCornerShape(50)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FitOption(
            label = "Doldur",
            selected = fit == FitMode.COVER,
            onClick = { onSelect(FitMode.COVER) },
        )
        FitOption(
            label = "Sığdır",
            selected = fit == FitMode.FIT,
            onClick = { onSelect(FitMode.FIT) },
        )
    }
}

@Composable
private fun FitOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(50))
            .background(if (selected) Kadraj.Paper else Color.Transparent)
            .pressable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = Kadraj.Type.Chip.copy(
                color = if (selected) Kadraj.Ink else Kadraj.Muted,
                fontSize = 10.5.sp,
            ),
        )
    }
}

@Composable
private fun PaletteSwatch(
    palette: Palette,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .border(
                width = if (selected) 1.6.dp else 1.dp,
                color = if (selected) Kadraj.Terracotta else Kadraj.Line,
                shape = CircleShape,
            )
            .pressable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(if (selected) 3.dp else 4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        palette.colors.map { color -> Color(color) },
                    ),
                ),
        )
    }
}
