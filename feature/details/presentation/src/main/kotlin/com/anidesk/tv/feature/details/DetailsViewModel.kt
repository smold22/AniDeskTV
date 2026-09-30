package com.anidesk.tv.feature.details

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.model.release.BookmarkStatus
import com.anidesk.tv.core.model.release.ContinueWatchingEntry
import com.anidesk.tv.core.mvi.BaseViewModel
import com.anidesk.tv.core.network.di.AnixartApiProvider
import com.anidesk.tv.core.network.dto.Episode
import com.anidesk.tv.core.network.dto.Release
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.navigation.player.IPlayerLauncher
import com.anidesk.tv.core.preferences.session.SessionStore
import com.anidesk.tv.core.preferences.settings.SettingsStore
import com.anidesk.tv.feature.details.utils.toAnimeRelease
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

private const val LOAD_ERROR_MESSAGE = "Не удалось загрузить тайтл"
private const val RELATED_ERROR_MESSAGE = "Не удалось загрузить связанные релизы"

/** Страницы related в API 0-индексированные, а `AnixartApi.relatedReleases` ждёт 1-индексацию. */
private const val FIRST_RELATED_PAGE = 1

@HiltViewModel(assistedFactory = DetailsViewModel.Factory::class)
class DetailsViewModel @AssistedInject internal constructor(
    @Assisted private val releaseId: Int,
    @Assisted("posterUrl") private val seedPosterUrl: String?,
    @Assisted("title") private val seedTitle: String?,
    private val apiProvider: AnixartApiProvider,
    private val settingsStore: SettingsStore,
    private val sessionStore: SessionStore,
    private val playerLauncher: IPlayerLauncher,
    private val nav: INavigationManager,
    private val detailsNavigator: IDetailsNavigator,
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
        // Закладки и избранное — серверные списки аккаунта: держим токен в API-синглтоне
        // актуальным при каждом входе/выходе и по нему же решаем, показывать ли кнопки.
        viewModelScope.launch {
            sessionStore.token.collect { token ->
                apiProvider.refreshFromSettings()
                setState { copy(isAuthorized = !token.isNullOrBlank()) }
            }
        }
        load()
    }

    override fun onEvent(event: DetailsState.Event) {
        when (event) {
            is DetailsState.Event.DubberSelected -> selectDubber(event.dubberId)

            is DetailsState.Event.SourceSelected -> selectSource(event.sourceId)

            is DetailsState.Event.EpisodeSelected -> openEpisode(event.episode)

            DetailsState.Event.FavoriteClicked -> toggleFavorite()

            is DetailsState.Event.BookmarkStatusSelected -> applyBookmarkStatus(event.status)

            DetailsState.Event.LoadMoreRelated -> loadMoreRelated()

            is DetailsState.Event.RelatedReleaseSelected -> openRelatedRelease(event.release)

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
                            isFavorite = release.isFavorite,
                            favoritesCount = release.favoritesCount,
                            bookmarkStatus = BookmarkStatus.fromType(release.profileListStatus),
                            isLoading = false,
                            error = null,
                        )
                    }
                    loadDubbers()
                    // Связанные релизы — второстепенный блок, поэтому грузим его отдельной
                    // корутиной: медленный ответ не должен задерживать озвучки и серии.
                    loadRelatedReleases(release)
                }
            } catch (t: Throwable) {
                setState { copy(isLoading = false, error = t.message ?: LOAD_ERROR_MESSAGE) }
            }
        }
    }

    /**
     * Первая страница связанных релизов. Связанная сущность приходит полем `related`
     * у самого тайтла, а её релизы — отдельным запросом `/related/{id}/{page}`.
     * Признак «есть ли что показывать» — [Release.relatedCount]: счётчик `release_count`
     * внутри самой `related` Anixart всегда отдаёт 0.
     * Свой тайтл из выдачи выкидываем, иначе он будет висеть в «Связанных релизах» сам у себя.
     */
    private fun loadRelatedReleases(release: Release) {
        val related = release.related
        if (related == null || release.relatedCount <= 0) {
            setState { copy(relatedReleases = emptyList(), hasMoreRelated = false, relatedError = null) }
            return
        }
        setState { copy(isRelatedLoading = true, relatedError = null) }
        viewModelScope.launch {
            try {
                val response = apiProvider.get().relatedReleases(
                    relatedId = related.id,
                    page = FIRST_RELATED_PAGE,
                )
                setState {
                    copy(
                        relatedReleases = response.content.toRelatedReleases(),
                        relatedPage = FIRST_RELATED_PAGE,
                        // Страниц у related сколько — API не сообщает (total_page_count = 0),
                        // поэтому конец списка определяется пустым ответом.
                        hasMoreRelated = response.content.isNotEmpty(),
                        isRelatedLoading = false,
                        relatedError = null,
                    )
                }
            } catch (t: Throwable) {
                setState { copy(isRelatedLoading = false, relatedError = RELATED_ERROR_MESSAGE) }
            }
        }
    }

    /**
     * Подгрузка следующей страницы: [DetailsState.State.relatedPage] хранит 1-индексированный
     * номер последней удачной страницы, поэтому следующая — `relatedPage + 1`.
     */
    private fun loadMoreRelated() {
        val snapshot = currentState
        val relatedId = snapshot.release?.related?.id ?: return
        if (snapshot.isRelatedLoading || !snapshot.hasMoreRelated) return
        val nextPage = snapshot.relatedPage + 1
        setState { copy(isRelatedLoading = true, relatedError = null) }
        viewModelScope.launch {
            try {
                val response = apiProvider.get().relatedReleases(
                    relatedId = relatedId,
                    page = nextPage,
                )
                setState {
                    copy(
                        relatedReleases = relatedReleases + response.content.toRelatedReleases(),
                        relatedPage = nextPage,
                        hasMoreRelated = response.content.isNotEmpty(),
                        isRelatedLoading = false,
                        relatedError = null,
                    )
                }
            } catch (t: Throwable) {
                setState { copy(isRelatedLoading = false, relatedError = RELATED_ERROR_MESSAGE) }
            }
        }
    }

    private fun openRelatedRelease(release: AnimeRelease) {
        nav.navigate(
            detailsNavigator.getDetailsDest(
                releaseId = release.id,
                posterUrl = release.posterUrl,
                title = release.titleRu.ifBlank { release.titleOriginal },
            ),
        )
    }

    private fun List<Release>.toRelatedReleases() = map { it.toAnimeRelease() }.filter { it.id != releaseId }

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

    /**
     * Переключение избранного: состояние меняется сразу (оптимистично), а при ошибке
     * откатывается и показывается уведомление. Флаг `isLibraryUpdating` не даёт отправить
     * два запроса подряд при быстрых нажатиях.
     */
    private fun toggleFavorite() {
        val state = currentState
        if (!state.isAuthorized || state.isLibraryUpdating) return
        val target = !state.isFavorite
        setState {
            copy(
                isFavorite = target,
                favoritesCount = (favoritesCount + if (target) 1 else -1).coerceAtLeast(0),
                isLibraryUpdating = true,
            )
        }
        viewModelScope.launch {
            try {
                val api = apiProvider.get()
                if (target) api.addFavorite(releaseId) else api.removeFavorite(releaseId)
                setState { copy(isLibraryUpdating = false) }
                setEffect(DetailsState.Effect.FavoriteChanged(target))
            } catch (t: Throwable) {
                setState {
                    copy(
                        isFavorite = !target,
                        favoritesCount = (favoritesCount + if (target) -1 else 1).coerceAtLeast(0),
                        isLibraryUpdating = false,
                    )
                }
                setEffect(DetailsState.Effect.LibraryActionFailed)
            }
        }
    }

    /**
     * Смена списка закладок. Списки в Anixart взаимоисключающие, поэтому новый статус
     * всегда применяется через «убрать из текущего → добавить в новый».
     */
    private fun applyBookmarkStatus(status: BookmarkStatus) {
        val state = currentState
        if (!state.isAuthorized || state.isLibraryUpdating) return
        if (state.bookmarkStatus == status) return
        val previous = state.bookmarkStatus
        setState { copy(bookmarkStatus = status, isLibraryUpdating = true) }
        viewModelScope.launch {
            try {
                val api = apiProvider.get()
                if (previous != BookmarkStatus.NONE) {
                    api.removeFromProfileList(type = previous.type, releaseId = releaseId)
                }
                if (status != BookmarkStatus.NONE) {
                    api.addToProfileList(type = status.type, releaseId = releaseId)
                }
                setState { copy(isLibraryUpdating = false) }
                setEffect(
                    if (status == BookmarkStatus.NONE) {
                        DetailsState.Effect.BookmarkRemoved
                    } else {
                        DetailsState.Effect.BookmarkAdded(status)
                    },
                )
            } catch (t: Throwable) {
                setState { copy(bookmarkStatus = previous, isLibraryUpdating = false) }
                setEffect(DetailsState.Effect.LibraryActionFailed)
            }
        }
    }
}