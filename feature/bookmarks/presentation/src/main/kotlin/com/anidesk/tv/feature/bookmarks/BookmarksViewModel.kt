package com.anidesk.tv.feature.bookmarks

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.mvi.BaseViewModel
import com.anidesk.tv.core.network.api.AnixartApi
import com.anidesk.tv.core.network.di.AnixartApiProvider
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.preferences.session.SessionStore
import com.anidesk.tv.feature.bookmarks.utils.toAnimeRelease
import com.anidesk.tv.feature.details.IDetailsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOAD_ERROR_MESSAGE = "Не удалось загрузить закладки"
private const val APPEND_ERROR_MESSAGE = "Не удалось загрузить следующую страницу"

@HiltViewModel
class BookmarksViewModel @Inject internal constructor(
    private val apiProvider: AnixartApiProvider,
    private val sessionStore: SessionStore,
    private val detailsNavigator: IDetailsNavigator,
    private val nav: INavigationManager,
) : BaseViewModel<BookmarksState.State, BookmarksState.Event, BookmarksState.Effect>() {

    private var reloadJob: Job? = null
    private var contentVersion = 0

    override fun createInitialState() = BookmarksState.State()

    override fun onEvent(event: BookmarksState.Event) {
        when (event) {
            is BookmarksState.Event.TabSelected -> {
                if (event.tabType != currentState.selectedTabType) {
                    setState { copy(selectedTabType = event.tabType) }
                    reloadFirstPage()
                }
            }

            BookmarksState.Event.LoadNextPage -> loadNextPage()

            is BookmarksState.Event.ReleaseSelected -> openDetails(event.release)

            BookmarksState.Event.Retry -> reloadFirstPage()
        }
    }

    init {
        // Закладки/история требуют токена: держим его в API-синглтоне актуальным при любом
        // входе/выходе, чтобы запросы всегда шли с текущей сессией.
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
                val api = apiProvider.get()
                val response = fetchPage(api, type = currentState.selectedTabType, page = 0)
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
                val api = apiProvider.get()
                val response = fetchPage(api, type = snapshot.selectedTabType, page = snapshot.page + 1)
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

    private suspend fun fetchPage(api: AnixartApi, type: Int?, page: Int) =
        if (type == null) api.history(page) else api.profileList(type = type, page = page)

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