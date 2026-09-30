package com.anidesk.tv.feature.details

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.anidesk.tv.core.designsystem.components.GlobalToastOverlay
import com.anidesk.tv.core.designsystem.dimensions.TvCardSpacing
import com.anidesk.tv.core.designsystem.dimensions.TvScreenPadding
import com.anidesk.tv.core.designsystem.dimensions.currentTvTitleCardDimensions
import com.anidesk.tv.core.designsystem.focus.requestFocusUntilTimeout
import com.anidesk.tv.core.designsystem.focus.tvFocusableClick
import com.anidesk.tv.core.designsystem.locals.LocalPreferredContentFocusRequester
import com.anidesk.tv.core.designsystem.theme.AniDeskSemanticColors
import com.anidesk.tv.core.designsystem.tv.TvActionButton
import com.anidesk.tv.core.designsystem.tv.TvChip
import com.anidesk.tv.core.designsystem.tv.TvLoadingScreen
import com.anidesk.tv.core.designsystem.tv.TvStateContent
import com.anidesk.tv.core.designsystem.tv.TvTitleCard
import com.anidesk.tv.core.model.release.BookmarkStatus
import com.anidesk.tv.core.network.dto.Episode
import com.anidesk.tv.core.network.dto.Release
import com.anidesk.tvfeature.details.uitv.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/** Сколько висит уведомление о результате действия со списками. */
private const val TOAST_MILLIS = 2_500L

@Composable
fun DetailsTvScreen(
    state: DetailsState.State,
    effect: Flow<DetailsState.Effect>,
    onEvent: (DetailsState.Event) -> Unit,
) {
    val registerPreferredContentFocusRequester = LocalPreferredContentFocusRequester.current
    val contentFocusRequester = remember { FocusRequester() }
    DisposableEffect(registerPreferredContentFocusRequester, contentFocusRequester) {
        registerPreferredContentFocusRequester?.invoke(contentFocusRequester)
        onDispose {
            registerPreferredContentFocusRequester?.invoke(null)
        }
    }

    val toast = rememberToast(effect)

    val dubbers = state.dubbers
    val sources = state.sources
    var initialFocusRequested by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading, state.release) {
        if (initialFocusRequested || state.isLoading || state.release == null) return@LaunchedEffect
        initialFocusRequested = true
        requestFocusUntilTimeout(contentFocusRequester)
    }

    TvStateContent(
        isLoading = state.isLoading,
        error = state.error,
        empty = state.release == null && !state.isLoading && state.error == null,
        emptyText = "Тайтл недоступен",
        modifier = Modifier.fillMaxSize(),
        onRetry = { onEvent(DetailsState.Event.Retry) },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = TvScreenPadding.Horizontal,
                    vertical = TvScreenPadding.Vertical,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "header") {
                HeaderRow(
                    state = state,
                    contentFocusRequester = contentFocusRequester,
                )
            }

            // Закладки и избранное — серверные списки аккаунта, без входа их не существует.
            if (state.isAuthorized) {
                item(key = "library") {
                    LibraryRow(
                        state = state,
                        onEvent = onEvent,
                    )
                }
            }

            val release = state.release
            if (!release?.description.isNullOrBlank()) {
                item(key = "description") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.details_description_section),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = release.description,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            // Связанные релизы идут до выбора озвучки: список серий бывает на сотни строк,
            // и внизу раздел «Ещё» просто не доехать.
            if (state.relatedReleases.isNotEmpty() || state.relatedError != null) {
                item(key = "related") {
                    RelatedReleasesRow(
                        state = state,
                        onEvent = onEvent,
                    )
                }
            }

            if (dubbers.isNotEmpty()) {
                item(key = "dubbers") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = stringResource(R.string.details_dubber_section),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(TvCardSpacing.Horizontal),
                        ) {
                            dubbers.forEach { dubber ->
                                TvChip(
                                    label = dubber.name.ifBlank { "Озвучка #${dubber.id}" },
                                    selected = state.selectedDubber == dubber.id,
                                    onClick = { onEvent(DetailsState.Event.DubberSelected(dubber.id)) },
                                )
                            }
                        }
                    }
                }
            }

            if (sources.isNotEmpty()) {
                item(key = "sources") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = stringResource(R.string.details_source_section),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(TvCardSpacing.Horizontal),
                        ) {
                            sources.forEach { source ->
                                TvChip(
                                    label = source.name.ifBlank { "Источник #${source.id}" },
                                    selected = state.selectedSource == source.id,
                                    onClick = { onEvent(DetailsState.Event.SourceSelected(source.id)) },
                                )
                            }
                        }
                    }
                }
            }

            when {
                state.isEpisodesLoading -> item(key = "episodes-loading") {
                    TvLoadingScreen()
                }

                state.selectedSource == null || state.episodes.isEmpty() -> item(
                    key = "episodes-empty",
                ) {
                    Text(
                        text = stringResource(R.string.details_no_episodes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> itemsIndexed(
                    state.episodes,
                    key = { _, episode -> episode.position },
                ) { _, episode ->
                    EpisodeRow(
                        episode = episode,
                        onClick = { onEvent(DetailsState.Event.EpisodeSelected(episode)) },
                    )
                }
            }
        }
    }

    GlobalToastOverlay(text = toast)
}

/**
 * Подписка на одноразовые [DetailsState.Effect] с автоскрытием: текст уведомления
 * резолвится в composable-теле, потому что [stringResource] нельзя вызывать из
 * корутины [LaunchedEffect].
 */
@Composable
private fun rememberToast(effect: Flow<DetailsState.Effect>): String? {
    var pending by remember { mutableStateOf<DetailsState.Effect?>(null) }
    LaunchedEffect(effect) {
        effect.collect { pending = it }
    }
    LaunchedEffect(pending) {
        if (pending == null) return@LaunchedEffect
        delay(TOAST_MILLIS)
        pending = null
    }
    return when (val current = pending) {
        is DetailsState.Effect.BookmarkAdded -> stringResource(
            R.string.details_bookmark_added,
            bookmarkStatusLabel(current.status),
        )

        DetailsState.Effect.BookmarkRemoved -> stringResource(R.string.details_bookmark_removed)

        is DetailsState.Effect.FavoriteChanged -> stringResource(
            if (current.isFavorite) R.string.details_favorite_added else R.string.details_favorite_removed,
        )

        DetailsState.Effect.LibraryActionFailed -> stringResource(R.string.details_library_action_failed)

        null -> null
    }
}

/** Название списка закладок, [BookmarkStatus.NONE] — «без закладки». */
@Composable
private fun bookmarkStatusLabel(status: BookmarkStatus): String = stringResource(
    when (status) {
        BookmarkStatus.NONE -> R.string.details_bookmark_none
        BookmarkStatus.WATCHING -> R.string.details_bookmark_watching
        BookmarkStatus.PLANNED -> R.string.details_bookmark_planned
        BookmarkStatus.COMPLETED -> R.string.details_bookmark_completed
        BookmarkStatus.ON_HOLD -> R.string.details_bookmark_on_hold
        BookmarkStatus.DROPPED -> R.string.details_bookmark_dropped
    },
)

/**
 * Блок действий над списками аккаунта: кнопка «Избранное» со счётчиком и ряд чипов
 * со статусами закладки. На TV вместо выпадающего списка статус выбирается сразу —
 * списки у тайтла взаимоисключающие, поэтому активен всегда ровно один чип.
 */
@Composable
private fun LibraryRow(
    state: DetailsState.State,
    onEvent: (DetailsState.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvActionButton(
                text = stringResource(R.string.details_favorites_action),
                icon = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                active = state.isFavorite,
                activeTint = AniDeskSemanticColors.StatusFavorite,
                onClick = { onEvent(DetailsState.Event.FavoriteClicked) },
            )
            if (state.favoritesCount > 0) {
                Text(
                    text = state.favoritesCount.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = stringResource(R.string.details_bookmarks_section),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(TvCardSpacing.Horizontal),
        ) {
            BookmarkStatus.entries.forEach { status ->
                TvChip(
                    label = bookmarkStatusLabel(status),
                    selected = state.bookmarkStatus == status,
                    onClick = { onEvent(DetailsState.Event.BookmarkStatusSelected(status)) },
                )
            }
        }
    }
}

/**
 * Ряд «Связанные релизы»: горизонтальные карточки релизов из связанной сущности тайтла
 * (сиквел, предыстория). Подгрузка страниц — явной кнопкой в заголовке раздела, а не
 * автоскроллом: в горизонтальном ряду автогрузка срабатывает на первом же свайпе мимо края.
 */
@Composable
private fun RelatedReleasesRow(
    state: DetailsState.State,
    onEvent: (DetailsState.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val related = state.relatedReleases
    val hasMoreAction = state.hasMoreRelated || state.isRelatedLoading || state.relatedError != null

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.details_related_section),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (hasMoreAction) {
                TvActionButton(
                    text = when {
                        state.isRelatedLoading -> stringResource(R.string.details_related_loading)
                        state.relatedError != null -> stringResource(R.string.details_related_retry)
                        else -> stringResource(R.string.details_related_more)
                    },
                    icon = state.relatedError?.let { Icons.Filled.Refresh },
                    onClick = { onEvent(DetailsState.Event.LoadMoreRelated) },
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(TvCardSpacing.Horizontal),
        ) {
            items(
                count = related.size,
                key = { index -> related[index].id },
            ) { index ->
                val release = related[index]
                TvTitleCard(
                    title = release.titleRu.ifBlank { release.titleOriginal },
                    posterUrl = release.posterUrl.ifBlank { null },
                    onClick = { onEvent(DetailsState.Event.RelatedReleaseSelected(release)) },
                    subtitle = listOfNotNull(release.year, release.category.ifBlank { null })
                        .joinToString(" · "),
                )
            }
        }
    }
}

@Composable
private fun HeaderRow(
    state: DetailsState.State,
    contentFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val release = state.release
    val cardDimensions = currentTvTitleCardDimensions()
    Row(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(width = cardDimensions.width, height = cardDimensions.posterHeight)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                )
                .focusRequester(contentFocusRequester)
                .tvFocusableClick(
                    onClick = {},
                    shape = RoundedCornerShape(12.dp),
                    focusedBorderColor = Color.Transparent,
                ),
            contentAlignment = Alignment.Center,
        ) {
            val posterUrl = state.posterUrl
            if (!posterUrl.isNullOrBlank()) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = state.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    text = (state.title ?: "").take(1),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f),
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = state.title.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            val meta = buildList {
                release?.year?.takeIf { it.isNotBlank() }?.let { add(it) }
                release?.genres?.takeIf { it.isNotBlank() }?.let { add(it) }
            }.joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (release != null && release.grade > 0.0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "%.1f".format(release.grade),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            val minor = buildList {
                release?.country?.takeIf { it.isNotBlank() }?.let { add(it) }
                release?.category?.name?.takeIf { it.isNotBlank() }?.let { add(it) }
                release?.status?.name?.takeIf { it.isNotBlank() }?.let { add(it) }
            }.joinToString(" · ")
            if (minor.isNotBlank()) {
                Text(
                    text = minor,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val episodesTotal = release?.episodesTotal
            if (episodesTotal != null) {
                Text(
                    text = "${stringResource(R.string.details_episodes_section)}: $episodesTotal",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    episode: Episode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (focused) colorScheme.primary else colorScheme.surfaceContainerHigh
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor, shape)
            .tvFocusableClick(
                onClick = onClick,
                shape = shape,
                interactionSource = interactionSource,
                focusedBorderColor = Color.Transparent,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = episode.position.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = if (focused) colorScheme.onPrimary else colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = episode.name.ifBlank { "Серия ${episode.position}" },
            style = MaterialTheme.typography.bodyLarge,
            color = if (focused) colorScheme.onPrimary else Color.Unspecified,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (episode.isWatched) {
            Text(
                text = stringResource(R.string.details_watched_dot),
                style = MaterialTheme.typography.labelMedium,
                color = if (focused) colorScheme.onPrimary.copy(alpha = 0.8f) else colorScheme.onSurfaceVariant,
            )
        }
    }
}