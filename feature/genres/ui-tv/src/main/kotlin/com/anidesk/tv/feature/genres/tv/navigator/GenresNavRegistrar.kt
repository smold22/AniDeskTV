package com.anidesk.tv.feature.genres.tv.navigator

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.core.designsystem.baseScreen.ScreenNavigator
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.navigation.registrar.NavRegistrar
import com.anidesk.tv.feature.genres.GenresTvScreen
import com.anidesk.tv.feature.genres.GenresViewModel
import com.anidesk.tv.feature.genres.navigator.GenresDestination
import javax.inject.Inject

class GenresNavRegistrar @Inject constructor() : NavRegistrar {

    override fun register(builder: EntryProviderScope<NavKey>, nav: INavigationManager) {
        builder.entry<GenresDestination> {
            val viewModel: GenresViewModel = hiltViewModel()
            ScreenNavigator(viewModel) { state, effect, onEvent ->
                GenresTvScreen(
                    state = state,
                    effect = effect,
                    onEvent = onEvent,
                )
            }
        }
    }
}