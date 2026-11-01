package com.omersusin.kadraj

import android.net.Uri
import com.omersusin.kadraj.collage.FitMode

data class CollageImage(
    val id: Long,
    val uri: Uri,
    val width: Int,
    val height: Int,
    val fit: FitMode = FitMode.COVER,
    val focusX: Float = 0.5f,
    val focusY: Float = 0.5f,
) {
    val aspect: Float
        get() = width.toFloat() / height.coerceAtLeast(1)
}
