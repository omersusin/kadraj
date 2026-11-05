package com.omersusin.kadraj

import com.omersusin.kadraj.collage.CaptionStyle
import com.omersusin.kadraj.collage.CompositionPresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompositionPresetTest {
    @Test
    fun `every preset is a valid matching composition`() {
        CompositionPresets.all.forEach { preset ->
            val style = preset.style(CaptionStyle(text = "Kept caption"))
            assertTrue(preset.name, preset.matches(style, preset.template, preset.aspect))
            assertEquals("Kept caption", style.caption.text)
        }
    }

    @Test
    fun `changing a material control leaves the preset`() {
        val preset = CompositionPresets.Gallery
        val changed = preset.style().copy(gap = preset.gap + 0.01f)
        assertFalse(preset.matches(changed, preset.template, preset.aspect))
    }

    @Test
    fun `presets are unique and have palettes`() {
        assertEquals(CompositionPresets.all.size, CompositionPresets.all.map { it.id }.toSet().size)
        CompositionPresets.all.forEach { preset ->
            assertTrue(preset.name, preset.paletteId.isNotBlank())
            assertTrue(preset.name, preset.style().palette.colors.size >= 2)
        }
    }
}
