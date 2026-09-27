package com.anidesk.tv.core.navigation.player

/**
 * Точка запуска TV-плеера (Activity-плеер, исходник AniDesk).
 * Реализацию привязывает app-модуль (запуск [com.anidesk.tv.player.PlayerActivity]).
 */
interface IPlayerLauncher {
    fun launch(
        releaseId: Int,
        dubberId: Int,
        sourceId: Int,
        startPosition: Int,
        sourceName: String,
    )
}