package com.omersusin.kadraj.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.omersusin.kadraj.collage.CollageStyle
import com.omersusin.kadraj.collage.LayoutResult
import com.omersusin.kadraj.image.ImageLoader
import com.omersusin.kadraj.render.CaptionTypefaces
import com.omersusin.kadraj.render.CollagePainter
import com.omersusin.kadraj.render.ImageProvider
import com.omersusin.kadraj.render.ImageRenderState
import com.omersusin.kadraj.render.ProvidedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

enum class ExportSize(val label: String, val longSide: Int) {
    AUTO("Auto", 2560),
    HIGH("2K", 3200),
    ULTRA("4K", 3840),
}

enum class ExportFormat(
    val label: String,
    val extension: String,
    val mimeType: String,
) {
    JPEG("JPG", "jpg", "image/jpeg"),
    PNG("PNG", "png", "image/png"),
}

data class ExportResult(
    val uri: Uri,
    val width: Int,
    val height: Int,
    val format: ExportFormat,
    val fileName: String,
)

class CollageExporter(
    private val context: Context,
    private val loader: ImageLoader,
) {
    suspend fun export(
        layout: LayoutResult,
        style: CollageStyle,
        imageStates: Map<Long, ImageRenderState>,
        uriById: Map<Long, Uri>,
        size: ExportSize,
        format: ExportFormat,
    ): ExportResult = withContext(Dispatchers.Default) {
        val bitmap = renderWithFallback(layout, style, imageStates, uriById, size)
        try {
            val saved = withContext(Dispatchers.IO) {
                saveToGallery(bitmap, format)
            }
            ExportResult(
                uri = saved.first,
                width = bitmap.width,
                height = bitmap.height,
                format = format,
                fileName = saved.second,
            )
        } finally {
            bitmap.recycle()
        }
    }

    private fun renderWithFallback(
        layout: LayoutResult,
        style: CollageStyle,
        imageStates: Map<Long, ImageRenderState>,
        uriById: Map<Long, Uri>,
        size: ExportSize,
    ): Bitmap {
        val requested = dimensions(layout.aspect, size.longSide)
        return try {
            render(layout, style, imageStates, uriById, requested.first, requested.second)
        } catch (_: OutOfMemoryError) {
            val fallback = dimensions(layout.aspect, minOf(size.longSide, 2560))
            render(layout, style, imageStates, uriById, fallback.first, fallback.second)
        }
    }

    private fun render(
        layout: LayoutResult,
        style: CollageStyle,
        imageStates: Map<Long, ImageRenderState>,
        uriById: Map<Long, Uri>,
        width: Int,
        height: Int,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val provider = ImageProvider { imageId, targetWidth, targetHeight ->
            val uri = uriById[imageId] ?: return@ImageProvider null
            val state = imageStates[imageId]
            val zoom = state?.zoom ?: 1f
            val needed = (max(targetWidth, targetHeight) * max(1f, zoom) * 1.45f)
                .roundToInt()
                .coerceIn(96, 4096)
            loader.decodeForExport(uri, needed)?.let { decoded ->
                ProvidedImage(decoded, recycleAfterUse = true)
            }
        }
        try {
            CollagePainter.draw(
                canvas = android.graphics.Canvas(bitmap),
                width = width,
                height = height,
                layout = layout,
                style = style,
                provider = provider,
                imageStates = imageStates,
                captionTypeface = CaptionTypefaces.load(context, style.caption.font),
            )
        } catch (error: Throwable) {
            bitmap.recycle()
            throw error
        }
        return bitmap
    }

    private suspend fun saveToGallery(bitmap: Bitmap, format: ExportFormat): Pair<Uri, String> {
        val resolver = context.contentResolver
        val name = "Kadraj_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.${format.extension}"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Kadraj")
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Couldn't open a place in Photos.")
        try {
            resolver.openOutputStream(uri)?.use { stream ->
                val compressed = bitmap.compress(
                    if (format == ExportFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                    96,
                    stream,
                )
                if (!compressed) error("Couldn't compress the image.")
            } ?: error("Couldn't open the output stream.")
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri to name
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    private fun dimensions(aspect: Float, longSide: Int): Pair<Int, Int> {
        val safeAspect = aspect.coerceIn(0.2f, 4.5f)
        val width: Int
        val height: Int
        if (safeAspect >= 1f) {
            width = longSide
            height = (longSide / safeAspect).roundToInt().coerceAtLeast(2)
        } else {
            height = longSide
            width = (longSide * safeAspect).roundToInt().coerceAtLeast(2)
        }
        return width to height
    }
}
