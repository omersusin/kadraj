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
    val Ink = Color(0xFF0B0B0D)
    val InkRaised = Color(0xFF121214)
    val InkSoft = Color(0xFF19191C)
    val InkLift = Color(0xFF232327)
    val InkDeep = Color(0xFF060607)

    val Paper = Color(0xFFF7F2EA)
    val PaperDim = Color(0xFFD8D1C6)
    val Muted = Color(0xFFA6A099)
    val MutedDim = Color(0xFF77736E)

    val Terracotta = Color(0xFFFF8A5C)
    val TerracottaSoft = Color(0xFFFFA37C)
    val Ember = Color(0xFFE06F45)
    val Peach = Color(0xFFFFB18A)
    val Acid = Color(0xFFD7E8B0)
    val Sky = Color(0xFFB5D5E3)
    val Violet = Color(0xFFC8C0DE)
    val Moss = Color(0xFFA8B2A0)
    val Gold = Color(0xFFD6B77E)

    val Line = Color(0x14FFFFFF)
    val LineStrong = Color(0x2EFFFFFF)
    val Glass = Color(0x0DFFFFFF)
    val GlassStrong = Color(0x1AFFFFFF)
    val Scrim = Color(0xEB0B0B0D)

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
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 40.sp,
            lineHeight = 42.sp,
            letterSpacing = (-1.2).sp,
            lineHeightStyle = tight,
        )
        val HeroItalic = TextStyle(
            fontFamily = Fraunces,
            fontWeight = FontWeight.Light,
            fontStyle = FontStyle.Italic,
            fontSize = 36.sp,
            lineHeight = 39.sp,
            letterSpacing = (-0.3).sp,
            lineHeightStyle = tight,
        )
        val Display = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.5).sp,
            lineHeightStyle = tight,
        )
        val DisplaySmall = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.2).sp,
            lineHeightStyle = tight,
        )
        val Wordmark = TextStyle(
            fontFamily = Fraunces,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            letterSpacing = 0.1.sp,
        )
        val Sticker = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 8.5.sp,
            lineHeight = 11.sp,
            letterSpacing = 1.4.sp,
        )
        val Title = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.5.sp,
            lineHeight = 20.sp,
            letterSpacing = (-0.1).sp,
        )
        val Body = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 13.5.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.05.sp,
        )
        val BodySmall = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            letterSpacing = 0.1.sp,
        )
        val Label = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.0.sp,
        )
        val LabelSmall = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 9.5.sp,
            lineHeight = 13.sp,
            letterSpacing = 0.8.sp,
        )
        val Button = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = 0.2.sp,
        )
        val Chip = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            letterSpacing = 0.1.sp,
        )
        val Numeric = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.5.sp,
            letterSpacing = 0.2.sp,
        )
    }
}

@Composable
fun KadrajTheme(content: @Composable () -> Unit) {
    content()
}
