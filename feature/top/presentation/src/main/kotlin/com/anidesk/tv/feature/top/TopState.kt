package com.anidesk.tv.feature.top

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.mvi.UiEffect
import com.anidesk.tv.core.mvi.UiEvent
import com.anidesk.tv.core.mvi.UiState
import com.anidesk.tv.core.network.dto.Type

class TopState {

    data class State(
        val types: List<Type> = emptyList(),
        val selectedTypes: List<Int> = emptyList(),
        val sortIndex: Int = 0,
        val directionAsc: Boolean = false,
        val items: List<AnimeRelease> = emptyList(),
        val page: Int = 0,
        val isLoading: Boolean = false,
        val isAppending: Boolean = false,
        val hasMore: Boolean = true,
        val error: String? = null,
    ) : UiState

    /** Пользовательские действия на экране «Топ». */
    sealed class Event : UiEvent {
        /** Тип релиза (Сериал/Фильм/OVA...) переключён в фильтре. */
        data class TypeToggled(val typeId: Int) : Event()

        /** Выбрана сортировка (0 — лучшие, 1 — популярные, 2 — новые). */
        data class SortChanged(val sortIndex: Int) : Event()

        /** Переключение направления сортировки (по возрастанию/убыванию). */
        data object DirectionToggled : Event()

        /** Подгрузить следующую страницу каталога. */
        data object LoadNextPage : Event()

        /** Выбрана карточка релиза. */
        data class ReleaseSelected(val release: AnimeRelease) : Event()

        /** Повторная загрузка первой страницы после ошибки. */
        data object Retry : Event()
    }

    sealed class Effect : UiEffect
}