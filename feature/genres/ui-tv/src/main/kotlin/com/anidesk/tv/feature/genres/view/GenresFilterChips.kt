package com.anidesk.tv.feature.genres.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.anidesk.tv.core.designsystem.dimensions.TvCardSpacing
import com.anidesk.tv.core.designsystem.tv.TvChip
import com.anidesk.tv.feature.genres.GenresState
import com.anidesk.tvfeature.genres.uitv.R

@Composable
internal fun GenresFilterChips(
    state: GenresState.State,
    onEvent: (GenresState.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(TvCardSpacing.Horizontal),
    ) {
        TvChip(
            label = stringResource(R.string.genres_all),
            selected = state.selectedGenre == null,
            onClick = { onEvent(GenresState.Event.SelectAll) },
        )
        state.genres.forEach { genre ->
            TvChip(
                label = genre,
                selected = state.selectedGenre == genre,
                onClick = { onEvent(GenresState.Event.GenreSelected(genre)) },
            )
        }
    }
}