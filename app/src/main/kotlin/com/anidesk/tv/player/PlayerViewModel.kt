package com.anidesk.tv.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anidesk.tv.core.network.api.AnixartApi
import com.anidesk.tv.core.network.dto.Dubber
import com.anidesk.tv.core.network.dto.Episode
import com.anidesk.tv.core.network.dto.Source
import com.anidesk.tv.core.preferences.settings.SettingsStore
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import android.util.Log

private val SPEED_OPTIONS = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f, 2.5f, 3f)

data class ResumeRequest(
    val positionMs: Long,
    val positionKey: String,
)

class PlayerViewModel(
    private val argExtra: PlayerExtra,
    private val api: AnixartApi,
    private val settingsStore: SettingsStore,
    private val playerController: PlayerController,
    private val guidedRouter: GuidedRouter,
    private val router: Router,
) : ViewModel() {

    val videoData = MutableStateFlow<Video?>(null)
    val qualityState = MutableStateFlow<String?>(null)
    val speedState = MutableStateFlow<Float?>(null)
    val playAction = EventFlow<Boolean>()
    val loading = MutableStateFlow(true)
    val resumeRequest = MutableStateFlow<ResumeRequest?>(null)

    private var currentEpisodes: List<Episode> = emptyList()
    private var currentDubbers: List<Dubber> = emptyList()
    private var currentDubberId: Int = 0
    private var currentSources: List<Source> = emptyList()
    private var currentSource: Source? = null
    private var currentLinks: Map<String, String> = emptyMap()
    private var currentEpisode: Episode? = null
    private var currentQuality: Int = 0
    private var releaseTitle: String = ""
    private var autoPlayEnabled = false
    private var initialPendingResume = false
    private var pendingResumeMs: Long = -1L

    private fun positionKey(episode: Episode): String =
        "${argExtra.releaseId}:${currentSource?.id ?: 0}:${episode.position}"

    init {
        playerController.reset()

        viewModelScope.launch {
            settingsStore.playbackSpeed.collectLatest { index ->
                speedState.value = SPEED_OPTIONS[index.coerceIn(0, SPEED_OPTIONS.lastIndex)]
            }
        }
        viewModelScope.launch {
            settingsStore.defaultQuality.collectLatest { quality ->
                currentQuality = quality
            }
        }
        viewModelScope.launch {
            settingsStore.autoPlay.collectLatest { autoPlayEnabled = it }
        }
        viewModelScope.launch {
            settingsStore.rewindTime.collectLatest { seconds ->
                playerController.skipIntervalMs.value = seconds * 1000L
            }
        }

        playerController
            .selectEpisodeRelay
            .onEach { episodeId ->
                currentEpisodes
                    .firstOrNull { it.position == episodeId.position }
                    ?.also { playEpisode(it, force = true) }
            }
            .launchIn(viewModelScope)

        playerController
            .selectQualityRelay
            .onEach { quality ->
                currentQuality = quality
                updateEpisode(force = true)
            }
            .launchIn(viewModelScope)

        playerController
            .selectDubberRelay
            .onEach { dubber ->
                viewModelScope.launch {
                    switchDubber(dubber)
                }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        try {
            val release = api.releaseInfo(argExtra.releaseId).release
            releaseTitle = release?.titleRu ?: ""
            currentDubbers = api.getDubbers(argExtra.releaseId).types
            val dubber = currentDubbers.firstOrNull { it.id == argExtra.dubberId }
                ?: playerController.selectedDubber?.let { selected ->
                    currentDubbers.firstOrNull { it.id == selected.id }
                }
                ?: currentDubbers.firstOrNull()
                ?: error("Нет доступных озвучек")
            currentDubberId = dubber.id
            currentSources = api.getDubberSources(argExtra.releaseId, dubber.id).sources
            val source = currentSources.firstOrNull { it.id == argExtra.sourceId }
                ?: currentSources.firstOrNull()
                ?: error("Нет доступных источников")
            currentSource = source
            currentEpisodes = api.getEpisodes(
                argExtra.releaseId,
                dubber.id,
                source.id
            ).episodes
            val startEpisode = findStartEpisode()
            initialPendingResume = true
            playEpisode(startEpisode, force = true, resumePrompt = true)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("PlayerViewModel", "loadData failed", e)
        }
        loading.value = false
    }

    private suspend fun findStartEpisode(): Episode {
        val targetId = argExtra.startPosition
        return currentEpisodes.firstOrNull { it.position == targetId }
            ?: currentEpisodes.firstOrNull()
            ?: error("Нет доступных серий")
    }

    fun onPauseClick(position: Long) {
        savePosition(position)
    }

    fun onNextClick(position: Long) {
        getNextEpisode()?.also {
            savePosition(position)
            playEpisode(it, force = true)
        }
    }

    fun onPrevClick(position: Long) {
        getPrevEpisode()?.also {
            savePosition(position)
            playEpisode(it, force = true)
        }
    }

    fun onEpisodesClick(position: Long) {
        savePosition(position)
        guidedRouter.open(PlayerEpisodesGuidedScreen(argExtra))
    }

    fun onQualityClick(position: Long) {
        savePosition(position)
        guidedRouter.open(PlayerQualityGuidedScreen(argExtra))
    }

    fun onSpeedClick() {
        guidedRouter.open(PlayerSpeedGuidedScreen(argExtra))
    }

    fun onDubberClick() {
        guidedRouter.open(PlayerDubberGuidedScreen(argExtra))
    }

    fun onComplete(position: Long) {
        savePosition(position)
        val nextEpisode = getNextEpisode()
        if (autoPlayEnabled && nextEpisode != null) {
            playEpisode(nextEpisode, force = true)
        } else {
            router.exit()
        }
    }

    fun onPrepare(duration: Long) {
        val episode = currentEpisode ?: return
        viewModelScope.launch {
            val saved = settingsStore.getPlaybackPosition(positionKey(episode))
            if (initialPendingResume) {
                initialPendingResume = false
                if (saved > 5000 && duration > 0 && saved < duration) {
                    playAction.emit(false)
                    resumeRequest.value = ResumeRequest(saved, positionKey(episode))
                    return@launch
                }
            }
            val complete = saved >= duration && duration > 0
            if (complete) {
                playAction.emit(false)
                val nextEpisode = getNextEpisode()
                if (autoPlayEnabled && nextEpisode != null) {
                    playEpisode(nextEpisode, force = true)
                } else {
                    playAction.emit(true)
                }
            } else {
                playAction.emit(true)
            }
        }
    }

    private fun getNextEpisode(): Episode? =
        currentEpisodes.getOrNull(getCurrentEpisodeIndex() + 1)

    private fun getPrevEpisode(): Episode? =
        currentEpisodes.getOrNull(getCurrentEpisodeIndex() - 1)

    private fun getCurrentEpisodeIndex(): Int =
        currentEpisodes.indexOfFirst { it.position == currentEpisode?.position }

    fun savePosition(position: Long) {
        val episode = currentEpisode ?: return
        if (position < 0) {
            return
        }
        viewModelScope.launch {
            settingsStore.setPlaybackPosition(positionKey(episode), position)
        }
    }

    fun clearPosition(key: String) {
        viewModelScope.launch {
            settingsStore.clearPlaybackPosition(key)
        }
    }

    private fun playEpisode(episode: Episode, force: Boolean = false, resumePrompt: Boolean = false) {
        currentEpisode = episode
        viewModelScope.launch {
            currentLinks = SourceParsers.parse(episode.url, currentSource?.name.orEmpty())
            if (currentLinks.isEmpty()) {
                Log.e("PlayerViewModel", "SourceParsers returned empty links for ${episode.url}")
                return@launch
            }
            updatePlayerData()
            updateEpisode(force, resumePrompt)
            if (api.token != null) {
                runCatching {
                    api.markEpisodeAsWatched(
                        argExtra.releaseId,
                        currentSource?.id ?: 0,
                        episode.position
                    )
                }
                runCatching {
                    api.addToHistory(
                        argExtra.releaseId,
                        currentSource?.id ?: 0,
                        episode.position
                    )
                }
            }
        }
    }

    private fun updatePlayerData() {
        playerController.data.value = PlayerController.PlayerData(
            releaseId = argExtra.releaseId,
            episodes = currentEpisodes,
            dubbers = currentDubbers,
            sources = currentSources,
            currentEpisode = currentEpisode,
            currentDubberId = currentDubberId,
            currentSourceId = currentSource?.id ?: 0,
            links = currentLinks,
            currentQuality = currentQuality,
        )
    }

    private fun updateEpisode(force: Boolean = false, resumePrompt: Boolean = false) {
        val episode = currentEpisode ?: return
        viewModelScope.launch {
            val url = pickUrl(currentLinks, currentQuality)
            if (url == null) {
                return@launch
            }
            val saved = settingsStore.getPlaybackPosition(positionKey(episode))
            val resumeMs = if (pendingResumeMs >= 0) pendingResumeMs else saved
            pendingResumeMs = -1
            val sourceName = currentSource?.name.orEmpty()
            val headers = if (sourceName.contains("Sibnet", ignoreCase = true)) {
                mapOf(
                    "Referer" to "https://video.sibnet.ru/",
                    "Origin" to "https://video.sibnet.ru",
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                )
            } else {
                emptyMap()
            }
            qualityState.value = if (currentQuality == 0) "auto" else "${currentQuality}"
            val newVideo = Video(
                url = url,
                seek = if (resumePrompt) 0 else resumeMs,
                title = releaseTitle,
                subtitle = episode.name,
                headers = headers,
            )
            if (force || videoData.value?.url != newVideo.url) {
                videoData.value = newVideo
            }
        }
    }

    private suspend fun switchDubber(dubber: Dubber) {
        Log.i("PlayerViewModel", "switchDubber ${dubber.name} id=${dubber.id}")
        try {
            val sources = api.getDubberSources(argExtra.releaseId, dubber.id).sources
            val source = sources.firstOrNull()
            if (source == null) {
                Log.e("PlayerViewModel", "No sources for dubber ${dubber.name}")
                return
            }
            val pos = currentEpisode?.position ?: argExtra.startPosition
            pendingResumeMs = playerController.currentPositionMs
            val res = api.getEpisodes(argExtra.releaseId, dubber.id, source.id).episodes
            currentDubbers = api.getDubbers(argExtra.releaseId).types
            currentDubberId = dubber.id
            currentSources = sources
            currentSource = source
            currentEpisodes = res
            val target = res.firstOrNull { it.position == pos } ?: res.firstOrNull() ?: return
            playEpisode(target, force = true)
            Log.i("PlayerViewModel", "switchDubber done -> ep=${target.position} src=${source.name}")
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("PlayerViewModel", "switchDubber failed", e)
        }
    }

    private fun pickUrl(links: Map<String, String>, quality: Int): String? =
        PlayerQuality.pickUrl(links, quality)
}