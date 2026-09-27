package com.anidesk.tv.feature.top.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anidesk.tv.core.designsystem.dimensions.TvCardSpacing
import com.anidesk.tv.core.designsystem.tv.TvChip
import com.anidesk.tv.feature.top.TopState
import com.anidesk.tvfeature.top.uitv.R

@Composable
internal fun TopFilterChips(
    state: TopState.State,
    onEvent: (TopState.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sortLabels = listOf(
        R.string.top_sort_best,
        R.string.top_sort_popular,
        R.string.top_sort_new,
    )

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(TvCardSpacing.Horizontal),
    ) {
        TvChip(
            label = stringResource(R.string.top_type_all),
            selected = state.selectedTypes.isEmpty(),
            onClick = {
                state.selectedTypes.forEach { onEvent(TopState.Event.TypeToggled(it)) }
            },
        )
        state.types.forEach { type ->
            TvChip(
                label = type.name,
                selected = state.selectedTypes.contains(type.id),
                onClick = { onEvent(TopState.Event.TypeToggled(type.id)) },
            )
        }
    }

    Column(
        modifier = Modifier.padding(top = TvCardSpacing.Vertical),
    ) {
        Row(
            modifier = modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(TvCardSpacing.Horizontal),
        ) {
            sortLabels.forEachIndexed { index, labelRes ->
                TvChip(
                    label = stringResource(labelRes),
                    selected = state.sortIndex == index,
                    onClick = { onEvent(TopState.Event.SortChanged(index)) },
                )
            }
            Spacer(modifier = Modifier.width(TvCardSpacing.Horizontal))
            TvChip(
                label = stringResource(
                    if (state.directionAsc) R.string.top_sort_asc else R.string.top_sort_desc,
                ),
                selected = state.directionAsc,
                onClick = { onEvent(TopState.Event.DirectionToggled) },
            )
        }
    }
}