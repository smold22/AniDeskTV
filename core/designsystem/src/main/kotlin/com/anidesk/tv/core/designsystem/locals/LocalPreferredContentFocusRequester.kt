package com.anidesk.tv.core.designsystem.locals

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.focus.FocusRequester

val LocalPreferredContentFocusRequester = compositionLocalOf<((FocusRequester?) -> Unit)?> { null }
