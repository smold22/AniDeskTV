package com.anidesk.tv.core.designsystem.components

import androidx.compose.ui.graphics.Color
import com.anidesk.tv.core.designsystem.theme.AniDeskSemanticColors

fun Double.toRatingColor(): Color = when {
    this < 3.0 -> AniDeskSemanticColors.RatingBadgeLow
    this < 4.0 -> AniDeskSemanticColors.StatusPostponed
    else -> AniDeskSemanticColors.RatingBadgeHigh
}
