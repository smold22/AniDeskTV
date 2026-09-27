package com.anidesk.tv.core.designsystem.dimensions

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object TvScreenPadding {
    val Horizontal = 32.dp
    val Vertical = 32.dp
}

object TvCardSpacing {
    val Horizontal = 12.dp
    val Vertical = 12.dp
}

const val TITLE_POSTER_ASPECT_RATIO = 570f / 800f

data class PosterCardDimensions(
    val width: Dp,
    val posterHeight: Dp,
)
