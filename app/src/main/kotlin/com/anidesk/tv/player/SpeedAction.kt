package com.anidesk.tv.player

import android.content.Context
import androidx.core.content.ContextCompat
import com.anidesk.tv.R
import androidx.leanback.widget.Action

class SpeedAction(context: Context) : Action(
    R.id.player_action_speed.toLong(),
    "1x",
    null,
    ContextCompat.getDrawable(context, R.drawable.ic_play_speed),
)