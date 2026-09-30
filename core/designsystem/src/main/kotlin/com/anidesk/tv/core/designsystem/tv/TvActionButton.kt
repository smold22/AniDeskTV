package com.anidesk.tv.core.designsystem.tv

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anidesk.tv.core.designsystem.focus.tvFocusableClick

private val ActionButtonShape = RoundedCornerShape(10.dp)

/**
 * ТВ-кнопка действия: прямоугольная (в отличие от pill-чипа [TvChip]), с иконкой и
 * активным состоянием — для переключателей вроде «в избранное». Фокус виден по заливке
 * `primary` так же, как у чипа, а активное состояние дополнительно красит иконку
 * в [activeTint].
 */
@Composable
fun TvActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    active: Boolean = false,
    activeTint: Color = MaterialTheme.colorScheme.primary,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val containerColor by animateColorAsState(
        targetValue = when {
            focused -> MaterialTheme.colorScheme.primary
            active -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "TvActionButtonContainer",
    )
    val contentColor = when {
        focused -> MaterialTheme.colorScheme.onPrimary
        active -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .background(containerColor, ActionButtonShape)
            .tvFocusableClick(
                onClick = onClick,
                shape = ActionButtonShape,
                interactionSource = interactionSource,
                focusedBorderColor = Color.Transparent,
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = when {
                    focused -> contentColor
                    active -> activeTint
                    else -> contentColor
                },
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (focused || active) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
