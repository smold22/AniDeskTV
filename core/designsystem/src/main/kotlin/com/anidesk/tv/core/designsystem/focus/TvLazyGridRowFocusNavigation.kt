package com.anidesk.tv.core.designsystem.focus

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Страховка для DPAD-вниз в вертикальном гриде: переход на ряд ниже с сохранением колонки.
 *
 * Штатный focus search Compose, упираясь в ещё не скомпонованный ряд, подтягивает beyond-bounds
 * ровно один элемент (`addNextInterval` двигает границу на один индекс) и отдаёт фокус первому же
 * найденному — то есть первой карточке ряда: с 3-й карточки фокус прыгает на 1-ю.
 *
 * Штатно этого не происходит, пока грид использует [TvPivotedGridBringIntoViewSpec]: ряд всегда
 * встаёт на 12% от верхней кромки, и под ним заведомо остаётся начало следующего ряда. Модификатор
 * нужен на случай, когда ряд ещё не доехал до пивота — например при удержании DPAD, когда события
 * приходят быстрее анимации скролла. Вверх такой страховки не требуется: ряд над сфокусированным
 * при пивоте всегда частично виден, а значит скомпонован.
 *
 * Если целевая карточка уже скомпонована, событие не перехватывается — работает обычный поиск
 * фокуса с анимированным bringIntoView.
 *
 * @param index индекс карточки в данных (не в lazy-списке).
 * @param columnCount число колонок грида — на столько сдвигается индекс за один ряд.
 * @param itemCount общее число карточек в данных.
 * @param focusRequesterAt requester карточки по её индексу в данных.
 * @param lazyIndexOffset сдвиг индекса данных до индекса в lazy-списке (шапка, спаны и т.п.).
 */
fun Modifier.tvLazyGridRowFocusNavigation(
    index: Int,
    columnCount: Int,
    itemCount: Int,
    gridState: LazyGridState,
    scope: CoroutineScope,
    focusRequesterAt: (Int) -> FocusRequester?,
    lazyIndexOffset: Int = 0,
): Modifier = onPreviewKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
    if (event.key != Key.DirectionDown) return@onPreviewKeyEvent false
    val targetIndex = index + columnCount
    if (targetIndex >= itemCount) return@onPreviewKeyEvent false
    val targetLazyIndex = targetIndex + lazyIndexOffset
    if (gridState.isItemComposed(targetLazyIndex)) return@onPreviewKeyEvent false
    val targetFocusRequester = focusRequesterAt(targetIndex) ?: return@onPreviewKeyEvent false

    scope.launch {
        // Отрицательный scrollOffset ставит ряд не вплотную к верхней кромке, а на тот же пивот,
        // куда его довёл бы TvPivotedGridBringIntoViewSpec — иначе переход выглядел бы рывком.
        gridState.scrollToItem(
            index = targetLazyIndex,
            scrollOffset = -gridState.focusPivotOffsetPx(),
        )
        snapshotFlow { gridState.isItemComposed(targetLazyIndex) }.first { it }
        requestFocusUntilTimeout(targetFocusRequester)
    }
    true
}

private fun LazyGridState.isItemComposed(lazyIndex: Int): Boolean =
    layoutInfo.visibleItemsInfo.any { it.index == lazyIndex }

private fun LazyGridState.focusPivotOffsetPx(): Int =
    (layoutInfo.viewportSize.height * FocusedItemPivotFraction).toInt()
