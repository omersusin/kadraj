package com.omersusin.kadraj

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.omersusin.kadraj.ui.components.ModalSheet
import com.omersusin.kadraj.ui.components.NoticePill
import com.omersusin.kadraj.ui.screens.EditorScreen
import com.omersusin.kadraj.ui.screens.EmptyState
import com.omersusin.kadraj.ui.screens.ExportSheet
import com.omersusin.kadraj.ui.screens.InfoSheet
import com.omersusin.kadraj.ui.theme.Kadraj
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun KadrajApp(viewModel: EditorViewModel = viewModel()) {
    var showInfo by rememberSaveable { mutableStateOf(false) }
    var showExport by rememberSaveable { mutableStateOf(false) }

    val images = viewModel.images
    val notice = viewModel.notice

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            EditorViewModel.MAX_IMAGES,
        ),
    ) { uris ->
        viewModel.addImages(uris)
    }

    val pickImages: () -> Unit = {
        picker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    val openExport: () -> Unit = {
        if (images.isEmpty()) {
            viewModel.showNotice("Add a few photos first.")
        } else {
            if (viewModel.exportState !is EditorViewModel.ExportState.Rendering) {
                viewModel.resetExport()
            }
            showInfo = false
            showExport = true
        }
    }

    val startNewProject: () -> Unit = {
        showExport = false
        showInfo = false
        viewModel.newProject()
    }

    BackHandler(enabled = showInfo || showExport) {
        if (showExport) {
            showExport = false
        } else {
            showInfo = false
        }
    }

    LaunchedEffect(notice?.id) {
        val id = notice?.id ?: return@LaunchedEffect
        delay(3000)
        viewModel.dismissNotice(id)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Kadraj.Ink)
            .appBackdrop(),
    ) {
        if (images.isEmpty()) {
            EmptyState(
                onPickImages = pickImages,
                onInfo = { showInfo = true },
                modifier = Modifier.fillMaxSize(),
                canUndo = viewModel.canUndo,
                canRedo = viewModel.canRedo,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
            )
        } else {
            EditorScreen(
                viewModel = viewModel,
                onPickImages = pickImages,
                onExport = openExport,
                onInfo = { showInfo = true },
                modifier = Modifier.fillMaxSize(),
            )
        }

        AnimatedVisibility(
            visible = notice != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 8.dp)
                .padding(horizontal = 20.dp),
            enter = slideInVertically(
                animationSpec = tween(220),
                initialOffsetY = { -it },
            ) + fadeIn(tween(180)),
            exit = slideOutVertically(
                animationSpec = tween(180),
                targetOffsetY = { -it },
            ) + fadeOut(tween(140)),
        ) {
            NoticePill(message = notice?.message.orEmpty())
        }

        ModalSheet(
            visible = showInfo,
            onDismiss = { showInfo = false },
        ) {
            InfoSheet(
                hasImages = images.isNotEmpty(),
                onClose = { showInfo = false },
                onNewProject = startNewProject,
            )
        }

        ModalSheet(
            visible = showExport,
            onDismiss = { showExport = false },
        ) {
            ExportSheet(
                viewModel = viewModel,
                onClose = { showExport = false },
                onNewProject = startNewProject,
            )
        }
    }
}

private fun Modifier.appBackdrop(): Modifier = drawBehind {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Kadraj.InkRaised,
                Kadraj.Ink,
                Color(0xFF090806),
            ),
        ),
    )

    val terracottaCenter = Offset(size.width * 0.13f, size.height * 0.06f)
    val terracottaRadius = size.width * 0.95f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Kadraj.Terracotta.copy(alpha = 0.075f),
                Color.Transparent,
            ),
            center = terracottaCenter,
            radius = terracottaRadius,
        ),
        radius = terracottaRadius,
        center = terracottaCenter,
    )

    val mossCenter = Offset(size.width * 0.92f, size.height * 0.78f)
    val mossRadius = size.width * 0.8f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Kadraj.Moss.copy(alpha = 0.035f),
                Color.Transparent,
            ),
            center = mossCenter,
            radius = mossRadius,
        ),
        radius = mossRadius,
        center = mossCenter,
    )

    drawLine(
        color = Kadraj.Line,
        start = Offset(size.width * 0.075f, 0f),
        end = Offset(size.width * 0.075f, size.height),
        strokeWidth = 1f,
    )
    drawLine(
        color = Kadraj.Line,
        start = Offset(size.width * 0.925f, 0f),
        end = Offset(size.width * 0.925f, size.height),
        strokeWidth = 1f,
    )

    val random = Random(17)
    repeat(70) {
        drawCircle(
            color = Kadraj.Paper.copy(alpha = 0.02f),
            radius = 0.65f.dp.toPx(),
            center = Offset(
                random.nextFloat() * size.width,
                random.nextFloat() * size.height,
            ),
        )
    }
}
