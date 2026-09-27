package com.anidesk.tv.feature.search

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.mvi.UiEffect
import com.anidesk.tv.core.mvi.UiEvent
import com.anidesk.tv.core.mvi.UiState

class SearchState {

    data class State(
        val query: String = "",
        val results: List<AnimeRelease> = emptyList(),
        val page: Int = 0,
        val hasMore: Boolean = false,
        val isLoading: Boolean = false,
        val isAppending: Boolean = false,
        val error: String? = null,
    ) : UiState

    sealed class Event : UiEvent {
        data class QueryChanged(val query: String) : Event()

        data object SearchRequested : Event()

        data class LoadNextPage(val query: String) : Event()

        data class ReleaseSelected(val release: AnimeRelease) : Event()

        data object Retry : Event()
    }

    sealed class Effect : UiEffect
}