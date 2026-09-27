package com.anidesk.tv.player

import android.content.Context
import com.anidesk.tv.core.navigation.player.IPlayerLauncher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerLauncher @Inject constructor(
    @ApplicationContext private val context: Context,
) : IPlayerLauncher {

    override fun launch(
        releaseId: Int,
        dubberId: Int,
        sourceId: Int,
        startPosition: Int,
        sourceName: String,
    ) {
        PlayerActivity.start(
            context = context,
            releaseId = releaseId,
            dubberId = dubberId,
            sourceId = sourceId,
            position = startPosition,
            sourceName = sourceName,
        )
    }
}