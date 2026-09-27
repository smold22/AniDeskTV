package com.anidesk.tv.player

import android.content.Context
import androidx.core.content.ContextCompat
import com.anidesk.tv.R
import androidx.leanback.widget.Action

class DubbersAction(context: Context) : Action(
    R.id.player_action_dubbers.toLong(),
    "Озвучка",
    null,
    ContextCompat.getDrawable(context, R.drawable.ic_dubber),
)