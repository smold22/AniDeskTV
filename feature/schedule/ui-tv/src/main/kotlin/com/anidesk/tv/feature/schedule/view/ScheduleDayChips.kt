package com.anidesk.tv.feature.schedule.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anidesk.tv.core.designsystem.tv.TvChip
import com.anidesk.tvfeature.schedule.uitv.R

@Composable
internal fun ScheduleDayChips(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DayShortLabels.forEachIndexed { index, labelRes ->
            TvChip(
                label = stringResource(labelRes),
                selected = index == selectedDay,
                onClick = { onDaySelected(index) },
            )
        }
    }
}

private val DayShortLabels = intArrayOf(
    R.string.schedule_day_short_monday,
    R.string.schedule_day_short_tuesday,
    R.string.schedule_day_short_wednesday,
    R.string.schedule_day_short_thursday,
    R.string.schedule_day_short_friday,
    R.string.schedule_day_short_saturday,
    R.string.schedule_day_short_sunday,
)