package com.anidesk.tv.feature.bookmarks.navigator

import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.feature.bookmarks.IBookmarksNavigator
import javax.inject.Inject

class BookmarksNavigator @Inject constructor() : IBookmarksNavigator {
    override fun getBookmarksDest(): NavKey = BookmarksDestination
}