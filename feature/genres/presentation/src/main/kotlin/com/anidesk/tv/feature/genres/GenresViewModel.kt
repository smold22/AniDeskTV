package com.anidesk.tv.feature.genres

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.mvi.BaseViewModel
import com.anidesk.tv.core.network.di.AnixartApiProvider
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.feature.details.IDetailsNavigator
import com.anidesk.tv.feature.genres.utils.toAnimeRelease
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOAD_ERROR_MESSAGE = "Не удалось загрузить релизы"
private const val APPEND_ERROR_MESSAGE = "Не удалось загрузить следующую страницу"

/** Размер страницы фильтра API (страница считается полной, значит есть следующая). */
private const val FILTER_PAGE_SIZE = 25

@HiltViewModel
class GenresViewModel @Inject internal constructor(
    private val apiProvider: AnixartApiProvider,
    private val detailsNavigator: IDetailsNavigator,
    private val nav: INavigationManager,
) : BaseViewModel<GenresState.State, GenresState.Event, GenresState.Effect>() {

    private var reloadJob: Job? = null
    private var contentVersion = 0

    override fun createInitialState() = GenresState.State()

    override fun onEvent(event: GenresState.Event) {
        when (event) {
            is GenresState.Event.GenreSelected -> {
                setState {
                    copy(
                        selectedGenre = if (selectedGenre == event.genreName) null else event.genreName,
                    )
                }
                reloadFirstPage()
            }

            GenresState.Event.SelectAll -> {
                setState { copy(selectedGenre = null) }
                reloadFirstPage()
            }

            GenresState.Event.LoadNextPage -> loadNextPage()

            is GenresState.Event.ReleaseSelected -> openDetails(event.release)

            GenresState.Event.Retry -> reloadFirstPage()
        }
    }

    init {
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
                val response = api.filterReleases(
                    page = 1,
                    genres = currentState.selectedGenre?.let { listOf(it) } ?: emptyList(),
                )
                if (version != contentVersion) return@launch
                setState {
                    copy(
                        items = response.content.map { it.toAnimeRelease() },
                        page = 1,
                        hasMore = response.content.size >= FILTER_PAGE_SIZE,
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
                val response = api.filterReleases(
                    page = snapshot.page + 1,
                    genres = snapshot.selectedGenre?.let { listOf(it) } ?: emptyList(),
                )
                if (version != contentVersion) return@launch
                setState {
                    copy(
                        items = items + response.content.map { it.toAnimeRelease() },
                        page = snapshot.page + 1,
                        hasMore = response.content.size >= FILTER_PAGE_SIZE,
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