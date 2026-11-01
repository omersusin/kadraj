package com.omersusin.kadraj.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.omersusin.kadraj.collage.Template
import com.omersusin.kadraj.ui.theme.Kadraj

enum class Glyph {
    Plus,
    Close,
    ChevronLeft,
    ChevronRight,
    Check,
    Export,
    Download,
    Share,
    Info,
    Grid,
    Flow,
    Hero,
    Strip,
    Pile,
    Sparkle,
    Image,
    Sliders,
    Fit,
    Cover,
    Shuffle,
    ArrowRight,
    Lock,
    Phone,
    Desktop,
    Mixed,
    Trash,
    More,
}

@Composable
fun GlyphIcon(
    glyph: Glyph,
    modifier: Modifier = Modifier,
    tint: Color = Kadraj.Paper,
    strokeWidth: Dp = 1.55.dp,
) {
    Canvas(modifier) {
        drawGlyph(glyph, tint, strokeWidth.toPx())
    }
}

fun glyphForTemplate(template: Template): Glyph = when (template) {
    Template.AUTO -> Glyph.Sparkle
    Template.GRID -> Glyph.Grid
    Template.FLOW -> Glyph.Flow
    Template.HERO -> Glyph.Hero
    Template.STRIP -> Glyph.Strip
    Template.PILE -> Glyph.Pile
}

private fun DrawScope.drawGlyph(glyph: Glyph, color: Color, strokeWidth: Float) {
    val scale = size.minDimension / 24f
    val offsetX = (size.width - 24f * scale) / 2f
    val offsetY = (size.height - 24f * scale) / 2f
    fun x(value: Float) = offsetX + value * scale
    fun y(value: Float) = offsetY + value * scale
    fun point(px: Float, py: Float) = Offset(x(px), y(py))
    val stroke = Stroke(
        width = strokeWidth,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )

    fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
        drawLine(color, point(x1, y1), point(x2, y2), strokeWidth, StrokeCap.Round)
    }

    fun path(build: Path.() -> Unit, fill: Boolean = false) {
        val path = Path().apply(build)
        drawPath(
            path = path,
            color = color,
            style = if (fill) androidx.compose.ui.graphics.drawscope.Fill else stroke,
        )
    }

    fun roundRect(left: Float, top: Float, width: Float, height: Float, radius: Float) {
        drawRoundRect(
            color = color,
            topLeft = point(left, top),
            size = Size(width * scale, height * scale),
            cornerRadius = CornerRadius(radius * scale),
            style = stroke,
        )
    }

    when (glyph) {
        Glyph.Plus -> {
            line(12f, 5f, 12f, 19f)
            line(5f, 12f, 19f, 12f)
        }
        Glyph.Close -> {
            line(6f, 6f, 18f, 18f)
            line(18f, 6f, 6f, 18f)
        }
        Glyph.ChevronLeft -> path {
            moveTo(x(15f), y(5f))
            lineTo(x(8f), y(12f))
            lineTo(x(15f), y(19f))
        }
        Glyph.ChevronRight -> path {
            moveTo(x(9f), y(5f))
            lineTo(x(16f), y(12f))
            lineTo(x(9f), y(19f))
        }
        Glyph.Check -> path {
            moveTo(x(5f), y(12.5f))
            lineTo(x(10f), y(17.5f))
            lineTo(x(19f), y(7f))
        }
        Glyph.Export -> {
            path {
                moveTo(x(5f), y(15f))
                lineTo(x(5f), y(18.5f))
                quadraticBezierTo(x(5f), y(19.5f), x(6f), y(19.5f))
                lineTo(x(18f), y(19.5f))
                quadraticBezierTo(x(19f), y(19.5f), x(19f), y(18.5f))
                lineTo(x(19f), y(15f))
            }
            line(12f, 15.5f, 12f, 4f)
            path {
                moveTo(x(7.5f), y(8.5f))
                lineTo(x(12f), y(4f))
                lineTo(x(16.5f), y(8.5f))
            }
        }
        Glyph.Download -> {
            path {
                moveTo(x(5f), y(15f))
                lineTo(x(5f), y(18.5f))
                quadraticBezierTo(x(5f), y(19.5f), x(6f), y(19.5f))
                lineTo(x(18f), y(19.5f))
                quadraticBezierTo(x(19f), y(19.5f), x(19f), y(18.5f))
                lineTo(x(19f), y(15f))
            }
            line(12f, 4f, 12f, 15.5f)
            path {
                moveTo(x(7.5f), y(11f))
                lineTo(x(12f), y(15.5f))
                lineTo(x(16.5f), y(11f))
            }
        }
        Glyph.Share -> {
            drawCircle(color, 1.9f * scale, point(6f, 12f), style = stroke)
            drawCircle(color, 1.9f * scale, point(17.5f, 5.5f), style = stroke)
            drawCircle(color, 1.9f * scale, point(17.5f, 18.5f), style = stroke)
            line(7.7f, 11f, 15.8f, 6.5f)
            line(7.7f, 13f, 15.8f, 17.5f)
        }
        Glyph.Info -> {
            drawCircle(color, 8.5f * scale, point(12f, 12f), style = stroke)
            line(12f, 10.5f, 12f, 17f)
            drawCircle(color, 1.05f * scale, point(12f, 7.2f))
        }
        Glyph.Grid -> {
            roundRect(3.5f, 3.5f, 7f, 7f, 1.4f)
            roundRect(13.5f, 3.5f, 7f, 7f, 1.4f)
            roundRect(3.5f, 13.5f, 7f, 7f, 1.4f)
            roundRect(13.5f, 13.5f, 7f, 7f, 1.4f)
        }
        Glyph.Flow -> {
            roundRect(3.5f, 3.5f, 5.5f, 12f, 1f)
            roundRect(10.5f, 3.5f, 4f, 17f, 1f)
            roundRect(16f, 3.5f, 4.5f, 9f, 1f)
        }
        Glyph.Hero -> {
            roundRect(3.5f, 4f, 10.5f, 16f, 1.5f)
            roundRect(16f, 4f, 4.5f, 7f, 1f)
            roundRect(16f, 13f, 4.5f, 7f, 1f)
        }
        Glyph.Strip -> {
            roundRect(4f, 5f, 4f, 14f, 1f)
            roundRect(10f, 5f, 4f, 14f, 1f)
            roundRect(16f, 5f, 4f, 14f, 1f)
        }
        Glyph.Pile -> {
            roundRect(4f, 7f, 12f, 12f, 1.5f)
            roundRect(8f, 4.5f, 12f, 12f, 1.5f)
            roundRect(12f, 9f, 8f, 8f, 1.2f)
        }
        Glyph.Sparkle -> {
            path(
                {
                    moveTo(x(12f), y(2.8f))
                    quadraticBezierTo(x(12.8f), y(9.8f), x(20.2f), y(12f))
                    quadraticBezierTo(x(12.8f), y(14.2f), x(12f), y(21.2f))
                    quadraticBezierTo(x(11.2f), y(14.2f), x(3.8f), y(12f))
                    quadraticBezierTo(x(11.2f), y(9.8f), x(12f), y(2.8f))
                    close()
                },
                fill = true,
            )
            drawCircle(color, 1.1f * scale, point(19.2f, 5f))
        }
        Glyph.Image -> {
            roundRect(3f, 4f, 18f, 16f, 2.4f)
            drawCircle(color, 1.55f * scale, point(8.5f, 9.2f), style = stroke)
            path {
                moveTo(x(5.2f), y(17.2f))
                lineTo(x(10.2f), y(12.2f))
                lineTo(x(13.2f), y(15.2f))
                lineTo(x(16.2f), y(12.6f))
                lineTo(x(19.2f), y(16.4f))
            }
        }
        Glyph.Sliders -> {
            line(3f, 7f, 21f, 7f)
            line(3f, 12f, 21f, 12f)
            line(3f, 17f, 21f, 17f)
            drawCircle(color, 1.9f * scale, point(8f, 7f), style = Stroke(strokeWidth, cap = StrokeCap.Round))
            drawCircle(color, 1.9f * scale, point(15f, 12f), style = Stroke(strokeWidth, cap = StrokeCap.Round))
            drawCircle(color, 1.9f * scale, point(10.5f, 17f), style = Stroke(strokeWidth, cap = StrokeCap.Round))
        }
        Glyph.Fit -> {
            path {
                moveTo(x(4f), y(9f))
                lineTo(x(4f), y(4f))
                lineTo(x(9f), y(4f))
            }
            path {
                moveTo(x(15f), y(4f))
                lineTo(x(20f), y(4f))
                lineTo(x(20f), y(9f))
            }
            path {
                moveTo(x(4f), y(15f))
                lineTo(x(4f), y(20f))
                lineTo(x(9f), y(20f))
            }
            path {
                moveTo(x(15f), y(20f))
                lineTo(x(20f), y(20f))
                lineTo(x(20f), y(15f))
            }
        }
        Glyph.Cover -> {
            line(10f, 10f, 4.5f, 4.5f)
            line(14f, 10f, 19.5f, 4.5f)
            line(10f, 14f, 4.5f, 19.5f)
            line(14f, 14f, 19.5f, 19.5f)
            path {
                moveTo(x(4.5f), y(8.5f))
                lineTo(x(4.5f), y(4.5f))
                lineTo(x(8.5f), y(4.5f))
            }
            path {
                moveTo(x(15.5f), y(4.5f))
                lineTo(x(19.5f), y(4.5f))
                lineTo(x(19.5f), y(8.5f))
            }
            path {
                moveTo(x(4.5f), y(15.5f))
                lineTo(x(4.5f), y(19.5f))
                lineTo(x(8.5f), y(19.5f))
            }
            path {
                moveTo(x(15.5f), y(19.5f))
                lineTo(x(19.5f), y(19.5f))
                lineTo(x(19.5f), y(15.5f))
            }
        }
        Glyph.Shuffle -> {
            path {
                moveTo(x(3.5f), y(7f))
                lineTo(x(7f), y(7f))
                cubicTo(x(10.5f), y(7f), x(10.5f), y(17f), x(14f), y(17f))
                lineTo(x(20f), y(17f))
            }
            path {
                moveTo(x(17f), y(14f))
                lineTo(x(20f), y(17f))
                lineTo(x(17f), y(20f))
            }
            path {
                moveTo(x(3.5f), y(17f))
                lineTo(x(7f), y(17f))
                cubicTo(x(10.5f), y(17f), x(10.5f), y(7f), x(14f), y(7f))
                lineTo(x(20f), y(7f))
            }
            path {
                moveTo(x(17f), y(4f))
                lineTo(x(20f), y(7f))
                lineTo(x(17f), y(10f))
            }
        }
        Glyph.ArrowRight -> {
            line(4f, 12f, 20f, 12f)
            path {
                moveTo(x(14f), y(6f))
                lineTo(x(20f), y(12f))
                lineTo(x(14f), y(18f))
            }
        }
        Glyph.Lock -> {
            roundRect(5f, 10f, 14f, 10f, 2f)
            path {
                moveTo(x(8.2f), y(10f))
                lineTo(x(8.2f), y(7.8f))
                cubicTo(x(8.2f), y(5.7f), x(9.9f), y(4f), x(12f), y(4f))
                cubicTo(x(14.1f), y(4f), x(15.8f), y(5.7f), x(15.8f), y(7.8f))
                lineTo(x(15.8f), y(10f))
            }
            drawCircle(color, 0.9f * scale, point(12f, 15f))
        }
        Glyph.Phone -> {
            roundRect(7f, 3f, 10f, 18f, 2f)
            line(10.5f, 17.5f, 13.5f, 17.5f)
        }
        Glyph.Desktop -> {
            roundRect(3f, 5f, 18f, 11.5f, 1.8f)
            line(9f, 19f, 15f, 19f)
            line(12f, 16.5f, 12f, 19f)
        }
        Glyph.Mixed -> {
            roundRect(4f, 5f, 10f, 12f, 1.4f)
            roundRect(10f, 8f, 10f, 12f, 1.4f)
        }
        Glyph.Trash -> {
            line(4f, 7f, 20f, 7f)
            path {
                moveTo(x(9f), y(7f))
                lineTo(x(9f), y(4.8f))
                lineTo(x(15f), y(4.8f))
                lineTo(x(15f), y(7f))
            }
            path {
                moveTo(x(6.5f), y(7f))
                lineTo(x(7.5f), y(19.5f))
                lineTo(x(16.5f), y(19.5f))
                lineTo(x(17.5f), y(7f))
            }
            line(10f, 11f, 10.4f, 16f)
            line(14f, 11f, 13.6f, 16f)
        }
        Glyph.More -> {
            drawCircle(color, 1.35f * scale, point(5.5f, 12f))
            drawCircle(color, 1.35f * scale, point(12f, 12f))
            drawCircle(color, 1.35f * scale, point(18.5f, 12f))
        }
    }
}
