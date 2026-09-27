package com.anidesk.tv.core.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.anidesk.tv.core.designsystem.components.MarqueeTitleText
import com.anidesk.tv.core.designsystem.dimensions.TITLE_POSTER_ASPECT_RATIO
import com.anidesk.tv.core.designsystem.dimensions.currentTvTitleCardDimensions
import com.anidesk.tv.core.designsystem.focus.tvFocusableClick

@Composable
fun TvTitleCard(
    title: String,
    posterUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    caption: String? = null,
    onFocused: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    width: Dp? = null,
    centerInCell: Boolean = false,
    posterOverlay: @Composable (BoxScope.() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    val cardDimensions = currentTvTitleCardDimensions()
    val cardWidth = width ?: cardDimensions.width
    val posterHeight = width?.div(TITLE_POSTER_ASPECT_RATIO) ?: cardDimensions.posterHeight
    val textBlockHeight = with(LocalDensity.current) {
        val titleLineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        val titleLh = if (titleLineHeight.value == 0f) {
            MaterialTheme.typography.bodyLarge.fontSize * 1.5f
        } else {
            titleLineHeight
        }
        val metaLineHeight = MaterialTheme.typography.bodySmall.lineHeight
        val metaLh = if (metaLineHeight.value == 0f) {
            MaterialTheme.typography.bodySmall.fontSize * 1.5f
        } else {
            metaLineHeight
        }
        val captionLines = if (caption.isNullOrBlank()) 0f else 2f
        (titleLh * 2f).toDp() + (metaLh * (1f + captionLines)).toDp() + 20.dp
    }
    var isFocused by remember { mutableStateOf(false) }

    val card: @Composable () -> Unit = {
        Card(
            modifier = modifier
                .width(cardWidth)
                .onFocusChanged { focusState ->
                    val focused = focusState.isFocused || focusState.hasFocus
                    if (focused && !isFocused) onFocused()
                    isFocused = focused
                }
                .tvFocusableClick(onClick = onClick, shape = shape, onLongClick = onLongClick),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(posterHeight)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    val imageUrl = posterUrl

                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )

                    if (imageUrl == null) {
                        Text(
                            text = title.take(1),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f),
                        )
                    }
                    posterOverlay?.invoke(this)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(textBlockHeight)
                        .padding(10.dp),
                ) {
                    MarqueeTitleText(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        minLines = 2,
                        maxLines = 2,
                        isFocused = isFocused,
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (!caption.isNullOrBlank()) {
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            // Отсчёт до серии в одну строку карточки не влезает.
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }

    // В сетках ячейка шире карточки (GridCells.Adaptive тянет её на всю ширину),
    // поэтому карточку по центру — иначе справа остаётся дыра.
    if (centerInCell) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            card()
        }
    } else {
        card()
    }
}
