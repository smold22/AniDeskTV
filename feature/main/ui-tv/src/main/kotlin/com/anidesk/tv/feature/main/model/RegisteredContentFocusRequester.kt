package com.anidesk.tv.feature.main.model

import androidx.compose.ui.focus.FocusRequester

internal data class RegisteredContentFocusRequester(
    val key: Any?,
    val requester: FocusRequester,
)