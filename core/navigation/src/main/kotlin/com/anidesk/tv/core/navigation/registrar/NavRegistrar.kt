package com.anidesk.tv.core.navigation.registrar

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.core.navigation.manager.INavigationManager

/**
 * Точка регистрации экранов фичи в общем `entryProvider`. Каждая фича реализует это через
 * Hilt multibinding (`@IntoSet`, см. [TvUi]).
 * `nav` типизирован интерфейсом [INavigationManager], а не конкретным `NavigationManager`
 * (тот `internal` в этом модуле).
 */
fun interface NavRegistrar {
    fun register(builder: EntryProviderScope<NavKey>, nav: INavigationManager)
}