package com.anidesk.tv.feature.search

import androidx.navigation3.runtime.NavKey

interface ISearchNavigator {
    fun getSearchDest(): NavKey
}