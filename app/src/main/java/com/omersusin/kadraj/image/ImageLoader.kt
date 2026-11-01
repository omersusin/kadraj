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
import kotlin.math.max

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
        val cacheSize = (maxMemory / 6L).coerceIn(24L * 1024L * 1024L, 96L * 1024L * 1024L)
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

    fun decodeForExport(uri: Uri, neededDimension: Int): Bitmap? {
        val target = neededDimension.coerceIn(96, 4096)
        return decode(uri, target)
    }

    private fun decode(uri: Uri, maxDimension: Int): Bitmap? {
        return try {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                val longest = max(info.size.width, info.size.height)
                var sample = 1
                while (longest / (sample * 2) >= maxDimension) {
                    sample *= 2
                }
                decoder.setTargetSampleSize(sample)
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
            }
        } catch (_: OutOfMemoryError) {
            try {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                    val longest = max(info.size.width, info.size.height)
                    var sample = 1
                    val fallback = (maxDimension / 2).coerceAtLeast(64)
                    while (longest / (sample * 2) >= fallback) {
                        sample *= 2
                    }
                    decoder.setTargetSampleSize(sample)
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = false
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

    private fun key(uri: Uri, maxDimension: Int): String = "$uri@$maxDimension"

    companion object {
        const val PREVIEW_DIMENSION = 1280
    }
}
