package com.anidesk.tv.player

import android.content.Context
import androidx.core.content.ContextCompat
import com.anidesk.tv.R
import androidx.leanback.widget.Action

class SkipAction(context: Context) : Action(
    R.id.player_action_skip.toLong(),
    "Пропуск",
    null,
    ContextCompat.getDrawable(context, R.drawable.ic_skip_forward),
)