package com.anidesk.tv.feature.settings.tv.navigator

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.core.designsystem.baseScreen.ScreenNavigator
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.navigation.registrar.NavRegistrar
import com.anidesk.tv.feature.settings.SettingsTvScreen
import com.anidesk.tv.feature.settings.SettingsViewModel
import com.anidesk.tv.feature.settings.navigator.SettingsDestination
import javax.inject.Inject

class SettingsNavRegistrar @Inject constructor() : NavRegistrar {

    override fun register(builder: EntryProviderScope<NavKey>, nav: INavigationManager) {
        builder.entry<SettingsDestination> {
            val viewModel: SettingsViewModel = hiltViewModel()
            ScreenNavigator(viewModel) { state, effect, onEvent ->
                SettingsTvScreen(
                    state = state,
                    effect = effect,
                    onEvent = onEvent,
                )
            }
        }
    }
}