package com.anidesk.tv.feature.main

import com.anidesk.tv.core.model.settings.PosterCardSize
import com.anidesk.tv.core.model.settings.PosterQuality
import com.anidesk.tv.core.model.settings.ThemeMode
import com.anidesk.tv.core.mvi.UiEffect
import com.anidesk.tv.core.mvi.UiEvent
import com.anidesk.tv.core.mvi.UiState
import com.anidesk.tv.core.navigation.root.RootTab

class MainState {

    data class State(
        val posterQuality: PosterQuality = PosterQuality.STANDARD,
        val posterCardSize: PosterCardSize = PosterCardSize.LARGE,
        val themeMode: ThemeMode = ThemeMode.DARK,
    ) : UiState

    /** Пользовательские действия в корневом контейнере приложения. */
    sealed class Event : UiEvent {
        /** Корневая TV вкладка выбрана из меню. */
        data class TvRootSelected(val root: RootTab) : Event()
    }

    sealed class Effect : UiEffect
}