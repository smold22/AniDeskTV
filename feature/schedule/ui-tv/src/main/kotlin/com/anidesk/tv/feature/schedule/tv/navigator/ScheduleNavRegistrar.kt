package com.anidesk.tv.feature.schedule.tv.navigator

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.core.designsystem.baseScreen.ScreenNavigator
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.navigation.registrar.NavRegistrar
import com.anidesk.tv.feature.schedule.ScheduleTvScreen
import com.anidesk.tv.feature.schedule.ScheduleViewModel
import com.anidesk.tv.feature.schedule.navigator.ScheduleDestination
import javax.inject.Inject

class ScheduleNavRegistrar @Inject constructor() : NavRegistrar {

    override fun register(builder: EntryProviderScope<NavKey>, nav: INavigationManager) {
        builder.entry<ScheduleDestination> {
            val viewModel: ScheduleViewModel = hiltViewModel()
            ScreenNavigator(viewModel) { state, effect, onEvent ->
                ScheduleTvScreen(
                    state = state,
                    effect = effect,
                    onEvent = onEvent,
                )
            }
        }
    }
}