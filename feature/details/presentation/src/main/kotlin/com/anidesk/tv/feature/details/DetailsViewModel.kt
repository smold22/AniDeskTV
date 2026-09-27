package com.anidesk.tv.feature.details

import com.anidesk.tv.core.model.release.ContinueWatchingEntry
import com.anidesk.tv.core.mvi.BaseViewModel
import com.anidesk.tv.core.network.di.AnixartApiProvider
import com.anidesk.tv.core.network.dto.Episode
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.navigation.player.IPlayerLauncher
import com.anidesk.tv.core.preferences.settings.SettingsStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

private const val LOAD_ERROR_MESSAGE = "Не удалось загрузить тайтл"

@HiltViewModel(assistedFactory = DetailsViewModel.Factory::class)
class DetailsViewModel @AssistedInject internal constructor(
    @Assisted private val releaseId: Int,
    @Assisted("posterUrl") private val seedPosterUrl: String?,
    @Assisted("title") private val seedTitle: String?,
    private val apiProvider: AnixartApiProvider,
    private val settingsStore: SettingsStore,
    private val playerLauncher: IPlayerLauncher,
    private val nav: INavigationManager,
) : BaseViewModel<DetailsState.State, DetailsState.Event, DetailsState.Effect>() {

    @AssistedFactory
    interface Factory {
        fun create(
            releaseId: Int,
            @Assisted("posterUrl") seedPosterUrl: String?,
            @Assisted("title") seedTitle: String?,
        ): DetailsViewModel
    }

    override fun createInitialState() = DetailsState.State(
        posterUrl = seedPosterUrl,
        title = seedTitle,
    )

    init {
        load()
    }

    override fun onEvent(event: DetailsState.Event) {
        when (event) {
            is DetailsState.Event.DubberSelected -> selectDubber(event.dubberId)

            is DetailsState.Event.SourceSelected -> selectSource(event.sourceId)

            is DetailsState.Event.EpisodeSelected -> openEpisode(event.episode)

            DetailsState.Event.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            try {
                val api = apiProvider.get()
                val release = api.releaseInfo(releaseId).release
                if (release == null) {
                    setState { copy(isLoading = false, error = LOAD_ERROR_MESSAGE) }
                } else {
                    setState {
                        copy(
                            release = release,
                            posterUrl = release.image.takeUnless {
                                it.isBlank() || it.contains("no_image")
                            } ?: posterUrl,
                            title = release.titleRu.ifBlank { title ?: release.titleOriginal },
                            isLoading = false,
                            error = null,
                        )
                    }
                    loadDubbers()
                }
            } catch (t: Throwable) {
                setState { copy(isLoading = false, error = t.message ?: LOAD_ERROR_MESSAGE) }
            }
        }
    }

    private suspend fun loadDubbers() {
        try {
            val dubbers = apiProvider.get().getDubbers(releaseId).types
            setState { copy(dubbers = dubbers) }
            val savedDubber = settingsStore.getDubberSource(releaseId).first
            val initialDubber = savedDubber
                ?: dubbers.firstOrNull { !it.isSub }?.id
                ?: dubbers.firstOrNull()?.id
            initialDubber?.let { selectDubber(it) }
        } catch (t: Throwable) {
        }
    }

    private fun selectDubber(dubberId: Int) {
        viewModelScope.launch {
            if (currentState.selectedDubber == dubberId && currentState.sources.isNotEmpty()) {
                return@launch
            }
            setState {
                copy(
                    selectedDubber = dubberId,
                    sources = emptyList(),
                    selectedSource = null,
                    episodes = emptyList(),
                )
            }
            try {
                val sources = apiProvider.get().getDubberSources(releaseId, dubberId).sources
                setState { copy(sources = sources) }
                val savedSource = settingsStore.getDubberSource(releaseId).second
                val initialSource = savedSource?.takeIf { saved -> sources.any { it.id == saved } }
                    ?: sources.firstOrNull()?.id
                initialSource?.let { selectSource(it) }
            } catch (t: Throwable) {
            }
        }
    }

    private fun selectSource(sourceId: Int) {
        viewModelScope.launch {
            val dubberId = currentState.selectedDubber ?: return@launch
            if (currentState.selectedSource == sourceId && currentState.episodes.isNotEmpty()) {
                return@launch
            }
            setState { copy(selectedSource = sourceId, episodes = emptyList(), isEpisodesLoading = true) }
            try {
                val episodes = apiProvider.get().getEpisodes(releaseId, dubberId, sourceId).episodes
                setState { copy(episodes = episodes, isEpisodesLoading = false) }
            } catch (t: Throwable) {
                setState { copy(isEpisodesLoading = false) }
            }
        }
    }

    private fun openEpisode(episode: Episode) {
        if (episode.url.isBlank()) return
        val state = currentState
        val dubberId = state.selectedDubber
        val sourceId = state.selectedSource
        val sourceName = state.sources.firstOrNull { it.id == sourceId }?.name.orEmpty()
        viewModelScope.launch {
            runCatching {
                apiProvider.get().markEpisodeAsWatched(releaseId, sourceId ?: 0, episode.position)
            }
            if (dubberId != null && sourceId != null) {
                runCatching { settingsStore.setDubberSource(releaseId, dubberId, sourceId) }
            }
            runCatching {
                settingsStore.putContinueWatching(
                    ContinueWatchingEntry(
                        releaseId = releaseId,
                        titleRu = state.title.orEmpty(),
                        titleOriginal = state.release?.titleOriginal.orEmpty(),
                        posterUrl = state.posterUrl.orEmpty(),
                        sourceId = sourceId ?: 0,
                        episodePosition = episode.position,
                        episodeName = episode.name,
                        positionMs = 0L,
                        durationMs = 0L,
                        lastWatchedAt = System.currentTimeMillis(),
                    ),
                )
            }
            playerLauncher.launch(
                releaseId = releaseId,
                dubberId = dubberId ?: 0,
                sourceId = sourceId ?: 0,
                startPosition = episode.position,
                sourceName = sourceName,
            )
        }
    }
}