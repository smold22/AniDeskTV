package com.anidesk.tv.feature.top.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.anidesk.tv.core.designsystem.dimensions.TvCardSpacing
import com.anidesk.tv.core.designsystem.dimensions.currentTvTitleCardDimensions
import com.anidesk.tv.core.designsystem.focus.FocusedItemPivotFraction
import com.anidesk.tv.core.designsystem.focus.tvLazyGridRowFocusNavigation
import com.anidesk.tv.core.designsystem.tv.TvAppendErrorFooter
import com.anidesk.tv.core.designsystem.tv.TvLoadingFooter
import com.anidesk.tv.core.designsystem.tv.TvTitleCard
import com.anidesk.tv.feature.top.TopState

/** Сколько карточек до конца грида нужно доехать, чтобы подгрузить следующую страницу. */
private const val PAGINATION_AHEAD = 8

/** Сколько рядов выше фокуса ещё держать FocusRequester'ы в кэше для DPAD-страховки. */
private const val PIVOT_KEEP_ROWS = 1

@Composable
internal fun TopBrowser(
    state: TopState.State,
    onEvent: (TopState.Event) -> Unit,
    firstCardFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val itemFocusRequesters = remember { mutableMapOf<Int, FocusRequester>() }
    val cardDimensions = currentTvTitleCardDimensions()

    // Сфокусированный ряд всегда паркуется на пивоте от верхней кромки, чтобы под ним был
    // скомпонован следующий ряд — иначе DPAD-вниз упирается в нескомпонованную границу и грид
    // перестаёт прокручиваться за второй видимый ряд.
    var focusedIndex by remember { mutableIntStateOf(-1) }
    LaunchedEffect(focusedIndex) {
        val index = focusedIndex
        if (index < 0) return@LaunchedEffect
        val layout = gridState.layoutInfo
        val viewportHeight = layout.viewportSize.height
        if (viewportHeight <= 0) return@LaunchedEffect
        val pivotPx = (viewportHeight * FocusedItemPivotFraction).toInt()
        gridState.animateScrollToItem(index = index, scrollOffset = -pivotPx)
        itemFocusRequesters.keys.removeAll { it < index - PIVOT_KEEP_ROWS }
    }

    val columnCount by remember(gridState) {
        derivedStateOf {
            (gridState.layoutInfo.visibleItemsInfo.maxOfOrNull { it.column } ?: 0) + 1
        }
    }

    val shouldLoadMore by remember(gridState) {
        derivedStateOf {
            val layout = gridState.layoutInfo
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= layout.totalItemsCount - PAGINATION_AHEAD
        }
    }
    LaunchedEffect(shouldLoadMore, state.hasMore, state.isAppending, state.isLoading, state.items.size) {
        if (shouldLoadMore && state.hasMore && !state.isAppending && !state.isLoading) {
            onEvent(TopState.Event.LoadNextPage)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = cardDimensions.width),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = TvCardSpacing.Vertical),
        horizontalArrangement = Arrangement.spacedBy(TvCardSpacing.Horizontal),
        verticalArrangement = Arrangement.spacedBy(TvCardSpacing.Vertical),
    ) {
        itemsIndexed(
            items = state.items,
            key = { _, item -> item.id },
        ) { index, item ->
            remember(item.id) {
                FocusRequester().also { itemFocusRequesters[index] = it }
            }
            val cardModifier = Modifier
                .then(if (index == 0) Modifier.focusRequester(firstCardFocusRequester) else Modifier)
                .tvLazyGridRowFocusNavigation(
                    index = index,
                    columnCount = columnCount,
                    itemCount = state.items.size,
                    gridState = gridState,
                    scope = scope,
                    focusRequesterAt = { itemFocusRequesters[it] },
                )
            TvTitleCard(
                title = item.titleRu,
                posterUrl = item.posterUrl.ifBlank { null },
                onClick = { onEvent(TopState.Event.ReleaseSelected(item)) },
                modifier = cardModifier,
                onFocused = { focusedIndex = index },
                centerInCell = true,
                subtitle = listOfNotNull(item.year, item.category.ifBlank { null })
                    .joinToString(" · "),
            )
        }

        if (state.items.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                when {
                    state.isLoading || state.isAppending -> TvLoadingFooter()
                    state.error != null -> TvAppendErrorFooter(
                        message = state.error.orEmpty(),
                        onRetry = { onEvent(TopState.Event.LoadNextPage) },
                    )
                }
            }
        }
    }
}