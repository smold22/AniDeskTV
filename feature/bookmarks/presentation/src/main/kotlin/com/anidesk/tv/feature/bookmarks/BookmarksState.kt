package com.anidesk.tv.feature.bookmarks

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.mvi.UiEffect
import com.anidesk.tv.core.mvi.UiEvent
import com.anidesk.tv.core.mvi.UiState

class BookmarksState {

    data class State(
        /** Тип списка закладок (1—5) или null — история просмотра. */
        val selectedTabType: Int? = null,
        val items: List<AnimeRelease> = emptyList(),
        val page: Int = 0,
        val isLoading: Boolean = false,
        val isAppending: Boolean = false,
        val hasMore: Boolean = true,
        val error: String? = null,
    ) : UiState

    /** Пользовательские действия на экране «Закладки». */
    sealed class Event : UiEvent {
        /** Переключена вкладка закладок (null — история). */
        data class TabSelected(val tabType: Int?) : Event()

        /** Подгрузить следующую страницу списка. */
        data object LoadNextPage : Event()

        /** Выбрана карточка релиза. */
        data class ReleaseSelected(val release: AnimeRelease) : Event()

        /** Повторная загрузка первой страницы после ошибки. */
        data object Retry : Event()
    }

    sealed class Effect : UiEffect
}