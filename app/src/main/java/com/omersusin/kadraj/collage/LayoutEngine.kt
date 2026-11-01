package com.omersusin.kadraj.collage

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

object LayoutEngine {
    fun compose(images: List<ImageSpec>, config: CollageConfig): LayoutResult {
        if (images.isEmpty()) return LayoutResult(1f, emptyList())

        val requested = if (config.template == Template.AUTO) {
            resolveAutoTemplate(images)
        } else {
            config.template
        }

        return when (requested) {
            Template.GRID, Template.AUTO -> grid(images, config)
            Template.FLOW -> {
                if (config.aspect.ratio != null) {
                    grid(images, config.copy(template = Template.GRID))
                } else {
                    flow(images, config)
                }
            }
            Template.HERO -> hero(images, config)
            Template.STRIP -> strip(images, config)
            Template.PILE -> pile(images, config)
        }
    }

    fun resolveAutoTemplate(images: List<ImageSpec>): Template {
        if (images.size <= 1) return Template.GRID
        val profile = images.orientationProfile()
        return when {
            profile.mixed -> Template.FLOW
            images.size <= 4 -> Template.GRID
            else -> Template.FLOW
        }
    }

    private fun grid(images: List<ImageSpec>, config: CollageConfig): LayoutResult {
        val profile = images.orientationProfile()
        val dominant = profile.dominantAspect
        val preferred = preferredAspect(profile)
        val fixedAspect = config.aspect.ratio
        val choice = chooseGrid(
            count = images.size,
            dominantAspect = dominant,
            preferredAspect = preferred,
            fixedAspect = fixedAspect,
        )
        val canvas = Canvas.of(choice.baseAspect, config.margin, config.gap)
        val count = images.size
        val rows = choice.rows
        val fullRows = count / choice.cols
        val remainder = count % choice.cols
        val rowHeight = (canvas.availableHeight - canvas.gap * (rows - 1)) / rows
        scalarCheck(rowHeight)

        val cells = ArrayList<CollageCell>(count)
        var index = 0
        for (row in 0 until rows) {
            val inRow = if (row < fullRows) choice.cols else remainder
            val cellWidth = (canvas.availableWidth - canvas.gap * (inRow - 1)) / inRow
            scalarCheck(cellWidth)
            val y = canvas.top + row * (rowHeight + canvas.gap)
            var x = canvas.left
            repeat(inRow) {
                val image = images[index]
                val rect = NRect(x, y, x + cellWidth, y + rowHeight)
                    .normalizedBy(canvas.width, canvas.height)
                cells += CollageCell(image.id, rect)
                x += cellWidth + canvas.gap
                index++
            }
        }
        return LayoutResult(canvas.width / canvas.height, cells)
    }

    private data class GridChoice(
        val cols: Int,
        val rows: Int,
        val baseAspect: Float,
        val cost: Float,
    )

    private fun chooseGrid(
        count: Int,
        dominantAspect: Float,
        preferredAspect: Float,
        fixedAspect: Float?,
    ): GridChoice {
        var best: GridChoice? = null
        for (cols in 1..count) {
            val rows = ceil(count.toFloat() / cols).toInt()
            val baseAspect = fixedAspect ?: (cols * dominantAspect / rows)
            val cellAspect = if (fixedAspect != null) {
                fixedAspect * rows / cols
            } else {
                dominantAspect
            }
            var cost = if (fixedAspect == null) {
                abs(ln(baseAspect / preferredAspect))
            } else {
                abs(ln(cellAspect / dominantAspect))
            }

            val leftover = cols * rows - count
            cost += leftover * 1.25f

            if (leftover > 0) {
                val remainder = count % cols
                if (remainder > 0) {
                    val specialAspect = baseAspect * rows / remainder
                    cost += abs(ln(specialAspect / dominantAspect)) *
                        (remainder.toFloat() / count) * 0.5f
                }
            }

            if (fixedAspect == null && (baseAspect < 0.3f || baseAspect > 3.2f)) {
                cost += 1.4f + abs(ln(baseAspect.coerceIn(0.3f, 3.2f) / preferredAspect))
            }

            if (best == null || cost < best.cost) {
                best = GridChoice(cols, rows, baseAspect, cost)
            }
        }
        return best ?: GridChoice(1, 1, dominantAspect, 0f)
    }

    private fun flow(images: List<ImageSpec>, config: CollageConfig): LayoutResult {
        val count = images.size
        if (count <= 1) return grid(images, config)

        val profile = images.orientationProfile()
        val preferred = preferredAspect(profile)
        val maxColumns = min(3, count)
        val minColumns = if (count == 2) 2 else 1

        var best: FlowCandidate? = null
        for (columns in minColumns..maxColumns) {
            for (assignment in assignments(images, columns)) {
                val candidate = buildFlow(images, assignment, columns, config, preferred)
                    ?: continue
                if (best == null || candidate.cost < best.cost) best = candidate
            }
        }

        return best?.result ?: grid(images, config.copy(template = Template.GRID))
    }

    private data class FlowCandidate(
        val result: LayoutResult,
        val cost: Float,
    )

    private fun assignments(images: List<ImageSpec>, columns: Int): List<IntArray> {
        val count = images.size
        val result = LinkedHashMap<String, IntArray>()

        fun add(assignment: IntArray) {
            result.putIfAbsent(assignment.joinToString(","), assignment)
        }

        add(IntArray(count) { it % columns })

        val balanced = IntArray(count)
        val sums = FloatArray(columns)
        val counts = IntArray(columns)
        for (index in 0 until count) {
            var target = 0
            for (column in 0 until columns) {
                val targetScore = sums[column] + counts[column] * 0.001f
                val bestScore = sums[target] + counts[target] * 0.001f
                if (targetScore < bestScore) target = column
            }
            balanced[index] = target
            sums[target] += 1f / images[index].safeAspect()
            counts[target]++
        }
        add(balanced)

        val byAspect = images.indices.sortedByDescending { images[it].safeAspect() }
        val sorted = IntArray(count)
        val sortedSums = FloatArray(columns)
        val sortedCounts = IntArray(columns)
        for ((order, index) in byAspect.withIndex()) {
            var target = 0
            for (column in 0 until columns) {
                val targetScore = sortedSums[column] + sortedCounts[column] * 0.001f
                val bestScore = sortedSums[target] + sortedCounts[target] * 0.001f
                if (targetScore < bestScore) target = column
            }
            sorted[index] = target
            sortedSums[target] += 1f / images[index].safeAspect()
            sortedCounts[target]++
        }
        add(sorted)

        return result.values.toList()
    }

    private fun buildFlow(
        images: List<ImageSpec>,
        assignment: IntArray,
        columns: Int,
        config: CollageConfig,
        preferredAspect: Float,
    ): FlowCandidate? {
        val height = 1000f
        val columnIndices = Array(columns) { mutableListOf<Int>() }
        assignment.forEachIndexed { index, column -> columnIndices[column] += index }
        if (columnIndices.any { it.isEmpty() }) return null

        val gapFraction = config.gap.coerceIn(0f, 0.1f)
        val marginFraction = config.margin.coerceIn(0f, 0.2f)
        var gap = gapFraction * height
        var widths = FloatArray(columns)
        var contentWidth = 0f

        repeat(2) {
            contentWidth = 0f
            for (column in 0 until columns) {
                val indices = columnIndices[column]
                val sum = indices.sumOf { 1.0 / images[it].safeAspect() }.toFloat()
                val available = height - gap * (indices.size - 1)
                if (sum <= 0f || available <= 0f) return null
                widths[column] = available / sum
                contentWidth += widths[column]
            }
            contentWidth += gap * (columns - 1)
            val minSide = min(contentWidth, height)
            gap = gapFraction * minSide
        }

        contentWidth = 0f
        for (column in 0 until columns) {
            val indices = columnIndices[column]
            val sum = indices.sumOf { 1.0 / images[it].safeAspect() }.toFloat()
            val available = height - gap * (indices.size - 1)
            if (sum <= 0f || available <= 0f) return null
            widths[column] = available / sum
            contentWidth += widths[column]
        }
        contentWidth += gap * (columns - 1)

        if (contentWidth <= 0f) return null
        val minSide = min(contentWidth, height)
        val margin = marginFraction * minSide
        val totalWidth = contentWidth + 2f * margin
        val totalHeight = height + 2f * margin
        val naturalAspect = totalWidth / totalHeight
        if (naturalAspect < 0.18f || naturalAspect > 4.5f) return null

        val widthRatio = (widths.maxOrNull() ?: 1f) / (widths.minOrNull() ?: 1f)
        if (widthRatio > 3.6f) return null

        val counts = columnIndices.map { it.size }
        val imbalance = ((counts.maxOrNull() ?: 0) - (counts.minOrNull() ?: 0)).toFloat() / images.size
        val deviation = abs(ln(naturalAspect / preferredAspect))
        val ratioCost = ln(widthRatio) * 0.55f
        val extremeCost = when {
            naturalAspect < 0.3f -> (0.3f / naturalAspect - 1f) * 0.35f
            naturalAspect > 3.2f -> (naturalAspect / 3.2f - 1f) * 0.35f
            else -> 0f
        }
        val cost = deviation + ratioCost + imbalance * 0.18f + extremeCost

        val cells = ArrayList<CollageCell>(images.size)
        var x = margin
        for (column in 0 until columns) {
            val indices = columnIndices[column]
            var y = margin
            for (index in indices) {
                val image = images[index]
                val cellHeight = widths[column] / image.safeAspect()
                val rect = NRect(x, y, x + widths[column], y + cellHeight)
                    .normalizedBy(totalWidth, totalHeight)
                cells += CollageCell(image.id, rect)
                y += cellHeight + gap
            }
            x += widths[column] + gap
        }

        return FlowCandidate(LayoutResult(naturalAspect, cells), cost)
    }

    private fun hero(images: List<ImageSpec>, config: CollageConfig): LayoutResult {
        if (images.size <= 1) return grid(images, config)
        val profile = images.orientationProfile()
        val autoAspect = when {
            profile.portraitShare >= 0.6f -> 0.8f
            profile.landscapeShare >= 0.6f -> 1.6f
            else -> 1f
        }
        val canvas = Canvas.of(
            config.aspect.ratio ?: autoAspect,
            config.margin,
            config.gap,
        )
        val cells = ArrayList<CollageCell>(images.size)
        val rest = images.drop(1)

        if (canvas.width / canvas.height < 1f) {
            val heroHeight = (canvas.availableHeight - canvas.gap) * 0.61f
            scalarCheck(heroHeight)
            val hero = NRect(
                canvas.left,
                canvas.top,
                canvas.right,
                canvas.top + heroHeight,
            ).normalizedBy(canvas.width, canvas.height)
            cells += CollageCell(images.first().id, hero)

            val top = canvas.top + heroHeight + canvas.gap
            val bottomHeight = canvas.bottom - top
            val bottomColumns = when {
                rest.size <= 3 -> rest.size
                rest.size <= 6 -> 4
                else -> 4
            }
            fillGrid(
                images = rest,
                left = canvas.left,
                top = top,
                right = canvas.right,
                bottom = top + bottomHeight,
                columns = bottomColumns,
                gap = canvas.gap,
                canvasWidth = canvas.width,
                canvasHeight = canvas.height,
                cells = cells,
            )
        } else {
            val heroWidth = (canvas.availableWidth - canvas.gap) * 0.63f
            scalarCheck(heroWidth)
            val hero = NRect(
                canvas.left,
                canvas.top,
                canvas.left + heroWidth,
                canvas.bottom,
            ).normalizedBy(canvas.width, canvas.height)
            cells += CollageCell(images.first().id, hero)

            val left = canvas.left + heroWidth + canvas.gap
            val sideWidth = canvas.right - left
            val sideRows = when {
                rest.size <= 4 -> rest.size
                else -> 4
            }
            fillGrid(
                images = rest,
                left = left,
                top = canvas.top,
                right = left + sideWidth,
                bottom = canvas.bottom,
                columns = if (rest.size <= sideRows) 1 else 2,
                gap = canvas.gap,
                canvasWidth = canvas.width,
                canvasHeight = canvas.height,
                cells = cells,
            )
        }

        return LayoutResult(canvas.width / canvas.height, cells)
    }

    private fun fillGrid(
        images: List<ImageSpec>,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        columns: Int,
        gap: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        cells: MutableList<CollageCell>,
    ) {
        if (images.isEmpty()) return
        val rows = ceil(images.size.toFloat() / columns).toInt()
        val rowHeight = (bottom - top - gap * (rows - 1)) / rows
        scalarCheck(rowHeight)
        var index = 0
        for (row in 0 until rows) {
            val remaining = images.size - index
            val inRow = min(columns, remaining)
            val cellWidth = (right - left - gap * (inRow - 1)) / inRow
            scalarCheck(cellWidth)
            val y = top + row * (rowHeight + gap)
            var x = left
            repeat(inRow) {
                val rect = NRect(x, y, x + cellWidth, y + rowHeight)
                    .normalizedBy(canvasWidth, canvasHeight)
                cells += CollageCell(images[index].id, rect)
                x += cellWidth + gap
                index++
            }
        }
    }

    private fun strip(images: List<ImageSpec>, config: CollageConfig): LayoutResult {
        val profile = images.orientationProfile()
        val dominant = profile.dominantAspect
        val count = images.size
        val fixed = config.aspect.ratio

        val horizontal: Boolean
        val baseAspect: Float
        if (fixed != null) {
            horizontal = fixed >= 1f
            baseAspect = fixed
        } else {
            val horizontalAspect = count * dominant
            val verticalAspect = dominant / count
            fun score(value: Float): Float = abs(ln(value / 1.4f)) +
                if (value < 0.3f || value > 3.2f) 1.5f else 0f
            horizontal = score(horizontalAspect) <= score(verticalAspect)
            baseAspect = (if (horizontal) horizontalAspect else verticalAspect)
                .coerceIn(0.28f, 3.2f)
        }

        val canvas = Canvas.of(baseAspect, config.margin, config.gap)
        val cells = ArrayList<CollageCell>(count)
        if (horizontal) {
            val cellWidth = (canvas.availableWidth - canvas.gap * (count - 1)) / count
            scalarCheck(cellWidth)
            var x = canvas.left
            images.forEach { image ->
                val rect = NRect(
                    x,
                    canvas.top,
                    x + cellWidth,
                    canvas.bottom,
                ).normalizedBy(canvas.width, canvas.height)
                cells += CollageCell(image.id, rect)
                x += cellWidth + canvas.gap
            }
        } else {
            val cellHeight = (canvas.availableHeight - canvas.gap * (count - 1)) / count
            scalarCheck(cellHeight)
            var y = canvas.top
            images.forEach { image ->
                val rect = NRect(
                    canvas.left,
                    y,
                    canvas.right,
                    y + cellHeight,
                ).normalizedBy(canvas.width, canvas.height)
                cells += CollageCell(image.id, rect)
                y += cellHeight + canvas.gap
            }
        }
        return LayoutResult(canvas.width / canvas.height, cells)
    }

    private fun pile(images: List<ImageSpec>, config: CollageConfig): LayoutResult {
        val profile = images.orientationProfile()
        val autoAspect = when {
            profile.portraitShare >= 0.6f -> 0.8f
            profile.landscapeShare >= 0.6f -> 1.6f
            else -> 1f
        }
        val canvas = Canvas.of(
            config.aspect.ratio ?: autoAspect,
            config.margin,
            config.gap,
        )
        val count = images.size
        val centerX = canvas.width / 2f
        val centerY = canvas.height / 2f
        val baseWidth = canvas.availableWidth * when {
            count <= 1 -> 0.8f
            count <= 3 -> 0.74f
            count <= 5 -> 0.68f
            else -> 0.62f
        }
        val maxCardHeight = canvas.availableHeight * 0.72f
        val maxOffsetMagnitude = min(4, (count + 1) / 2)
        val cells = ArrayList<CollageCell>(count)

        images.forEachIndexed { index, image ->
            val scale = 1f - min(index, 6) * 0.022f
            var width = baseWidth * scale
            var height = width / image.safeAspect()
            if (height > maxCardHeight) {
                height = maxCardHeight
                width = height * image.safeAspect()
            }

            val direction = if (index % 2 == 1) -1f else 1f
            val magnitude = ((index + 1) / 2).coerceAtMost(maxOffsetMagnitude).toFloat()
            val offsetX = if (index == 0) 0f else {
                direction * magnitude * canvas.availableWidth * 0.036f
            }
            val offsetY = if (index == 0) 0f else {
                (if (index % 3 == 0) 1f else -0.6f) * magnitude *
                    canvas.availableHeight * 0.012f
            }
            val rotation = if (index == 0) 0f else {
                direction * magnitude * 3.6f * sign(index.toFloat())
            }
            val left = centerX + offsetX - width / 2f
            val top = centerY + offsetY - height / 2f
            val rect = NRect(left, top, left + width, top + height)
                .normalizedBy(canvas.width, canvas.height)
            cells += CollageCell(
                imageId = image.id,
                rect = rect,
                rotation = rotation,
                z = count - index,
            )
        }

        return LayoutResult(canvas.width / canvas.height, cells)
    }

    private fun scalarCheck(value: Float) {
        require(value > 0f && value.isFinite()) { "Invalid collage geometry" }
    }

    private data class Canvas(
        val width: Float,
        val height: Float,
        val margin: Float,
        val gap: Float,
    ) {
        val left: Float get() = margin
        val top: Float get() = margin
        val right: Float get() = width - margin
        val bottom: Float get() = height - margin
        val availableWidth: Float get() = right - left
        val availableHeight: Float get() = bottom - top

        companion object {
            fun of(aspect: Float, marginFraction: Float, gapFraction: Float): Canvas {
                val safe = aspect.coerceIn(0.25f, 4f)
                val width: Float
                val height: Float
                if (safe >= 1f) {
                    width = 1000f
                    height = width / safe
                } else {
                    height = 1000f
                    width = height * safe
                }
                val minSide = min(width, height)
                return Canvas(
                    width = width,
                    height = height,
                    margin = marginFraction.coerceIn(0f, 0.2f) * minSide,
                    gap = gapFraction.coerceIn(0f, 0.1f) * minSide,
                )
            }
        }
    }
}
