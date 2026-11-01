package com.omersusin.kadraj.collage

import kotlin.math.abs
import kotlin.math.min

enum class Template {
    AUTO,
    GRID,
    FLOW,
    HERO,
    STRIP,
    PILE,
}

enum class AspectPreset(val ratio: Float?) {
    AUTO(null),
    SQUARE(1f),
    FOUR_FIVE(4f / 5f),
    STORY(9f / 16f),
    WIDE(16f / 9f),
    DESKTOP(16f / 10f),
    CLASSIC(3f / 2f),
}

enum class FitMode {
    COVER,
    FIT,
}

data class ImageSpec(
    val id: Long,
    val aspect: Float,
)

data class CollageConfig(
    val template: Template = Template.AUTO,
    val aspect: AspectPreset = AspectPreset.AUTO,
    val gap: Float = 0.014f,
    val margin: Float = 0.035f,
)

data class NRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val aspect: Float get() = width / height
    val area: Float get() = width * height

    fun inset(dx: Float, dy: Float): NRect =
        NRect(left + dx, top + dy, right - dx, bottom - dy)

    fun inset(all: Float): NRect = inset(all, all)

    fun overlaps(other: NRect, epsilon: Float = 1e-5f): Boolean {
        if (right <= other.left + epsilon || other.right <= left + epsilon) return false
        if (bottom <= other.top + epsilon || other.bottom <= top + epsilon) return false
        return true
    }

    fun normalizedBy(width: Float, height: Float): NRect =
        NRect(left / width, top / height, right / width, bottom / height)

    fun coerceInside(epsilon: Float = 0f): NRect = NRect(
        left.coerceAtLeast(epsilon),
        top.coerceAtLeast(epsilon),
        right.coerceAtMost(1f - epsilon),
        bottom.coerceAtMost(1f - epsilon),
    )
}

data class CollageCell(
    val imageId: Long,
    val rect: NRect,
    val rotation: Float = 0f,
    val z: Int = 0,
)

data class LayoutResult(
    val aspect: Float,
    val cells: List<CollageCell>,
) {
    fun cellFor(imageId: Long): CollageCell? = cells.firstOrNull { it.imageId == imageId }
}

data class OrientationProfile(
    val dominantAspect: Float,
    val portraitShare: Float,
    val landscapeShare: Float,
    val squareShare: Float,
) {
    val mixed: Boolean
        get() = maxOf(portraitShare, landscapeShare, squareShare) < 0.7f
}

internal fun ImageSpec.safeAspect(): Float = aspect.coerceIn(0.2f, 5f)

internal fun List<ImageSpec>.dominantAspect(): Float {
    val sorted = map { it.safeAspect() }.sorted()
    if (sorted.isEmpty()) return 1f
    val middle = sorted.size / 2
    val median = if (sorted.size % 2 == 0) {
        (sorted[middle - 1] + sorted[middle]) / 2f
    } else {
        sorted[middle]
    }
    return median.coerceIn(0.3f, 3.0f)
}

internal fun List<ImageSpec>.orientationProfile(): OrientationProfile {
    if (isEmpty()) return OrientationProfile(1f, 0f, 0f, 1f)
    var portrait = 0
    var landscape = 0
    var square = 0
    forEach {
        val aspect = it.safeAspect()
        when {
            aspect < 0.85f -> portrait++
            aspect > 1.25f -> landscape++
            else -> square++
        }
    }
    val count = size.toFloat()
    return OrientationProfile(
        dominantAspect = dominantAspect(),
        portraitShare = portrait / count,
        landscapeShare = landscape / count,
        squareShare = square / count,
    )
}

internal fun preferredAspect(profile: OrientationProfile): Float = when {
    profile.portraitShare >= 0.6f -> 0.8f
    profile.landscapeShare >= 0.6f -> 1.6f
    else -> 1f
}

internal fun chooseColumns(profile: OrientationProfile, count: Int): Int {
    if (count <= 1) return 1
    return when {
        profile.portraitShare >= 0.65f -> min(3, count)
        profile.landscapeShare >= 0.65f -> min(2, count)
        else -> min(2, count)
    }
}

internal fun NRect.intersectionArea(other: NRect): Float {
    val l = maxOf(left, other.left)
    val t = maxOf(top, other.top)
    val r = minOf(right, other.right)
    val b = minOf(bottom, other.bottom)
    if (r <= l || b <= t) return 0f
    return (r - l) * (b - t)
}

internal fun Float.approxEquals(other: Float, tolerance: Float = 1e-3f): Boolean =
    abs(this - other) <= tolerance
