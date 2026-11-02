package com.omersusin.kadraj

import android.net.Uri
import com.omersusin.kadraj.collage.FitMode
import com.omersusin.kadraj.collage.PhotoLook

data class CollageImage(
    val id: Long,
    val uri: Uri,
    val width: Int,
    val height: Int,
    val fit: FitMode = FitMode.COVER,
    val focusX: Float = 0.5f,
    val focusY: Float = 0.5f,
    val look: PhotoLook = PhotoLook.ORIGINAL,
    val lookAmount: Float = 1f,
    val zoom: Float = 1f,
    val tilt: Float = 0f,
    val flipX: Boolean = false,
    val flipY: Boolean = false,
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val warmth: Float = 0f,
) {
    val aspect: Float
        get() = width.toFloat() / height.coerceAtLeast(1)
}
