package com.anidesk.tv.feature.schedule.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anidesk.tv.core.designsystem.components.CachedAsyncImage
import com.anidesk.tv.core.designsystem.components.MarqueeTitleText
import com.anidesk.tv.core.designsystem.components.RatingBadge
import com.anidesk.tv.core.designsystem.dimensions.currentTvTitleCardDimensions
import com.anidesk.tv.core.designsystem.focus.tvFocusableClick
import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tvfeature.schedule.uitv.R

/** Обложка в строке расписания — доля от размера карточки каталога. */
private const val SCHEDULE_POSTER_SCALE = 0.3f

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ScheduleReleaseRow(
    release: AnimeRelease,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    // Обложка в строке — уменьшенная копия карточки из «Топа», чтобы размер
    // обложек не расходился между экранами при смене настройки размера карточек.
    val cardDimensions = currentTvTitleCardDimensions()
    val thumbnailWidth = cardDimensions.width * SCHEDULE_POSTER_SCALE
    val thumbnailHeight = cardDimensions.posterHeight * SCHEDULE_POSTER_SCALE

    val shape = RoundedCornerShape(8.dp)
    val containerColor = if (focused) MaterialTheme.colorScheme.primary else Color.Transparent
    val titleColor = if (focused) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
    val subtitleColor = if (focused) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val episodesLabel = release.episodesTotal?.let { stringResource(R.string.schedule_episodes_total, it) }
    val descriptor = release.category.ifBlank { release.genres.firstOrNull().orEmpty() }
    val subtitle = listOfNotNull(episodesLabel, descriptor.takeIf { it.isNotBlank() }).joinToString(" \u00b7 ")

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
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CachedAsyncImage(
            url = release.posterUrl,
            contentDescription = null,
            modifier = Modifier
                .size(width = thumbnailWidth, height = thumbnailHeight)
                .clip(RoundedCornerShape(8.dp)),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MarqueeTitleText(
                text = release.titleRu.ifBlank { release.titleOriginal },
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                minLines = 1,
                maxLines = 1,
                isFocused = focused,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        RatingBadge(rating = release.rating)
    }
}