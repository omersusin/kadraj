package com.omersusin.kadraj.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape as GraphicsShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.omersusin.kadraj.R

enum class ThemeMode(
    val key: String,
    val label: String,
    val blurb: String,
) {
    CINEMA(
        key = "cinema",
        label = "Cinema",
        blurb = "Dark stage, peach light",
    ),
    PAPER(
        key = "paper",
        label = "Paper",
        blurb = "Warm paper, terracotta",
    ),
    CONTACT(
        key = "contact",
        label = "Contact",
        blurb = "Monochrome, signal red",
    ),
    ;

    companion object {
        fun fromKey(value: String?): ThemeMode? =
            entries.firstOrNull { it.key == value }

        fun fromStorage(value: String?): ThemeMode =
            fromKey(value) ?: CINEMA
    }
}

object ThemePreferences {
    private const val FILE = "kadraj_appearance"
    private const val KEY = "theme_mode"

    fun read(context: Context): ThemeMode {
        val stored = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getString(KEY, null)
        return ThemeMode.fromStorage(stored)
    }

    fun write(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, mode.key)
            .apply()
    }
}

private val frauncesFamily = FontFamily(
    Font(R.font.fraunces_light, FontWeight.Light),
    Font(R.font.fraunces_regular, FontWeight.Normal),
    Font(R.font.fraunces_semibold, FontWeight.SemiBold),
    Font(R.font.fraunces_italic, FontWeight.Light, FontStyle.Italic),
)

private val manropeFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
)

private val tight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private data class TypeSet(
    val hero: TextStyle,
    val heroItalic: TextStyle,
    val display: TextStyle,
    val displaySmall: TextStyle,
    val wordmark: TextStyle,
    val sticker: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val label: TextStyle,
    val labelSmall: TextStyle,
    val button: TextStyle,
    val chip: TextStyle,
    val numeric: TextStyle,
)

private data class ThemeSpec(
    val mode: ThemeMode,
    val ink: Color,
    val inkRaised: Color,
    val inkSoft: Color,
    val inkLift: Color,
    val inkDeep: Color,
    val paper: Color,
    val paperDim: Color,
    val muted: Color,
    val mutedDim: Color,
    val terracotta: Color,
    val terracottaSoft: Color,
    val ember: Color,
    val peach: Color,
    val acid: Color,
    val sky: Color,
    val violet: Color,
    val moss: Color,
    val gold: Color,
    val line: Color,
    val lineStrong: Color,
    val glass: Color,
    val glassStrong: Color,
    val scrim: Color,
    val buttonRadius: Dp,
    val cardRadius: Dp,
    val controlRadius: Dp,
    val tileRadius: Dp,
    val pillRadius: Dp,
    val sheetRadius: Dp,
    val stageRadius: Dp,
    val heroRadius: Dp,
    val dockRadius: Dp,
    val previewRadius: Dp,
    val iconIsCircle: Boolean,
    val types: TypeSet,
)

private fun typeSet(mode: ThemeMode): TypeSet {
    val labelFamily = if (mode == ThemeMode.CONTACT) {
        FontFamily.Monospace
    } else {
        manropeFamily
    }
    return when (mode) {
        ThemeMode.CINEMA -> TypeSet(
            hero = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 40.sp,
                lineHeight = 42.sp,
                letterSpacing = (-1.2).sp,
                lineHeightStyle = tight,
            ),
            heroItalic = TextStyle(
                fontFamily = frauncesFamily,
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
                fontSize = 36.sp,
                lineHeight = 39.sp,
                letterSpacing = (-0.3).sp,
                lineHeightStyle = tight,
            ),
            display = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.5).sp,
                lineHeightStyle = tight,
            ),
            displaySmall = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                letterSpacing = (-0.2).sp,
                lineHeightStyle = tight,
            ),
            wordmark = TextStyle(
                fontFamily = frauncesFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                letterSpacing = 0.1.sp,
            ),
            sticker = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 8.5.sp,
                lineHeight = 11.sp,
                letterSpacing = 1.4.sp,
            ),
            title = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
                lineHeight = 20.sp,
                letterSpacing = (-0.1).sp,
            ),
            body = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.05.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                letterSpacing = 0.1.sp,
            ),
            label = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                letterSpacing = 1.0.sp,
            ),
            labelSmall = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 9.5.sp,
                lineHeight = 13.sp,
                letterSpacing = 0.8.sp,
            ),
            button = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                letterSpacing = 0.2.sp,
            ),
            chip = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.5.sp,
                letterSpacing = 0.1.sp,
            ),
            numeric = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp,
                letterSpacing = 0.2.sp,
            ),
        )

        ThemeMode.PAPER -> TypeSet(
            hero = TextStyle(
                fontFamily = frauncesFamily,
                fontWeight = FontWeight.Light,
                fontSize = 42.sp,
                lineHeight = 43.sp,
                letterSpacing = (-0.8).sp,
                lineHeightStyle = tight,
            ),
            heroItalic = TextStyle(
                fontFamily = frauncesFamily,
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
                fontSize = 38.sp,
                lineHeight = 41.sp,
                letterSpacing = (-0.2).sp,
                lineHeightStyle = tight,
            ),
            display = TextStyle(
                fontFamily = frauncesFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 27.sp,
                lineHeight = 31.sp,
                letterSpacing = (-0.3).sp,
                lineHeightStyle = tight,
            ),
            displaySmall = TextStyle(
                fontFamily = frauncesFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                letterSpacing = (-0.1).sp,
                lineHeightStyle = tight,
            ),
            wordmark = TextStyle(
                fontFamily = frauncesFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 21.sp,
                letterSpacing = 0.sp,
            ),
            sticker = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 8.5.sp,
                lineHeight = 11.sp,
                letterSpacing = 1.8.sp,
            ),
            title = TextStyle(
                fontFamily = frauncesFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = (-0.1).sp,
            ),
            body = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.05.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                letterSpacing = 0.1.sp,
            ),
            label = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                letterSpacing = 1.2.sp,
            ),
            labelSmall = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 9.5.sp,
                lineHeight = 13.sp,
                letterSpacing = 0.9.sp,
            ),
            button = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                letterSpacing = 0.1.sp,
            ),
            chip = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.5.sp,
                letterSpacing = 0.1.sp,
            ),
            numeric = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp,
                letterSpacing = 0.2.sp,
            ),
        )

        ThemeMode.CONTACT -> TypeSet(
            hero = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 40.sp,
                lineHeight = 41.sp,
                letterSpacing = (-0.8).sp,
                lineHeightStyle = tight,
            ),
            heroItalic = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 36.sp,
                lineHeight = 39.sp,
                letterSpacing = (-0.3).sp,
                lineHeightStyle = tight,
            ),
            display = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                lineHeight = 29.sp,
                letterSpacing = (-0.4).sp,
                lineHeightStyle = tight,
            ),
            displaySmall = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                letterSpacing = (-0.1).sp,
                lineHeightStyle = tight,
            ),
            wordmark = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 19.sp,
                letterSpacing = 2.2.sp,
            ),
            sticker = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
                lineHeight = 12.sp,
                letterSpacing = 1.6.sp,
            ),
            title = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
                lineHeight = 19.sp,
                letterSpacing = 0.1.sp,
            ),
            body = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.05.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                letterSpacing = 0.1.sp,
            ),
            label = TextStyle(
                fontFamily = labelFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                letterSpacing = 1.0.sp,
            ),
            labelSmall = TextStyle(
                fontFamily = labelFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
                lineHeight = 13.sp,
                letterSpacing = 0.8.sp,
            ),
            button = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp,
            ),
            chip = TextStyle(
                fontFamily = manropeFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.5.sp,
                letterSpacing = 0.2.sp,
            ),
            numeric = TextStyle(
                fontFamily = labelFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.5.sp,
                letterSpacing = 0.4.sp,
            ),
        )
    }
}

private fun themeSpec(mode: ThemeMode): ThemeSpec = when (mode) {
    ThemeMode.CINEMA -> ThemeSpec(
        mode = mode,
        ink = Color(0xFF0B0B0D),
        inkRaised = Color(0xFF121214),
        inkSoft = Color(0xFF19191C),
        inkLift = Color(0xFF232327),
        inkDeep = Color(0xFF060607),
        paper = Color(0xFFF7F2EA),
        paperDim = Color(0xFFD8D1C6),
        muted = Color(0xFFA6A099),
        mutedDim = Color(0xFF77736E),
        terracotta = Color(0xFFFF8A5C),
        terracottaSoft = Color(0xFFFFA37C),
        ember = Color(0xFFE06F45),
        peach = Color(0xFFFFB18A),
        acid = Color(0xFFD7E8B0),
        sky = Color(0xFFB5D5E3),
        violet = Color(0xFFC8C0DE),
        moss = Color(0xFFA8B2A0),
        gold = Color(0xFFD6B77E),
        line = Color(0x14FFFFFF),
        lineStrong = Color(0x2EFFFFFF),
        glass = Color(0x0DFFFFFF),
        glassStrong = Color(0x1AFFFFFF),
        scrim = Color(0xEB0B0B0D),
        buttonRadius = 16.dp,
        cardRadius = 16.dp,
        controlRadius = 14.dp,
        tileRadius = 11.dp,
        pillRadius = 50.dp,
        sheetRadius = 28.dp,
        stageRadius = 8.dp,
        heroRadius = 28.dp,
        dockRadius = 26.dp,
        previewRadius = 12.dp,
        iconIsCircle = true,
        types = typeSet(mode),
    )

    ThemeMode.PAPER -> ThemeSpec(
        mode = mode,
        ink = Color(0xFFF2EBDF),
        inkRaised = Color(0xFFF8F2E7),
        inkSoft = Color(0xFFE9DFD0),
        inkLift = Color(0xFFDDD1C0),
        inkDeep = Color(0xFFEAE1D2),
        paper = Color(0xFF1B1814),
        paperDim = Color(0xFF433B32),
        muted = Color(0xFF6B6257),
        mutedDim = Color(0xFF8C8275),
        terracotta = Color(0xFFAD5132),
        terracottaSoft = Color(0xFFC46B47),
        ember = Color(0xFF8C3C23),
        peach = Color(0xFFB45535),
        acid = Color(0xFF788361),
        sky = Color(0xFF5E7D86),
        violet = Color(0xFF7C697A),
        moss = Color(0xFF6C7761),
        gold = Color(0xFF9B7438),
        line = Color(0x1F1B1814),
        lineStrong = Color(0x3D1B1814),
        glass = Color(0x0D1B1814),
        glassStrong = Color(0x1A1B1814),
        scrim = Color(0xB31B1814),
        buttonRadius = 1.dp,
        cardRadius = 2.dp,
        controlRadius = 1.dp,
        tileRadius = 1.dp,
        pillRadius = 2.dp,
        sheetRadius = 2.dp,
        stageRadius = 0.dp,
        heroRadius = 0.dp,
        dockRadius = 2.dp,
        previewRadius = 1.dp,
        iconIsCircle = false,
        types = typeSet(mode),
    )

    ThemeMode.CONTACT -> ThemeSpec(
        mode = mode,
        ink = Color(0xFF050505),
        inkRaised = Color(0xFF0B0B0B),
        inkSoft = Color(0xFF131313),
        inkLift = Color(0xFF1A1A1A),
        inkDeep = Color(0xFF000000),
        paper = Color(0xFFF4F4EF),
        paperDim = Color(0xFFD8D8D1),
        muted = Color(0xFF9B9B94),
        mutedDim = Color(0xFF6D6D67),
        terracotta = Color(0xFFFF4B36),
        terracottaSoft = Color(0xFFFF6D59),
        ember = Color(0xFFD53824),
        peach = Color(0xFFFF4B36),
        acid = Color(0xFFDDDDD4),
        sky = Color(0xFFB9C2C4),
        violet = Color(0xFFC4BEC8),
        moss = Color(0xFFAAB1A6),
        gold = Color(0xFFC8B48B),
        line = Color(0x1FFFFFFF),
        lineStrong = Color(0x3DFFFFFF),
        glass = Color(0x0FFFFFFF),
        glassStrong = Color(0x1FFFFFFF),
        scrim = Color(0xF2050505),
        buttonRadius = 0.dp,
        cardRadius = 0.dp,
        controlRadius = 0.dp,
        tileRadius = 0.dp,
        pillRadius = 0.dp,
        sheetRadius = 0.dp,
        stageRadius = 0.dp,
        heroRadius = 0.dp,
        dockRadius = 0.dp,
        previewRadius = 0.dp,
        iconIsCircle = false,
        types = typeSet(mode),
    )
}

private var activeSpec by mutableStateOf(themeSpec(ThemeMode.CINEMA))

object Kadraj {
    val FixedInk = Color(0xFF09080A)
    val FixedPaper = Color(0xFFF7F2EA)
    val FixedPeach = Color(0xFFFFB18A)
    val OnPhotoLine = Color(0x33FFFFFF)
    val OnPhotoMuted = Color(0xB8F7F2EA)

    val mode: ThemeMode get() = activeSpec.mode
    val isPaper: Boolean get() = activeSpec.mode == ThemeMode.PAPER
    val isContact: Boolean get() = activeSpec.mode == ThemeMode.CONTACT

    val Ink: Color get() = activeSpec.ink
    val InkRaised: Color get() = activeSpec.inkRaised
    val InkSoft: Color get() = activeSpec.inkSoft
    val InkLift: Color get() = activeSpec.inkLift
    val InkDeep: Color get() = activeSpec.inkDeep

    val Paper: Color get() = activeSpec.paper
    val PaperDim: Color get() = activeSpec.paperDim
    val Muted: Color get() = activeSpec.muted
    val MutedDim: Color get() = activeSpec.mutedDim

    val Terracotta: Color get() = activeSpec.terracotta
    val TerracottaSoft: Color get() = activeSpec.terracottaSoft
    val Ember: Color get() = activeSpec.ember
    val Peach: Color get() = activeSpec.peach
    val Acid: Color get() = activeSpec.acid
    val Sky: Color get() = activeSpec.sky
    val Violet: Color get() = activeSpec.violet
    val Moss: Color get() = activeSpec.moss
    val Gold: Color get() = activeSpec.gold

    val Line: Color get() = activeSpec.line
    val LineStrong: Color get() = activeSpec.lineStrong
    val Glass: Color get() = activeSpec.glass
    val GlassStrong: Color get() = activeSpec.glassStrong
    val Scrim: Color get() = activeSpec.scrim

    val AccentBorder: Color
        get() = when (activeSpec.mode) {
            ThemeMode.CINEMA -> Color.White.copy(alpha = 0.18f)
            ThemeMode.PAPER -> Color(0x331B1814)
            ThemeMode.CONTACT -> Color.White.copy(alpha = 0.42f)
        }

    val Fraunces: FontFamily = frauncesFamily
    val Manrope: FontFamily = manropeFamily

    object Type {
        val Hero: TextStyle get() = activeSpec.types.hero
        val HeroItalic: TextStyle get() = activeSpec.types.heroItalic
        val Display: TextStyle get() = activeSpec.types.display
        val DisplaySmall: TextStyle get() = activeSpec.types.displaySmall
        val Wordmark: TextStyle get() = activeSpec.types.wordmark
        val Sticker: TextStyle get() = activeSpec.types.sticker
        val Title: TextStyle get() = activeSpec.types.title
        val Body: TextStyle get() = activeSpec.types.body
        val BodySmall: TextStyle get() = activeSpec.types.bodySmall
        val Label: TextStyle get() = activeSpec.types.label
        val LabelSmall: TextStyle get() = activeSpec.types.labelSmall
        val Button: TextStyle get() = activeSpec.types.button
        val Chip: TextStyle get() = activeSpec.types.chip
        val Numeric: TextStyle get() = activeSpec.types.numeric
    }

    object Shape {
        val Button: GraphicsShape get() = RoundedCornerShape(activeSpec.buttonRadius)
        val Card: GraphicsShape get() = RoundedCornerShape(activeSpec.cardRadius)
        val Control: GraphicsShape get() = RoundedCornerShape(activeSpec.controlRadius)
        val Tile: GraphicsShape get() = RoundedCornerShape(activeSpec.tileRadius)
        val Pill: GraphicsShape get() = RoundedCornerShape(activeSpec.pillRadius)
        val Sheet: GraphicsShape get() = RoundedCornerShape(
            topStart = activeSpec.sheetRadius,
            topEnd = activeSpec.sheetRadius,
        )
        val Stage: GraphicsShape get() = RoundedCornerShape(activeSpec.stageRadius)
        val Hero: GraphicsShape get() = RoundedCornerShape(
            bottomStart = activeSpec.heroRadius,
            bottomEnd = activeSpec.heroRadius,
        )
        val Dock: GraphicsShape get() = RoundedCornerShape(activeSpec.dockRadius)
        val Preview: GraphicsShape get() = RoundedCornerShape(activeSpec.previewRadius)
        val Icon: GraphicsShape
            get() = if (activeSpec.iconIsCircle) {
                CircleShape
            } else {
                RoundedCornerShape(activeSpec.tileRadius)
            }
    }
}

@Composable
fun KadrajTheme(
    mode: ThemeMode = ThemeMode.CINEMA,
    content: @Composable () -> Unit,
) {
    val spec = remember(mode) { themeSpec(mode) }
    activeSpec = spec
    val view = LocalView.current
    val context = LocalContext.current

    SideEffect {
        val activity = context.findActivity() ?: return@SideEffect
        WindowCompat.getInsetsController(activity.window, view).apply {
            isAppearanceLightStatusBars = mode == ThemeMode.PAPER
            isAppearanceLightNavigationBars = mode == ThemeMode.PAPER
        }
        activity.window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }

    content()
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
