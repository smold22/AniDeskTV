package com.anidesk.tv.core.designsystem.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private data class YummyTvPalette(
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    // Светлые варианты акцента: более тёмный/насыщенный тон
    // того же оттенка, читаемый на белом фоне. Нейтрали при этом общие (см. LightNeutrals).
    val primaryLight: Color,
    val onPrimaryLight: Color,
    val primaryContainerLight: Color,
    val onPrimaryContainerLight: Color,
    val secondaryLight: Color,
    val error: Color = Color(0xFFFFB4AB),
    val onError: Color = Color(0xFF690005),
    val outline: Color = Color(0xFF8E877D),
)

private val OceanPalette = YummyTvPalette(
    background = Color(0xFF080E14),
    onBackground = Color(0xFFEAF3FA),
    surface = Color(0xFF101820),
    surfaceVariant = Color(0xFF1A2631),
    onSurfaceVariant = Color(0xFFBFD0DD),
    primary = Color(0xFF8DCCFF),
    onPrimary = Color(0xFF001D32),
    primaryContainer = Color(0xFF163851),
    onPrimaryContainer = Color(0xFFD2EBFF),
    secondary = Color(0xFFC0D3E1),
    primaryLight = Color(0xFF00629E),
    onPrimaryLight = Color(0xFFFFFFFF),
    primaryContainerLight = Color(0xFFCFE5FF),
    onPrimaryContainerLight = Color(0xFF001D33),
    secondaryLight = Color(0xFF4A607A),
    outline = Color(0xFF8292A0),
)

/** Общие нейтрали светлой схемы (фон/поверхности/текст), задаются режимом фона. */
private data class LightNeutrals(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
)

private val LightNeutralsWhite = LightNeutrals(
    // Фон белый, панели чуть серее — иначе сливаются с белым.
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF0F0F0),
    surfaceVariant = Color(0xFFE6E6E6),
)

private val LightOnBackground = Color(0xFF1A1A1A)
private val LightOnSurfaceVariant = Color(0xFF444444)
private val LightOutline = Color(0xFF757575)

@Composable
fun AniDeskTvTheme(
    isTelevision: Boolean = true,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val useTvTypography = isTelevision
    val palette = OceanPalette
    val colorScheme = if (darkTheme) {
        palette.toDarkColorScheme()
    } else {
        palette.toLightColorScheme(LightNeutralsWhite)
    }

    // Иконки статус-бара и навигационной полосы: тёмные на светлом фоне, светлые на тёмном.
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(darkTheme) {
            view.context.findActivity()?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            onDispose { }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = if (useTvTypography) YummyTvTypography else YummyMobileTypography,
    ) {
        // Дефолт LocalContentColor в Material3 — чёрный: на светлой теме это совпадало с фоном,
        // а на тёмной давало нечитаемый текст. Заголовки и подписи, не задающие цвет явно,
        // берут его отсюда; M3-компоненты (Surface, Card, Button) переопределяют его сами.
        CompositionLocalProvider(LocalContentColor provides colorScheme.onBackground) {
            content()
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Тёмная схема: тёмные нейтрали палитры плюс светлый акцент (читается на тёмном фоне). */
private fun YummyTvPalette.toDarkColorScheme() = darkColorScheme(
    background = background,
    onBackground = onBackground,
    surface = surface,
    onSurface = onBackground,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariant,
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onPrimary,
    // Акцентная «таблетка» выделения (напр. активная вкладка нижнего меню) несёт оттенок палитры.
    secondaryContainer = primaryContainer,
    onSecondaryContainer = onPrimaryContainer,
    tertiary = primaryContainer,
    onTertiary = onPrimaryContainer,
    tertiaryContainer = primaryContainer,
    onTertiaryContainer = onPrimaryContainer,
    error = error,
    onError = onError,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = outline,
    outlineVariant = surfaceVariant,
    scrim = Color.Black,
    inverseSurface = onBackground,
    inverseOnSurface = surface,
    inversePrimary = primary,
)

private fun YummyTvPalette.toLightColorScheme(neutrals: LightNeutrals) = lightColorScheme(
    background = neutrals.background,
    onBackground = LightOnBackground,
    surface = neutrals.surface,
    onSurface = LightOnBackground,
    surfaceVariant = neutrals.surfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onPrimaryLight,
    // Акцентная «таблетка» выделения (напр. активная вкладка нижнего меню) несёт оттенок палитры.
    secondaryContainer = primaryContainerLight,
    onSecondaryContainer = onPrimaryContainerLight,
    tertiary = primaryContainerLight,
    onTertiary = onPrimaryContainerLight,
    tertiaryContainer = primaryContainerLight,
    onTertiaryContainer = onPrimaryContainerLight,
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = LightOutline,
    outlineVariant = neutrals.surfaceVariant,
    scrim = Color.Black,
    inverseSurface = LightOnBackground,
    inverseOnSurface = neutrals.background,
    inversePrimary = primaryContainerLight,
)