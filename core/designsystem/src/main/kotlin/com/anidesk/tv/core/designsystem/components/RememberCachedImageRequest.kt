package com.anidesk.tv.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil3.request.ImageRequest

/** Запрос с общим по URL ключом — для мест, где картинку рисует не [CachedAsyncImage]. */
@Composable
fun rememberCachedImageRequest(url: String): ImageRequest {
    val context = LocalContext.current
    return remember(context, url) {
        ImageRequest.Builder(context)
            .data(url)
            .memoryCacheKey(url)
            .placeholderMemoryCacheKey(url)
            .build()
    }
}
