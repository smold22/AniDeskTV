package com.anidesk.tv.feature.search.navigator

import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.feature.search.ISearchNavigator
import javax.inject.Inject

class SearchNavigator @Inject constructor() : ISearchNavigator {
    override fun getSearchDest(): NavKey = SearchDestination
}