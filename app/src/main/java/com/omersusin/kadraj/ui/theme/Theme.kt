package com.omersusin.kadraj.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.omersusin.kadraj.R

object Kadraj {
    val Ink = Color(0xFF0D0C0A)
    val InkRaised = Color(0xFF15120E)
    val InkSoft = Color(0xFF1D1914)
    val InkLift = Color(0xFF272117)
    val InkDeep = Color(0xFF070605)

    val Paper = Color(0xFFF5EFE5)
    val PaperDim = Color(0xFFD9D0C0)
    val Muted = Color(0xFF9E9587)
    val MutedDim = Color(0xFF6E675C)

    val Terracotta = Color(0xFFD47251)
    val TerracottaSoft = Color(0xFFE99A76)
    val Ember = Color(0xFFA84934)
    val Peach = Color(0xFFFFB58B)
    val Acid = Color(0xFFD8F27A)
    val Sky = Color(0xFF8FD8F1)
    val Violet = Color(0xFFA998FF)
    val Moss = Color(0xFF93A287)
    val Gold = Color(0xFFC8A15C)

    val Line = Color(0x1FFFFFFF)
    val LineStrong = Color(0x38FFFFFF)
    val Glass = Color(0x17FFFFFF)
    val GlassStrong = Color(0x29FFFFFF)
    val Scrim = Color(0xD9080706)

    val Fraunces = FontFamily(
        Font(R.font.fraunces_light, FontWeight.Light),
        Font(R.font.fraunces_regular, FontWeight.Normal),
        Font(R.font.fraunces_semibold, FontWeight.SemiBold),
        Font(R.font.fraunces_italic, FontWeight.Light, FontStyle.Italic),
    )

    val Manrope = FontFamily(
        Font(R.font.manrope_regular, FontWeight.Normal),
        Font(R.font.manrope_medium, FontWeight.Medium),
        Font(R.font.manrope_semibold, FontWeight.SemiBold),
    )

    private val tight = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    )

    object Type {
        val Hero = TextStyle(
            fontFamily = Fraunces,
            fontWeight = FontWeight.Light,
            fontSize = 46.sp,
            lineHeight = 48.sp,
            letterSpacing = (-0.6).sp,
            lineHeightStyle = tight,
        )
        val HeroItalic = TextStyle(
            fontFamily = Fraunces,
            fontWeight = FontWeight.Light,
            fontStyle = FontStyle.Italic,
            fontSize = 46.sp,
            lineHeight = 48.sp,
            letterSpacing = (-0.4).sp,
            lineHeightStyle = tight,
        )
        val Display = TextStyle(
            fontFamily = Fraunces,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.3).sp,
            lineHeightStyle = tight,
        )
        val DisplaySmall = TextStyle(
            fontFamily = Fraunces,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 26.sp,
            letterSpacing = (-0.1).sp,
            lineHeightStyle = tight,
        )
        val Wordmark = TextStyle(
            fontFamily = Fraunces,
            fontWeight = FontWeight.SemiBold,
            fontSize = 19.sp,
            letterSpacing = 0.2.sp,
        )
        val Sticker = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 8.sp,
            lineHeight = 10.sp,
            letterSpacing = 1.8.sp,
        )
        val Title = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.1).sp,
        )
        val Body = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            letterSpacing = 0.05.sp,
        )
        val BodySmall = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 12.5.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.1.sp,
        )
        val Label = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.7.sp,
        )
        val LabelSmall = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
            lineHeight = 12.sp,
            letterSpacing = 1.4.sp,
        )
        val Button = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 1.2.sp,
        )
        val Chip = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 0.2.sp,
        )
        val Numeric = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 0.4.sp,
        )
    }
}

@Composable
fun KadrajTheme(content: @Composable () -> Unit) {
    content()
}
