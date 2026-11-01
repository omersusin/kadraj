package com.omersusin.kadraj

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.omersusin.kadraj.collage.AspectPreset
import com.omersusin.kadraj.collage.CollageConfig
import com.omersusin.kadraj.collage.CollageStyle
import com.omersusin.kadraj.collage.FitMode
import com.omersusin.kadraj.collage.ImageSpec
import com.omersusin.kadraj.collage.LayoutEngine
import com.omersusin.kadraj.collage.LayoutResult
import com.omersusin.kadraj.collage.Palette
import com.omersusin.kadraj.collage.Palettes
import com.omersusin.kadraj.collage.Template
import com.omersusin.kadraj.export.CollageExporter
import com.omersusin.kadraj.export.ExportFormat
import com.omersusin.kadraj.export.ExportResult
import com.omersusin.kadraj.export.ExportSize
import com.omersusin.kadraj.image.ImageLoader
import com.omersusin.kadraj.render.ImageRenderState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val loader = ImageLoader(application.contentResolver)
    private val exporter = CollageExporter(application, loader)

    var images: List<CollageImage> by mutableStateOf(emptyList())
        private set
    var style: CollageStyle by mutableStateOf(CollageStyle())
        private set
    var template: Template by mutableStateOf(Template.AUTO)
        private set
    var aspect: AspectPreset by mutableStateOf(AspectPreset.AUTO)
        private set
    var selectedId: Long? by mutableStateOf(null)
        private set
    var isImporting: Boolean by mutableStateOf(false)
        private set
    var notice: UiNotice? by mutableStateOf(null)
        private set
    var exportSize: ExportSize by mutableStateOf(ExportSize.AUTO)
        private set
    var exportFormat: ExportFormat by mutableStateOf(ExportFormat.JPEG)
        private set
    var exportState: ExportState by mutableStateOf(ExportState.Idle)
        private set

    val imageLoader: ImageLoader get() = loader

    private var nextId = 1L
    private var nextNotice = 1L
    private val random = Random()

    fun addImages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        if (isImporting) {
            showNotice("Görseller hâlâ hazırlanıyor.")
            return
        }
        val remaining = MAX_IMAGES - images.size
        if (remaining <= 0) {
            showNotice("Bir kolajda en fazla $MAX_IMAGES görsel olabilir.")
            return
        }

        val unique = uris.distinctBy { it.toString() }
        val selected = unique.take(remaining)
        val truncated = unique.size > selected.size
        val already = images.map { it.uri.toString() }.toSet()
        val candidates = selected.filterNot { it.toString() in already }
        if (candidates.isEmpty()) {
            showNotice(if (truncated) "En fazla $MAX_IMAGES görsel eklenebilir." else "Bu görseller zaten ekli.")
            return
        }

        isImporting = true
        viewModelScope.launch {
            val inspected = withContext(Dispatchers.IO) {
                candidates.mapNotNull { uri ->
                    loader.inspect(uri)?.let { info -> uri to info }
                }
            }
            val added = inspected.map { (uri, info) ->
                CollageImage(
                    id = nextId++,
                    uri = uri,
                    width = info.width.coerceAtLeast(1),
                    height = info.height.coerceAtLeast(1),
                )
            }
            if (added.isNotEmpty()) {
                images = images + added
                selectedId = added.first().id
            }
            isImporting = false
            when {
                added.isEmpty() -> showNotice("Görseller açılamadı. Başka bir seçim deneyin.")
                added.size < candidates.size -> showNotice("Bazı görseller açılamadı.")
                truncated -> showNotice("En fazla $MAX_IMAGES görsel eklendi.")
            }
        }
    }

    fun selectImage(id: Long?) {
        selectedId = id
    }

    fun removeImage(id: Long) {
        images = images.filterNot { it.id == id }
        if (selectedId == id) selectedId = null
    }

    fun moveImage(id: Long, delta: Int) {
        val from = images.indexOfFirst { it.id == id }
        if (from < 0) return
        val to = (from + delta).coerceIn(0, images.lastIndex)
        if (to == from) return
        val mutable = images.toMutableList()
        val item = mutable.removeAt(from)
        mutable.add(to, item)
        images = mutable
    }

    fun shuffle() {
        if (images.size < 2) return
        val mutable = images.toMutableList()
        mutable.shuffle(random)
        images = mutable
    }

    fun toggleFit(id: Long) {
        images = images.map { image ->
            if (image.id == id) {
                image.copy(
                    fit = if (image.fit == FitMode.COVER) FitMode.FIT else FitMode.COVER,
                )
            } else {
                image
            }
        }
    }

    fun setFit(id: Long, fit: FitMode) {
        images = images.map { image ->
            if (image.id == id) image.copy(fit = fit) else image
        }
    }

    fun updateFocus(id: Long, focusX: Float, focusY: Float) {
        images = images.map { image ->
            if (image.id == id) {
                image.copy(
                    focusX = focusX.coerceIn(0f, 1f),
                    focusY = focusY.coerceIn(0f, 1f),
                )
            } else {
                image
            }
        }
    }

    fun updateTemplate(value: Template) {
        template = value
        if (value == Template.FLOW && aspect != AspectPreset.AUTO) {
            aspect = AspectPreset.AUTO
            showNotice("Akış kendi oranını kurar.")
        }
    }

    fun updateAspect(value: AspectPreset) {
        aspect = value
        if (value != AspectPreset.AUTO && template == Template.FLOW) {
            template = Template.GRID
        }
    }

    fun setPalette(value: Palette) {
        style = style.copy(palette = value)
    }

    fun setGap(value: Float) {
        style = style.copy(gap = value.coerceIn(0f, 0.06f))
    }

    fun setMargin(value: Float) {
        style = style.copy(margin = value.coerceIn(0f, 0.12f))
    }

    fun setRadius(value: Float) {
        style = style.copy(radius = value.coerceIn(0f, 0.1f))
    }

    fun setMat(value: Float) {
        style = style.copy(mat = value.coerceIn(0f, 0.08f))
    }

    fun toggleMat() {
        style = style.copy(mat = if (style.mat > 0.002f) 0f else 0.026f)
    }

    fun toggleShadow() {
        style = style.copy(shadow = !style.shadow)
    }

    fun toggleTexture() {
        style = style.copy(texture = !style.texture)
    }

    fun updateExportSize(value: ExportSize) {
        exportSize = value
    }

    fun updateExportFormat(value: ExportFormat) {
        exportFormat = value
    }

    fun resetExport() {
        if (exportState !is ExportState.Rendering) {
            exportState = ExportState.Idle
        }
    }

    fun export() {
        if (images.isEmpty()) {
            showNotice("Önce birkaç görsel ekleyin.")
            return
        }
        if (exportState is ExportState.Rendering) return

        val layout = buildLayout()
        val states = imageStates()
        val uris = images.associate { it.id to it.uri }
        exportState = ExportState.Rendering
        viewModelScope.launch {
            try {
                val result = exporter.export(
                    layout = layout,
                    style = style,
                    imageStates = states,
                    uriById = uris,
                    size = exportSize,
                    format = exportFormat,
                )
                exportState = ExportState.Saved(result)
                showNotice("Galeriye kaydedildi.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                exportState = ExportState.Failed(
                    "Dışa aktarılamadı. Daha küçük bir boyut ya da JPG deneyin.",
                )
            }
        }
    }

    fun showNotice(message: String) {
        notice = UiNotice(nextNotice++, message)
    }

    fun dismissNotice(id: Long) {
        if (notice?.id == id) notice = null
    }

    fun buildLayout(): LayoutResult =
        LayoutEngine.compose(
            images = images.map { ImageSpec(it.id, it.aspect) },
            config = currentConfig(),
        )

    fun currentConfig(): CollageConfig = style.toConfig(template, aspect)

    fun imageStates(): Map<Long, ImageRenderState> =
        images.associate {
            it.id to ImageRenderState(
                fit = it.fit,
                focusX = it.focusX,
                focusY = it.focusY,
            )
        }

    fun newProject() {
        images = emptyList()
        template = Template.AUTO
        aspect = AspectPreset.AUTO
        selectedId = null
        style = CollageStyle()
        exportState = ExportState.Idle
        notice = null
    }

    data class UiNotice(
        val id: Long,
        val message: String,
    )

    sealed interface ExportState {
        data object Idle : ExportState
        data object Rendering : ExportState
        data class Saved(val result: ExportResult) : ExportState
        data class Failed(val message: String) : ExportState
    }

    companion object {
        const val MAX_IMAGES = 12
    }
}
