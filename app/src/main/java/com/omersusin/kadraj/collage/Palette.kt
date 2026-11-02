package com.omersusin.kadraj.collage

data class Palette(
    val id: String,
    val name: String,
    val colors: List<Int>,
    val matte: Int,
    val surface: Int,
    val accent: Int,
    val dark: Boolean,
    val grainAlpha: Int,
)

object Palettes {
    val Paper = Palette(
        id = "paper",
        name = "Paper",
        colors = listOf(0xFFF6F0E6.toInt(), 0xFFECE2D3.toInt(), 0xFFDCCDB7.toInt()),
        matte = 0xFFFFFBF3.toInt(),
        surface = 0xFFD6C6AE.toInt(),
        accent = 0xFFB35A3B.toInt(),
        dark = false,
        grainAlpha = 10,
    )

    val Night = Palette(
        id = "night",
        name = "Night",
        colors = listOf(0xFF1B1712.toInt(), 0xFF221D16.toInt(), 0xFF0D0B09.toInt()),
        matte = 0xFFF1E8D9.toInt(),
        surface = 0xFF2C251C.toInt(),
        accent = 0xFFC8A15C.toInt(),
        dark = true,
        grainAlpha = 15,
    )

    val Clay = Palette(
        id = "clay",
        name = "Clay",
        colors = listOf(0xFF7C4231.toInt(), 0xFF955440.toInt(), 0xFF48251C.toInt()),
        matte = 0xFFF5E9D8.toInt(),
        surface = 0xFF5D3023.toInt(),
        accent = 0xFFE6AE8B.toInt(),
        dark = true,
        grainAlpha = 14,
    )

    val Sage = Palette(
        id = "sage",
        name = "Sage",
        colors = listOf(0xFF303D31.toInt(), 0xFF435444.toInt(), 0xFF1B241D.toInt()),
        matte = 0xFFE8E3D3.toInt(),
        surface = 0xFF394A3A.toInt(),
        accent = 0xFFB7C6A6.toInt(),
        dark = true,
        grainAlpha = 14,
    )

    val Mist = Palette(
        id = "mist",
        name = "Mist",
        colors = listOf(0xFF2C343D.toInt(), 0xFF3D4955.toInt(), 0xFF171D24.toInt()),
        matte = 0xFFE9E7E1.toInt(),
        surface = 0xFF34404B.toInt(),
        accent = 0xFFAFC1CD.toInt(),
        dark = true,
        grainAlpha = 14,
    )

    val Rose = Palette(
        id = "rose",
        name = "Rose",
        colors = listOf(0xFF4C2C34.toInt(), 0xFF653C47.toInt(), 0xFF2A171D.toInt()),
        matte = 0xFFF4E7E1.toInt(),
        surface = 0xFF5A343E.toInt(),
        accent = 0xFFE0A9A2.toInt(),
        dark = true,
        grainAlpha = 14,
    )

    val Ivory = Palette(
        id = "ivory",
        name = "Ivory",
        colors = listOf(0xFFEADFCB.toInt(), 0xFFDFD0B5.toInt(), 0xFFD0BC99.toInt()),
        matte = 0xFFFFFDF7.toInt(),
        surface = 0xFFCBB998.toInt(),
        accent = 0xFF8A4A32.toInt(),
        dark = false,
        grainAlpha = 8,
    )

    val Olive = Palette(
        id = "olive",
        name = "Olive",
        colors = listOf(0xFF3B3E28.toInt(), 0xFF4E5235.toInt(), 0xFF222416.toInt()),
        matte = 0xFFEDE6D5.toInt(),
        surface = 0xFF41452C.toInt(),
        accent = 0xFFC9C08B.toInt(),
        dark = true,
        grainAlpha = 13,
    )

    val Sunset = Palette(
        id = "sunset",
        name = "Sunset",
        colors = listOf(0xFF54283A.toInt(), 0xFF7B3743.toInt(), 0xFF29141F.toInt()),
        matte = 0xFFFFE8D9.toInt(),
        surface = 0xFF5A2B36.toInt(),
        accent = 0xFFFF9E74.toInt(),
        dark = true,
        grainAlpha = 14,
    )

    val Lagoon = Palette(
        id = "lagoon",
        name = "Lagoon",
        colors = listOf(0xFF0D3034.toInt(), 0xFF15545B.toInt(), 0xFF071B1E.toInt()),
        matte = 0xFFE4F7F0.toInt(),
        surface = 0xFF113F44.toInt(),
        accent = 0xFF77E6C7.toInt(),
        dark = true,
        grainAlpha = 13,
    )

    val all: List<Palette> = listOf(
        Paper,
        Night,
        Clay,
        Sage,
        Mist,
        Rose,
        Ivory,
        Olive,
        Sunset,
        Lagoon,
    )

    fun byId(id: String): Palette = all.firstOrNull { it.id == id } ?: Paper
}
