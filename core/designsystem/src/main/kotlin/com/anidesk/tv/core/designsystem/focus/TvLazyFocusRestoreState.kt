package com.anidesk.tv.core.designsystem.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
class TvLazyFocusRestoreState<Key : Any> internal constructor(
    private val savedKeyState: MutableState<Key?>,
    private val savedIndexState: MutableIntState,
) {
    val savedKey: Key?
        get() = savedKeyState.value

    val savedIndex: Int
        get() = savedIndexState.intValue

    fun onItemFocused(key: Key, index: Int) {
        savedKeyState.value = key
        savedIndexState.intValue = index
    }

    fun clear() {
        savedKeyState.value = null
        savedIndexState.intValue = 0
    }

    fun targetIndex(keys: List<Key>): Int? {
        if (keys.isEmpty()) return null
        val key = savedKey
        if (key != null) {
            val keyIndex = keys.indexOf(key)
            if (keyIndex >= 0) return keyIndex
        }
        return savedIndex.coerceIn(0, keys.lastIndex)
    }
}

@Composable
fun <Key : Any> rememberTvLazyFocusRestoreState(
    vararg inputs: Any?,
): TvLazyFocusRestoreState<Key> {
    val savedKeyState = rememberSaveable(*inputs) { mutableStateOf<Key?>(null) }
    val savedIndexState = rememberSaveable(*inputs) { mutableIntStateOf(0) }
    return remember(*inputs) { TvLazyFocusRestoreState(savedKeyState, savedIndexState) }
}
