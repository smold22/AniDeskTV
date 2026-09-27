package com.anidesk.tv.feature.details.navigator

import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.feature.details.IDetailsNavigator
import javax.inject.Inject

class DetailsNavigator @Inject constructor() : IDetailsNavigator {
    override fun getDetailsDest(
        releaseId: Int,
        posterUrl: String?,
        title: String?,
    ): NavKey = DetailsDestination(
        releaseId = releaseId,
        posterUrl = posterUrl,
        title = title,
    )
}