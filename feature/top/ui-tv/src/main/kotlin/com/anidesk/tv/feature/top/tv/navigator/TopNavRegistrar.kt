package com.anidesk.tv.feature.top.tv.navigator

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.core.designsystem.baseScreen.ScreenNavigator
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.navigation.registrar.NavRegistrar
import com.anidesk.tv.feature.top.TopTvScreen
import com.anidesk.tv.feature.top.TopViewModel
import com.anidesk.tv.feature.top.navigator.TopDestination
import javax.inject.Inject

class TopNavRegistrar @Inject constructor() : NavRegistrar {

    override fun register(builder: EntryProviderScope<NavKey>, nav: INavigationManager) {
        builder.entry<TopDestination> {
            val viewModel: TopViewModel = hiltViewModel()
            ScreenNavigator(viewModel) { state, effect, onEvent ->
                TopTvScreen(
                    state = state,
                    effect = effect,
                    onEvent = onEvent,
                )
            }
        }
    }
}