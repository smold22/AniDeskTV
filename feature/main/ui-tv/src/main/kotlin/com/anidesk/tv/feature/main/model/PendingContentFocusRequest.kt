package com.anidesk.tv.feature.main.model

import com.anidesk.tv.core.navigation.root.RootTab

internal data class PendingContentFocusRequest(
    val root: RootTab,
    val token: Int,
)