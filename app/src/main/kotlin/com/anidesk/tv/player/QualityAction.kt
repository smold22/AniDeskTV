package com.anidesk.tv.player

import android.content.Context
import androidx.core.content.ContextCompat
import com.anidesk.tv.R
import androidx.leanback.widget.PlaybackControlsRow

class QualityAction(context: Context) : PlaybackControlsRow.MultiAction(R.id.player_action_quality) {

    init {
        setLabels(DISPLAY_LABELS.toTypedArray())
        setDrawables(
            ICON_RES.map { ContextCompat.getDrawable(context, it)!! }.toTypedArray()
        )
        setIndex(QUALITIES.indexOf("auto"))
    }

    fun setQuality(label: String) {
        val index = DISPLAY_LABELS.indexOf(label).coerceAtLeast(0)
        setIndex(index)
    }

    companion object {
        val QUALITIES = listOf("auto", "480", "720", "1080")
        private val DISPLAY_LABELS = QUALITIES.map { if (it == "auto") "Авто" else "${it}p" }
        private val ICON_RES = listOf(
            R.drawable.ic_quality_hd_base,
            R.drawable.ic_quality_sd_base,
            R.drawable.ic_quality_hd_base,
            R.drawable.ic_quality_full_hd_base,
        )
    }
}