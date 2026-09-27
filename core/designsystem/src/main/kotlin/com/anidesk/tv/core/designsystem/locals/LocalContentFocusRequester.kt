package com.anidesk.tv.core.designsystem.locals

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.focus.FocusRequester

val LocalContentFocusRequester = compositionLocalOf<FocusRequester?> { null }
