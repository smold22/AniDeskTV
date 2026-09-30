package com.anidesk.tv.feature.details

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.model.release.BookmarkStatus
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
        /** Релизы из связанной сущности тайтла (сиквел/предыстория) — отдельной ленивой строкой. */
        val relatedReleases: List<AnimeRelease> = emptyList(),
        /** 1-индексированный номер последней загруженной страницы связанных релизов (0 — не загружено). */
        val relatedPage: Int = 0,
        val isRelatedLoading: Boolean = false,
        /** Есть ли ещё страницы. API отдаёт `total_page_count` = 0, поэтому конец списка — пустая страница. */
        val hasMoreRelated: Boolean = false,
        val relatedError: String? = null,
        /** Есть ли аккаунт: закладки и избранное живут на сервере и требуют токен. */
        val isAuthorized: Boolean = false,
        /** Тайтл в избранном текущего пользователя. */
        val isFavorite: Boolean = false,
        /** Сколько человек отметили тайтл избранным (счётчик с сервера, а не «моё» состояние). */
        val favoritesCount: Int = 0,
        /** Статус пользовательского списка, в котором лежит тайтл. */
        val bookmarkStatus: BookmarkStatus = BookmarkStatus.NONE,
        /** Идёт запрос к спискам закладок/избранного — блокирует повторное нажатие. */
        val isLibraryUpdating: Boolean = false,
    ) : UiState

    sealed class Event : UiEvent {
        data class DubberSelected(val dubberId: Int) : Event()

        data class SourceSelected(val sourceId: Int) : Event()

        data class EpisodeSelected(val episode: Episode) : Event()

        /** Переключить тайтл в избранном. */
        data object FavoriteClicked : Event()

        /** Добавить тайтл в выбранный список закладок или убрать из него ([BookmarkStatus.NONE]). */
        data class BookmarkStatusSelected(val status: BookmarkStatus) : Event()

        /** Подгрузить следующую страницу связанных релизов. */
        data object LoadMoreRelated : Event()

        /** Выбрана карточка связанного релиза. */
        data class RelatedReleaseSelected(val release: AnimeRelease) : Event()

        data object Retry : Event()
    }

    sealed class Effect : UiEffect {
        /** Короткое уведомление поверх экрана: тайтл добавлен в список [status]. */
        data class BookmarkAdded(val status: BookmarkStatus) : Effect()

        /** Короткое уведомление: тайтл убран из списка закладок. */
        data object BookmarkRemoved : Effect()

        /** Короткое уведомление: избранное переключено на [isFavorite]. */
        data class FavoriteChanged(val isFavorite: Boolean) : Effect()

        /** Не удалось изменить закладки/избранное — состояние откатилось. */
        data object LibraryActionFailed : Effect()
    }
}
