package com.anidesk.tv.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.anidesk.tv.feature.home.view.ContinueWatchingRow
import com.anidesk.tv.feature.home.view.HomeReleasesRow
import com.anidesk.tvfeature.home.uitv.R
import kotlinx.coroutines.flow.Flow

@Composable
fun HomeTvScreen(
    state: HomeState.State,
    effect: Flow<HomeState.Effect>,
    onEvent: (HomeState.Event) -> Unit,
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
        val hasContent = state.continueWatching.isNotEmpty() ||
            state.watching.isNotEmpty() ||
            state.recommendations.isNotEmpty() ||
            state.updates.isNotEmpty()

        when {
            state.isLoading && !hasContent -> TvStateContent(
                isLoading = true,
                error = null,
                empty = false,
                emptyText = "",
            ) {
            }

            state.error != null && !hasContent -> TvStateContent(
                isLoading = false,
                error = state.error,
                empty = false,
                emptyText = "",
                onRetry = { onEvent(HomeState.Event.Retry) },
            ) {
            }

            !hasContent -> Unit

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(TvCardSpacing.Vertical * 2),
                ) {
                    ContinueWatchingRow(
                        entries = state.continueWatching,
                        onEvent = onEvent,
                        firstFocusRequester = firstCardFocusRequester,
                    )

                    HomeReleasesRow(
                        title = stringResource(R.string.home_watching),
                        releases = state.watching,
                        onEvent = onEvent,
                    )

                    HomeReleasesRow(
                        title = stringResource(R.string.home_recommendations),
                        releases = state.recommendations,
                        onEvent = onEvent,
                    )

                    HomeReleasesRow(
                        title = stringResource(R.string.home_updates),
                        releases = state.updates,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}