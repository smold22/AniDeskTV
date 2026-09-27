package com.anidesk.tv.core.designsystem.dimensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anidesk.tv.core.designsystem.locals.LocalPosterCardSize
import com.anidesk.tv.core.model.settings.PosterCardSize

@Composable
fun currentTvHomeFeedCardDimensions(): PosterCardDimensions =
    LocalPosterCardSize.current.tvHomeFeedCardDimensions

private val PosterCardSize.tvHomeFeedCardDimensions: PosterCardDimensions
    get() = when (this) {
        PosterCardSize.COMPACT -> posterCardDimensions(width = 176.dp)
        PosterCardSize.STANDARD -> posterCardDimensions(width = 208.dp)
        PosterCardSize.LARGE -> posterCardDimensions(width = 240.dp)
    }

private fun posterCardDimensions(width: Dp): PosterCardDimensions = PosterCardDimensions(
    width = width,
    posterHeight = width / TITLE_POSTER_ASPECT_RATIO,
)
