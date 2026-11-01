package com.omersusin.kadraj.render

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.omersusin.kadraj.R
import com.omersusin.kadraj.collage.CaptionFont

object CaptionTypefaces {
    fun load(context: Context, font: CaptionFont): Typeface =
        ResourcesCompat.getFont(
            context,
            when (font) {
                CaptionFont.SERIF -> R.font.fraunces_semibold
                CaptionFont.SANS -> R.font.manrope_semibold
            },
        ) ?: Typeface.SERIF
}
