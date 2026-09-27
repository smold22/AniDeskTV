package com.anidesk.tv.feature.details.tv.navigator

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.core.designsystem.baseScreen.ScreenNavigator
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.navigation.registrar.NavRegistrar
import com.anidesk.tv.feature.details.DetailsTvScreen
import com.anidesk.tv.feature.details.DetailsViewModel
import com.anidesk.tv.feature.details.navigator.DetailsDestination
import javax.inject.Inject

class DetailsNavRegistrar @Inject constructor() : NavRegistrar {

    override fun register(builder: EntryProviderScope<NavKey>, nav: INavigationManager) {
        builder.entry<DetailsDestination> { destination ->
            val viewModel = hiltViewModel<DetailsViewModel, DetailsViewModel.Factory>(
                key = "details-${destination.releaseId}",
                creationCallback = { factory ->
                    factory.create(
                        releaseId = destination.releaseId,
                        seedPosterUrl = destination.posterUrl,
                        seedTitle = destination.title,
                    )
                },
            )
            ScreenNavigator(viewModel) { state, effect, onEvent ->
                DetailsTvScreen(
                    state = state,
                    effect = effect,
                    onEvent = onEvent,
                )
            }
        }
    }
}