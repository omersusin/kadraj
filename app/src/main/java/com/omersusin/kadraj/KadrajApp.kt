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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicText
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.omersusin.kadraj.ui.components.AuroraBackdrop
import com.omersusin.kadraj.ui.components.ButtonTone
import com.omersusin.kadraj.ui.components.KadrajButton
import com.omersusin.kadraj.ui.components.ModalSheet
import com.omersusin.kadraj.ui.components.NoticePill
import com.omersusin.kadraj.ui.screens.EditorScreen
import com.omersusin.kadraj.ui.screens.EmptyState
import com.omersusin.kadraj.ui.screens.ExportSheet
import com.omersusin.kadraj.ui.screens.InfoSheet
import com.omersusin.kadraj.ui.theme.Kadraj
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun KadrajApp(
    viewModel: EditorViewModel = viewModel(),
    startWithDemo: Boolean = false,
    initialTab: String? = null,
) {
    var showInfo by rememberSaveable { mutableStateOf(false) }
    var showExport by rememberSaveable { mutableStateOf(false) }
    var showCloseConfirm by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val images = viewModel.images
    val notice = viewModel.notice

    LaunchedEffect(startWithDemo) {
        if (!BuildConfig.DEBUG || !startWithDemo || images.isNotEmpty()) {
            return@LaunchedEffect
        }
        val demoUris = withContext(Dispatchers.IO) {
            listOf(
                R.drawable.demo_portrait to "portrait.jpg",
                R.drawable.demo_architecture to "architecture.jpg",
                R.drawable.demo_coffee to "coffee.jpg",
            ).mapNotNull { (resource, name) ->
                runCatching {
                    val file = File(context.cacheDir, "demo_$name")
                    context.resources.openRawResource(resource).use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                    Uri.fromFile(file)
                }.getOrNull()
            }
        }
        if (demoUris.isNotEmpty()) {
            viewModel.addImages(demoUris)
        }
    }

    val systemPickerMax = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            MediaStore.getPickImagesMaxLimit()
        } else {
            EditorViewModel.MAX_IMAGES
        }
    }
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            minOf(EditorViewModel.MAX_IMAGES, systemPickerMax).coerceAtLeast(2),
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
        showCloseConfirm = false
        viewModel.newProject()
    }

    BackHandler(enabled = showInfo || showExport) {
        if (showExport) {
            showExport = false
        } else {
            showInfo = false
        }
    }

    BackHandler(enabled = showCloseConfirm) {
        showCloseConfirm = false
    }

    BackHandler(
        enabled = images.isNotEmpty() &&
            !showInfo &&
            !showExport &&
            !showCloseConfirm,
    ) {
        showCloseConfirm = true
    }

    LaunchedEffect(notice?.id) {
        val id = notice?.id ?: return@LaunchedEffect
        delay(3000)
        viewModel.dismissNotice(id)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Kadraj.Ink),
    ) {
        AuroraBackdrop()

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
                onCloseProject = { showCloseConfirm = true },
                initialTab = initialTab,
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

        ModalSheet(
            visible = showCloseConfirm,
            onDismiss = { showCloseConfirm = false },
        ) {
            BasicText(
                text = "Close collage?",
                style = Kadraj.Type.Display.copy(
                    color = Kadraj.Paper,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                ),
            )
            Spacer(Modifier.height(8.dp))
            BasicText(
                text = "Your current edits will be cleared. Export first if you want to keep this collage.",
                style = Kadraj.Type.Body.copy(color = Kadraj.Muted),
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KadrajButton(
                    label = "Keep editing",
                    onClick = { showCloseConfirm = false },
                    modifier = Modifier.weight(1f),
                    tone = ButtonTone.Quiet,
                )
                KadrajButton(
                    label = "Close",
                    onClick = {
                        showCloseConfirm = false
                        viewModel.newProject()
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
