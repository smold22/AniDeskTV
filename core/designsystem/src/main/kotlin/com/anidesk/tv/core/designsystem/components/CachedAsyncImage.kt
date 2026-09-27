package com.anidesk.tv.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * Картинка с memory-cache ключом по одному только URL, без размера.
 *
 * По умолчанию Coil добавляет в ключ размер таргета, поэтому одна и та же картинка
 * в списке и на экране деталей — это два разных ключа и две отдельные загрузки.
 * Здесь ключ общий: деталка сразу показывает уже загруженное превью из списка
 * (placeholderMemoryCacheKey), а пока подтягивается версия в полном разрешении.
 */
@Composable
fun CachedAsyncImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    AsyncImage(
        model = rememberCachedImageRequest(url),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}
