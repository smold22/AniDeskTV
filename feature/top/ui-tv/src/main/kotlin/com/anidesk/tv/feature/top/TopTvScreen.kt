package com.anidesk.tv.feature.top

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import com.anidesk.tv.core.designsystem.dimensions.TvCardSpacing
import com.anidesk.tv.core.designsystem.dimensions.TvScreenPadding
import com.anidesk.tv.core.designsystem.locals.LocalPreferredContentFocusRequester
import com.anidesk.tv.core.designsystem.tv.TvStateContent
import com.anidesk.tv.feature.top.view.TopBrowser
import com.anidesk.tv.feature.top.view.TopFilterChips
import com.anidesk.tvfeature.top.uitv.R
import kotlinx.coroutines.flow.Flow

@Composable
fun TopTvScreen(
    state: TopState.State,
    effect: Flow<TopState.Effect>,
    onEvent: (TopState.Event) -> Unit,
) {
    val registerPreferredContentFocusRequester = LocalPreferredContentFocusRequester.current
    val firstCardFocusRequester = remember { FocusRequester() }
    DisposableEffect(registerPreferredContentFocusRequester, firstCardFocusRequester) {
        registerPreferredContentFocusRequester?.invoke(firstCardFocusRequester)
        onDispose {
            registerPreferredContentFocusRequester?.invoke(null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                horizontal = TvScreenPadding.Horizontal,
                vertical = TvScreenPadding.Vertical,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = TvCardSpacing.Vertical),
        ) {
            TopFilterChips(
                state = state,
                onEvent = onEvent,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            TvStateContent(
                isLoading = state.isLoading && state.items.isEmpty(),
                error = state.error.takeIf { state.items.isEmpty() },
                empty = !state.isLoading && state.error == null && state.items.isEmpty(),
                emptyText = stringResource(R.string.top_empty),
                emptyDescription = stringResource(R.string.top_empty_description),
                onRetry = { onEvent(TopState.Event.Retry) },
            ) {
                TopBrowser(
                    state = state,
                    onEvent = onEvent,
                    firstCardFocusRequester = firstCardFocusRequester,
                )
            }
        }
    }
}