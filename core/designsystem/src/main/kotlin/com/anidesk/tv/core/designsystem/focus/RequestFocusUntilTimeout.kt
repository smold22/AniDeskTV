package com.anidesk.tv.core.designsystem.focus

import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val TvLazyGridFocusRestoreTimeout = 500.milliseconds

/**
 * Retries [FocusRequester.requestFocus] on every frame until it succeeds or [timeout] elapses,
 * instead of a fixed number of attempts - the item may not be focusable yet on the frame right
 * after it's scrolled into view, and a hardcoded retry count is either too short (still racy) or
 * wastefully long (burns frames after focus would've already succeeded). Shared beyond this file's
 * own grid/list restore use: any TV screen that needs to grab focus on something not yet guaranteed
 * to be attached (e.g. right after a scroll, or right after a conditionally-shown overlay appears)
 * should reuse this instead of rolling its own `repeat(N) { requestFocus(); withFrameNanos {} }` or
 * a hand-written `withTimeoutOrNull { while (!focused) { ... } }` copy.
 */
suspend fun requestFocusUntilTimeout(
    requester: FocusRequester,
    timeout: Duration = TvLazyGridFocusRestoreTimeout,
): Boolean =
    withTimeoutOrNull(timeout) {
        var focused = false
        while (!focused) {
            withFrameNanos { }
            focused = runCatching { requester.requestFocus() }.getOrDefault(false)
        }
        focused
    } ?: false
