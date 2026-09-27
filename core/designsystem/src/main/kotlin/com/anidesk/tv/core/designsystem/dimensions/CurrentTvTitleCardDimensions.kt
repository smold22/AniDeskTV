package com.anidesk.tv.core.designsystem.dimensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anidesk.tv.core.designsystem.locals.LocalPosterCardSize
import com.anidesk.tv.core.model.settings.PosterCardSize

@Composable
fun currentTvTitleCardDimensions(): PosterCardDimensions =
    LocalPosterCardSize.current.tvTitleCardDimensions

private val PosterCardSize.tvTitleCardDimensions: PosterCardDimensions
    get() = when (this) {
        PosterCardSize.COMPACT -> posterCardDimensions(width = 168.dp)
        PosterCardSize.STANDARD -> posterCardDimensions(width = 200.dp)
        PosterCardSize.LARGE -> posterCardDimensions(width = 232.dp)
    }

private fun posterCardDimensions(width: Dp): PosterCardDimensions = PosterCardDimensions(
    width = width,
    posterHeight = width / TITLE_POSTER_ASPECT_RATIO,
)
