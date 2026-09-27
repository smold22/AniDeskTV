package com.anidesk.tv.feature.details.navigator

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class DetailsDestination(
    val releaseId: Int,
    val posterUrl: String? = null,
    val title: String? = null,
) : NavKey