package com.anidesk.tv.feature.favorites

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.mvi.UiEffect
import com.anidesk.tv.core.mvi.UiEvent
import com.anidesk.tv.core.mvi.UiState

class FavoritesState {

    data class State(
        val items: List<AnimeRelease> = emptyList(),
        val page: Int = 0,
        val isLoading: Boolean = false,
        val isAppending: Boolean = false,
        val hasMore: Boolean = true,
        val error: String? = null,
    ) : UiState

    /** Пользовательские действия на экране «Избранное». */
    sealed class Event : UiEvent {
        /** Подгрузить следующую страницу избранного. */
        data object LoadNextPage : Event()

        /** Выбрана карточка релиза. */
        data class ReleaseSelected(val release: AnimeRelease) : Event()

        /** Повторная загрузка первой страницы после ошибки. */
        data object Retry : Event()
    }

    sealed class Effect : UiEffect
}
