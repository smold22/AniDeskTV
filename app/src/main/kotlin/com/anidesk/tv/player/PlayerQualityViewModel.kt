package com.anidesk.tv.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.anidesk.tv.core.preferences.settings.SettingsStore

class PlayerQualityViewModel(
    private val argExtra: PlayerExtra,
    private val settingsStore: SettingsStore,
    private val playerController: PlayerController,
) : ViewModel() {

    private var availableItems: List<Int> = emptyList()

    fun getPlayerData(): PlayerController.PlayerData? = playerController.data.value

    fun updateAvailable(data: PlayerController.PlayerData?) {
        availableItems = data?.links?.keys.orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .sortedDescending()
    }

    fun getItems(): List<Int> = listOf(0) + availableItems

    fun getSelectedItem(): Int = playerController.data.value?.currentQuality ?: 0

    fun onQualityClick(quality: Int) {
        viewModelScope.launch {
            settingsStore.setDefaultQuality(quality)
        }
        playerController.selectQualityRelay.emit(quality)
        playerController.data.value?.let { data ->
            playerController.data.value = data.copy(currentQuality = quality)
        }
    }
}