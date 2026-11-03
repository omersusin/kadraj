package com.omersusin.kadraj.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omersusin.kadraj.BuildConfig
import com.omersusin.kadraj.EditorViewModel
import com.omersusin.kadraj.R
import com.omersusin.kadraj.collage.AspectPreset
import com.omersusin.kadraj.collage.CollageConfig
import com.omersusin.kadraj.collage.CollageStyle
import com.omersusin.kadraj.collage.ImageSpec
import com.omersusin.kadraj.collage.LayoutEngine
import com.omersusin.kadraj.collage.Palettes
import com.omersusin.kadraj.collage.Template
import com.omersusin.kadraj.render.CollagePainter
import com.omersusin.kadraj.render.ImageProvider
import com.omersusin.kadraj.render.ProvidedImage
import com.omersusin.kadraj.ui.components.CircleIconButton
import com.omersusin.kadraj.ui.components.Glyph
import com.omersusin.kadraj.ui.components.GlyphIcon
import com.omersusin.kadraj.ui.components.KadrajButton
import com.omersusin.kadraj.ui.components.Wordmark
import com.omersusin.kadraj.ui.components.pressable
import com.omersusin.kadraj.ui.theme.Kadraj
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val demoImageSpecs = listOf(
    ImageSpec(id = 1L, aspect = 0.8f),
    ImageSpec(id = 2L, aspect = 1.5f),
    ImageSpec(id = 3L, aspect = 1.5f),
)

private val demoImageResources = listOf(
    R.drawable.demo_portrait,
    R.drawable.demo_architecture,
    R.drawable.demo_coffee,
)

private val demoCollageConfig = CollageConfig(
    template = Template.HERO,
    aspect = AspectPreset.SQUARE,
    gap = 0.006f,
    margin = 0f,
)

private val demoCollageStyle = CollageStyle(
    palette = Palettes.Night,
    gap = 0.006f,
    margin = 0f,
    radius = 0f,
    mat = 0f,
    shadow = false,
    texture = false,
)

@Composable
fun EmptyState(
    onPickImages: () -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
) {
    val haptics = LocalHapticFeedback.current

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val compact = maxHeight < 700.dp
        val headlineSize = if (maxWidth < 360.dp) 29.sp else 32.sp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            PhotoHero(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                onInfo = onInfo,
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(if (compact) 17.dp else 21.dp))

                BasicText(
                    text = "Mixed ratios.",
                    style = Kadraj.Type.Display.copy(
                        color = Kadraj.Paper,
                        fontSize = headlineSize,
                        lineHeight = (headlineSize.value + 2).sp,
                        letterSpacing = (-0.7).sp,
                    ),
                )
                BasicText(
                    text = "One composition.",
                    style = Kadraj.Type.Display.copy(
                        color = Kadraj.Peach,
                        fontSize = headlineSize,
                        lineHeight = (headlineSize.value + 2).sp,
                        letterSpacing = (-0.7).sp,
                    ),
                )

                Spacer(Modifier.height(9.dp))
                BasicText(
                    text = "Pick phone and desktop shots. Kadraj reads every frame and composes " +
                        "a balanced layout around them.",
                    modifier = Modifier.fillMaxWidth(0.97f),
                    style = Kadraj.Type.Body.copy(color = Kadraj.Muted),
                )

                Spacer(Modifier.height(if (compact) 15.dp else 19.dp))

                KadrajButton(
                    label = "Choose photos",
                    onClick = onPickImages,
                    modifier = Modifier.fillMaxWidth(),
                    leadingGlyph = Glyph.Image,
                    trailingGlyph = Glyph.ArrowRight,
                )

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlyphIcon(
                        glyph = Glyph.Lock,
                        modifier = Modifier.size(11.dp),
                        tint = Kadraj.MutedDim,
                        strokeWidth = 1.3.dp,
                    )
                    Spacer(Modifier.width(6.dp))
                    BasicText(
                        text = "On-device  ·  No account  ·  Up to ${EditorViewModel.MAX_IMAGES} photos",
                        style = Kadraj.Type.BodySmall.copy(
                            color = Kadraj.MutedDim,
                            fontSize = 10.5.sp,
                        ),
                    )
                }

                Spacer(Modifier.height(if (compact) 18.dp else 24.dp))
                FeatureRow()

                if (canUndo || canRedo) {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (canUndo) {
                            HistoryAction(
                                glyph = Glyph.Undo,
                                label = "Undo",
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onUndo()
                                },
                            )
                        }
                        if (canRedo) {
                            Spacer(Modifier.width(8.dp))
                            HistoryAction(
                                glyph = Glyph.Redo,
                                label = "Redo",
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onRedo()
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Kadraj.Line))
                Spacer(Modifier.height(11.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = "Kadraj ${BuildConfig.VERSION_NAME}",
                        style = Kadraj.Type.LabelSmall.copy(
                            color = Kadraj.MutedDim,
                            fontSize = 8.5.sp,
                            letterSpacing = 0.4.sp,
                        ),
                    )
                    Spacer(Modifier.weight(1f))
                    BasicText(
                        text = "Local  ·  Private",
                        style = Kadraj.Type.LabelSmall.copy(
                            color = Kadraj.MutedDim,
                            fontSize = 8.5.sp,
                            letterSpacing = 0.4.sp,
                        ),
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PhotoHero(
    modifier: Modifier = Modifier,
    onInfo: () -> Unit,
) {
    val context = LocalContext.current
    val bitmaps by produceState<List<Bitmap>?>(initialValue = null) {
        value = withContext(Dispatchers.IO) {
            demoImageResources.mapNotNull { resource ->
                BitmapFactory.decodeResource(context.resources, resource)
            }
        }
    }
    val layout = remember(demoImageSpecs) {
        LayoutEngine.compose(demoImageSpecs, demoCollageConfig)
    }
    val reveal by animateFloatAsState(
        targetValue = if (bitmaps != null) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "hero-reveal",
    )
    val shape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomEnd = 28.dp,
        bottomStart = 28.dp,
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(Kadraj.InkDeep)
            .semantics {
                contentDescription = "Sample Kadraj collage with a portrait, architecture and coffee"
            },
    ) {
        val photos = bitmaps
        if (photos != null && photos.size == demoImageSpecs.size) {
            val provider = remember(photos) {
                ImageProvider { imageId, _, _ ->
                    photos.getOrNull((imageId - 1L).toInt())?.let { bitmap ->
                        ProvidedImage(bitmap)
                    }
                }
            }
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = reveal
                        scaleX = 1f + (1f - reveal) * 0.012f
                        scaleY = 1f + (1f - reveal) * 0.012f
                    },
            ) {
                drawIntoCanvas { canvas ->
                    CollagePainter.draw(
                        canvas = canvas.nativeCanvas,
                        width = size.width.toInt(),
                        height = size.height.toInt(),
                        layout = layout,
                        style = demoCollageStyle,
                        provider = provider,
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Kadraj.InkSoft, Kadraj.InkDeep),
                        ),
                    ),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(132.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.62f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(96.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.56f),
                        ),
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Wordmark()
            Spacer(Modifier.weight(1f))
            CircleIconButton(
                glyph = Glyph.Info,
                onClick = onInfo,
                size = 35.dp,
                tint = Kadraj.Paper,
                background = Color.Black.copy(alpha = 0.26f),
                border = Color.White.copy(alpha = 0.2f),
                contentDescription = "Help and info",
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeroTag(label = "Preview  ·  Auto layout", showDot = true)
            Spacer(Modifier.weight(1f))
            HeroTag(label = "3 photos")
        }
    }
}

@Composable
private fun HeroTag(
    label: String,
    showDot: Boolean = false,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.36f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(50))
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Kadraj.Peach),
            )
        }
        BasicText(
            text = label,
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.Paper,
                fontSize = 9.5.sp,
                letterSpacing = 0.2.sp,
            ),
        )
    }
}

@Composable
private fun FeatureRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Feature(
            glyph = Glyph.Phone,
            label = "Ratio aware",
            modifier = Modifier.weight(1f),
        )
        Feature(
            glyph = Glyph.Sparkle,
            label = "Tone + text",
            modifier = Modifier.weight(1f),
        )
        Feature(
            glyph = Glyph.Download,
            label = "4K export",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HistoryAction(
    glyph: Glyph,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, Kadraj.Line, RoundedCornerShape(50))
            .pressable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        GlyphIcon(
            glyph = glyph,
            modifier = Modifier.size(13.dp),
            tint = Kadraj.Muted,
            strokeWidth = 1.5.dp,
        )
        BasicText(
            text = label,
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.Muted,
                fontSize = 9.5.sp,
                letterSpacing = 0.2.sp,
            ),
        )
    }
}

@Composable
private fun Feature(
    glyph: Glyph,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        GlyphIcon(
            glyph = glyph,
            modifier = Modifier.size(14.dp),
            tint = Kadraj.Peach.copy(alpha = 0.92f),
            strokeWidth = 1.4.dp,
        )
        BasicText(
            text = label,
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.PaperDim,
                fontSize = 9.5.sp,
                letterSpacing = 0.1.sp,
            ),
            maxLines = 1,
        )
    }
}
