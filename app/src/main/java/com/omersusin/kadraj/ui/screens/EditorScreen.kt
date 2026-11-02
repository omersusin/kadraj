package com.omersusin.kadraj.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omersusin.kadraj.CollageImage
import com.omersusin.kadraj.EditorViewModel
import com.omersusin.kadraj.collage.AspectPreset
import com.omersusin.kadraj.collage.CaptionAlignment
import com.omersusin.kadraj.collage.CaptionFont
import com.omersusin.kadraj.collage.CaptionPlacement
import com.omersusin.kadraj.collage.CaptionTone
import com.omersusin.kadraj.collage.FitMode
import com.omersusin.kadraj.collage.Palette
import com.omersusin.kadraj.collage.Palettes
import com.omersusin.kadraj.collage.PhotoLook
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
import kotlin.math.roundToInt

private enum class EditorTab {
    Layout,
    Style,
    Photo,
    Text,
}

private enum class PhotoPanel {
    Look,
    Adjust,
    Frame,
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
    var guides by rememberSaveable { mutableStateOf(false) }

    val images = viewModel.images
    val selectedImage = viewModel.selectedImage
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
            canUndo = viewModel.canUndo,
            canRedo = viewModel.canRedo,
            onUndo = viewModel::undo,
            onRedo = viewModel::redo,
            onExport = onExport,
            onInfo = onInfo,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(top = 5.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "COMPOSITION",
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
                    "FREE"
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
            Spacer(Modifier.width(7.dp))
            CircleIconButton(
                glyph = Glyph.Grid,
                onClick = { guides = !guides },
                size = 25.dp,
                tint = if (guides) Kadraj.Peach else Kadraj.Muted,
                background = if (guides) {
                    Kadraj.Terracotta.copy(alpha = 0.18f)
                } else {
                    Color.Transparent
                },
                border = if (guides) {
                    Kadraj.TerracottaSoft.copy(alpha = 0.6f)
                } else {
                    Kadraj.Line
                },
                contentDescription = if (guides) {
                    "Hide composition guides"
                } else {
                    "Show composition guides"
                },
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
                if (id != null) tab = EditorTab.Photo
            },
            onFocus = viewModel::updateFocus,
            onToggleFit = viewModel::toggleFit,
            guides = guides,
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
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onExport: () -> Unit,
    onInfo: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Wordmark()
            Spacer(Modifier.height(2.dp))
            BasicText(
                text = "$imageCount ${if (imageCount == 1) "PHOTO" else "PHOTOS"}",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 8.sp,
                    letterSpacing = 1.0.sp,
                ),
            )
        }
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Kadraj.GlassStrong)
                .border(1.dp, Kadraj.Line, RoundedCornerShape(50))
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            CircleIconButton(
                glyph = Glyph.Undo,
                onClick = onUndo,
                size = 28.dp,
                tint = if (canUndo) Kadraj.PaperDim else Kadraj.MutedDim,
                background = Color.Transparent,
                border = Color.Transparent,
                enabled = canUndo,
                contentDescription = "Undo",
            )
            CircleIconButton(
                glyph = Glyph.Redo,
                onClick = onRedo,
                size = 28.dp,
                tint = if (canRedo) Kadraj.PaperDim else Kadraj.MutedDim,
                background = Color.Transparent,
                border = Color.Transparent,
                enabled = canRedo,
                contentDescription = "Redo",
            )
        }
        Spacer(Modifier.width(6.dp))
        CircleIconButton(
            glyph = Glyph.Info,
            onClick = onInfo,
            size = 34.dp,
            tint = Kadraj.Muted,
            background = Kadraj.Glass,
            border = Kadraj.Line,
            contentDescription = "About Kadraj",
        )
        Spacer(Modifier.width(6.dp))
        ExportPill(onClick = onExport)
    }
}

@Composable
private fun ExportPill(onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .height(36.dp)
            .shadow(
                elevation = 12.dp,
                shape = shape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.65f),
                spotColor = Kadraj.Violet.copy(alpha = 0.32f),
            )
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(Kadraj.Peach, Kadraj.Terracotta, Kadraj.Ember),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.25f), shape)
            .pressable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        GlyphIcon(
            glyph = Glyph.Export,
            modifier = Modifier.size(13.dp),
            tint = Kadraj.Ink,
            strokeWidth = 1.8.dp,
        )
        BasicText(
            text = "EXPORT",
            style = Kadraj.Type.Button.copy(
                color = Kadraj.Ink,
                fontSize = 9.5.sp,
                letterSpacing = 1.1.sp,
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
                elevation = 30.dp,
                shape = shape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.26f),
                spotColor = Kadraj.Violet.copy(alpha = 0.18f),
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF261E14).copy(alpha = 0.98f),
                        Kadraj.InkRaised.copy(alpha = 0.98f),
                        Color(0xFF100D09).copy(alpha = 0.99f),
                    ),
                ),
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Kadraj.Peach.copy(alpha = 0.34f),
                        Kadraj.LineStrong,
                        Kadraj.Violet.copy(alpha = 0.24f),
                    ),
                ),
                shape,
            )
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            DockTab(
                glyph = Glyph.Grid,
                label = "LAYOUT",
                selected = tab == EditorTab.Layout,
                onClick = { onTabChange(EditorTab.Layout) },
                modifier = Modifier.weight(1f),
            )
            DockTab(
                glyph = Glyph.Sliders,
                label = "STYLE",
                selected = tab == EditorTab.Style,
                onClick = { onTabChange(EditorTab.Style) },
                modifier = Modifier.weight(1f),
            )
            DockTab(
                glyph = Glyph.Image,
                label = "PHOTO",
                selected = tab == EditorTab.Photo,
                onClick = { onTabChange(EditorTab.Photo) },
                modifier = Modifier.weight(1f),
            )
            DockTab(
                glyph = Glyph.Text,
                label = "TEXT",
                selected = tab == EditorTab.Text,
                onClick = { onTabChange(EditorTab.Text) },
                modifier = Modifier.weight(1f),
            )
        }
        Hairline()
        AnimatedContent(
            targetState = tab,
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                (
                    fadeIn(tween(180)) +
                        slideInVertically(tween(220)) { height -> height / 10 }
                    ).togetherWith(
                    fadeOut(tween(110)) +
                        slideOutVertically(tween(150)) { height -> -height / 12 },
                )
            },
            label = "dock-panel",
        ) { currentTab ->
            when (currentTab) {
                EditorTab.Layout -> LayoutControls(viewModel)
                EditorTab.Style -> StyleControls(viewModel)
                EditorTab.Photo -> PhotoControls(
                    viewModel = viewModel,
                    selectedImage = selectedImage,
                    onPickImages = onPickImages,
                )
                EditorTab.Text -> TextControls(viewModel)
            }
        }
    }
}

@Composable
private fun LayoutControls(viewModel: EditorViewModel) {
    Column(Modifier.padding(horizontal = 16.dp).padding(top = 9.dp, bottom = 11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                text = "TEMPLATE",
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
                contentDescription = "Shuffle photos",
            )
        }
        Spacer(Modifier.height(6.dp))
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
                    onClick = { viewModel.updateTemplate(template) },
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                text = "RATIO",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    letterSpacing = 1.5.sp,
                ),
            )
            Spacer(Modifier.weight(1f))
            if (viewModel.template == Template.FLOW) {
                BasicText(
                    text = "FLOW SETS ITS OWN RATIO",
                    style = Kadraj.Type.LabelSmall.copy(
                        color = Kadraj.TerracottaSoft,
                        fontSize = 8.sp,
                        letterSpacing = 0.9.sp,
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
                    onClick = { viewModel.updateAspect(aspect) },
                )
            }
        }
    }
}

@Composable
private fun StyleControls(viewModel: EditorViewModel) {
    Column(Modifier.padding(horizontal = 16.dp).padding(top = 9.dp, bottom = 11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                text = "PALETTE",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    letterSpacing = 1.5.sp,
                ),
            )
            Spacer(Modifier.width(7.dp))
            BasicText(
                text = viewModel.style.palette.name.uppercase(Locale.ENGLISH),
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.TerracottaSoft,
                    letterSpacing = 1.1.sp,
                ),
            )
            Spacer(Modifier.weight(1f))
            CircleIconButton(
                glyph = Glyph.Sparkle,
                onClick = viewModel::randomizeStyle,
                size = 26.dp,
                tint = Kadraj.TerracottaSoft,
                background = Color.Transparent,
                border = Kadraj.Line,
                contentDescription = "Surprise me",
            )
            Spacer(Modifier.width(5.dp))
            CircleIconButton(
                glyph = Glyph.Reset,
                onClick = viewModel::resetStyle,
                size = 26.dp,
                tint = Kadraj.Muted,
                background = Color.Transparent,
                border = Kadraj.Line,
                contentDescription = "Reset style",
            )
        }
        Spacer(Modifier.height(7.dp))
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
        Spacer(Modifier.height(7.dp))
        KadrajSlider(
            label = "Spacing",
            value = viewModel.style.gap,
            valueRange = 0f..0.06f,
            onValueChange = viewModel::setGap,
        )
        KadrajSlider(
            label = "Margin",
            value = viewModel.style.margin,
            valueRange = 0f..0.12f,
            onValueChange = viewModel::setMargin,
        )
        KadrajSlider(
            label = "Corners",
            value = viewModel.style.radius,
            valueRange = 0f..0.1f,
            onValueChange = viewModel::setRadius,
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            ToggleChip(
                label = "Mat",
                selected = viewModel.style.mat > 0.002f,
                onClick = viewModel::toggleMat,
            )
            ToggleChip(
                label = "Shadow",
                selected = viewModel.style.shadow,
                onClick = viewModel::toggleShadow,
            )
            ToggleChip(
                label = "Grain",
                selected = viewModel.style.texture,
                onClick = viewModel::toggleTexture,
            )
        }
    }
}

@Composable
private fun PhotoControls(
    viewModel: EditorViewModel,
    selectedImage: CollageImage?,
    onPickImages: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val images = viewModel.images
    val index = selectedImage?.let { images.indexOfFirst { item -> item.id == it.id } } ?: -1
    var panel by rememberSaveable { mutableStateOf(PhotoPanel.Look) }

    Column(Modifier.padding(horizontal = 16.dp).padding(top = 9.dp, bottom = 11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                text = "PHOTOS",
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
                    text = "ADD",
                    style = Kadraj.Type.LabelSmall.copy(
                        color = Kadraj.PaperDim,
                        fontSize = 8.sp,
                        letterSpacing = 1.0.sp,
                    ),
                )
            }
        }
        Spacer(Modifier.height(7.dp))
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
                    label = "Photo ${imageIndex + 1}",
                )
            }
            AddImageTile(
                onClick = onPickImages,
                enabled = images.size < EditorViewModel.MAX_IMAGES,
            )
        }
        Spacer(Modifier.height(8.dp))
        if (selectedImage != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FitSwitch(
                    fit = selectedImage.fit,
                    onSelect = { fit -> viewModel.setFit(selectedImage.id, fit) },
                )
                Spacer(Modifier.weight(1f))
                CircleIconButton(
                    glyph = Glyph.ChevronLeft,
                    onClick = { viewModel.moveImage(selectedImage.id, -1) },
                    size = 32.dp,
                    tint = if (index > 0) Kadraj.Paper else Kadraj.MutedDim,
                    background = Kadraj.InkSoft,
                    border = Kadraj.Line,
                    enabled = index > 0,
                    contentDescription = "Move earlier",
                )
                CircleIconButton(
                    glyph = Glyph.ChevronRight,
                    onClick = { viewModel.moveImage(selectedImage.id, 1) },
                    size = 32.dp,
                    tint = if (index in 0 until images.lastIndex) Kadraj.Paper else Kadraj.MutedDim,
                    background = Kadraj.InkSoft,
                    border = Kadraj.Line,
                    enabled = index in 0 until images.lastIndex,
                    contentDescription = "Move later",
                )
                CircleIconButton(
                    glyph = Glyph.Trash,
                    onClick = { viewModel.removeImage(selectedImage.id) },
                    size = 32.dp,
                    tint = Kadraj.TerracottaSoft,
                    background = Kadraj.InkSoft,
                    border = Kadraj.Line,
                    contentDescription = "Remove photo",
                )
            }
            Spacer(Modifier.height(7.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChoiceChip(
                    label = "Look",
                    selected = panel == PhotoPanel.Look,
                    onClick = { panel = PhotoPanel.Look },
                )
                ChoiceChip(
                    label = "Adjust",
                    selected = panel == PhotoPanel.Adjust,
                    onClick = { panel = PhotoPanel.Adjust },
                )
                ChoiceChip(
                    label = "Frame",
                    selected = panel == PhotoPanel.Frame,
                    onClick = { panel = PhotoPanel.Frame },
                )
            }
            Spacer(Modifier.height(5.dp))
            when (panel) {
                PhotoPanel.Look -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        PhotoLook.entries.forEach { look ->
                            ChoiceChip(
                                label = lookLabel(look),
                                selected = selectedImage.look == look,
                                onClick = { viewModel.setLook(selectedImage.id, look) },
                            )
                        }
                    }
                    if (selectedImage.look != PhotoLook.ORIGINAL) {
                        KadrajSlider(
                            label = "Strength",
                            value = selectedImage.lookAmount,
                            valueRange = 0f..1f,
                            onValueChange = { viewModel.setLookAmount(selectedImage.id, it) },
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MiniAction(
                            glyph = Glyph.Sparkle,
                            label = "AUTO ALL",
                            onClick = viewModel::autoEnhance,
                        )
                        if (images.size > 1) {
                            MiniAction(
                                glyph = Glyph.Copy,
                                label = "SYNC TO ALL",
                                onClick = { viewModel.applyTreatmentToAll(selectedImage.id) },
                            )
                        }
                    }
                }
                PhotoPanel.Adjust -> {
                    KadrajSlider(
                        label = "Light",
                        value = selectedImage.brightness,
                        valueRange = -1f..1f,
                        onValueChange = { viewModel.setBrightness(selectedImage.id, it) },
                        valueLabel = signedPercent(selectedImage.brightness),
                    )
                    KadrajSlider(
                        label = "Contrast",
                        value = selectedImage.contrast,
                        valueRange = -1f..1f,
                        onValueChange = { viewModel.setContrast(selectedImage.id, it) },
                        valueLabel = signedPercent(selectedImage.contrast),
                    )
                    KadrajSlider(
                        label = "Color",
                        value = selectedImage.saturation,
                        valueRange = -1f..1f,
                        onValueChange = { viewModel.setSaturation(selectedImage.id, it) },
                        valueLabel = signedPercent(selectedImage.saturation),
                    )
                    KadrajSlider(
                        label = "Warmth",
                        value = selectedImage.warmth,
                        valueRange = -1f..1f,
                        onValueChange = { viewModel.setWarmth(selectedImage.id, it) },
                        valueLabel = signedPercent(selectedImage.warmth),
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MiniAction(
                                glyph = Glyph.Reset,
                                label = "RESET TONE",
                                onClick = { viewModel.resetTone(selectedImage.id) },
                            )
                            if (images.size > 1) {
                                MiniAction(
                                    glyph = Glyph.Copy,
                                    label = "SYNC TO ALL",
                                    onClick = { viewModel.applyTreatmentToAll(selectedImage.id) },
                                )
                            }
                        }
                        Spacer(Modifier.width(7.dp))
                        MiniAction(
                            glyph = Glyph.Sparkle,
                            label = "AUTO ALL",
                            onClick = viewModel::autoEnhance,
                            highlighted = true,
                        )
                    }
                }
                PhotoPanel.Frame -> {
                    KadrajSlider(
                        label = "Zoom",
                        value = selectedImage.zoom,
                        valueRange = 1f..3f,
                        onValueChange = { viewModel.setZoom(selectedImage.id, it) },
                        valueLabel = "${(selectedImage.zoom * 100f).roundToInt()}%",
                    )
                    KadrajSlider(
                        label = "Tilt",
                        value = selectedImage.tilt,
                        valueRange = -12f..12f,
                        onValueChange = { viewModel.setTilt(selectedImage.id, it) },
                        valueLabel = "${selectedImage.tilt.roundToInt()}\u00B0",
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircleIconButton(
                            glyph = Glyph.FlipHorizontal,
                            onClick = { viewModel.toggleFlipHorizontal(selectedImage.id) },
                            size = 32.dp,
                            tint = Kadraj.Paper,
                            background = Kadraj.InkSoft,
                            border = Kadraj.Line,
                            contentDescription = "Flip horizontally",
                        )
                        CircleIconButton(
                            glyph = Glyph.FlipVertical,
                            onClick = { viewModel.toggleFlipVertical(selectedImage.id) },
                            size = 32.dp,
                            tint = Kadraj.Paper,
                            background = Kadraj.InkSoft,
                            border = Kadraj.Line,
                            contentDescription = "Flip vertically",
                        )
                        CircleIconButton(
                            glyph = Glyph.Copy,
                            onClick = { viewModel.duplicateImage(selectedImage.id) },
                            size = 32.dp,
                            tint = Kadraj.Paper,
                            background = Kadraj.InkSoft,
                            border = Kadraj.Line,
                            enabled = images.size < EditorViewModel.MAX_IMAGES,
                            contentDescription = "Duplicate photo",
                        )
                        CircleIconButton(
                            glyph = Glyph.Reset,
                            onClick = { viewModel.resetPhoto(selectedImage.id) },
                            size = 32.dp,
                            tint = Kadraj.TerracottaSoft,
                            background = Kadraj.InkSoft,
                            border = Kadraj.Line,
                            contentDescription = "Reset photo",
                        )
                    }
                }
            }
            Spacer(Modifier.height(5.dp))
            BasicText(
                text = when (panel) {
                    PhotoPanel.Look -> "Pick a look, then dial in its strength."
                    PhotoPanel.Adjust -> "Small corrections, applied to this photo only."
                    PhotoPanel.Frame -> "Drag the canvas to shift the crop. Double-tap to fill or fit."
                },
                style = Kadraj.Type.BodySmall.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 10.sp,
                ),
            )
        } else {
            BasicText(
                text = "Select a photo on the canvas to adjust its crop, look, and order.",
                style = Kadraj.Type.BodySmall.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 10.5.sp,
                ),
            )
        }
    }
}

@Composable
private fun TextControls(viewModel: EditorViewModel) {
    val caption = viewModel.style.caption
    val focusManager = LocalFocusManager.current

    Column(Modifier.padding(horizontal = 16.dp).padding(top = 9.dp, bottom = 11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                text = "CAPTION",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    letterSpacing = 1.5.sp,
                ),
            )
            Spacer(Modifier.weight(1f))
            if (caption.text.isNotEmpty()) {
                CircleIconButton(
                    glyph = Glyph.Close,
                    onClick = { viewModel.setCaptionText("") },
                    size = 24.dp,
                    tint = Kadraj.Muted,
                    background = Color.Transparent,
                    border = Kadraj.Line,
                    contentDescription = "Clear caption",
                )
                Spacer(Modifier.width(7.dp))
            }
            BasicText(
                text = "${caption.text.length}/120",
                style = Kadraj.Type.Numeric.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 8.5.sp,
                ),
            )
        }
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = caption.text,
            onValueChange = viewModel::setCaptionText,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp, max = 68.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Kadraj.InkSoft)
                .border(1.dp, Kadraj.Line, RoundedCornerShape(13.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            textStyle = Kadraj.Type.Body.copy(
                color = Kadraj.Paper,
                fontSize = 13.sp,
            ),
            cursorBrush = SolidColor(Kadraj.TerracottaSoft),
            maxLines = 2,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            decorationBox = { innerTextField ->
                Box {
                    if (caption.text.isEmpty()) {
                        BasicText(
                            text = "Write a short caption...",
                            style = Kadraj.Type.BodySmall.copy(
                                color = Kadraj.MutedDim,
                                fontSize = 12.sp,
                            ),
                        )
                    }
                    innerTextField()
                }
            },
        )
        if (caption.text.isBlank()) {
            Spacer(Modifier.height(7.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BasicText(
                    text = "IDEAS",
                    style = Kadraj.Type.LabelSmall.copy(
                        color = Kadraj.MutedDim,
                        letterSpacing = 1.3.sp,
                    ),
                )
                captionIdeas.forEach { idea ->
                    ChoiceChip(
                        label = idea,
                        selected = false,
                        onClick = { viewModel.setCaptionText(idea) },
                    )
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            ChoiceChip(
                label = "Serif",
                selected = caption.font == CaptionFont.SERIF,
                onClick = { viewModel.setCaptionFont(CaptionFont.SERIF) },
            )
            ChoiceChip(
                label = "Sans",
                selected = caption.font == CaptionFont.SANS,
                onClick = { viewModel.setCaptionFont(CaptionFont.SANS) },
            )
            Spacer(Modifier.weight(1f))
            CaptionAlignButton(
                glyph = Glyph.AlignLeft,
                selected = caption.alignment == CaptionAlignment.START,
                description = "Align caption left",
                onClick = { viewModel.setCaptionAlignment(CaptionAlignment.START) },
            )
            CaptionAlignButton(
                glyph = Glyph.AlignCenter,
                selected = caption.alignment == CaptionAlignment.CENTER,
                description = "Align caption center",
                onClick = { viewModel.setCaptionAlignment(CaptionAlignment.CENTER) },
            )
            CaptionAlignButton(
                glyph = Glyph.AlignRight,
                selected = caption.alignment == CaptionAlignment.END,
                description = "Align caption right",
                onClick = { viewModel.setCaptionAlignment(CaptionAlignment.END) },
            )
        }
        KadrajSlider(
            label = "Size",
            value = caption.size,
            valueRange = 0.025f..0.075f,
            onValueChange = viewModel::setCaptionSize,
            valueLabel = String.format(Locale.US, "%.1f%%", caption.size * 100f),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            ChoiceChip(
                label = "Top",
                selected = caption.placement == CaptionPlacement.TOP,
                onClick = { viewModel.setCaptionPlacement(CaptionPlacement.TOP) },
            )
            ChoiceChip(
                label = "Middle",
                selected = caption.placement == CaptionPlacement.CENTER,
                onClick = { viewModel.setCaptionPlacement(CaptionPlacement.CENTER) },
            )
            ChoiceChip(
                label = "Bottom",
                selected = caption.placement == CaptionPlacement.BOTTOM,
                onClick = { viewModel.setCaptionPlacement(CaptionPlacement.BOTTOM) },
            )
            Spacer(Modifier.weight(1f))
            ToggleChip(
                label = "Plate",
                selected = caption.plate,
                onClick = viewModel::toggleCaptionPlate,
            )
        }
        Spacer(Modifier.height(5.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            BasicText(
                text = "TONE",
                modifier = Modifier.width(38.dp),
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    letterSpacing = 1.2.sp,
                ),
            )
            ChoiceChip(
                label = "Auto",
                selected = caption.tone == CaptionTone.AUTO,
                onClick = { viewModel.setCaptionTone(CaptionTone.AUTO) },
            )
            ChoiceChip(
                label = "Paper",
                selected = caption.tone == CaptionTone.PAPER,
                onClick = { viewModel.setCaptionTone(CaptionTone.PAPER) },
            )
            ChoiceChip(
                label = "Accent",
                selected = caption.tone == CaptionTone.ACCENT,
                onClick = { viewModel.setCaptionTone(CaptionTone.ACCENT) },
            )
        }
    }
}

@Composable
private fun CaptionAlignButton(
    glyph: Glyph,
    selected: Boolean,
    description: String,
    onClick: () -> Unit,
) {
    CircleIconButton(
        glyph = glyph,
        onClick = onClick,
        size = 30.dp,
        tint = if (selected) Kadraj.Ink else Kadraj.Muted,
        background = if (selected) Kadraj.Paper else Color.Transparent,
        border = if (selected) Color.Transparent else Kadraj.Line,
        contentDescription = description,
    )
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
            label = "Fill",
            selected = fit == FitMode.COVER,
            onClick = { onSelect(FitMode.COVER) },
        )
        FitOption(
            label = "Fit",
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
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .height(30.dp)
            .shadow(
                elevation = if (selected) 6.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.4f),
                spotColor = Kadraj.Terracotta.copy(alpha = 0.3f),
            )
            .clip(shape)
            .background(
                if (selected) {
                    Brush.horizontalGradient(listOf(Color.White, Kadraj.PaperDim))
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                },
            )
            .border(
                1.dp,
                if (selected) Color.White.copy(alpha = 0.35f) else Color.Transparent,
                shape,
            )
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
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "palette-scale",
    )
    Box(
        modifier = Modifier
            .size(34.dp)
            .semantics {
                contentDescription = "Palette ${palette.name}${if (selected) ", selected" else ""}"
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (selected) 12.dp else 0.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.55f),
                spotColor = Kadraj.Violet.copy(alpha = 0.25f),
            )
            .clip(CircleShape)
            .border(
                width = if (selected) 1.8.dp else 1.dp,
                color = if (selected) Kadraj.Peach else Kadraj.Line,
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

@Composable
private fun MiniAction(
    glyph: Glyph,
    label: String,
    onClick: () -> Unit,
    highlighted: Boolean = false,
) {
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(shape)
            .background(
                if (highlighted) {
                    Brush.horizontalGradient(
                        listOf(Kadraj.Terracotta.copy(alpha = 0.24f), Kadraj.Glass),
                    )
                } else {
                    Brush.horizontalGradient(listOf(Kadraj.Glass, Color.Transparent))
                },
            )
            .border(
                1.dp,
                if (highlighted) Kadraj.TerracottaSoft.copy(alpha = 0.55f) else Kadraj.Line,
                shape,
            )
            .pressable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        GlyphIcon(
            glyph = glyph,
            modifier = Modifier.size(11.dp),
            tint = if (highlighted) Kadraj.Peach else Kadraj.Muted,
            strokeWidth = 1.6.dp,
        )
        BasicText(
            text = label,
            style = Kadraj.Type.LabelSmall.copy(
                color = if (highlighted) Kadraj.Paper else Kadraj.PaperDim,
                fontSize = 8.sp,
                letterSpacing = 1.0.sp,
            ),
            maxLines = 1,
        )
    }
}

private fun signedPercent(value: Float): String {
    val percent = (value * 100f).roundToInt()
    return if (percent > 0) "+$percent%" else "$percent%"
}

private val captionIdeas = listOf(
    "Weekend notes",
    "A story in frames",
    "Studio diary",
    "Small things",
)

private fun lookLabel(look: PhotoLook): String = when (look) {
    PhotoLook.ORIGINAL -> "Original"
    PhotoLook.WARM -> "Warm"
    PhotoLook.COOL -> "Cool"
    PhotoLook.MONO -> "Mono"
    PhotoLook.FADE -> "Fade"
    PhotoLook.VIVID -> "Vivid"
    PhotoLook.FILM -> "Film"
}
