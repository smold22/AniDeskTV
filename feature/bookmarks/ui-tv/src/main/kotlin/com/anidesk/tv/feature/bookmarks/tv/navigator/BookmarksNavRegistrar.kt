package com.anidesk.tv.feature.bookmarks.tv.navigator

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.core.designsystem.baseScreen.ScreenNavigator
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.navigation.registrar.NavRegistrar
import com.anidesk.tv.feature.bookmarks.BookmarksTvScreen
import com.anidesk.tv.feature.bookmarks.BookmarksViewModel
import com.anidesk.tv.feature.bookmarks.navigator.BookmarksDestination
import javax.inject.Inject

class BookmarksNavRegistrar @Inject constructor() : NavRegistrar {

    override fun register(builder: EntryProviderScope<NavKey>, nav: INavigationManager) {
        builder.entry<BookmarksDestination> {
            val viewModel: BookmarksViewModel = hiltViewModel()
            ScreenNavigator(viewModel) { state, effect, onEvent ->
                BookmarksTvScreen(
                    state = state,
                    effect = effect,
                    onEvent = onEvent,
                )
            }
        }
    }
}