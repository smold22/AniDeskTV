package com.anidesk.tv.feature.search.tv.navigator

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.core.designsystem.baseScreen.ScreenNavigator
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.navigation.registrar.NavRegistrar
import com.anidesk.tv.feature.search.SearchTvScreen
import com.anidesk.tv.feature.search.SearchViewModel
import com.anidesk.tv.feature.search.navigator.SearchDestination
import javax.inject.Inject

class SearchNavRegistrar @Inject constructor() : NavRegistrar {

    override fun register(builder: EntryProviderScope<NavKey>, nav: INavigationManager) {
        builder.entry<SearchDestination> {
            val viewModel: SearchViewModel = hiltViewModel()
            ScreenNavigator(viewModel) { state, effect, onEvent ->
                SearchTvScreen(
                    state = state,
                    effect = effect,
                    onEvent = onEvent,
                )
            }
        }
    }
}