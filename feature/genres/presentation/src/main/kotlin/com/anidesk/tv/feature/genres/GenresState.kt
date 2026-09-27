package com.anidesk.tv.feature.genres

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.mvi.UiEffect
import com.anidesk.tv.core.mvi.UiEvent
import com.anidesk.tv.core.mvi.UiState

class GenresState {

    data class State(
        /** Доступные жанры (имена, с учётом каталога Anixart). */
        val genres: List<String> = GenresCatalog.all,
        /** Выбранный жанр (имя), null — все релизы. */
        val selectedGenre: String? = null,
        val items: List<AnimeRelease> = emptyList(),
        val page: Int = 0,
        val isLoading: Boolean = false,
        val isAppending: Boolean = false,
        val hasMore: Boolean = true,
        val error: String? = null,
    ) : UiState

    /** Пользовательские действия на экране «Жанры». */
    sealed class Event : UiEvent {
        /** Жанр переключён в фильтре (повторный тап снимает выбор). */
        data class GenreSelected(val genreName: String) : Event()

        /** Сброшен фильтр жанра («Все»). */
        data object SelectAll : Event()

        /** Подгрузить следующую страницу каталога. */
        data object LoadNextPage : Event()

        /** Выбрана карточка релиза. */
        data class ReleaseSelected(val release: AnimeRelease) : Event()

        /** Повторная загрузка первой страницы после ошибки. */
        data object Retry : Event()
    }

    sealed class Effect : UiEffect
}