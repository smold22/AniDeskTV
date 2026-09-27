package com.anidesk.tv.core.navigation.di

import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.core.navigation.manager.INavigationManager
import com.anidesk.tv.core.navigation.manager.NavigationManager
import com.anidesk.tv.core.navigation.root.RootTab
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NavManagerDiModule {

    @Provides
    fun provideInitialRoot(): RootTab = RootTab.HOME

    @Provides
    @Singleton
    internal fun provideNavigationManager(
        roots: @JvmSuppressWildcards Map<RootTab, NavKey>,
        initialRoot: RootTab,
    ): NavigationManager = NavigationManager(roots = roots, initialRoot = initialRoot)

    @Provides
    @Singleton
    internal fun provideINavigationManager(navigationManager: NavigationManager): INavigationManager =
        navigationManager
}