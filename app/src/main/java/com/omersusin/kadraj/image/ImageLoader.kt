package com.omersusin.kadraj.image

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ExifInterface
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.omersusin.kadraj.collage.ToneAnalysis
import kotlin.math.max
import kotlin.math.sqrt

data class ImageInfo(
    val width: Int,
    val height: Int,
) {
    val aspect: Float get() = width.toFloat() / height.toFloat()
}

class ImageLoader(
    private val resolver: ContentResolver,
) {
    private val cache: LruCache<String, Bitmap>

    init {
        val maxMemory = Runtime.getRuntime().maxMemory()
        val cacheSize = (maxMemory / 4L).coerceIn(40L * 1024L * 1024L, 96L * 1024L * 1024L)
        cache = object : LruCache<String, Bitmap>(cacheSize.toInt()) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
        }
    }

    fun cached(uri: Uri, maxDimension: Int): Bitmap? =
        cache.get(key(uri, maxDimension))?.takeIf { !it.isRecycled }

    suspend fun inspect(uri: Uri): ImageInfo? = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        try {
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (_: Exception) {
            // Fall through to ImageDecoder below.
        }

        var width = options.outWidth
        var height = options.outHeight

        if (width <= 0 || height <= 0) {
            try {
                var size: android.util.Size? = null
                val tiny = ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                    size = info.size
                    decoder.setTargetSize(1, 1)
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = false
                }
                tiny.recycle()
                width = size?.width ?: 0
                height = size?.height ?: 0
            } catch (_: Exception) {
                return@withContext null
            }
        }

        if (width <= 0 || height <= 0) return@withContext null

        try {
            resolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
                if (orientation in setOf(
                        ExifInterface.ORIENTATION_ROTATE_90,
                        ExifInterface.ORIENTATION_ROTATE_270,
                        ExifInterface.ORIENTATION_TRANSPOSE,
                        ExifInterface.ORIENTATION_TRANSVERSE,
                    )
                ) {
                    val old = width
                    width = height
                    height = old
                }
            }
        } catch (_: Exception) {
            // Orientation is a best-effort improvement for layout decisions.
        }

        ImageInfo(width, height)
    }

    suspend fun preview(uri: Uri, maxDimension: Int = PREVIEW_DIMENSION): Bitmap? =
        withContext(Dispatchers.IO) {
            val target = maxDimension.coerceIn(64, 2400)
            val cacheKey = key(uri, target)
            cache.get(cacheKey)?.takeIf { !it.isRecycled }?.let { return@withContext it }

            val bitmap = decode(uri, target) ?: return@withContext null
            cache.put(cacheKey, bitmap)
            bitmap
        }

    suspend fun analyze(uri: Uri): ToneAnalysis? = withContext(Dispatchers.IO) {
        val bitmap = preview(uri, 112) ?: return@withContext null
        if (bitmap.width <= 0 || bitmap.height <= 0) return@withContext null
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var sum = 0.0
        var sumSq = 0.0
        var saturation = 0.0
        var warmth = 0.0
        var focusWeight = 0.0
        var focusX = 0.0
        var focusY = 0.0
        var count = 0
        val step = max(1, minOf(bitmap.width, bitmap.height) / 72)
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val color = pixels[y * bitmap.width + x]
                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF
                val luminance = (0.2126f * r + 0.7152f * g + 0.0722f * b) / 255f
                val high = maxOf(r, maxOf(g, b))
                val low = minOf(r, minOf(g, b))
                val chroma = if (high == 0) 0f else (high - low).toFloat() / high
                sum += luminance.toDouble()
                sumSq += (luminance * luminance).toDouble()
                saturation += chroma.toDouble()
                warmth += (r - b).toDouble() / 255.0

                val rightIndex = (y * bitmap.width + (x + step).coerceAtMost(bitmap.width - 1))
                val belowIndex = ((y + step).coerceAtMost(bitmap.height - 1)) * bitmap.width + x
                val rightLuma = (((pixels[rightIndex] shr 16) and 0xFF) * 0.2126f +
                    ((pixels[rightIndex] shr 8) and 0xFF) * 0.7152f +
                    (pixels[rightIndex] and 0xFF) * 0.0722f) / 255f
                val belowLuma = (((pixels[belowIndex] shr 16) and 0xFF) * 0.2126f +
                    ((pixels[belowIndex] shr 8) and 0xFF) * 0.7152f +
                    (pixels[belowIndex] and 0xFF) * 0.0722f) / 255f
                val edge = kotlin.math.abs(luminance - rightLuma) +
                    kotlin.math.abs(luminance - belowLuma)
                val nx = x.toFloat() / bitmap.width
                val ny = y.toFloat() / bitmap.height
                val centerDistance = sqrt(
                    (nx - 0.5f) * (nx - 0.5f) + (ny - 0.5f) * (ny - 0.5f),
                ).toDouble()
                val weight = (edge * 2.4f + chroma * 1.15f + 0.01f) *
                    (1f - 0.32f * centerDistance.toFloat()).coerceAtLeast(0.35f)
                focusX += (nx * weight).toDouble()
                focusY += (ny * weight).toDouble()
                focusWeight += weight.toDouble()

                count++
                x += step
            }
            y += step
        }
        if (count == 0) return@withContext null
        val mean = sum / count
        val variance = (sumSq / count - mean * mean).coerceAtLeast(0.0)
        ToneAnalysis(
            luminance = mean.toFloat(),
            contrast = sqrt(variance).toFloat(),
            saturation = (saturation / count).toFloat(),
            warmth = (warmth / count).toFloat(),
            focusX = if (focusWeight > 0.0) {
                (focusX / focusWeight).toFloat().coerceIn(0.12f, 0.88f)
            } else {
                0.5f
            },
            focusY = if (focusWeight > 0.0) {
                (focusY / focusWeight).toFloat().coerceIn(0.12f, 0.88f)
            } else {
                0.5f
            },
        )
    }

    fun decodeForExport(uri: Uri, neededDimension: Int): Bitmap? {
        val target = neededDimension.coerceIn(96, 4096)
        return decode(uri, target)
    }

    private fun decode(uri: Uri, maxDimension: Int): Bitmap? {
        return try {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                configureTarget(decoder, info, maxDimension)
            }
        } catch (_: OutOfMemoryError) {
            try {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                    configureTarget(decoder, info, (maxDimension / 2).coerceAtLeast(64))
                }
            } catch (_: Exception) {
                null
            } catch (_: OutOfMemoryError) {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun configureTarget(
        decoder: ImageDecoder,
        info: ImageDecoder.ImageInfo,
        maxDimension: Int,
    ) {
        val longest = max(info.size.width, info.size.height)
        val target = maxDimension.coerceIn(64, 4096)
        if (longest > target) {
            val scale = target.toFloat() / longest.toFloat()
            decoder.setTargetSize(
                (info.size.width * scale).toInt().coerceAtLeast(1),
                (info.size.height * scale).toInt().coerceAtLeast(1),
            )
        } else {
            decoder.setTargetSize(info.size.width, info.size.height)
        }
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        decoder.isMutableRequired = false
    }

    private fun key(uri: Uri, maxDimension: Int): String = "$uri@$maxDimension"

    companion object {
        const val PREVIEW_DIMENSION = 1280
    }
}
