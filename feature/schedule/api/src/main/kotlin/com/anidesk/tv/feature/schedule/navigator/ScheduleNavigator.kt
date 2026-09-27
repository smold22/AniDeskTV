package com.anidesk.tv.feature.schedule.navigator

import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.feature.schedule.IScheduleNavigator
import javax.inject.Inject

class ScheduleNavigator @Inject constructor() : IScheduleNavigator {
    override fun getScheduleDest(): NavKey = ScheduleDestination
}