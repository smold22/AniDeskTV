package com.anidesk.tv.feature.bookmarks.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.anidesk.tv.core.designsystem.dimensions.TvCardSpacing
import com.anidesk.tv.core.designsystem.tv.TvChip
import com.anidesk.tv.feature.bookmarks.BookmarksState
import com.anidesk.tvfeature.bookmarks.uitv.R

/** Вкладки закладок: (тип списка закладок или null — история, ресурс названия). */
private val BookmarksTabs = listOf(
    (null as Int?) to R.string.bookmarks_tab_history,
    1 to R.string.bookmarks_tab_watching,
    2 to R.string.bookmarks_tab_planned,
    3 to R.string.bookmarks_tab_completed,
    4 to R.string.bookmarks_tab_on_hold,
    5 to R.string.bookmarks_tab_dropped,
)

@Composable
internal fun BookmarksTabChips(
    state: BookmarksState.State,
    onEvent: (BookmarksState.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(TvCardSpacing.Horizontal),
    ) {
        BookmarksTabs.forEach { (tabType, titleRes) ->
            TvChip(
                label = stringResource(titleRes),
                selected = state.selectedTabType == tabType,
                onClick = { onEvent(BookmarksState.Event.TabSelected(tabType)) },
            )
        }
    }
}