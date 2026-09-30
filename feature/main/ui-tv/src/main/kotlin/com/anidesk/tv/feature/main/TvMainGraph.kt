package com.anidesk.tv.feature.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anidesk.tv.core.designsystem.baseScreen.ScreenNavigator
import com.anidesk.tv.core.designsystem.locals.LocalIsOffline
import com.anidesk.tv.core.designsystem.locals.LocalPosterCardSize
import com.anidesk.tv.core.designsystem.locals.LocalPosterQuality
import com.anidesk.tv.core.designsystem.theme.AniDeskTvTheme
import com.anidesk.tv.core.model.settings.ThemeMode
import com.anidesk.tv.core.navigation.host.AppNavHost
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.navigation.registrar.NavRegistrar
import com.anidesk.tv.core.navigation.registrar.TvUi
import com.anidesk.tv.core.navigation.root.RootTab
import com.anidesk.tv.core.network.connectivity.NetworkConnectivityMonitor
import com.anidesk.tv.core.preferences.session.SessionStore
import com.anidesk.tv.feature.main.api.MainGraph
import com.anidesk.tv.feature.main.model.TvMenuItem
import com.anidesk.tv.feature.main.view.TvMainScaffold
import com.anidesk.tvfeature.main.uitv.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvMainGraph @Inject constructor(
    private val navManager: INavigationManager,
    private val commonRegistrars: Set<@JvmSuppressWildcards NavRegistrar>,
    @param:TvUi private val tvRegistrars: Set<@JvmSuppressWildcards NavRegistrar>,
    private val networkConnectivityMonitor: NetworkConnectivityMonitor,
    private val sessionStore: SessionStore,
) : MainGraph {

    @Composable
    private fun rememberMenuItems(): List<TvMenuItem> {
        // «Закладки» и «Избранное» видны только после входа в аккаунт — они требуют токен.
        val token by sessionStore.token
            .collectAsStateWithLifecycle(initialValue = null)
        return buildList {
            add(TvMenuItem(R.string.main_tab_home, RootTab.HOME, Icons.Default.Home))
            add(TvMenuItem(R.string.main_tab_search, RootTab.SEARCH, Icons.Default.Search))
            add(TvMenuItem(R.string.main_tab_schedule, RootTab.SCHEDULE, Icons.Filled.CalendarMonth))
            add(TvMenuItem(R.string.main_tab_top, RootTab.TOP, Icons.Default.Star))
            add(TvMenuItem(R.string.main_tab_genres, RootTab.GENRES, Icons.Default.Category))
            if (token != null) {
                add(TvMenuItem(R.string.main_tab_bookmarks, RootTab.BOOKMARKS, Icons.Filled.Bookmarks))
                add(TvMenuItem(R.string.main_tab_favorites, RootTab.FAVORITES, Icons.Filled.Favorite))
            }
        }
    }

    @Composable
    override fun MainGraph() {
        val viewModel: MainViewModel = hiltViewModel()
        val inAppFlow = navManager.appBackStack.isNotEmpty()
        val atRoot = !inAppFlow && navManager.backStack.size <= 1
        val currentDestination = navManager.backStack.lastOrNull()
        val showMainMenu = atRoot
        val menuItems = rememberMenuItems()

        ScreenNavigator(viewModel) { state, effect, onEvent ->
            val isOnline by networkConnectivityMonitor.isOnline
                .collectAsStateWithLifecycle(initialValue = true)

            AniDeskTvTheme(
                isTelevision = true,
                darkTheme = state.themeMode == ThemeMode.DARK,
            ) {
                CompositionLocalProvider(
                    LocalPosterQuality provides state.posterQuality,
                    LocalPosterCardSize provides state.posterCardSize,
                    LocalIsOffline provides !isOnline,
                ) {
                    TvMainScaffold(
                        selectedRoot = navManager.currentRoot,
                        contentFocusKey = navManager.currentRoot to currentDestination,
                        menuItems = menuItems,
                        state = state,
                        showMainMenu = showMainMenu,
                        onEvent = onEvent,
                    ) {
                        AppNavHost(
                            navManager = navManager,
                            registrars = commonRegistrars + tvRegistrars,
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = {
                                fadeIn(tween(TV_NAV_TRANSITION_MILLIS)) +
                                        scaleIn(
                                            initialScale = TV_NAV_TRANSITION_SCALE,
                                            animationSpec = tween(TV_NAV_TRANSITION_MILLIS),
                                        ) togetherWith fadeOut(tween(TV_NAV_TRANSITION_MILLIS))
                            },
                            popTransitionSpec = {
                                fadeIn(tween(TV_NAV_TRANSITION_MILLIS)) togetherWith
                                        fadeOut(tween(TV_NAV_TRANSITION_MILLIS)) +
                                        scaleOut(
                                            targetScale = TV_NAV_TRANSITION_SCALE,
                                            animationSpec = tween(TV_NAV_TRANSITION_MILLIS),
                                        )
                            },
                        )
                    }
                }
            }
        }
    }
}

private const val TV_NAV_TRANSITION_MILLIS = 280
private const val TV_NAV_TRANSITION_SCALE = 1.05f