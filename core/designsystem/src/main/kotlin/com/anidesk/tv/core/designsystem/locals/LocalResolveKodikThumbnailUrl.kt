package com.anidesk.tv.core.designsystem.locals

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

val LocalResolveKodikThumbnailUrl: CompositionLocal<(String) -> String> =
    staticCompositionLocalOf { { url -> url } }