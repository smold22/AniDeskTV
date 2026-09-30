package com.anidesk.tv.feature.settings

import com.anidesk.tv.core.model.settings.PosterCardSize
import com.anidesk.tv.core.model.settings.PosterQuality
import com.anidesk.tv.core.model.settings.ThemeMode
import com.anidesk.tv.core.mvi.UiEffect
import com.anidesk.tv.core.mvi.UiEvent
import com.anidesk.tv.core.mvi.UiState
import com.anidesk.tv.core.preferences.settings.SettingsStore

class SettingsState {

    data class State(
        val posterQuality: PosterQuality = PosterQuality.STANDARD,
        val posterCardSize: PosterCardSize = PosterCardSize.LARGE,
        val themeMode: ThemeMode = ThemeMode.DARK,
        val apiEndpoint: String = SettingsStore.DEFAULT_API_ENDPOINT,
        val isAuthenticated: Boolean = false,
        val authSkipped: Boolean = false,
        val profileLogin: String = "",
        val sessionLoading: Boolean = false,
        val sessionError: String? = null,
    ) : UiState

    sealed class Event : UiEvent {
        data class PosterQualitySelected(val quality: PosterQuality) : Event()

        data class PosterCardSizeSelected(val size: PosterCardSize) : Event()

        data class ThemeModeSelected(val mode: ThemeMode) : Event()

        data class ApiEndpointSelected(val endpoint: String) : Event()

        data class LoginRequested(val login: String, val password: String) : Event()

        data object AuthSkipped : Event()

        data object LogoutRequested : Event()
    }

    /** Эффекты настроек не нужны — все сбои отражаются в [State.sessionError]. */
    sealed class Effect : UiEffect
}