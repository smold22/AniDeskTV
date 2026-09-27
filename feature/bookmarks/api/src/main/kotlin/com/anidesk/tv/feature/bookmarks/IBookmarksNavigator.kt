package com.anidesk.tv.feature.bookmarks

import androidx.navigation3.runtime.NavKey

interface IBookmarksNavigator {
    fun getBookmarksDest(): NavKey
}