package com.omersusin.kadraj.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omersusin.kadraj.CollageImage
import com.omersusin.kadraj.image.ImageLoader
import com.omersusin.kadraj.ui.theme.Kadraj
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val COLLAGE_PREVIEW_DIMENSION = 1024

class PreviewLoadStates internal constructor(
    val revision: State<Int>,
    val loading: State<Boolean>,
)

@Composable
fun rememberPreviewLoadStates(
    loader: ImageLoader,
    images: List<CollageImage>,
): PreviewLoadStates {
    val revision = remember { mutableIntStateOf(0) }
    val loading = remember { mutableStateOf(images.isNotEmpty()) }
    val key = remember(images) {
        images.joinToString("|") { "${it.id}:${it.uri}" }
    }

    LaunchedEffect(key) {
        loading.value = images.isNotEmpty()
        images.forEach { image ->
            withContext(Dispatchers.IO) {
                loader.preview(image.uri, COLLAGE_PREVIEW_DIMENSION)
            }
            revision.intValue += 1
        }
        loading.value = false
    }

    return remember { PreviewLoadStates(revision, loading) }
}

@Composable
fun rememberThumbnail(
    loader: ImageLoader,
    uri: Uri,
    maxDimension: Int = 320,
): State<Bitmap?> = produceState<Bitmap?>(
    initialValue = loader.cached(uri, maxDimension),
    key1 = uri,
    key2 = maxDimension,
) {
    if (value == null) {
        value = withContext(Dispatchers.IO) {
            loader.preview(uri, maxDimension)
        }
    }
}

@Composable
fun ImageThumb(
    image: CollageImage,
    loader: ImageLoader,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Photo",
) {
    val haptics = LocalHapticFeedback.current
    val bitmap = rememberThumbnail(loader, image.uri).value
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = modifier
            .size(52.dp)
            .semantics { contentDescription = label }
            .shadow(
                elevation = if (selected) 10.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = Kadraj.Terracotta.copy(alpha = 0.5f),
                spotColor = Kadraj.Terracotta.copy(alpha = 0.34f),
            )
            .clip(shape)
            .background(Kadraj.InkSoft)
            .border(
                width = if (selected) 1.8.dp else 1.dp,
                color = if (selected) Kadraj.Peach else Kadraj.Line,
                shape = shape,
            )
            .then(
                Modifier.pressable {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Kadraj.InkLift, Kadraj.InkSoft),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                GlyphIcon(
                    glyph = Glyph.Image,
                    modifier = Modifier.size(18.dp),
                    tint = Kadraj.MutedDim,
                    strokeWidth = 1.4.dp,
                )
            }
        }
    }
}

@Composable
fun AddImageTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(11.dp)
    Column(
        modifier = modifier
            .size(52.dp)
            .semantics { contentDescription = "Add photos" }
            .clip(shape)
            .background(Color.Transparent)
            .border(1.dp, Kadraj.LineStrong, shape)
            .pressable(enabled = enabled) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(11.dp))
        GlyphIcon(
            glyph = Glyph.Plus,
            modifier = Modifier.size(16.dp),
            tint = Kadraj.TerracottaSoft,
            strokeWidth = 1.7.dp,
        )
        Spacer(Modifier.height(3.dp))
        BasicText(
            text = "ADD",
            style = Kadraj.Type.LabelSmall.copy(
                color = Kadraj.PaperDim,
                fontSize = 7.5.sp,
                letterSpacing = 1.0.sp,
            ),
        )
    }
}
