package com.anidesk.tv.feature.settings

import com.anidesk.tv.core.mvi.BaseViewModel
import com.anidesk.tv.core.network.di.AnixartApiProvider
import com.anidesk.tv.core.preferences.session.SessionStore
import com.anidesk.tv.core.preferences.settings.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject internal constructor(
    private val settingsStore: SettingsStore,
    private val sessionStore: SessionStore,
    private val apiProvider: AnixartApiProvider,
) : BaseViewModel<SettingsState.State, SettingsState.Event, SettingsState.Effect>() {

    override fun createInitialState() = SettingsState.State()

    override fun onEvent(event: SettingsState.Event) {
        when (event) {
            is SettingsState.Event.PosterQualitySelected ->
                viewModelScope.launch { settingsStore.setPosterQuality(event.quality) }

            is SettingsState.Event.PosterCardSizeSelected ->
                viewModelScope.launch { settingsStore.setPosterCardSize(event.size) }

            is SettingsState.Event.ThemeModeSelected ->
                viewModelScope.launch { settingsStore.setThemeMode(event.mode) }

            is SettingsState.Event.ApiEndpointSelected ->
                viewModelScope.launch {
                    settingsStore.setApiEndpoint(event.endpoint)
                    apiProvider.refreshFromSettings()
                }

            is SettingsState.Event.LoginRequested -> login(event.login, event.password)

            SettingsState.Event.AuthSkipped ->
                viewModelScope.launch {
                    sessionStore.setAuthSkipped(true)
                    setState { copy(sessionError = null) }
                }

            SettingsState.Event.LogoutRequested ->
                viewModelScope.launch {
                    sessionStore.clear()
                    apiProvider.refreshFromSettings()
                    setState { copy(sessionError = null) }
                }
        }
    }

    init {
        observeSettings()
        observeSession()
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

        settingsStore.apiEndpoint
            .onEach { endpoint -> setState { copy(apiEndpoint = endpoint) } }
            .launchIn(viewModelScope)
    }

    private fun observeSession() {
        combine(sessionStore.token, sessionStore.profileId) { token, id -> token to id }
            .onEach { (token, id) ->
                setState { copy(isAuthenticated = token != null, profileLogin = "") }
                if (token != null && id != null) loadProfileLogin(id)
            }
            .launchIn(viewModelScope)

        sessionStore.authSkipped
            .onEach { skipped -> setState { copy(authSkipped = skipped) } }
            .launchIn(viewModelScope)
    }

    private fun loadProfileLogin(profileId: Int) {
        viewModelScope.launch {
            runCatching { apiProvider.get().profile(profileId) }
                .onSuccess { response ->
                    response.profile?.let { profile -> setState { copy(profileLogin = profile.login) } }
                }
        }
    }

    private fun login(login: String, password: String) {
        viewModelScope.launch {
            setState { copy(sessionLoading = true, sessionError = null) }
            try {
                val response = apiProvider.get().signIn(login = login, password = password)
                val profile = response.profile
                val profileToken = response.profileToken
                if (profile != null && profileToken != null) {
                    sessionStore.save(profileId = profile.id, token = profileToken.token)
                    // После смены сессии API-синглтон должен использовать свежий токен,
                    // иначе следующие запросы (закладки, профиль) уйдут со старым.
                    apiProvider.refreshFromSettings()
                } else {
                    setState { copy(sessionError = LOGIN_BAD_RESPONSE) }
                }
            } catch (t: Throwable) {
                setState { copy(sessionError = t.message ?: LOGIN_FAILED) }
            } finally {
                setState { copy(sessionLoading = false) }
            }
        }
    }

    private companion object {
        const val LOGIN_FAILED = "Не удалось войти"
        const val LOGIN_BAD_RESPONSE = "Сервер не вернул профиль"
    }
}