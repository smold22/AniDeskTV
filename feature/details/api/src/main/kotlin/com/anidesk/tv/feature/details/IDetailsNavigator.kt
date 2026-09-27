package com.anidesk.tv.feature.details

import androidx.navigation3.runtime.NavKey

interface IDetailsNavigator {
    fun getDetailsDest(
        releaseId: Int,
        posterUrl: String? = null,
        title: String? = null,
    ): NavKey
}