package com.anidesk.tv.feature.main.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.anidesk.tv.core.designsystem.focus.requestFocusUntilTimeout
import com.anidesk.tv.core.navigation.root.RootTab
import kotlin.time.Duration.Companion.milliseconds

private val ContentFocusRestoreTimeout = 900.milliseconds

internal fun Modifier.moveFocusToContentOnKey(
    onMoveToContent: (force: Boolean) -> Unit,
): Modifier =
    onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        when (event.key) {
            Key.DirectionRight -> {
                onMoveToContent(true)
                true
            }

            Key.DirectionCenter,
            Key.Enter,
            Key.NumPadEnter -> {
                onMoveToContent(false)
                true
            }

            else -> false
        }
    }
        .onKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
            when (event.key) {
                Key.DirectionRight -> {
                    onMoveToContent(true)
                    true
                }

                Key.DirectionCenter,
                Key.Enter,
                Key.NumPadEnter -> {
                    onMoveToContent(false)
                    true
                }

                else -> false
            }
        }

internal suspend fun requestFocusOnFrameBoundary(
    requester: FocusRequester,
): Boolean = requestFocusUntilTimeout(requester, ContentFocusRestoreTimeout)

internal fun Any?.isContentFocusKeyFor(root: RootTab): Boolean =
    this == null || (this is Pair<*, *> && first == root)