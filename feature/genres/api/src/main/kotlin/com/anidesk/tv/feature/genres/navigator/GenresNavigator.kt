package com.anidesk.tv.feature.genres.navigator

import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.feature.genres.IGenresNavigator
import javax.inject.Inject

class GenresNavigator @Inject constructor() : IGenresNavigator {
    override fun getGenresDest(): NavKey = GenresDestination
}