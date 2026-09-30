package com.anidesk.tv.feature.favorites.navigator

import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.feature.favorites.IFavoritesNavigator
import javax.inject.Inject

class FavoritesNavigator @Inject constructor() : IFavoritesNavigator {
    override fun getFavoritesDest(): NavKey = FavoritesDestination
}
