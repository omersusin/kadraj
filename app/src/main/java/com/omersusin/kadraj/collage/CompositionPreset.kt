package com.omersusin.kadraj.collage

import kotlin.math.abs

/**
 * A curated starting point that combines a layout, a canvas ratio, and a
 * material treatment. Presets are intentionally additive: the user's caption
 * is preserved when a preset is applied.
 */
data class CompositionPreset(
    val id: String,
    val name: String,
    val tagline: String,
    val template: Template,
    val aspect: AspectPreset,
    val paletteId: String,
    val gap: Float,
    val margin: Float,
    val radius: Float,
    val mat: Float,
    val shadow: Boolean,
    val texture: Boolean,
) {
    fun style(caption: CaptionStyle = CaptionStyle()): CollageStyle = CollageStyle(
        palette = Palettes.byId(paletteId),
        gap = gap,
        margin = margin,
        radius = radius,
        mat = mat,
        shadow = shadow,
        texture = texture,
        caption = caption,
    )

    fun matches(
        style: CollageStyle,
        template: Template,
        aspect: AspectPreset,
    ): Boolean = template == this.template &&
        aspect == this.aspect &&
        style.palette.id == paletteId &&
        near(style.gap, gap) &&
        near(style.margin, margin) &&
        near(style.radius, radius) &&
        near(style.mat, mat) &&
        style.shadow == shadow &&
        style.texture == texture

    private fun near(left: Float, right: Float): Boolean = abs(left - right) <= 0.0015f
}

object CompositionPresets {
    val Studio = CompositionPreset(
        id = "studio",
        name = "Studio",
        tagline = "Quiet grid, soft matte",
        template = Template.GRID,
        aspect = AspectPreset.AUTO,
        paletteId = "ivory",
        gap = 0.012f,
        margin = 0.04f,
        radius = 0.025f,
        mat = 0.018f,
        shadow = true,
        texture = true,
    )

    val Gallery = CompositionPreset(
        id = "gallery",
        name = "Gallery",
        tagline = "One lead frame, generous white",
        template = Template.HERO,
        aspect = AspectPreset.CLASSIC,
        paletteId = "paper",
        gap = 0.024f,
        margin = 0.07f,
        radius = 0f,
        mat = 0.045f,
        shadow = false,
        texture = false,
    )

    val Zine = CompositionPreset(
        id = "zine",
        name = "Zine",
        tagline = "Loose stack, printed edge",
        template = Template.PILE,
        aspect = AspectPreset.FOUR_FIVE,
        paletteId = "clay",
        gap = 0.006f,
        margin = 0.05f,
        radius = 0.009f,
        mat = 0f,
        shadow = true,
        texture = true,
    )

    val Noir = CompositionPreset(
        id = "noir",
        name = "Noir",
        tagline = "Tall columns, quiet contrast",
        template = Template.FLOW,
        aspect = AspectPreset.AUTO,
        paletteId = "night",
        gap = 0.008f,
        margin = 0.03f,
        radius = 0f,
        mat = 0f,
        shadow = false,
        texture = true,
    )

    val Postcard = CompositionPreset(
        id = "postcard",
        name = "Postcard",
        tagline = "Wide strip, warm paper",
        template = Template.STRIP,
        aspect = AspectPreset.CLASSIC,
        paletteId = "sage",
        gap = 0.012f,
        margin = 0.05f,
        radius = 0.01f,
        mat = 0.025f,
        shadow = true,
        texture = true,
    )

    val Bento = CompositionPreset(
        id = "bento",
        name = "Bento",
        tagline = "Square rhythm, clean gaps",
        template = Template.GRID,
        aspect = AspectPreset.SQUARE,
        paletteId = "mist",
        gap = 0.02f,
        margin = 0.045f,
        radius = 0.035f,
        mat = 0.01f,
        shadow = false,
        texture = true,
    )

    val all: List<CompositionPreset> = listOf(
        Studio,
        Gallery,
        Zine,
        Noir,
        Postcard,
        Bento,
    )
}
