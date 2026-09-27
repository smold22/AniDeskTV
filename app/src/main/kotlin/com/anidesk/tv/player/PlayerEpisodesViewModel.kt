package com.anidesk.tv.player

import androidx.lifecycle.ViewModel
import com.anidesk.tv.core.network.api.AnixartApi
import com.anidesk.tv.core.preferences.settings.SettingsStore

class PlayerEpisodesViewModel(
    private val argExtra: PlayerExtra,
    private val api: AnixartApi,
    private val settingsStore: SettingsStore,
    private val playerController: PlayerController,
    private val guidedRouter: GuidedRouter,
) : ViewModel() {

    fun getPlayerData(): PlayerController.PlayerData? = playerController.data.value

    fun onEpisodeClick(position: Int) {
        val data = playerController.data.value ?: return
        playerController.selectEpisodeRelay.emit(
            PlayerController.EpisodeId(argExtra.releaseId, data.currentSourceId, position)
        )
        guidedRouter.finishGuidedChain()
    }
}