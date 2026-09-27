package com.anidesk.tv.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.anidesk.tvcore.designsystem.R

@Composable
fun AppBrandIcon(
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(R.drawable.ic_anidesk_brand),
        contentDescription = null,
        modifier = modifier,
    )
}
