package com.anidesk.tv.feature.search

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.mvi.BaseViewModel
import com.anidesk.tv.core.network.di.AnixartApiProvider
import com.anidesk.tv.core.network.dto.Release
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.feature.details.IDetailsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEARCH_DEBOUNCE_MILLIS = 450L
private const val LOAD_ERROR_MESSAGE = "Не удалось выполнить поиск"
private const val APPEND_ERROR_MESSAGE = "Не удалось загрузить следующие результаты"

@HiltViewModel
class SearchViewModel @Inject internal constructor(
    private val apiProvider: AnixartApiProvider,
    private val detailsNavigator: IDetailsNavigator,
    private val nav: INavigationManager,
) : BaseViewModel<SearchState.State, SearchState.Event, SearchState.Effect>() {

    private var searchJob: Job? = null
    private var contentVersion = 0

    override fun createInitialState() = SearchState.State()

    override fun onEvent(event: SearchState.Event) {
        when (event) {
            is SearchState.Event.QueryChanged -> {
                setState { copy(query = event.query) }
                searchJob?.cancel()
                val query = event.query.trim()
                if (query.isEmpty()) {
                    contentVersion += 1
                    setState { copy(results = emptyList(), page = 0, hasMore = false, error = null) }
                } else {
                    searchJob = viewModelScope.launch {
                        delay(SEARCH_DEBOUNCE_MILLIS)
                        searchFirstPage(query)
                    }
                }
            }

            SearchState.Event.SearchRequested -> {
                searchJob?.cancel()
                val query = currentState.query.trim()
                if (query.isNotEmpty()) {
                    viewModelScope.launch { searchFirstPage(query) }
                }
            }

            is SearchState.Event.LoadNextPage -> loadNextPage(event.query)

            is SearchState.Event.ReleaseSelected -> openDetails(event.release)

            SearchState.Event.Retry -> {
                val query = currentState.query.trim()
                if (query.isNotEmpty()) {
                    viewModelScope.launch { searchFirstPage(query) }
                }
            }
        }
    }

    private suspend fun searchFirstPage(query: String) {
        contentVersion += 1
        val version = contentVersion
        setState { copy(isLoading = true, isAppending = false, error = null) }
        try {
            val releases = apiProvider.get().searchReleases(page = 1, query = query)
            if (version != contentVersion) return
            setState {
                copy(
                    results = releases.map { it.toAnimeRelease() },
                    page = 1,
                    hasMore = releases.isNotEmpty(),
                    isLoading = false,
                    error = null,
                )
            }
        } catch (t: Throwable) {
            if (version != contentVersion) return
            setState {
                copy(
                    isLoading = false,
                    isAppending = false,
                    error = if (results.isEmpty()) LOAD_ERROR_MESSAGE else null,
                )
            }
        }
    }

    private fun loadNextPage(query: String) {
        val snapshot = currentState
        if (snapshot.isLoading || snapshot.isAppending || !snapshot.hasMore) return
        if (snapshot.results.isEmpty()) return
        val version = contentVersion
        setState { copy(isAppending = true, error = null) }
        viewModelScope.launch {
            try {
                val releases = apiProvider.get().searchReleases(
                    page = snapshot.page + 1,
                    query = query,
                )
                if (version != contentVersion) return@launch
                setState {
                    copy(
                        results = results + releases.map { it.toAnimeRelease() },
                        page = snapshot.page + 1,
                        hasMore = releases.isNotEmpty(),
                        isAppending = false,
                        error = null,
                    )
                }
            } catch (t: Throwable) {
                if (version != contentVersion) return@launch
                setState {
                    copy(
                        isAppending = false,
                        error = if (results.isEmpty()) LOAD_ERROR_MESSAGE else APPEND_ERROR_MESSAGE,
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
                title = release.titleRu.ifBlank { release.titleOriginal },
            ),
        )
    }

    private fun Release.toAnimeRelease(): AnimeRelease =
        AnimeRelease(
            id = id,
            titleRu = titleRu,
            titleOriginal = titleOriginal,
            posterUrl = image,
            year = year.toIntOrNull(),
            rating = grade,
            genres = genres.split(',').map { it.trim() }.filter { it.isNotEmpty() },
            category = category?.name.orEmpty(),
            episodesTotal = episodesTotal,
            isViewed = isViewed,
        )
}