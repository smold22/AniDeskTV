package com.anidesk.tv.feature.home.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anidesk.tv.core.designsystem.dimensions.TvCardSpacing
import com.anidesk.tv.core.designsystem.tv.TvTitleCard
import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.model.release.ContinueWatchingEntry
import com.anidesk.tv.feature.home.HomeState
import com.anidesk.tvfeature.home.uitv.R
import kotlin.math.roundToInt

/** Секция главной: заголовок + горизонтальный ряд карточек релизов. */
@Composable
internal fun HomeReleasesRow(
    title: String,
    releases: List<AnimeRelease>,
    onEvent: (HomeState.Event) -> Unit,
    modifier: Modifier = Modifier,
    firstFocusRequester: androidx.compose.ui.focus.FocusRequester? = null,
) {
    if (releases.isEmpty()) return

    HomeSection(title = title, modifier = modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(TvCardSpacing.Horizontal),
        ) {
            items(count = releases.size) { index ->
                val release = releases[index]
                TvTitleCard(
                    title = release.titleRu,
                    posterUrl = release.posterUrl.ifBlank { null },
                    onClick = { onEvent(HomeState.Event.ReleaseSelected(release)) },
                    modifier = if (index == 0 && firstFocusRequester != null) {
                        Modifier.focusRequester(firstFocusRequester)
                    } else {
                        Modifier
                    },
                    subtitle = listOfNotNull(release.year, release.category.ifBlank { null })
                        .joinToString(" · "),
                )
            }
        }
    }
}

/** Секция "Продолжить просмотр": карточки с прогрессом поверх постера. */
@Composable
internal fun ContinueWatchingRow(
    entries: List<ContinueWatchingEntry>,
    onEvent: (HomeState.Event) -> Unit,
    modifier: Modifier = Modifier,
    firstFocusRequester: androidx.compose.ui.focus.FocusRequester? = null,
) {
    if (entries.isEmpty()) return

    HomeSection(title = stringResource(R.string.home_continue_watching), modifier = modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(TvCardSpacing.Horizontal),
        ) {
            items(count = entries.size) { index ->
                val entry = entries[index]
                val progress = if (entry.durationMs > 0) {
                    (entry.positionMs.toFloat() / entry.durationMs).coerceIn(0f, 1f)
                } else {
                    0f
                }
                TvTitleCard(
                    title = entry.titleRu.ifBlank { entry.titleOriginal },
                    posterUrl = entry.posterUrl.ifBlank { null },
                    onClick = { onEvent(HomeState.Event.ContinueWatchingSelected(entry)) },
                    onLongClick = {
                        onEvent(HomeState.Event.RemoveContinueWatching(entry.releaseId))
                    },
                    modifier = if (index == 0 && firstFocusRequester != null) {
                        Modifier.focusRequester(firstFocusRequester)
                    } else {
                        Modifier
                    },
                    subtitle = entry.episodeName.ifBlank { null },
                    caption = if (entry.episodePosition > 0 && progress > 0) {
                        stringResource(
                            R.string.home_continue_watching_episode_position,
                            entry.episodePosition,
                            "${(progress * 100).roundToInt()}%",
                        )
                    } else {
                        null
                    },
                    posterOverlay = {
                        ContinueWatchingProgress(progress)
                    },
                )
            }
        }
    }
}

@Composable
private fun BoxScope.ContinueWatchingProgress(progress: Float) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f),
    )
}

@Composable
private fun HomeSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}