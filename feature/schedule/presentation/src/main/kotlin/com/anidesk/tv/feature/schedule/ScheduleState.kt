package com.anidesk.tv.feature.schedule

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.mvi.UiEffect
import com.anidesk.tv.core.mvi.UiEvent
import com.anidesk.tv.core.mvi.UiState

class ScheduleState {

    data class State(
        val selectedDay: Int = 0,
        val days: List<List<AnimeRelease>> = List(DAY_COUNT) { emptyList() },
        val isLoading: Boolean = false,
        val error: String? = null,
    ) : UiState

    sealed class Event : UiEvent {
        data class DaySelected(val day: Int) : Event()
        data class AnimeSelected(val release: AnimeRelease) : Event()
        data object Retry : Event()
    }

    sealed class Effect : UiEffect

    companion object {
        const val DAY_COUNT = 7
    }
}