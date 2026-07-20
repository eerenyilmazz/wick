package com.wick.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.wick.core.designsystem.theme.WickColors

@Composable
fun WickAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onError: ((Throwable) -> Unit)? = null,
) {
    val context = LocalContext.current
    val request = remember(model) {
        ImageRequest.Builder(context)
            .data(model)
            .crossfade(true)
            .listener(onError = { _, result: ErrorResult -> onError?.invoke(result.throwable) })
            .build()
    }
    val placeholder = remember { ColorPainter(WickColors.Surface) }

    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = placeholder,
        error = placeholder,
    )
}
