package com.anidesk.tv.core.navigation.manager

import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.core.navigation.root.RootTab

/**
 * Узкая, немутабельная граница [NavigationManager] для потребителей вне модуля навигации,
 * которым не нужен доступ к internal wiring (см. [NavigationManager.attachBackStacks]).
 */
interface INavigationManager {
    val backStack: List<NavKey>
    val appBackStack: List<NavKey>
    val currentRoot: RootTab
    val roots: Map<RootTab, NavKey>

    fun navigate(dest: NavKey)
    fun navigateApp(dest: NavKey)
    fun replace(dest: NavKey)
    fun back()
    fun backTwo()
    fun popBackTo(dest: NavKey, inclusive: Boolean = false)
    fun popToRoot()
    fun switchRoot(root: RootTab, reselectPopToRoot: Boolean = true)
    fun restoreRoot(root: RootTab)
    fun replaceRoot(root: RootTab, dest: NavKey)
    fun resetAllRoots()
    fun stack(root: RootTab): List<NavKey>
}