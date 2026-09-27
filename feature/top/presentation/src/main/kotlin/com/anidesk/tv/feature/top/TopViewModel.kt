package com.anidesk.tv.feature.top

import com.anidesk.tv.core.mvi.BaseViewModel
import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.network.di.AnixartApiProvider
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.network.dto.Release
import com.anidesk.tv.feature.details.IDetailsNavigator
import com.anidesk.tv.feature.top.utils.toAnimeRelease
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOAD_ERROR_MESSAGE = "Не удалось загрузить топ"
private const val APPEND_ERROR_MESSAGE = "Не удалось загрузить следующую страницу"

/** Размер страницы фильтра API (страница считается полной, значит есть следующая). */
private const val FILTER_PAGE_SIZE = 25

/** Значения `sort` в `POST /filter/{page}`. */
private const val SORT_BY_ADDED = 0
private const val SORT_BY_RATING = 1
private const val SORT_BY_YEAR = 2

@HiltViewModel
class TopViewModel @Inject internal constructor(
    private val apiProvider: AnixartApiProvider,
    private val detailsNavigator: IDetailsNavigator,
    private val nav: INavigationManager,
) : BaseViewModel<TopState.State, TopState.Event, TopState.Effect>() {

    private var reloadJob: Job? = null
    private var contentVersion = 0

    override fun createInitialState() = TopState.State()

    override fun onEvent(event: TopState.Event) {
        when (event) {
            is TopState.Event.TypeToggled -> {
                setState { copy(selectedTypes = toggleType(selectedTypes, event.typeId)) }
                reloadFirstPage()
            }

            is TopState.Event.SortChanged -> {
                setState { copy(sortIndex = event.sortIndex) }
                reloadFirstPage()
            }

            TopState.Event.DirectionToggled -> {
                setState { copy(directionAsc = !directionAsc) }
                reloadFirstPage()
            }

            TopState.Event.LoadNextPage -> loadNextPage()

            is TopState.Event.ReleaseSelected -> openDetails(event.release)

            TopState.Event.Retry -> reloadFirstPage()
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
                if (currentState.types.isEmpty()) {
                    val types = api.types()
                    setState { copy(types = types) }
                }
                val response = api.filterReleases(
                    page = 1,
                    sort = currentState.sortIndex,
                    types = currentState.selectedTypes,
                )
                if (version != contentVersion) return@launch
                setState {
                    copy(
                        items = response.content
                            .sortedByDirection(currentState.sortIndex, currentState.directionAsc)
                            .map { it.toAnimeRelease() },
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
                    sort = snapshot.sortIndex,
                    types = snapshot.selectedTypes,
                )
                if (version != contentVersion) return@launch
                setState {
                    copy(
                        items = items + response.content
                            .sortedByDirection(snapshot.sortIndex, snapshot.directionAsc)
                            .map { it.toAnimeRelease() },
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

    private fun toggleType(types: List<Int>, typeId: Int): List<Int> =
        if (typeId in types) types - typeId else types + typeId

    /**
     * `POST /filter` не умеет направление сортировки (asc/desc), поэтому страница
     * упорядочивается на клиенте по тому же ключу, что и `sort` на сервере.
     */
    private fun List<Release>.sortedByDirection(sortIndex: Int, asc: Boolean): List<Release> {
        val comparator = when (sortIndex) {
            SORT_BY_ADDED -> compareBy<Release> { it.creationDate }
            SORT_BY_RATING -> compareBy { it.grade }
            SORT_BY_YEAR -> compareBy { it.year.toIntOrNull() ?: 0 }
            else -> return this
        }
        return if (asc) sortedWith(comparator) else sortedWith(comparator.reversed())
    }
}