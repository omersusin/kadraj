package com.omersusin.kadraj.render

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.omersusin.kadraj.collage.CaptionAlignment
import com.omersusin.kadraj.collage.CaptionPlacement
import com.omersusin.kadraj.collage.CaptionStyle
import com.omersusin.kadraj.collage.CaptionTone
import com.omersusin.kadraj.collage.CollageCell
import com.omersusin.kadraj.collage.CollageStyle
import com.omersusin.kadraj.collage.FitMode
import com.omersusin.kadraj.collage.LayoutResult
import com.omersusin.kadraj.collage.Palette
import com.omersusin.kadraj.collage.PhotoLook
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

data class ProvidedImage(
    val bitmap: Bitmap,
    val recycleAfterUse: Boolean = false,
)

data class ImageRenderState(
    val fit: FitMode = FitMode.COVER,
    val focusX: Float = 0.5f,
    val focusY: Float = 0.5f,
    val look: PhotoLook = PhotoLook.ORIGINAL,
    val lookAmount: Float = 1f,
    val zoom: Float = 1f,
    val tilt: Float = 0f,
    val flipX: Boolean = false,
    val flipY: Boolean = false,
)

fun interface ImageProvider {
    fun load(imageId: Long, targetWidth: Int, targetHeight: Int): ProvidedImage?
}

object CollagePainter {
    private val grain: Bitmap by lazy { createGrain(128) }
    private val filterCache = ConcurrentHashMap<Int, ColorMatrixColorFilter>()
    private val identity = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )

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
        captionTypeface: Typeface = Typeface.SERIF,
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
        drawCaption(canvas, w, h, style.caption, palette, captionTypeface)
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

        val inner = RectF(rect)
        inner.inset(mat, mat)
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
                this.style = Paint.Style.STROKE
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
                this.style = Paint.Style.STROKE
                strokeWidth = max(3f, minimum * 0.005f)
                color = Color.argb(150, 0, 0, 0)
            }
            val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.style = Paint.Style.STROKE
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
        val zoom = state.zoom.coerceIn(1f, 3f)
        val tilt = state.tilt.coerceIn(-12f, 12f)
        val cover = if (abs(tilt) > 0.01f) tiltCoverScale(rect.width(), rect.height(), tilt) else 1f
        val scale = zoom * cover
        val previousFilter = paint.colorFilter
        val filter = photoFilter(state.look, state.lookAmount)
        if (filter != null) paint.colorFilter = filter

        try {
            if (fitMode == FitMode.FIT) {
                canvas.drawRect(
                    rect,
                    Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.surface },
                )
            }

            canvas.save()
            if (abs(scale - 1f) > 0.001f || abs(tilt) > 0.01f || state.flipX || state.flipY) {
                canvas.translate(rect.centerX(), rect.centerY())
                canvas.rotate(tilt)
                canvas.scale(
                    if (state.flipX) -scale else scale,
                    if (state.flipY) -scale else scale,
                )
                canvas.translate(-rect.centerX(), -rect.centerY())
            }

            if (fitMode == FitMode.FIT) {
                val fitScale = min(rect.width() / bitmap.width, rect.height() / bitmap.height)
                val dstWidth = bitmap.width * fitScale
                val dstHeight = bitmap.height * fitScale
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
            canvas.restore()
        } finally {
            paint.colorFilter = previousFilter
            if (provided.recycleAfterUse) bitmap.recycle()
        }
    }

    private fun drawCaption(
        canvas: Canvas,
        width: Float,
        height: Float,
        caption: CaptionStyle,
        palette: Palette,
        typeface: Typeface,
    ) {
        val text = caption.text.trim()
        if (text.isEmpty()) return

        val minimum = min(width, height)
        val margin = (captionMargin(caption) * minimum).coerceAtLeast(minimum * 0.02f)
        val available = (width - margin * 2f).coerceAtLeast(width * 0.35f)
        val textSize = (caption.size.coerceIn(0.025f, 0.075f) * minimum).coerceAtLeast(10f)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.textSize = textSize
            color = captionColor(caption.tone, palette)
            letterSpacing = 0.005f
        }
        val layout = StaticLayout.Builder.obtain(
            text,
            0,
            text.length,
            textPaint,
            available.toInt().coerceAtLeast(1),
        )
            .setAlignment(
                when (caption.alignment) {
                    CaptionAlignment.START -> Layout.Alignment.ALIGN_NORMAL
                    CaptionAlignment.CENTER -> Layout.Alignment.ALIGN_CENTER
                    CaptionAlignment.END -> Layout.Alignment.ALIGN_OPPOSITE
                },
            )
            .setIncludePad(false)
            .setLineSpacing(0f, 1.02f)
            .setMaxLines(4)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()

        val top = when (caption.placement) {
            CaptionPlacement.TOP -> margin
            CaptionPlacement.CENTER -> (height - layout.height) / 2f
            CaptionPlacement.BOTTOM -> height - margin - layout.height
        }

        if (caption.plate) {
            val padX = textSize * 0.55f
            val padY = textSize * 0.38f
            val plate = RectF(
                (margin - padX).coerceAtLeast(0f),
                (top - padY).coerceAtLeast(0f),
                (margin + available + padX).coerceAtMost(width),
                (top + layout.height + padY).coerceAtMost(height),
            )
            val platePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (Color.luminance(textPaint.color) > 0.5f) {
                    Color.argb(150, 12, 10, 8)
                } else {
                    Color.argb(168, 255, 251, 243)
                }
            }
            val radius = min(textSize * 0.28f, min(plate.width(), plate.height()) / 2f)
            canvas.drawRoundRect(plate, radius, radius, platePaint)
        }

        canvas.save()
        canvas.translate(margin, top)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun captionMargin(caption: CaptionStyle): Float {
        val base = 0.035f + if (caption.enabled) 0.005f else 0f
        return base.coerceIn(0.02f, 0.12f)
    }

    private fun captionColor(tone: CaptionTone, palette: Palette): Int = when (tone) {
        CaptionTone.AUTO -> if (palette.dark) palette.matte else 0xFF241C15.toInt()
        CaptionTone.PAPER -> if (palette.dark) palette.matte else 0xFF4A3327.toInt()
        CaptionTone.ACCENT -> palette.accent
    }

    private fun tiltCoverScale(width: Float, height: Float, angle: Float): Float {
        val radians = Math.toRadians(angle.toDouble())
        val cosAngle = abs(cos(radians)).toFloat()
        val sinAngle = abs(sin(radians)).toFloat()
        return max(
            (width * cosAngle + height * sinAngle) / width,
            (width * sinAngle + height * cosAngle) / height,
        )
    }

    private fun photoFilter(look: PhotoLook, amount: Float): ColorMatrixColorFilter? {
        val strength = amount.coerceIn(0f, 1f)
        if (look == PhotoLook.ORIGINAL || strength <= 0.01f) return null
        val key = look.ordinal * 100 + (strength * 20f).roundToInt()
        return filterCache.getOrPut(key) {
            val target = targetMatrix(look).array
            val mixed = FloatArray(20) { identity[it] + (target[it] - identity[it]) * strength }
            ColorMatrixColorFilter(ColorMatrix(mixed))
        }
    }

    private fun targetMatrix(look: PhotoLook): ColorMatrix = when (look) {
        PhotoLook.ORIGINAL -> ColorMatrix()
        PhotoLook.WARM -> ColorMatrix().apply {
            setScale(1.07f, 1.01f, 0.9f, 1f)
            postConcat(colorOffset(7f, 2f, -4f))
        }
        PhotoLook.COOL -> ColorMatrix().apply {
            setScale(0.93f, 1f, 1.07f, 1f)
            postConcat(colorOffset(-4f, 1f, 7f))
        }
        PhotoLook.MONO -> ColorMatrix().apply {
            setSaturation(0f)
            postConcat(colorOffset(-6f, -6f, -6f))
            postConcat(colorContrast(1.08f, 4f))
        }
        PhotoLook.FADE -> ColorMatrix().apply {
            setSaturation(0.78f)
            postConcat(colorContrast(0.86f, 16f))
        }
        PhotoLook.VIVID -> ColorMatrix().apply {
            setSaturation(1.35f)
            postConcat(colorContrast(1.06f, -4f))
        }
        PhotoLook.FILM -> ColorMatrix().apply {
            setSaturation(0.72f)
            postConcat(colorContrast(1.04f, -2f))
            postConcat(colorOffset(9f, 4f, -6f))
        }
    }

    private fun colorOffset(red: Float, green: Float, blue: Float): ColorMatrix = ColorMatrix(
        floatArrayOf(
            1f, 0f, 0f, 0f, red,
            0f, 1f, 0f, 0f, green,
            0f, 0f, 1f, 0f, blue,
            0f, 0f, 0f, 1f, 0f,
        ),
    )

    private fun colorContrast(scale: Float, offset: Float): ColorMatrix = ColorMatrix(
        floatArrayOf(
            scale, 0f, 0f, 0f, offset,
            0f, scale, 0f, 0f, offset,
            0f, 0f, scale, 0f, offset,
            0f, 0f, 0f, 1f, 0f,
        ),
    )

    private fun drawPlaceholder(canvas: Canvas, rect: RectF, palette: Palette) {
        canvas.drawRect(rect, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.surface })
        val marker = RectF(rect)
        marker.inset(rect.width() * 0.2f, rect.height() * 0.2f)
        val shade = if (palette.dark) 255 else 0
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
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
