package com.omersusin.kadraj

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.omersusin.kadraj.collage.AspectPreset
import com.omersusin.kadraj.collage.CaptionAlignment
import com.omersusin.kadraj.collage.CaptionFont
import com.omersusin.kadraj.collage.CaptionPlacement
import com.omersusin.kadraj.collage.CaptionStyle
import com.omersusin.kadraj.collage.CaptionTone
import com.omersusin.kadraj.collage.CollageConfig
import com.omersusin.kadraj.collage.CollageStyle
import com.omersusin.kadraj.collage.FitMode
import com.omersusin.kadraj.collage.ImageSpec
import com.omersusin.kadraj.collage.LayoutEngine
import com.omersusin.kadraj.collage.LayoutResult
import com.omersusin.kadraj.collage.Palette
import com.omersusin.kadraj.collage.Palettes
import com.omersusin.kadraj.collage.PhotoLook
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
    var canUndo: Boolean by mutableStateOf(false)
        private set
    var canRedo: Boolean by mutableStateOf(false)
        private set

    val imageLoader: ImageLoader get() = loader

    private var nextId = 1L
    private var nextNotice = 1L
    private val random = Random()
    private val undoStack = ArrayDeque<EditorSnapshot>()
    private val redoStack = ArrayDeque<EditorSnapshot>()
    private var lastHistoryKey: String? = null
    private var lastHistoryAt = 0L

    val selectedImage: CollageImage?
        get() = selectedId?.let { id -> images.firstOrNull { it.id == id } }

    fun addImages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        if (isImporting) {
            showNotice("Images are still being prepared.")
            return
        }
        val remaining = MAX_IMAGES - images.size
        if (remaining <= 0) {
            showNotice("A collage can hold up to $MAX_IMAGES images.")
            return
        }

        val unique = uris.distinctBy { it.toString() }
        val selected = unique.take(remaining)
        val truncated = unique.size > selected.size
        val already = images.map { it.uri.toString() }.toSet()
        val candidates = selected.filterNot { it.toString() in already }
        if (candidates.isEmpty()) {
            showNotice(if (truncated) "Only $MAX_IMAGES images can be added." else "Those images are already in the collage.")
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
                recordHistory()
                images = images + added
                selectedId = added.first().id
            }
            isImporting = false
            when {
                added.isEmpty() -> showNotice("Couldn't open those images. Try another selection.")
                added.size < candidates.size -> showNotice("Some images couldn't be opened.")
                truncated -> showNotice("Added the first $MAX_IMAGES images.")
            }
        }
    }

    fun selectImage(id: Long?) {
        selectedId = id
    }

    fun removeImage(id: Long) {
        val index = images.indexOfFirst { it.id == id }
        if (index < 0) return
        recordHistory()
        val remaining = images.filterNot { it.id == id }
        images = remaining
        if (selectedId == id) {
            selectedId = remaining.getOrNull(index.coerceAtMost(remaining.lastIndex))?.id
        }
    }

    fun duplicateImage(id: Long) {
        val index = images.indexOfFirst { it.id == id }
        if (index < 0) return
        if (images.size >= MAX_IMAGES) {
            showNotice("A collage can hold up to $MAX_IMAGES images.")
            return
        }
        recordHistory()
        val copy = images[index].copy(id = nextId++)
        val mutable = images.toMutableList()
        mutable.add(index + 1, copy)
        images = mutable
        selectedId = copy.id
        showNotice("Photo duplicated.")
    }

    fun moveImage(id: Long, delta: Int) {
        val from = images.indexOfFirst { it.id == id }
        if (from < 0) return
        val to = (from + delta).coerceIn(0, images.lastIndex)
        if (to == from) return
        recordHistory()
        val mutable = images.toMutableList()
        val item = mutable.removeAt(from)
        mutable.add(to, item)
        images = mutable
    }

    fun shuffle() {
        if (images.size < 2) return
        recordHistory()
        val mutable = images.toMutableList()
        mutable.shuffle(random)
        images = mutable
    }

    fun toggleFit(id: Long) {
        val image = images.firstOrNull { it.id == id } ?: return
        recordHistory()
        images = images.map {
            if (it.id == id) it.copy(fit = if (image.fit == FitMode.COVER) FitMode.FIT else FitMode.COVER) else it
        }
    }

    fun setFit(id: Long, fit: FitMode) {
        if (images.none { it.id == id }) return
        recordHistory()
        images = images.map { image ->
            if (image.id == id) image.copy(fit = fit) else image
        }
    }

    fun updateFocus(id: Long, focusX: Float, focusY: Float) {
        if (images.none { it.id == id }) return
        recordHistory("focus-$id")
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

    fun setLook(id: Long, look: PhotoLook) {
        val image = images.firstOrNull { it.id == id } ?: return
        if (image.look == look) return
        recordHistory()
        images = images.map { if (it.id == id) it.copy(look = look) else it }
    }

    fun setLookAmount(id: Long, amount: Float) {
        if (images.none { it.id == id }) return
        recordHistory("look-amount-$id")
        images = images.map {
            if (it.id == id) it.copy(lookAmount = amount.coerceIn(0f, 1f)) else it
        }
    }

    fun setBrightness(id: Long, value: Float) {
        if (images.none { it.id == id }) return
        recordHistory("brightness-$id")
        images = images.map {
            if (it.id == id) it.copy(brightness = value.coerceIn(-1f, 1f)) else it
        }
    }

    fun setContrast(id: Long, value: Float) {
        if (images.none { it.id == id }) return
        recordHistory("contrast-$id")
        images = images.map {
            if (it.id == id) it.copy(contrast = value.coerceIn(-1f, 1f)) else it
        }
    }

    fun setSaturation(id: Long, value: Float) {
        if (images.none { it.id == id }) return
        recordHistory("saturation-$id")
        images = images.map {
            if (it.id == id) it.copy(saturation = value.coerceIn(-1f, 1f)) else it
        }
    }

    fun setWarmth(id: Long, value: Float) {
        if (images.none { it.id == id }) return
        recordHistory("warmth-$id")
        images = images.map {
            if (it.id == id) it.copy(warmth = value.coerceIn(-1f, 1f)) else it
        }
    }

    fun resetTone(id: Long) {
        val image = images.firstOrNull { it.id == id } ?: return
        if (image.brightness == 0f &&
            image.contrast == 0f &&
            image.saturation == 0f &&
            image.warmth == 0f
        ) {
            return
        }
        recordHistory()
        images = images.map {
            if (it.id == id) {
                it.copy(
                    brightness = 0f,
                    contrast = 0f,
                    saturation = 0f,
                    warmth = 0f,
                )
            } else {
                it
            }
        }
        showNotice("Photo tone reset.")
    }

    fun applyTreatmentToAll(id: Long) {
        val source = images.firstOrNull { it.id == id } ?: return
        if (images.size < 2) return
        recordHistory()
        images = images.map { image ->
            image.copy(
                look = source.look,
                lookAmount = source.lookAmount,
                brightness = source.brightness,
                contrast = source.contrast,
                saturation = source.saturation,
                warmth = source.warmth,
            )
        }
        showNotice("This treatment now runs through every photo.")
    }

    fun autoEnhance() {
        if (images.isEmpty()) return
        recordHistory()
        images = images.map { image ->
            image.copy(
                look = if (image.look == PhotoLook.ORIGINAL) PhotoLook.FILM else image.look,
                lookAmount = if (image.look == PhotoLook.ORIGINAL) 0.42f else image.lookAmount,
                brightness = (image.brightness + 0.04f).coerceIn(-1f, 1f),
                contrast = (image.contrast + 0.10f).coerceIn(-1f, 1f),
                saturation = (image.saturation + 0.08f).coerceIn(-1f, 1f),
                warmth = (image.warmth + 0.12f).coerceIn(-1f, 1f),
            )
        }
        showNotice("A little more life, in every frame.")
    }

    fun setZoom(id: Long, zoom: Float) {
        if (images.none { it.id == id }) return
        recordHistory("zoom-$id")
        images = images.map {
            if (it.id == id) it.copy(zoom = zoom.coerceIn(1f, 3f)) else it
        }
    }

    fun setTilt(id: Long, tilt: Float) {
        if (images.none { it.id == id }) return
        recordHistory("tilt-$id")
        images = images.map {
            if (it.id == id) it.copy(tilt = tilt.coerceIn(-12f, 12f)) else it
        }
    }

    fun toggleFlipHorizontal(id: Long) {
        if (images.none { it.id == id }) return
        recordHistory()
        images = images.map { if (it.id == id) it.copy(flipX = !it.flipX) else it }
    }

    fun toggleFlipVertical(id: Long) {
        if (images.none { it.id == id }) return
        recordHistory()
        images = images.map { if (it.id == id) it.copy(flipY = !it.flipY) else it }
    }

    fun resetPhoto(id: Long) {
        if (images.none { it.id == id }) return
        recordHistory()
        images = images.map {
            if (it.id == id) {
                it.copy(
                    look = PhotoLook.ORIGINAL,
                    lookAmount = 1f,
                    zoom = 1f,
                    tilt = 0f,
                    flipX = false,
                    flipY = false,
                    brightness = 0f,
                    contrast = 0f,
                    saturation = 0f,
                    warmth = 0f,
                )
            } else {
                it
            }
        }
        showNotice("Photo adjustments reset.")
    }

    fun updateTemplate(value: Template) {
        if (template == value) return
        recordHistory()
        template = value
        if (value == Template.FLOW && aspect != AspectPreset.AUTO) {
            aspect = AspectPreset.AUTO
            showNotice("Flow sets its own canvas ratio.")
        }
    }

    fun updateAspect(value: AspectPreset) {
        if (aspect == value) return
        recordHistory()
        aspect = value
        if (value != AspectPreset.AUTO && template == Template.FLOW) {
            template = Template.GRID
        }
    }

    fun setPalette(value: Palette) {
        if (style.palette.id == value.id) return
        recordHistory()
        style = style.copy(palette = value)
    }

    fun setGap(value: Float) {
        recordHistory("gap")
        style = style.copy(gap = value.coerceIn(0f, 0.06f))
    }

    fun setMargin(value: Float) {
        recordHistory("margin")
        style = style.copy(margin = value.coerceIn(0f, 0.12f))
    }

    fun setRadius(value: Float) {
        recordHistory("radius")
        style = style.copy(radius = value.coerceIn(0f, 0.1f))
    }

    fun setMat(value: Float) {
        recordHistory("mat")
        style = style.copy(mat = value.coerceIn(0f, 0.08f))
    }

    fun toggleMat() {
        recordHistory()
        style = style.copy(mat = if (style.mat > 0.002f) 0f else 0.026f)
    }

    fun toggleShadow() {
        recordHistory()
        style = style.copy(shadow = !style.shadow)
    }

    fun toggleTexture() {
        recordHistory()
        style = style.copy(texture = !style.texture)
    }

    fun randomizeStyle() {
        recordHistory()
        val palette = pick(Palettes.all.filter { it.id != style.palette.id })
        style = CollageStyle(
            palette = palette,
            gap = pick(listOf(0f, 0.008f, 0.014f, 0.022f)),
            margin = pick(listOf(0.02f, 0.035f, 0.05f)),
            radius = pick(listOf(0f, 0.02f, 0.04f, 0.065f)),
            mat = pick(listOf(0f, 0f, 0.026f)),
            shadow = random.nextBoolean(),
            texture = true,
            caption = style.caption,
        )
        showNotice("A new look, composed.")
    }

    fun resetStyle() {
        recordHistory()
        style = CollageStyle(caption = style.caption)
        showNotice("Style reset.")
    }

    fun setCaptionText(value: String) {
        updateCaption("caption-text") { it.copy(text = value.take(120)) }
    }

    fun setCaptionFont(value: CaptionFont) {
        updateCaption { it.copy(font = value) }
    }

    fun setCaptionPlacement(value: CaptionPlacement) {
        updateCaption { it.copy(placement = value) }
    }

    fun setCaptionAlignment(value: CaptionAlignment) {
        updateCaption { it.copy(alignment = value) }
    }

    fun setCaptionTone(value: CaptionTone) {
        updateCaption { it.copy(tone = value) }
    }

    fun setCaptionSize(value: Float) {
        updateCaption("caption-size") { it.copy(size = value.coerceIn(0.025f, 0.075f)) }
    }

    fun toggleCaptionPlate() {
        updateCaption { it.copy(plate = !it.plate) }
    }

    private fun updateCaption(
        key: String? = null,
        transform: (CaptionStyle) -> CaptionStyle,
    ) {
        recordHistory(key)
        style = style.copy(caption = transform(style.caption))
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
            showNotice("Add a few images first.")
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
                showNotice("Saved to Photos.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                exportState = ExportState.Failed(
                    "Couldn't export. Try a smaller size or JPEG.",
                )
            }
        }
    }

    fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(snapshot())
        restore(previous)
        lastHistoryKey = null
        updateHistoryFlags()
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(snapshot())
        restore(next)
        lastHistoryKey = null
        updateHistoryFlags()
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
                look = it.look,
                lookAmount = it.lookAmount,
                zoom = it.zoom,
                tilt = it.tilt,
                flipX = it.flipX,
                flipY = it.flipY,
                brightness = it.brightness,
                contrast = it.contrast,
                saturation = it.saturation,
                warmth = it.warmth,
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
        undoStack.clear()
        redoStack.clear()
        lastHistoryKey = null
        updateHistoryFlags()
    }

    private fun snapshot() = EditorSnapshot(
        images = images,
        style = style,
        template = template,
        aspect = aspect,
        selectedId = selectedId,
    )

    private fun restore(snapshot: EditorSnapshot) {
        images = snapshot.images
        style = snapshot.style
        template = snapshot.template
        aspect = snapshot.aspect
        selectedId = snapshot.selectedId
        exportState = ExportState.Idle
    }

    private fun recordHistory(key: String? = null) {
        val now = System.currentTimeMillis()
        if (key != null && key == lastHistoryKey && now - lastHistoryAt < 1000L) {
            lastHistoryAt = now
            return
        }
        undoStack.addLast(snapshot())
        while (undoStack.size > HISTORY_LIMIT) undoStack.removeFirst()
        redoStack.clear()
        lastHistoryKey = key
        lastHistoryAt = now
        updateHistoryFlags()
    }

    private fun updateHistoryFlags() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }

    private fun <T> pick(values: List<T>): T = values[random.nextInt(values.size)]

    private data class EditorSnapshot(
        val images: List<CollageImage>,
        val style: CollageStyle,
        val template: Template,
        val aspect: AspectPreset,
        val selectedId: Long?,
    )

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
        private const val HISTORY_LIMIT = 40
    }
}
