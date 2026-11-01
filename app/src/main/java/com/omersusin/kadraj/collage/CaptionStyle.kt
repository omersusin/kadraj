package com.omersusin.kadraj.collage

enum class CaptionFont {
    SERIF,
    SANS,
}

enum class CaptionPlacement {
    TOP,
    CENTER,
    BOTTOM,
}

enum class CaptionAlignment {
    START,
    CENTER,
    END,
}

enum class CaptionTone {
    AUTO,
    PAPER,
    ACCENT,
}

data class CaptionStyle(
    val text: String = "",
    val font: CaptionFont = CaptionFont.SERIF,
    val placement: CaptionPlacement = CaptionPlacement.BOTTOM,
    val alignment: CaptionAlignment = CaptionAlignment.START,
    val tone: CaptionTone = CaptionTone.AUTO,
    val size: Float = 0.045f,
    val plate: Boolean = false,
) {
    val enabled: Boolean get() = text.isNotBlank()
}
