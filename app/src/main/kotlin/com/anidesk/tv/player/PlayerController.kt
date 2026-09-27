package com.anidesk.tv.player

import kotlinx.coroutines.flow.MutableStateFlow
import com.anidesk.tv.core.network.dto.Dubber
import com.anidesk.tv.core.network.dto.Episode
import com.anidesk.tv.core.network.dto.Source

class PlayerController {

    val data = MutableStateFlow<PlayerData?>(null)

    var selectedDubber: Dubber? = null

    /** Текущая позиция воспроизведения (обновляется плеером) */
    var currentPositionMs: Long = 0L

    /** Интервал пропуска, мс (60_000 / 85_000 / 110_000) — обновляется из SettingsStore */
    val skipIntervalMs = MutableStateFlow(85_000L)

    val selectEpisodeRelay = EventFlow<EpisodeId>()

    val selectQualityRelay = EventFlow<Int>()

    val selectDubberRelay = EventFlow<Dubber>()

    fun reset() {
        data.value = null
    }

    data class PlayerData(
        val releaseId: Int,
        val episodes: List<Episode>,
        val dubbers: List<Dubber>,
        val sources: List<Source>,
        val currentEpisode: Episode?,
        val currentDubberId: Int = 0,
        val currentSourceId: Int,
        val links: Map<String, String>,
        val currentQuality: Int,
    ) {
        val selection: PlayerSelection
            get() = PlayerSelection(
                releaseId = releaseId,
                dubberId = currentDubberId,
                sourceId = currentSourceId,
                sourceName = "",
                episodePosition = currentEpisode?.position ?: 0,
                quality = currentQuality,
            )
    }

    data class EpisodeId(
        val releaseId: Int,
        val sourceId: Int,
        val position: Int,
    )
}