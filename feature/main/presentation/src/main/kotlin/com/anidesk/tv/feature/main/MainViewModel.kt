package com.anidesk.tv.feature.main

import com.anidesk.tv.core.mvi.BaseViewModel
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.preferences.settings.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject internal constructor(
    private val settingsStore: SettingsStore,
    private val nav: INavigationManager,
) : BaseViewModel<MainState.State, MainState.Event, MainState.Effect>() {

    override fun createInitialState() = MainState.State()

    override fun onEvent(event: MainState.Event) {
        when (event) {
            is MainState.Event.TvRootSelected -> nav.switchRoot(
                root = event.root,
                reselectPopToRoot = false,
            )
        }
    }

    init {
        observeSettings()
    }

    private fun observeSettings() {
        settingsStore.mainSettingsSnapshot
            .onEach { snapshot ->
                setState {
                    copy(
                        posterQuality = snapshot.posterQuality,
                        posterCardSize = snapshot.posterCardSize,
                        themeMode = snapshot.themeMode,
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}