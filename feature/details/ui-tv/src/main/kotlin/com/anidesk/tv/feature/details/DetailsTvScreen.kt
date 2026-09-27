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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.anidesk.tv.core.designsystem.dimensions.TvCardSpacing
import com.anidesk.tv.core.designsystem.dimensions.TvScreenPadding
import com.anidesk.tv.core.designsystem.dimensions.currentTvTitleCardDimensions
import com.anidesk.tv.core.designsystem.focus.requestFocusUntilTimeout
import com.anidesk.tv.core.designsystem.focus.tvFocusableClick
import com.anidesk.tv.core.designsystem.locals.LocalPreferredContentFocusRequester
import com.anidesk.tv.core.designsystem.tv.TvChip
import com.anidesk.tv.core.designsystem.tv.TvLoadingScreen
import com.anidesk.tv.core.designsystem.tv.TvStateContent
import com.anidesk.tv.core.network.dto.Episode
import com.anidesk.tv.core.network.dto.Release
import com.anidesk.tvfeature.details.uitv.R
import kotlinx.coroutines.flow.Flow

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