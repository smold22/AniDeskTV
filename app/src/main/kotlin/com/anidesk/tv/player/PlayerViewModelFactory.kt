package com.anidesk.tv.player

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Ручное создание ViewModel плеера (в AniDesk нет DI-фреймворка). */
class PlayerViewModelFactory(
    private val activity: PlayerActivity,
    private val extra: PlayerExtra,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = when {
            modelClass.isAssignableFrom(PlayerViewModel::class.java) ->
                PlayerViewModel(
                    argExtra = extra,
                    api = activity.api,
                    settingsStore = activity.settingsStore,
                    playerController = activity.playerController,
                    guidedRouter = activity.guidedRouter,
                    router = activity.router,
                )

            modelClass.isAssignableFrom(PlayerEpisodesViewModel::class.java) ->
                PlayerEpisodesViewModel(
                    argExtra = extra,
                    api = activity.api,
                    settingsStore = activity.settingsStore,
                    playerController = activity.playerController,
                    guidedRouter = activity.guidedRouter,
                )

            modelClass.isAssignableFrom(PlayerQualityViewModel::class.java) ->
                PlayerQualityViewModel(
                    argExtra = extra,
                    settingsStore = activity.settingsStore,
                    playerController = activity.playerController,
                )

            modelClass.isAssignableFrom(PlayerSpeedViewModel::class.java) ->
                PlayerSpeedViewModel(
                    argExtra = extra,
                    settingsStore = activity.settingsStore,
                )

            modelClass.isAssignableFrom(PlayerDubberViewModel::class.java) ->
                PlayerDubberViewModel(
                    argExtra = extra,
                    api = activity.api,
                    playerController = activity.playerController,
                    guidedRouter = activity.guidedRouter,
                )

            else -> error("Unknown ViewModel class ${modelClass.name}")
        }
        return viewModel as T
    }
}

inline fun <reified T : ViewModel> Fragment.playerViewModel(
    crossinline extraProvider: () -> PlayerExtra,
): Lazy<T> = lazy {
    val activity = requireActivity() as PlayerActivity
    ViewModelProvider(this, PlayerViewModelFactory(activity, extraProvider()))[T::class.java]
}