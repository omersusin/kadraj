package com.omersusin.kadraj.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.Dp
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
    onCloseProject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by rememberSaveable { mutableStateOf(EditorTab.Layout) }
    var guides by rememberSaveable { mutableStateOf(false) }
    var dockExpanded by rememberSaveable { mutableStateOf(true) }

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
    val ratioText = if (viewModel.template == Template.FLOW) {
        "Free flow"
    } else {
        ratioLabel(layout.aspect)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val panelMaxHeight = (maxHeight * 0.42f).coerceIn(180.dp, 326.dp)
        Column(Modifier.fillMaxSize()) {
            EditorTopBar(
                canUndo = viewModel.canUndo,
                canRedo = viewModel.canRedo,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
                onExport = onExport,
                onInfo = onInfo,
                onCloseProject = onCloseProject,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 2.dp, bottom = 6.dp),
            ) {
                CollageStage(
                    layout = layout,
                    style = viewModel.style,
                    images = images,
                    selectedId = viewModel.selectedId,
                    loader = viewModel.imageLoader,
                    onSelect = { id ->
                        viewModel.selectImage(id)
                        if (id != null) {
                            tab = EditorTab.Photo
                            dockExpanded = true
                        }
                    },
                    onFocus = viewModel::updateFocus,
                    onToggleFit = viewModel::toggleFit,
                    guides = guides,
                    modifier = Modifier.fillMaxSize(),
                )

                StageMetaPill(
                    ratio = ratioText,
                    photoCount = images.size,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                )

                CircleIconButton(
                    glyph = Glyph.Grid,
                    onClick = { guides = !guides },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    size = 32.dp,
                    tint = if (guides) Kadraj.Peach else Kadraj.Muted,
                    background = if (guides) {
                        Kadraj.Peach.copy(alpha = 0.16f)
                    } else {
                        Kadraj.Ink.copy(alpha = 0.72f)
                    },
                    border = if (guides) {
                        Kadraj.Peach.copy(alpha = 0.55f)
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

            EditorDock(
                tab = tab,
                onTabChange = {
                    tab = it
                    dockExpanded = true
                },
                expanded = dockExpanded,
                onExpandedChange = { dockExpanded = it },
                panelMaxHeight = panelMaxHeight,
                viewModel = viewModel,
                selectedImage = selectedImage,
                onPickImages = onPickImages,
            )
        }
    }
}

@Composable
private fun EditorTopBar(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onExport: () -> Unit,
    onInfo: () -> Unit,
    onCloseProject: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(
            glyph = Glyph.ChevronLeft,
            onClick = onCloseProject,
            size = 36.dp,
            tint = Kadraj.Paper,
            background = Kadraj.InkSoft,
            border = Kadraj.Line,
            contentDescription = "Close project",
        )
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            Wordmark()
        }
        Spacer(Modifier.width(6.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Kadraj.InkSoft.copy(alpha = 0.65f))
                .border(1.dp, Kadraj.Line, RoundedCornerShape(50))
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(
                glyph = Glyph.Undo,
                onClick = onUndo,
                size = 30.dp,
                tint = if (canUndo) Kadraj.PaperDim else Kadraj.MutedDim,
                background = Color.Transparent,
                border = Color.Transparent,
                enabled = canUndo,
                contentDescription = "Undo",
            )
            CircleIconButton(
                glyph = Glyph.Redo,
                onClick = onRedo,
                size = 30.dp,
                tint = if (canRedo) Kadraj.PaperDim else Kadraj.MutedDim,
                background = Color.Transparent,
                border = Color.Transparent,
                enabled = canRedo,
                contentDescription = "Redo",
            )
        }
        Spacer(Modifier.width(4.dp))
        CircleIconButton(
            glyph = Glyph.Info,
            onClick = onInfo,
            size = 32.dp,
            tint = Kadraj.Muted,
            background = Color.Transparent,
            border = Kadraj.Line,
            contentDescription = "Help and info",
        )
        Spacer(Modifier.width(7.dp))
        ExportPill(onClick = onExport)
    }
}

@Composable
private fun StageMetaPill(
    ratio: String,
    photoCount: Int,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Kadraj.Ink.copy(alpha = 0.74f))
            .border(1.dp, Kadraj.Line, shape)
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(Kadraj.Peach),
        )
        BasicText(
            text = ratio,
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.PaperDim,
                fontSize = 10.sp,
                letterSpacing = 0.25.sp,
            ),
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(10.dp)
                .background(Kadraj.LineStrong),
        )
        BasicText(
            text = "$photoCount ${if (photoCount == 1) "photo" else "photos"}",
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.Muted,
                fontSize = 10.sp,
                letterSpacing = 0.15.sp,
            ),
        )
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
                elevation = 10.dp,
                shape = shape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.35f),
                spotColor = Kadraj.Terracotta.copy(alpha = 0.2f),
            )
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(Kadraj.Peach, Kadraj.TerracottaSoft),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.18f), shape)
            .pressable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 12.dp),
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
            text = "Export",
            style = Kadraj.Type.Button.copy(
                color = Kadraj.Ink,
                fontSize = 11.5.sp,
                letterSpacing = 0.1.sp,
            ),
        )
    }
}

@Composable
private fun EditorDock(
    tab: EditorTab,
    onTabChange: (EditorTab) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    panelMaxHeight: Dp,
    viewModel: EditorViewModel,
    selectedImage: CollageImage?,
    onPickImages: () -> Unit,
) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .padding(bottom = 8.dp)
            .shadow(
                elevation = 20.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black,
                spotColor = Color.Black,
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Kadraj.InkRaised,
                        Kadraj.Ink,
                    ),
                ),
            )
            .border(1.dp, Kadraj.Line, shape)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.86f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .pressable { onExpandedChange(!expanded) },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Kadraj.LineStrong),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DockTab(
                glyph = Glyph.Grid,
                label = "Layout",
                selected = tab == EditorTab.Layout,
                onClick = { onTabChange(EditorTab.Layout) },
                modifier = Modifier.weight(1f),
            )
            DockTab(
                glyph = Glyph.Sliders,
                label = "Style",
                selected = tab == EditorTab.Style,
                onClick = { onTabChange(EditorTab.Style) },
                modifier = Modifier.weight(1f),
            )
            DockTab(
                glyph = Glyph.Image,
                label = "Photo",
                selected = tab == EditorTab.Photo,
                onClick = { onTabChange(EditorTab.Photo) },
                modifier = Modifier.weight(1f),
            )
            DockTab(
                glyph = Glyph.Text,
                label = "Text",
                selected = tab == EditorTab.Text,
                onClick = { onTabChange(EditorTab.Text) },
                modifier = Modifier.weight(1f),
            )
            CircleIconButton(
                glyph = if (expanded) Glyph.ChevronDown else Glyph.ChevronUp,
                onClick = { onExpandedChange(!expanded) },
                size = 30.dp,
                tint = Kadraj.Muted,
                background = Color.Transparent,
                border = Color.Transparent,
                contentDescription = if (expanded) {
                    "Collapse controls"
                } else {
                    "Expand controls"
                },
            )
            Spacer(Modifier.width(3.dp))
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(160)) + expandVertically(tween(220)),
            exit = fadeOut(tween(100)) + shrinkVertically(tween(160)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = panelMaxHeight)
                    .verticalScroll(rememberScrollState()),
            ) {
                Hairline(Modifier.padding(horizontal = 16.dp))
                AnimatedContent(
                    targetState = tab,
                    modifier = Modifier.fillMaxWidth(),
                    transitionSpec = {
                        (
                            fadeIn(tween(170)) +
                                slideInVertically(tween(210)) { height -> height / 10 }
                            ).togetherWith(
                            fadeOut(tween(90)) +
                                slideOutVertically(tween(130)) { height -> -height / 12 },
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
    }
}

@Composable
private fun LayoutControls(viewModel: EditorViewModel) {
    val templates = Template.entries
    val aspects = AspectPreset.entries

    Column(Modifier.padding(top = 12.dp, bottom = 14.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "Template",
                style = Kadraj.Type.Title.copy(color = Kadraj.Paper),
            )
            Spacer(Modifier.width(8.dp))
            BasicText(
                text = templateLabel(viewModel.template),
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.Peach,
                    fontSize = 10.sp,
                    letterSpacing = 0.4.sp,
                ),
            )
            Spacer(Modifier.weight(1f))
            CircleIconButton(
                glyph = Glyph.Shuffle,
                onClick = viewModel::shuffle,
                size = 30.dp,
                tint = Kadraj.Muted,
                background = Kadraj.InkSoft,
                border = Kadraj.Line,
                enabled = viewModel.images.size > 1,
                contentDescription = "Shuffle photos",
            )
        }
        Spacer(Modifier.height(9.dp))
        AutoScrollRow(
            entries = templates,
            selectedIndex = templates.indexOf(viewModel.template),
            key = { it.name },
            modifier = Modifier.padding(horizontal = 16.dp),
        ) { template ->
            TemplateChip(
                template = template,
                selected = viewModel.template == template,
                onClick = { viewModel.updateTemplate(template) },
            )
        }
        Spacer(Modifier.height(13.dp))
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "Ratio",
                style = Kadraj.Type.Title.copy(color = Kadraj.Paper),
            )
            Spacer(Modifier.weight(1f))
            if (viewModel.template == Template.FLOW) {
                BasicText(
                    text = "Flow chooses its own frame",
                    style = Kadraj.Type.LabelSmall.copy(
                        color = Kadraj.MutedDim,
                        fontSize = 10.sp,
                        letterSpacing = 0.2.sp,
                    ),
                )
            }
        }
        Spacer(Modifier.height(7.dp))
        AutoScrollRow(
            entries = aspects,
            selectedIndex = aspects.indexOf(viewModel.aspect),
            key = { it.name },
            modifier = Modifier.padding(horizontal = 16.dp),
        ) { aspect ->
            ChoiceChip(
                label = aspectLabel(aspect),
                selected = viewModel.aspect == aspect,
                onClick = { viewModel.updateAspect(aspect) },
            )
        }
    }
}

@Composable
private fun StyleControls(viewModel: EditorViewModel) {
    val palettes = Palettes.all
    val selectedPalette = palettes.indexOfFirst { it.id == viewModel.style.palette.id }

    Column(Modifier.padding(top = 12.dp, bottom = 14.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "Style",
                style = Kadraj.Type.Title.copy(color = Kadraj.Paper),
            )
            Spacer(Modifier.width(8.dp))
            BasicText(
                text = viewModel.style.palette.name,
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.Peach,
                    fontSize = 10.sp,
                    letterSpacing = 0.2.sp,
                ),
            )
            Spacer(Modifier.weight(1f))
            CircleIconButton(
                glyph = Glyph.Sparkle,
                onClick = viewModel::randomizeStyle,
                size = 30.dp,
                tint = Kadraj.Peach,
                background = Kadraj.Peach.copy(alpha = 0.12f),
                border = Kadraj.Peach.copy(alpha = 0.25f),
                contentDescription = "Surprise me",
            )
            Spacer(Modifier.width(7.dp))
            CircleIconButton(
                glyph = Glyph.Reset,
                onClick = viewModel::resetStyle,
                size = 30.dp,
                tint = Kadraj.Muted,
                background = Kadraj.InkSoft,
                border = Kadraj.Line,
                contentDescription = "Reset style",
            )
        }
        Spacer(Modifier.height(9.dp))
        AutoScrollRow(
            entries = palettes,
            selectedIndex = selectedPalette,
            key = { it.id },
            modifier = Modifier.padding(horizontal = 16.dp),
        ) { palette ->
            PaletteSwatch(
                palette = palette,
                selected = viewModel.style.palette.id == palette.id,
                onClick = { viewModel.setPalette(palette) },
            )
        }
        Spacer(Modifier.height(10.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
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
            Spacer(Modifier.height(5.dp))
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
    val thumbState = rememberLazyListState()

    LaunchedEffect(index) {
        if (index >= 0) {
            thumbState.animateScrollToItem(index)
        }
    }

    Column(Modifier.padding(top = 12.dp, bottom = 14.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "Photo",
                style = Kadraj.Type.Title.copy(color = Kadraj.Paper),
            )
            Spacer(Modifier.width(8.dp))
            BasicText(
                text = "${images.size} / ${EditorViewModel.MAX_IMAGES} photos",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 10.sp,
                    letterSpacing = 0.3.sp,
                ),
            )
            Spacer(Modifier.weight(1f))
            val remaining = (EditorViewModel.MAX_IMAGES - images.size).coerceAtLeast(0)
            val addLabel = when {
                remaining == 0 -> "Full"
                remaining <= 3 -> "Add $remaining"
                else -> "Add"
            }
            Row(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (remaining > 0) {
                            Kadraj.Peach.copy(alpha = 0.16f)
                        } else {
                            Kadraj.InkLift.copy(alpha = 0.55f)
                        },
                    )
                    .border(
                        1.dp,
                        if (remaining > 0) {
                            Kadraj.Peach.copy(alpha = 0.32f)
                        } else {
                            Color.Transparent
                        },
                        RoundedCornerShape(50),
                    )
                    .pressable(enabled = remaining > 0) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPickImages()
                    }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                GlyphIcon(
                    glyph = Glyph.Plus,
                    modifier = Modifier.size(12.dp),
                    tint = if (remaining > 0) Kadraj.Peach else Kadraj.MutedDim,
                    strokeWidth = 1.8.dp,
                )
                BasicText(
                    text = addLabel,
                    style = Kadraj.Type.LabelSmall.copy(
                        color = if (remaining > 0) Kadraj.PaperDim else Kadraj.MutedDim,
                        fontSize = 10.5.sp,
                        letterSpacing = 0.2.sp,
                    ),
                )
            }
        }
        Spacer(Modifier.height(9.dp))
        LazyRow(
            state = thumbState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemsIndexed(images, key = { _, image -> image.id }) { imageIndex, image ->
                ImageThumb(
                    image = image,
                    loader = viewModel.imageLoader,
                    selected = image.id == viewModel.selectedId,
                    onClick = { viewModel.selectImage(image.id) },
                    label = "Photo ${imageIndex + 1}",
                )
            }
            item {
                AddImageTile(
                    onClick = onPickImages,
                    enabled = images.size < EditorViewModel.MAX_IMAGES,
                )
            }
        }
        Spacer(Modifier.height(11.dp))

        if (selectedImage != null) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
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
                    background = Kadraj.Terracotta.copy(alpha = 0.1f),
                    border = Kadraj.Terracotta.copy(alpha = 0.2f),
                    contentDescription = "Remove photo",
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
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
            Spacer(Modifier.height(8.dp))
            when (panel) {
                PhotoPanel.Look -> {
                    AutoScrollRow(
                        entries = PhotoLook.entries,
                        selectedIndex = PhotoLook.entries.indexOf(selectedImage.look),
                        key = { it.name },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) { look ->
                        ChoiceChip(
                            label = lookLabel(look),
                            selected = selectedImage.look == look,
                            onClick = { viewModel.setLook(selectedImage.id, look) },
                        )
                    }
                    if (selectedImage.look != PhotoLook.ORIGINAL) {
                        Spacer(Modifier.height(4.dp))
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            KadrajSlider(
                                label = "Strength",
                                value = selectedImage.lookAmount,
                                valueRange = 0f..1f,
                                onValueChange = { viewModel.setLookAmount(selectedImage.id, it) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MiniAction(
                            glyph = Glyph.Sparkle,
                            label = "Auto all",
                            onClick = viewModel::autoEnhance,
                        )
                        if (images.size > 1) {
                            MiniAction(
                                glyph = Glyph.Copy,
                                label = "Apply to all",
                                onClick = { viewModel.applyTreatmentToAll(selectedImage.id) },
                            )
                        }
                    }
                }
                PhotoPanel.Adjust -> {
                    Column(Modifier.padding(horizontal = 16.dp)) {
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
                    }
                    Spacer(Modifier.height(7.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
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
                                label = "Reset tone",
                                onClick = { viewModel.resetTone(selectedImage.id) },
                            )
                            if (images.size > 1) {
                                MiniAction(
                                    glyph = Glyph.Copy,
                                    label = "Apply to all",
                                    onClick = { viewModel.applyTreatmentToAll(selectedImage.id) },
                                )
                            }
                        }
                        Spacer(Modifier.width(7.dp))
                        MiniAction(
                            glyph = Glyph.Sparkle,
                            label = "Auto all",
                            onClick = viewModel::autoEnhance,
                            highlighted = true,
                        )
                    }
                }
                PhotoPanel.Frame -> {
                    Column(Modifier.padding(horizontal = 16.dp)) {
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
                    }
                    Spacer(Modifier.height(7.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
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
                            background = Kadraj.Terracotta.copy(alpha = 0.1f),
                            border = Kadraj.Terracotta.copy(alpha = 0.2f),
                            contentDescription = "Reset photo",
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            BasicText(
                text = when (panel) {
                    PhotoPanel.Look -> "Pick a look, then dial in its strength."
                    PhotoPanel.Adjust -> "Small corrections, applied to this photo only."
                    PhotoPanel.Frame -> "Drag the canvas to shift the crop. Double-tap to fill or fit."
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                style = Kadraj.Type.BodySmall.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 11.5.sp,
                ),
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Kadraj.InkSoft)
                    .border(1.dp, Kadraj.Line, RoundedCornerShape(14.dp))
                    .padding(14.dp),
            ) {
                BasicText(
                    text = "Choose a photo",
                    style = Kadraj.Type.Title.copy(color = Kadraj.Paper),
                )
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = "Tap any frame on the canvas to crop, tune, or reorder it.",
                    style = Kadraj.Type.BodySmall.copy(color = Kadraj.Muted),
                )
            }
        }
    }
}

@Composable
private fun TextControls(viewModel: EditorViewModel) {
    val caption = viewModel.style.caption
    val focusManager = LocalFocusManager.current

    Column(Modifier.padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                text = "Caption",
                style = Kadraj.Type.Title.copy(color = Kadraj.Paper),
            )
            Spacer(Modifier.width(8.dp))
            BasicText(
                text = if (caption.text.isBlank()) "Optional" else "${caption.text.length}/120",
                style = Kadraj.Type.LabelSmall.copy(
                    color = Kadraj.MutedDim,
                    fontSize = 10.sp,
                    letterSpacing = 0.2.sp,
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
                    text = "Ideas",
                    style = Kadraj.Type.BodySmall.copy(
                        color = Kadraj.MutedDim,
                        fontSize = 11.sp,
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
                text = "Tone",
                modifier = Modifier.width(38.dp),
                style = Kadraj.Type.BodySmall.copy(
                    color = Kadraj.Muted,
                    fontSize = 11.5.sp,
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
            .clip(shape)
            .background(if (selected) Kadraj.Paper else Color.Transparent)
            .border(
                1.dp,
                if (selected) Color.Transparent else Kadraj.Line,
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
                elevation = if (selected) 6.dp else 0.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.3f),
                spotColor = Kadraj.Violet.copy(alpha = 0.12f),
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
                fontSize = 10.sp,
                letterSpacing = 0.2.sp,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun <T> AutoScrollRow(
    entries: List<T>,
    selectedIndex: Int,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val state = rememberLazyListState()
    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            state.animateScrollToItem(selectedIndex)
        }
    }
    LazyRow(
        state = state,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(end = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            count = entries.size,
            key = { index -> key(entries[index]) },
        ) { index ->
            content(entries[index])
        }
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
