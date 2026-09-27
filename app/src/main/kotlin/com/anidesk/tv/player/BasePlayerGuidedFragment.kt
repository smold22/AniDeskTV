package com.anidesk.tv.player

import android.os.Bundle
import androidx.leanback.app.GuidedStepSupportFragment

abstract class BasePlayerGuidedFragment : FakeGuidedStepFragment() {

    private var extra: PlayerExtra? = null

    protected fun getPlayerExtra(): PlayerExtra {
        extra?.let { return it }
        val args = requireArguments()
        return PlayerExtra(
            releaseId = args.getInt(ARG_RELEASE_ID),
            dubberId = args.getInt(ARG_DUBBER_ID),
            sourceId = args.getInt(ARG_SOURCE_ID),
            startPosition = args.getInt(ARG_POSITION),
            sourceName = args.getString(ARG_SOURCE_NAME) ?: "Kodik",
        ).also { extra = it }
    }

    companion object {
        const val ARG_RELEASE_ID = "release id"
        const val ARG_DUBBER_ID = "dubber id"
        const val ARG_SOURCE_ID = "source id"
        const val ARG_POSITION = "position"
        const val ARG_SOURCE_NAME = "source name"
    }
}

fun <T : BasePlayerGuidedFragment> T.putExtra(extra: PlayerExtra): T = apply {
    arguments = Bundle().apply {
        putInt(BasePlayerGuidedFragment.ARG_RELEASE_ID, extra.releaseId)
        putInt(BasePlayerGuidedFragment.ARG_DUBBER_ID, extra.dubberId)
        putInt(BasePlayerGuidedFragment.ARG_SOURCE_ID, extra.sourceId)
        putInt(BasePlayerGuidedFragment.ARG_POSITION, extra.startPosition)
        putString(BasePlayerGuidedFragment.ARG_SOURCE_NAME, extra.sourceName)
    }
}