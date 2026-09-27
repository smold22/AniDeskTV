package com.anidesk.tv.di

import androidx.navigation3.runtime.NavKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import com.anidesk.tv.core.navigation.player.IPlayerLauncher
import com.anidesk.tv.core.navigation.registrar.NavRegistrar
import com.anidesk.tv.core.navigation.registrar.TvUi
import com.anidesk.tv.core.navigation.root.RootTab
import com.anidesk.tv.feature.details.IDetailsNavigator
import com.anidesk.tv.feature.details.tv.navigator.DetailsNavRegistrar
import com.anidesk.tv.feature.genres.IGenresNavigator
import com.anidesk.tv.feature.genres.tv.navigator.GenresNavRegistrar
import com.anidesk.tv.feature.bookmarks.IBookmarksNavigator
import com.anidesk.tv.feature.bookmarks.tv.navigator.BookmarksNavRegistrar
import com.anidesk.tv.feature.home.IHomeNavigator
import com.anidesk.tv.feature.home.tv.navigator.HomeNavRegistrar

import com.anidesk.tv.feature.schedule.IScheduleNavigator
import com.anidesk.tv.feature.schedule.tv.navigator.ScheduleNavRegistrar
import com.anidesk.tv.feature.search.ISearchNavigator
import com.anidesk.tv.feature.search.tv.navigator.SearchNavRegistrar
import com.anidesk.tv.feature.settings.ISettingsNavigator
import com.anidesk.tv.feature.settings.tv.navigator.SettingsNavRegistrar
import com.anidesk.tv.feature.top.ITopNavigator
import com.anidesk.tv.feature.top.tv.navigator.TopNavRegistrar
import javax.inject.Singleton

/**
 * Собирает навигаторы и TV-регистраторы экранов всех фич в общий Hilt-граф.
 */
@Module
@InstallIn(SingletonComponent::class)
interface TvNavigationModule {

    @Binds
    @Singleton
    fun bindHomeNavigator(impl: com.anidesk.tv.feature.home.navigator.HomeNavigator): IHomeNavigator

    @Binds
    @Singleton
    fun bindDetailsNavigator(
        impl: com.anidesk.tv.feature.details.navigator.DetailsNavigator,
    ): IDetailsNavigator

    @Binds
    @Singleton
    fun bindPlayerLauncher(
        impl: com.anidesk.tv.player.PlayerLauncher,
    ): IPlayerLauncher

    @Binds
    @Singleton
    fun bindSearchNavigator(
        impl: com.anidesk.tv.feature.search.navigator.SearchNavigator,
    ): ISearchNavigator

    @Binds
    @Singleton
    fun bindTopNavigator(impl: com.anidesk.tv.feature.top.navigator.TopNavigator): ITopNavigator

    @Binds
    @Singleton
    fun bindGenresNavigator(
        impl: com.anidesk.tv.feature.genres.navigator.GenresNavigator,
    ): IGenresNavigator

    @Binds
    @Singleton
    fun bindBookmarksNavigator(
        impl: com.anidesk.tv.feature.bookmarks.navigator.BookmarksNavigator,
    ): IBookmarksNavigator

    @Binds
    @Singleton
    fun bindScheduleNavigator(
        impl: com.anidesk.tv.feature.schedule.navigator.ScheduleNavigator,
    ): IScheduleNavigator

    @Binds
    @Singleton
    fun bindSettingsNavigator(
        impl: com.anidesk.tv.feature.settings.navigator.SettingsNavigator,
    ): ISettingsNavigator

    @Binds
    @IntoSet
    @TvUi
    fun bindTvHomeNavRegistrar(impl: HomeNavRegistrar): NavRegistrar

    @Binds
    @IntoSet
    @TvUi
    fun bindTvDetailsNavRegistrar(impl: DetailsNavRegistrar): NavRegistrar

    @Binds
    @IntoSet
    @TvUi
    fun bindTvSearchNavRegistrar(impl: SearchNavRegistrar): NavRegistrar

    @Binds
    @IntoSet
    @TvUi
    fun bindTvTopNavRegistrar(impl: TopNavRegistrar): NavRegistrar

    @Binds
    @IntoSet
    @TvUi
    fun bindTvGenresNavRegistrar(impl: GenresNavRegistrar): NavRegistrar

    @Binds
    @IntoSet
    @TvUi
    fun bindTvBookmarksNavRegistrar(impl: BookmarksNavRegistrar): NavRegistrar

    @Binds
    @IntoSet
    @TvUi
    fun bindTvScheduleNavRegistrar(impl: ScheduleNavRegistrar): NavRegistrar

    @Binds
    @IntoSet
    @TvUi
    fun bindTvSettingsNavRegistrar(impl: SettingsNavRegistrar): NavRegistrar

    companion object {
        @Provides
        fun provideCommonNavRegistrars(): Set<@JvmSuppressWildcards NavRegistrar> = emptySet()

        @Provides
        fun provideRootTabs(
            searchNav: ISearchNavigator,
            homeNav: IHomeNavigator,
            scheduleNav: IScheduleNavigator,
            topNav: ITopNavigator,
            genresNav: IGenresNavigator,
            bookmarksNav: IBookmarksNavigator,
            settingsNav: ISettingsNavigator,
        ): @JvmSuppressWildcards Map<RootTab, NavKey> = mapOf(
            RootTab.SEARCH to searchNav.getSearchDest(),
            RootTab.HOME to homeNav.getHomeDest(),
            RootTab.SCHEDULE to scheduleNav.getScheduleDest(),
            RootTab.TOP to topNav.getTopDest(),
            RootTab.GENRES to genresNav.getGenresDest(),
            RootTab.BOOKMARKS to bookmarksNav.getBookmarksDest(),
            RootTab.SETTINGS to settingsNav.getSettingsDest(),
        )
    }
}