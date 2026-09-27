package com.anidesk.tv.feature.details

import com.anidesk.tv.core.mvi.UiEffect
import com.anidesk.tv.core.mvi.UiEvent
import com.anidesk.tv.core.mvi.UiState
import com.anidesk.tv.core.network.dto.Dubber
import com.anidesk.tv.core.network.dto.Episode
import com.anidesk.tv.core.network.dto.Release
import com.anidesk.tv.core.network.dto.Source

class DetailsState {

    data class State(
        val release: Release? = null,
        val posterUrl: String? = null,
        val title: String? = null,
        val dubbers: List<Dubber> = emptyList(),
        val selectedDubber: Int? = null,
        val sources: List<Source> = emptyList(),
        val selectedSource: Int? = null,
        val episodes: List<Episode> = emptyList(),
        val isLoading: Boolean = false,
        val isEpisodesLoading: Boolean = false,
        val error: String? = null,
    ) : UiState

    sealed class Event : UiEvent {
        data class DubberSelected(val dubberId: Int) : Event()

        data class SourceSelected(val sourceId: Int) : Event()

        data class EpisodeSelected(val episode: Episode) : Event()

        data object Retry : Event()
    }

    sealed class Effect : UiEffect
}