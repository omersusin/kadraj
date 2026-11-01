package com.omersusin.kadraj.render

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import com.omersusin.kadraj.collage.CollageCell
import com.omersusin.kadraj.collage.CollageStyle
import com.omersusin.kadraj.collage.FitMode
import com.omersusin.kadraj.collage.LayoutResult
import com.omersusin.kadraj.collage.Palette
import java.util.Random
import kotlin.math.max
import kotlin.math.min

data class ProvidedImage(
    val bitmap: Bitmap,
    val recycleAfterUse: Boolean = false,
)

data class ImageRenderState(
    val fit: FitMode = FitMode.COVER,
    val focusX: Float = 0.5f,
    val focusY: Float = 0.5f,
)

fun interface ImageProvider {
    fun load(imageId: Long, targetWidth: Int, targetHeight: Int): ProvidedImage?
}

object CollagePainter {
    private val grain: Bitmap by lazy { createGrain(128) }

    fun draw(
        canvas: Canvas,
        width: Int,
        height: Int,
        layout: LayoutResult,
        style: CollageStyle,
        provider: ImageProvider,
        imageStates: Map<Long, ImageRenderState> = emptyMap(),
        selectedId: Long? = null,
        selectionColor: Int = 0xFFD17252.toInt(),
    ) {
        if (width <= 0 || height <= 0) return
        val w = width.toFloat()
        val h = height.toFloat()
        val palette = style.palette

        drawBackground(canvas, w, h, palette)

        val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        layout.cells.sortedBy { it.z }.forEach { cell ->
            drawCell(
                canvas = canvas,
                cell = cell,
                canvasWidth = w,
                canvasHeight = h,
                style = style,
                provider = provider,
                imageStates = imageStates,
                imagePaint = imagePaint,
                selected = cell.imageId == selectedId,
                selectionColor = selectionColor,
            )
        }

        drawVignette(canvas, w, h, palette)
        if (style.texture) drawGrain(canvas, w, h, palette)
    }

    private fun drawBackground(canvas: Canvas, width: Float, height: Float, palette: Palette) {
        val paint = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                width * 0.55f,
                height,
                palette.colors.toIntArray(),
                floatArrayOf(0f, 0.56f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, width, height, paint)

        val glow = Paint().apply {
            shader = RadialGradient(
                width * 0.2f,
                height * 0.06f,
                max(width, height) * 0.95f,
                Color.argb(
                    if (palette.dark) 34 else 20,
                    Color.red(palette.accent),
                    Color.green(palette.accent),
                    Color.blue(palette.accent),
                ),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, width, height, glow)
    }

    private fun drawCell(
        canvas: Canvas,
        cell: CollageCell,
        canvasWidth: Float,
        canvasHeight: Float,
        style: CollageStyle,
        provider: ImageProvider,
        imageStates: Map<Long, ImageRenderState>,
        imagePaint: Paint,
        selected: Boolean,
        selectionColor: Int,
    ) {
        val rect = RectF(
            cell.rect.left * canvasWidth,
            cell.rect.top * canvasHeight,
            cell.rect.right * canvasWidth,
            cell.rect.bottom * canvasHeight,
        )
        if (rect.width() < 1f || rect.height() < 1f) return

        val minimum = min(rect.width(), rect.height())
        val palette = style.palette
        val radius = (style.radius.coerceIn(0f, 0.16f) * minimum)
            .coerceAtMost(minimum / 2f)
        val mat = style.mat.coerceIn(0f, 0.1f) * minimum

        canvas.save()
        if (cell.rotation != 0f) {
            canvas.rotate(cell.rotation, rect.centerX(), rect.centerY())
        }

        if (style.shadow) drawShadow(canvas, rect, radius, palette)

        canvas.save()
        clipRoundRect(canvas, rect, radius)

        if (mat > 0.01f) {
            canvas.drawRoundRect(
                rect,
                radius,
                radius,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.matte },
            )
        }

        val inner = rect.inset(mat, mat)
        if (inner.width() > 0.5f && inner.height() > 0.5f) {
            val innerRadius = (radius - mat * 0.72f).coerceAtLeast(0f)
            canvas.save()
            clipRoundRect(canvas, inner, innerRadius)
            drawImageContent(
                canvas = canvas,
                rect = inner,
                imageId = cell.imageId,
                provider = provider,
                imageStates = imageStates,
                paint = imagePaint,
                palette = palette,
            )
            canvas.restore()

            val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = max(1f, minimum * 0.0009f)
                color = if (palette.dark) {
                    Color.argb(28, 255, 255, 255)
                } else {
                    Color.argb(24, 0, 0, 0)
                }
            }
            canvas.drawRoundRect(inner, innerRadius, innerRadius, edge)
        }

        canvas.restore()

        if (selected) {
            val under = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = max(3f, minimum * 0.005f)
                color = Color.argb(150, 0, 0, 0)
            }
            val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = max(1.5f, minimum * 0.0024f)
                color = selectionColor
            }
            canvas.drawRoundRect(rect, radius, radius, under)
            canvas.drawRoundRect(rect, radius, radius, accent)
        }

        canvas.restore()
    }

    private fun drawImageContent(
        canvas: Canvas,
        rect: RectF,
        imageId: Long,
        provider: ImageProvider,
        imageStates: Map<Long, ImageRenderState>,
        paint: Paint,
        palette: Palette,
    ) {
        val provided = provider.load(
            imageId,
            rect.width().toInt().coerceAtLeast(1),
            rect.height().toInt().coerceAtLeast(1),
        )
        if (provided == null || provided.bitmap.isRecycled) {
            drawPlaceholder(canvas, rect, palette)
            return
        }

        val bitmap = provided.bitmap
        val state = imageStates[imageId] ?: ImageRenderState()
        val fitMode = state.fit
        val focusX = state.focusX.coerceIn(0f, 1f)
        val focusY = state.focusY.coerceIn(0f, 1f)

        try {
            if (fitMode == FitMode.FIT) {
                canvas.drawRect(
                    rect,
                    Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.surface },
                )
                val scale = min(rect.width() / bitmap.width, rect.height() / bitmap.height)
                val dstWidth = bitmap.width * scale
                val dstHeight = bitmap.height * scale
                val left = rect.left + (rect.width() - dstWidth) / 2f
                val top = rect.top + (rect.height() - dstHeight) / 2f
                canvas.drawBitmap(
                    bitmap,
                    Rect(0, 0, bitmap.width, bitmap.height),
                    RectF(left, top, left + dstWidth, top + dstHeight),
                    paint,
                )
            } else {
                val sourceAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
                val destinationAspect = rect.width() / rect.height()
                val src: Rect
                if (sourceAspect > destinationAspect) {
                    val sourceWidth = bitmap.height * destinationAspect
                    val left = ((bitmap.width - sourceWidth) * focusX).toInt()
                    src = Rect(
                        left.coerceAtLeast(0),
                        0,
                        (left + sourceWidth).toInt().coerceAtMost(bitmap.width),
                        bitmap.height,
                    )
                } else {
                    val sourceHeight = bitmap.width / destinationAspect
                    val top = ((bitmap.height - sourceHeight) * focusY).toInt()
                    src = Rect(
                        0,
                        top.coerceAtLeast(0),
                        bitmap.width,
                        (top + sourceHeight).toInt().coerceAtMost(bitmap.height),
                    )
                }
                if (src.width() > 0 && src.height() > 0) {
                    canvas.drawBitmap(bitmap, src, rect, paint)
                }
            }
        } finally {
            if (provided.recycleAfterUse) bitmap.recycle()
        }
    }

    private fun drawPlaceholder(canvas: Canvas, rect: RectF, palette: Palette) {
        canvas.drawRect(rect, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.surface })
        val marker = rect.inset(rect.width() * 0.2f, rect.height() * 0.2f)
        val shade = if (palette.dark) 255 else 0
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = max(1f, min(rect.width(), rect.height()) * 0.006f)
            color = Color.argb(54, shade, shade, shade)
        }
        val markerRadius = min(marker.width(), marker.height()) * 0.08f
        canvas.drawRoundRect(marker, markerRadius, markerRadius, paint)
    }

    private fun drawShadow(
        canvas: Canvas,
        rect: RectF,
        radius: Float,
        palette: Palette,
    ) {
        val minimum = min(rect.width(), rect.height())
        val offset = minimum * 0.012f
        val baseAlpha = if (palette.dark) 80 else 34
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (layer in 4 downTo 1) {
            val grow = minimum * 0.008f * layer
            paint.color = Color.argb(baseAlpha / (layer + 2), 0, 0, 0)
            canvas.drawRoundRect(
                rect.left - grow,
                rect.top - grow + offset,
                rect.right + grow,
                rect.bottom + grow + offset,
                radius + grow,
                radius + grow,
                paint,
            )
        }
    }

    private fun clipRoundRect(canvas: Canvas, rect: RectF, radius: Float) {
        val path = Path().apply {
            addRoundRect(rect, radius, radius, Path.Direction.CW)
        }
        canvas.clipPath(path)
    }

    private fun drawVignette(canvas: Canvas, width: Float, height: Float, palette: Palette) {
        val edgeAlpha = if (palette.dark) 58 else 22
        val paint = Paint().apply {
            shader = RadialGradient(
                width / 2f,
                height / 2f,
                max(width, height) * 0.78f,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                    Color.argb(edgeAlpha, 0, 0, 0),
                ),
                floatArrayOf(0f, 0.52f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, width, height, paint)
    }

    private fun drawGrain(canvas: Canvas, width: Float, height: Float, palette: Palette) {
        val shader = BitmapShader(grain, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        val scale = (min(width, height) / 900f).coerceIn(0.75f, 4f)
        shader.setLocalMatrix(Matrix().apply { setScale(scale, scale) })
        val paint = Paint().apply {
            this.shader = shader
            alpha = palette.grainAlpha
        }
        canvas.drawRect(0f, 0f, width, height, paint)
    }

    private fun createGrain(size: Int): Bitmap {
        val random = Random(17L)
        val pixels = IntArray(size * size) {
            val value = random.nextInt(256)
            Color.argb(255, value, value, value)
        }
        return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
    }
}
