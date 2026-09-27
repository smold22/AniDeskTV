package com.anidesk.tv.feature.home

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.model.release.ContinueWatchingEntry
import com.anidesk.tv.core.mvi.BaseViewModel
import com.anidesk.tv.core.network.di.AnixartApiProvider
import com.anidesk.tv.core.network.dto.Release
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.navigation.player.IPlayerLauncher
import com.anidesk.tv.core.preferences.settings.SettingsStore
import com.anidesk.tv.feature.details.IDetailsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOAD_ERROR_MESSAGE = "Не удалось загрузить главную"

/** Статус релиза «выходит» (ongoing) в фильтре Anixart. */
private const val ONGOING_STATUS_ID = 2

@HiltViewModel
class HomeViewModel @Inject internal constructor(
    private val apiProvider: AnixartApiProvider,
    private val settingsStore: SettingsStore,
    private val detailsNavigator: IDetailsNavigator,
    private val playerLauncher: IPlayerLauncher,
    private val nav: INavigationManager,
) : BaseViewModel<HomeState.State, HomeState.Event, HomeState.Effect>() {

    override fun createInitialState() = HomeState.State()

    override fun onEvent(event: HomeState.Event) {
        when (event) {
            is HomeState.Event.ReleaseSelected -> openDetails(event.release)

            is HomeState.Event.ContinueWatchingSelected -> openContinueWatching(event.entry)

            is HomeState.Event.RemoveContinueWatching ->
                viewModelScope.launch { settingsStore.removeContinueWatching(event.releaseId) }

            HomeState.Event.Retry -> loadFeed()
        }
    }

    init {
        loadFeed()
        observeContinueWatching()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            try {
                val api = apiProvider.get()
                val watchingResponse = api.discoverWatching(page = 1)
                val recommendations = api.discoverRecommendations(page = 1).content
                val updates = runCatching {
                    api.filterReleases(page = 1, sort = 0, statusId = ONGOING_STATUS_ID).content
                }.getOrDefault(emptyList())
                setState {
                    copy(
                        watching = watchingResponse.content.map { it.toAnimeRelease() },
                        recommendations = recommendations.map { it.toAnimeRelease() },
                        updates = updates.map { it.toAnimeRelease() },
                        isLoading = false,
                        error = null,
                    )
                }
            } catch (t: Throwable) {
                val noContent = currentState.watching.isEmpty() &&
                    currentState.recommendations.isEmpty() &&
                    currentState.updates.isEmpty()
                setState {
                    copy(
                        isLoading = false,
                        error = if (noContent) LOAD_ERROR_MESSAGE else null,
                    )
                }
            }
        }
    }

    private fun observeContinueWatching() {
        settingsStore.continueWatching
            .onEach { entries -> setState { copy(continueWatching = entries) } }
            .launchIn(viewModelScope)
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

    private fun openContinueWatching(entry: ContinueWatchingEntry) {
        viewModelScope.launch {
            val saved = settingsStore.getDubberSource(entry.releaseId)
            val dubberId = saved.first ?: 0
            val sourceId = saved.second ?: entry.sourceId
            playerLauncher.launch(
                releaseId = entry.releaseId,
                dubberId = dubberId,
                sourceId = sourceId,
                startPosition = entry.episodePosition,
                sourceName = "",
            )
        }
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