package com.anidesk.tv.core.designsystem.focus

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import kotlin.math.abs

/**
 * Стейтлес BringIntoViewSpec для TV-фокуса — чистая функция от (offset, size, containerSize),
 * без общего изменяемого состояния между вызовами (общий mutable object однажды уже ломал
 * скролл по всему приложению).
 *
 * [skipIfFullyVisible] — не скроллить, если элемент уже целиком помещается в контейнер; нужно,
 * чтобы переход фокуса поперёк оси скролла (например вбок внутри уже видимого ряда грида) не
 * давал лишний "подскролл".
 *
 * [centered] — если true, элемент центрируется: target = (containerSize - size) / 2, что
 * корректно учитывает размер самого элемента (в отличие от pivotFraction = 0.5f, который тянул
 * бы leading edge элемента к середине контейнера, а не центрировал бы сам элемент). Если false,
 * используется [pivotFraction] от leading edge контейнера.
 *
 * [toleranceFraction] — доля размера элемента, в пределах которой промах мимо цели считается
 * нулевым. Нужна там, где выключен [skipIfFullyVisible], чтобы лёгкий «недоскролл» до пивота
 * не превращался в постоянное дёрганье при переходах вбок.
 */
@OptIn(ExperimentalFoundationApi::class)
class TvPivotBringIntoViewSpec(
    private val skipIfFullyVisible: Boolean = true,
    private val centered: Boolean = false,
    private val pivotFraction: Float = FocusedItemPivotFraction,
    private val toleranceFraction: Float = 0f,
) : BringIntoViewSpec {
    override fun calculateScrollDistance(
        offset: Float,
        size: Float,
        containerSize: Float,
    ): Float {
        if (containerSize <= 0f) return 0f

        if (skipIfFullyVisible) {
            val trailingEdge = offset + size
            if (offset >= 0f && trailingEdge <= containerSize) return 0f
        }

        val distance = when {
            size >= containerSize -> offset
            centered -> offset - (containerSize - size) / 2f
            else -> offset - containerSize * pivotFraction
        }
        val tolerance = maxOf(MinScrollDistancePx, size * toleranceFraction)
        return if (abs(distance) <= tolerance) 0f else distance
    }
}

/** Прежнее поведение: пропускает уже видимые элементы, иначе тянет к 12% от leading edge. */
val TvFocusedGridBringIntoViewSpec: BringIntoViewSpec =
    TvPivotBringIntoViewSpec(skipIfFullyVisible = true, centered = false)

/**
 * Для вертикальных TV-гридов, где сфокусированный ряд должен всегда стоять на [пивоте][
 * FocusedItemPivotFraction] от верхней кромки: над ним видно край предыдущего ряда, под ним —
 * начало следующего.
 *
 * Именно поэтому здесь `skipIfFullyVisible = false`. Со `skipIfFullyVisible = true` спек
 * пересчитывается каждый кадр анимации (`ContentInViewNode.afterFrame`) и возвращает 0 сразу, как
 * только ряд поместился целиком — ряд паркуется впритык к нижней кромке, а под ним остаётся
 * случайный «перелёт» пружины за последний кадр (0–30 dp). Окно композиции грида заканчивается
 * ровно на кромке (`maxMainAxis = mainAxisAvailableSize + afterContentPadding`, где
 * `mainAxisAvailableSize` — высота уже без паддингов), поэтому при нулевом перелёте следующий ряд
 * не компонуется, и DPAD-вниз уезжает в beyond-bounds поиск, теряя колонку.
 *
 * Лишнего «подскролла» при переходе вбок это не даёт: ряд уже стоит на пивоте, дистанция 0.
 */
val TvPivotedGridBringIntoViewSpec: BringIntoViewSpec =
    TvPivotBringIntoViewSpec(
        skipIfFullyVisible = false,
        centered = false,
        toleranceFraction = FocusedCardScaleTolerance,
    )

/**
 * Для вертикальных гридов, где промах должен центрировать ряд (а не подтягивать его к 12%
 * сверху), но переход фокуса вбок внутри уже видимого ряда по-прежнему не должен скроллить.
 */
val TvCenteredGridBringIntoViewSpec: BringIntoViewSpec =
    TvPivotBringIntoViewSpec(skipIfFullyVisible = true, centered = true)

/**
 * Для горизонтальных каруселей, где ось скролла совпадает с осью навигации — "skip if fully
 * visible" здесь не нужен (нет поперечного перехода, который надо защищать), а без него каждая
 * сфокусированная карточка стабильно центрируется, вместо чередования "не скроллим" / "тянем к
 * 12%", которое выглядит как дёрганье.
 */
val TvCenteredCarouselBringIntoViewSpec: BringIntoViewSpec =
    TvPivotBringIntoViewSpec(skipIfFullyVisible = false, centered = true)

const val FocusedItemPivotFraction = 0.12f

/**
 * Запас на лёгкий «недоскролл» до пивота, оставаясь незаметным на глаз промахом мимо цели.
 */
private const val FocusedCardScaleTolerance = 0.05f
private const val MinScrollDistancePx = 1f
