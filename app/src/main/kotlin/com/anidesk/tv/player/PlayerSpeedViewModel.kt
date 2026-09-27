package com.anidesk.tv.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.anidesk.tv.core.preferences.settings.SettingsStore

private val SPEED_OPTIONS = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f, 2.5f, 3f)

class PlayerSpeedViewModel(
    private val argExtra: PlayerExtra,
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private var speedIndex: Int = runBlocking { settingsStore.playbackSpeed.first() }

    fun getItems(): List<Float> = SPEED_OPTIONS

    fun getSelectedIndex(): Int = speedIndex

    fun onSpeedClick(actionId: Long) {
        val index = SPEED_OPTIONS.indexOfFirst { it.hashCode().toLong() == actionId }
        if (index < 0) {
            return
        }
        speedIndex = index
        viewModelScope.launch {
            settingsStore.setPlaybackSpeed(index)
        }
    }
}