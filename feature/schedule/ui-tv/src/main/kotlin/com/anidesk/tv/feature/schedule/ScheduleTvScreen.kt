package com.anidesk.tv.feature.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anidesk.tv.core.designsystem.dimensions.TvCardSpacing
import com.anidesk.tv.core.designsystem.dimensions.TvScreenPadding
import com.anidesk.tv.core.designsystem.focus.requestFocusUntilTimeout
import com.anidesk.tv.core.designsystem.locals.LocalPreferredContentFocusRequester
import com.anidesk.tv.core.designsystem.tv.TvLoadingScreen
import com.anidesk.tv.core.designsystem.tv.TvStateMessage
import com.anidesk.tv.feature.schedule.view.ScheduleDayChips
import com.anidesk.tv.feature.schedule.view.ScheduleReleaseRow
import com.anidesk.tvfeature.schedule.uitv.R
import kotlinx.coroutines.flow.Flow

@Composable
fun ScheduleTvScreen(
    state: ScheduleState.State,
    effect: Flow<ScheduleState.Effect>,
    onEvent: (ScheduleState.Event) -> Unit,
) {
    val registerPreferredContentFocusRequester = LocalPreferredContentFocusRequester.current
    val contentFocusRequester = remember { FocusRequester() }
    DisposableEffect(registerPreferredContentFocusRequester, contentFocusRequester) {
        registerPreferredContentFocusRequester?.invoke(contentFocusRequester)
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScheduleDayChips(
            selectedDay = state.selectedDay,
            onDaySelected = { onEvent(ScheduleState.Event.DaySelected(it)) },
        )
        Text(
            text = stringResource(dayTitleRes(state.selectedDay)),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> TvLoadingScreen()

                state.error != null -> TvStateMessage(
                    title = stringResource(R.string.schedule_error),
                    description = state.error,
                    onRetry = { onEvent(ScheduleState.Event.Retry) },
                )

                state.days[state.selectedDay].isEmpty() -> TvStateMessage(
                    title = stringResource(R.string.schedule_empty),
                )

                else -> {
                    val releases = state.days[state.selectedDay]
                    var initialFocusRequested by remember { mutableStateOf(false) }
                    LaunchedEffect(releases, contentFocusRequester) {
                        if (releases.isNotEmpty() && !initialFocusRequested) {
                            initialFocusRequested = true
                            requestFocusUntilTimeout(contentFocusRequester)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(TvCardSpacing.Vertical),
                    ) {
                        itemsIndexed(releases, key = { _, release -> release.id }) { index, release ->
                            ScheduleReleaseRow(
                                release = release,
                                onClick = { onEvent(ScheduleState.Event.AnimeSelected(release)) },
                                modifier = if (index == 0) {
                                    Modifier.focusRequester(contentFocusRequester)
                                } else {
                                    Modifier
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private val DayTitleRes = intArrayOf(
    R.string.schedule_day_monday,
    R.string.schedule_day_tuesday,
    R.string.schedule_day_wednesday,
    R.string.schedule_day_thursday,
    R.string.schedule_day_friday,
    R.string.schedule_day_saturday,
    R.string.schedule_day_sunday,
)

private fun dayTitleRes(day: Int): Int = DayTitleRes[day.coerceIn(0, ScheduleState.DAY_COUNT - 1)]