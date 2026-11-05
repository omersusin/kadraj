package com.omersusin.kadraj.collage

/**
 * Lightweight, on-device measurements used to make a considered first pass at
 * tone. Values are normalized to 0..1 where noted.
 */
data class ToneAnalysis(
    val luminance: Float,
    val contrast: Float,
    val saturation: Float,
    val warmth: Float,
    val focusX: Float = 0.5f,
    val focusY: Float = 0.5f,
)

data class ToneSuggestion(
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val warmth: Float,
)

/**
 * A restrained auto-tone curve. It aims for a print-like midtone, opens flat
 * images, and leaves already-good photos almost untouched.
 */
fun suggestTone(analysis: ToneAnalysis): ToneSuggestion {
    val luma = analysis.luminance.coerceIn(0f, 1f)
    val spread = analysis.contrast.coerceIn(0f, 1f)
    val chroma = analysis.saturation.coerceIn(0f, 1f)
    val temperature = analysis.warmth.coerceIn(-1f, 1f)

    return ToneSuggestion(
        brightness = ((0.54f - luma) * 0.82f).coerceIn(-0.26f, 0.26f),
        contrast = ((0.18f - spread) * 1.05f).coerceIn(-0.16f, 0.28f),
        saturation = ((0.34f - chroma) * 0.62f).coerceIn(-0.18f, 0.24f),
        warmth = ((0.035f - temperature) * 0.55f).coerceIn(-0.12f, 0.12f),
    )
}
