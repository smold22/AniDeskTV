package com.anidesk.tv.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.anidesk.tv.core.network.api.AnixartApi
import com.anidesk.tv.core.network.dto.Dubber

class PlayerDubberViewModel(
    private val argExtra: PlayerExtra,
    private val api: AnixartApi,
    private val playerController: PlayerController,
    private val guidedRouter: GuidedRouter,
) : ViewModel() {

    val dubbersState = MutableStateFlow<List<Dubber>>(emptyList())

    fun load() {
        viewModelScope.launch {
            val list = runCatching { api.getDubbers(argExtra.releaseId).types }
                .getOrDefault(emptyList())
            dubbersState.value = list
        }
    }

    fun getItems(): List<Dubber> = dubbersState.value

    fun getSelectedId(): Int? = playerController.selectedDubber?.id

    fun onDubberClick(dubberId: Int) {
        val dubber = dubbersState.value.firstOrNull { it.id == dubberId } ?: return
        playerController.selectedDubber = dubber
        playerController.selectDubberRelay.emit(dubber)
        guidedRouter.finishGuidedChain()
    }
}