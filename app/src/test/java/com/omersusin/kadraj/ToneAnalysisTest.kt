package com.omersusin.kadraj

import com.omersusin.kadraj.collage.ToneAnalysis
import com.omersusin.kadraj.collage.suggestTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToneAnalysisTest {
    @Test
    fun `dark flat image gets lifted`() {
        val suggestion = suggestTone(
            ToneAnalysis(luminance = 0.24f, contrast = 0.08f, saturation = 0.18f, warmth = -0.04f),
        )
        assertTrue(suggestion.brightness > 0.15f)
        assertTrue(suggestion.contrast > 0.05f)
        assertTrue(suggestion.saturation > 0.03f)
        assertTrue(suggestion.warmth > 0.01f)
    }

    @Test
    fun `bright image is pulled back`() {
        val suggestion = suggestTone(
            ToneAnalysis(luminance = 0.82f, contrast = 0.24f, saturation = 0.46f, warmth = 0.08f),
        )
        assertTrue(suggestion.brightness < -0.15f)
        assertTrue(suggestion.warmth < 0f)
    }

    @Test
    fun `balanced image stays nearly untouched`() {
        val suggestion = suggestTone(
            ToneAnalysis(luminance = 0.54f, contrast = 0.18f, saturation = 0.34f, warmth = 0.035f),
        )
        assertEquals(0f, suggestion.brightness, 0.001f)
        assertEquals(0f, suggestion.contrast, 0.001f)
        assertEquals(0f, suggestion.saturation, 0.001f)
        assertEquals(0f, suggestion.warmth, 0.001f)
    }

    @Test
    fun `suggestions stay inside safe bounds`() {
        val darkest = suggestTone(ToneAnalysis(0f, 0f, 0f, -1f))
        val brightest = suggestTone(ToneAnalysis(1f, 1f, 1f, 1f))
        listOf(darkest, brightest).forEach {
            assertTrue(it.brightness in -0.26f..0.26f)
            assertTrue(it.contrast in -0.16f..0.28f)
            assertTrue(it.saturation in -0.18f..0.24f)
            assertTrue(it.warmth in -0.12f..0.12f)
        }
    }
}
