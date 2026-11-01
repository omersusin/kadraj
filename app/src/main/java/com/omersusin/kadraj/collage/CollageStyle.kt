package com.omersusin.kadraj.collage

data class CollageStyle(
    val palette: Palette = Palettes.Paper,
    val gap: Float = 0.014f,
    val margin: Float = 0.035f,
    val radius: Float = 0.032f,
    val mat: Float = 0f,
    val shadow: Boolean = true,
    val texture: Boolean = true,
    val caption: CaptionStyle = CaptionStyle(),
) {
    fun toConfig(template: Template, aspect: AspectPreset): CollageConfig =
        CollageConfig(
            template = template,
            aspect = aspect,
            gap = gap,
            margin = margin,
        )
}
