package com.anidesk.tv.feature.favorites.tv.navigator

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.core.designsystem.baseScreen.ScreenNavigator
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.navigation.registrar.NavRegistrar
import com.anidesk.tv.feature.favorites.FavoritesTvScreen
import com.anidesk.tv.feature.favorites.FavoritesViewModel
import com.anidesk.tv.feature.favorites.navigator.FavoritesDestination
import javax.inject.Inject

class FavoritesNavRegistrar @Inject constructor() : NavRegistrar {

    override fun register(builder: EntryProviderScope<NavKey>, nav: INavigationManager) {
        builder.entry<FavoritesDestination> {
            val viewModel: FavoritesViewModel = hiltViewModel()
            ScreenNavigator(viewModel) { state, effect, onEvent ->
                FavoritesTvScreen(
                    state = state,
                    effect = effect,
                    onEvent = onEvent,
                )
            }
        }
    }
}
