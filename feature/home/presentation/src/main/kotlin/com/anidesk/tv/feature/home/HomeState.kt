package com.anidesk.tv.feature.home

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.model.release.ContinueWatchingEntry
import com.anidesk.tv.core.mvi.UiEffect
import com.anidesk.tv.core.mvi.UiEvent
import com.anidesk.tv.core.mvi.UiState

class HomeState {

    data class State(
        val continueWatching: List<ContinueWatchingEntry> = emptyList(),
        val watching: List<AnimeRelease> = emptyList(),
        val recommendations: List<AnimeRelease> = emptyList(),
        val updates: List<AnimeRelease> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
    ) : UiState

    sealed class Event : UiEvent {
        data class ReleaseSelected(val release: AnimeRelease) : Event()

        data class ContinueWatchingSelected(val entry: ContinueWatchingEntry) : Event()

        data class RemoveContinueWatching(val releaseId: Int) : Event()

        data object Retry : Event()
    }

    sealed class Effect : UiEffect
}