package com.omersusin.kadraj

import com.omersusin.kadraj.collage.AspectPreset
import com.omersusin.kadraj.collage.CollageConfig
import com.omersusin.kadraj.collage.ImageSpec
import com.omersusin.kadraj.collage.LayoutEngine
import com.omersusin.kadraj.collage.LayoutResult
import com.omersusin.kadraj.collage.Template
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class LayoutEngineTest {

    private val phone = 9f / 19.5f
    private val desktop = 16f / 10f
    private val portraitPhoto = 3f / 4f
    private val landscapePhoto = 3f / 2f

    private fun images(vararg aspects: Float): List<ImageSpec> =
        aspects.mapIndexed { index, aspect -> ImageSpec(index.toLong(), aspect) }

    private fun assertGeometry(images: List<ImageSpec>, config: CollageConfig) {
        val result = LayoutEngine.compose(images, config)
        assertCore(images, result)
        if (config.template != Template.PILE) {
            assertNoOverlap(result)
        }
        if (config.aspect != AspectPreset.AUTO && config.template != Template.FLOW) {
            assertEquals(config.aspect.ratio!!, result.aspect, 0.002f)
        }
    }

    private fun assertCore(images: List<ImageSpec>, result: LayoutResult) {
        assertEquals(images.size, result.cells.size)
        assertEquals(images.map { it.id }.toSet(), result.cells.map { it.imageId }.toSet())
        assertTrue(result.aspect > 0.15f && result.aspect < 4.5f)
        result.cells.forEach { cell ->
            val rect = cell.rect
            assertTrue("left ${rect.left}", rect.left >= -0.0001f)
            assertTrue("top ${rect.top}", rect.top >= -0.0001f)
            assertTrue("right ${rect.right}", rect.right <= 1.0001f)
            assertTrue("bottom ${rect.bottom}", rect.bottom <= 1.0001f)
            assertTrue("width", rect.width > 0.0001f)
            assertTrue("height", rect.height > 0.0001f)
        }
    }

    private fun assertNoOverlap(result: LayoutResult) {
        val cells = result.cells
        for (first in cells.indices) {
            for (second in first + 1 until cells.size) {
                assertFalse(
                    "cells overlap: ${cells[first].imageId} and ${cells[second].imageId}",
                    cells[first].rect.overlaps(cells[second].rect),
                )
            }
        }
    }

    @Test
    fun `auto template understands phones desktops and mixed sets`() {
        assertEquals(Template.GRID, LayoutEngine.resolveAutoTemplate(images(phone, phone, phone, phone)))
        assertEquals(Template.FLOW, LayoutEngine.resolveAutoTemplate(images(phone, phone, phone, phone, phone)))
        assertEquals(Template.GRID, LayoutEngine.resolveAutoTemplate(images(desktop, desktop)))
        assertEquals(Template.FLOW, LayoutEngine.resolveAutoTemplate(images(phone, desktop)))
        assertEquals(Template.FLOW, LayoutEngine.resolveAutoTemplate(images(phone, desktop, portraitPhoto)))
        assertEquals(
            Template.GRID,
            LayoutEngine.resolveAutoTemplate(images(*List(30) { if (it % 2 == 0) phone else desktop }.toFloatArray())),
        )
    }

    @Test
    fun `phone grids stay tall and desktop grids stay wide`() {
        val phones = LayoutEngine.compose(images(phone, phone, phone, phone), CollageConfig())
        assertTrue("phone collage should be portrait: ${phones.aspect}", phones.aspect < 0.8f)

        val desktops = LayoutEngine.compose(
            images(desktop, desktop, desktop, desktop),
            CollageConfig(template = Template.GRID),
        )
        assertTrue("desktop collage should be landscape: ${desktops.aspect}", desktops.aspect > 1.2f)
    }

    @Test
    fun `two desktop screenshots stack into a document`() {
        val result = LayoutEngine.compose(images(desktop, desktop), CollageConfig())
        assertTrue(result.aspect < 1f)
        assertTrue(result.aspect > 0.6f)
    }

    @Test
    fun `flow preserves every image aspect ratio`() {
        val list = images(phone, desktop, portraitPhoto, landscapePhoto, phone, 1f)
        val result = LayoutEngine.compose(
            list,
            CollageConfig(template = Template.FLOW, aspect = AspectPreset.AUTO),
        )
        result.cells.forEach { cell ->
            val source = list.first { it.id == cell.imageId }
            val pixelAspect = cell.rect.aspect * result.aspect
            assertTrue(
                "aspect for ${source.id}: $pixelAspect vs ${source.aspect}",
                abs(pixelAspect - source.aspect) < 0.01f,
            )
        }
    }

    @Test
    fun `all templates and counts produce valid geometry`() {
        val aspectSets = listOf(
            List(30) { phone },
            List(30) { desktop },
            listOf(phone, desktop, 1f, portraitPhoto, landscapePhoto, phone, desktop),
        )
        for (set in aspectSets) {
            for (count in 1..set.size) {
                val source = images(*set.take(count).toFloatArray())
                for (template in Template.entries) {
                    for (aspect in AspectPreset.entries) {
                        val config = CollageConfig(template = template, aspect = aspect)
                        assertGeometry(source, config)
                    }
                }
            }
        }
    }

    @Test
    fun `extreme controls do not break geometry`() {
        val source = images(*List(30) { if (it % 2 == 0) phone else desktop }.toFloatArray())
        for (template in Template.entries) {
            for (aspect in listOf(AspectPreset.AUTO, AspectPreset.FOUR_FIVE, AspectPreset.DESKTOP)) {
                assertCore(
                    source,
                    LayoutEngine.compose(
                        source,
                        CollageConfig(template = template, aspect = aspect, gap = 0.06f, margin = 0.12f),
                    ),
                )
            }
        }
    }

    @Test
    fun `pile keeps the first image on top and rotates the rest`() {
        val result = LayoutEngine.compose(
            images(phone, desktop, portraitPhoto, landscapePhoto),
            CollageConfig(template = Template.PILE, aspect = AspectPreset.FOUR_FIVE),
        )
        assertCore(images(phone, desktop, portraitPhoto, landscapePhoto), result)
        val first = result.cellFor(0)
        assertTrue(first != null && first.z == result.cells.size)
        assertTrue(result.cells.filter { it.imageId != 0L }.all { it.rotation != 0f })
    }

    @Test
    fun `layout is deterministic`() {
        val source = images(phone, desktop, portraitPhoto, landscapePhoto, phone)
        val first = LayoutEngine.compose(source, CollageConfig())
        val second = LayoutEngine.compose(source, CollageConfig())
        assertEquals(first.aspect, second.aspect, 0f)
        assertEquals(first.cells, second.cells)
    }
}
