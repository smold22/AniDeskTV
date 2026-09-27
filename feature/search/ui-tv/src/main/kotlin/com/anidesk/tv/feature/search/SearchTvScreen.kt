package com.anidesk.tv.feature.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anidesk.tv.core.designsystem.dimensions.TvScreenPadding
import com.anidesk.tv.core.designsystem.locals.LocalPreferredContentFocusRequester
import com.anidesk.tv.core.designsystem.tv.TvStateContent
import com.anidesk.tv.feature.search.view.SearchResultsGrid
import com.anidesk.tvfeature.search.uitv.R
import kotlinx.coroutines.flow.Flow

@Composable
fun SearchTvScreen(
    state: SearchState.State,
    effect: Flow<SearchState.Effect>,
    onEvent: (SearchState.Event) -> Unit,
) {
    val registerPreferredContentFocusRequester = LocalPreferredContentFocusRequester.current
    val firstCardFocusRequester = remember { FocusRequester() }
    val searchFieldFocusRequester = remember { FocusRequester() }
    DisposableEffect(registerPreferredContentFocusRequester, firstCardFocusRequester) {
        registerPreferredContentFocusRequester?.invoke(firstCardFocusRequester)
        onDispose {
            registerPreferredContentFocusRequester?.invoke(null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = TvScreenPadding.Horizontal,
                vertical = TvScreenPadding.Vertical,
            ),
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = { onEvent(SearchState.Event.QueryChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(searchFieldFocusRequester),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            TvStateContent(
                isLoading = state.isLoading && state.results.isEmpty(),
                error = state.error.takeIf { state.results.isEmpty() },
                empty = !state.isLoading && state.error == null &&
                    state.query.isNotBlank() && state.results.isEmpty(),
                emptyText = stringResource(R.string.search_empty),
                emptyDescription = stringResource(R.string.search_empty_description),
                onRetry = { onEvent(SearchState.Event.Retry) },
            ) {
                SearchResultsGrid(
                    state = state,
                    onEvent = onEvent,
                    firstCardFocusRequester = firstCardFocusRequester,
                )
            }
        }
    }
}