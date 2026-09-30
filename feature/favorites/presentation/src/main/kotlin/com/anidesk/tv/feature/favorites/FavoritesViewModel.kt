package com.anidesk.tv.feature.favorites

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.mvi.BaseViewModel
import com.anidesk.tv.core.network.di.AnixartApiProvider
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.preferences.session.SessionStore
import com.anidesk.tv.feature.details.IDetailsNavigator
import com.anidesk.tv.feature.favorites.utils.toAnimeRelease
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOAD_ERROR_MESSAGE = "Не удалось загрузить избранное"
private const val APPEND_ERROR_MESSAGE = "Не удалось загрузить следующую страницу"

@HiltViewModel
class FavoritesViewModel @Inject internal constructor(
    private val apiProvider: AnixartApiProvider,
    private val sessionStore: SessionStore,
    private val detailsNavigator: IDetailsNavigator,
    private val nav: INavigationManager,
) : BaseViewModel<FavoritesState.State, FavoritesState.Event, FavoritesState.Effect>() {

    private var reloadJob: Job? = null
    private var contentVersion = 0

    override fun createInitialState() = FavoritesState.State()

    override fun onEvent(event: FavoritesState.Event) {
        when (event) {
            FavoritesState.Event.LoadNextPage -> loadNextPage()

            is FavoritesState.Event.ReleaseSelected -> openDetails(event.release)

            FavoritesState.Event.Retry -> reloadFirstPage()
        }
    }

    init {
        // Избранное требует токен: держим его в API-синглтоне актуальным при входе/выходе,
        // чтобы запросы всегда шли с текущей сессией.
        viewModelScope.launch {
            sessionStore.token.collect { apiProvider.refreshFromSettings() }
        }
        reloadFirstPage()
    }

    private fun reloadFirstPage() {
        reloadJob?.cancel()
        contentVersion += 1
        val version = contentVersion
        setState { copy(isLoading = true, isAppending = false, error = null) }
        reloadJob = viewModelScope.launch {
            try {
                val response = apiProvider.get().favorites(page = 0)
                if (version != contentVersion) return@launch
                setState {
                    copy(
                        items = response.content.map { it.toAnimeRelease() },
                        page = 0,
                        hasMore = response.content.isNotEmpty(),
                        isLoading = false,
                        isAppending = false,
                        error = null,
                    )
                }
            } catch (t: Throwable) {
                if (version != contentVersion) return@launch
                setState {
                    copy(
                        isLoading = false,
                        isAppending = false,
                        error = if (items.isEmpty()) LOAD_ERROR_MESSAGE else null,
                    )
                }
            }
        }
    }

    private fun loadNextPage() {
        val snapshot = currentState
        if (snapshot.isLoading || snapshot.isAppending || !snapshot.hasMore) return
        if (snapshot.error != null && snapshot.items.isEmpty()) return
        val version = contentVersion
        setState { copy(isAppending = true, error = null) }
        viewModelScope.launch {
            try {
                val response = apiProvider.get().favorites(page = snapshot.page + 1)
                if (version != contentVersion) return@launch
                setState {
                    copy(
                        items = items + response.content.map { it.toAnimeRelease() },
                        page = snapshot.page + 1,
                        hasMore = response.content.isNotEmpty(),
                        isAppending = false,
                        error = null,
                    )
                }
            } catch (t: Throwable) {
                if (version != contentVersion) return@launch
                setState {
                    copy(
                        isAppending = false,
                        error = if (items.isEmpty()) LOAD_ERROR_MESSAGE else APPEND_ERROR_MESSAGE,
                    )
                }
            }
        }
    }

    private fun openDetails(release: AnimeRelease) {
        nav.navigate(
            detailsNavigator.getDetailsDest(
                releaseId = release.id,
                posterUrl = release.posterUrl,
                title = release.titleRu,
            ),
        )
    }
}
