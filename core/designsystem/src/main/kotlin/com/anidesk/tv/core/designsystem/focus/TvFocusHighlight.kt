package com.anidesk.tv.core.designsystem.focus

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Подсветка фокуса заливкой фона (как в читалке «Новостей»): вместо цветной рамки
 * рисует фон [focusedColor] на сфокусированном элементе. Годится и для кликабельных
 * (передать [onClick]), и для некликабельных фокус-стопов (текст/заголовки).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.tvFocusHighlight(
    shape: Shape = RoundedCornerShape(12.dp),
    focusedColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: (() -> Unit)? = null,
): Modifier {
    val focused by interactionSource.collectIsFocusedAsState()
    val clickOrFocus = if (onClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier.focusable(interactionSource = interactionSource)
    }
    return this
        .clip(shape)
        .background(if (focused) focusedColor else Color.Transparent)
        .then(clickOrFocus)
        .padding(contentPadding)
}
