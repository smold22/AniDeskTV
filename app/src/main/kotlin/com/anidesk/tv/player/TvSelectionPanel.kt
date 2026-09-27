package com.anidesk.tv.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SelectionPanelType { QUALITY, SPEED, EPISODES, DUBBERS }

// Локальные константы палитры панели выбора (соответствуют тёмным значениям оригинала AniDesk,
// панель всегда тёмная поверх видео независимо от темы приложения).
private val Carmine = Color(0xFFFFB86B)
private val AltBackground = Color(0xFF14171C)
private val MainText = Color(0xFFF4F1EA)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TvSelectionPanel(
    type: SelectionPanelType,
    data: PlayerController.PlayerData?,
    playbackSpeedIndex: Int,
    onSelect: (SelectionPanelType, Long) -> Unit,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)

    val title = when (type) {
        SelectionPanelType.QUALITY -> "Качество"
        SelectionPanelType.SPEED -> "Скорость"
        SelectionPanelType.EPISODES -> "Серии"
        SelectionPanelType.DUBBERS -> "Озвучка"
    }

    val items: List<Pair<Long, String>> = when (type) {
        SelectionPanelType.QUALITY -> PlayerQuality.labels(data?.links.orEmpty()).map { it.first.toLong() to it.second }
        SelectionPanelType.SPEED -> SPEED_OPTIONS_LABELS.mapIndexed { index, label -> index.toLong() to label }
        SelectionPanelType.EPISODES -> data?.episodes.orEmpty().map { it.position.toLong() to it.name }
        SelectionPanelType.DUBBERS -> data?.dubbers.orEmpty().map { it.id.toLong() to it.name }
    }

    val selectedId: Long = when (type) {
        SelectionPanelType.QUALITY -> (data?.currentQuality ?: 0).toLong()
        SelectionPanelType.SPEED -> playbackSpeedIndex.toLong()
        SelectionPanelType.EPISODES -> data?.currentEpisode?.position?.toLong() ?: -1L
        SelectionPanelType.DUBBERS -> (data?.currentDubberId ?: 0).toLong()
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(type, data, playbackSpeedIndex) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = 920.dp)
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, MainText.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                .background(AltBackground.copy(alpha = 0.96f), RoundedCornerShape(18.dp))
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MainText,
                )
                SelectionChip("Закрыть", selected = false, onClick = onClose)
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items.forEach { (id, label) ->
                    SelectionChip(
                        label = label,
                        selected = id == selectedId,
                        onClick = { onSelect(type, id) },
                        modifier = if (id == selectedId) Modifier.focusRequester(focusRequester) else Modifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = when {
                    focused -> Carmine
                    selected -> Carmine.copy(alpha = 0.6f)
                    else -> MainText.copy(alpha = 0.25f)
                },
                shape = shape,
            )
            .background(
                when {
                    focused -> Carmine
                    selected -> Carmine.copy(alpha = 0.15f)
                    else -> Color.Transparent
                },
                shape,
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected || focused) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                focused -> Color.White
                selected -> Carmine
                else -> MainText
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val SPEED_OPTIONS_LABELS = listOf(
    "0.25x", "0.5x", "0.75x", "1x", "1.25x", "1.5x", "1.75x", "2x", "2.5x", "3x",
)