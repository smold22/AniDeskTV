package com.anidesk.tv.player

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import com.anidesk.tv.R

class PlayerSpeedGuidedFragment : BasePlayerGuidedFragment() {

    companion object {
        fun newInstance(extra: PlayerExtra): PlayerSpeedGuidedFragment =
            PlayerSpeedGuidedFragment().putExtra(extra)
    }

    private val viewModel: PlayerSpeedViewModel by playerViewModel { getPlayerExtra() }

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)
        val selectedIndex = viewModel.getSelectedIndex()
        viewModel.getItems().forEachIndexed { index, speed ->
            actions.add(
                GuidedAction.Builder(requireContext())
                    .id(speed.hashCode().toLong())
                    .title(if (speed == 1f) "Обычная" else formatSpeed(speed))
                    .checked(index == selectedIndex)
                    .build()
            )
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        super.onGuidedActionClicked(action)
        viewModel.onSpeedClick(action.id)
    }

    private fun formatSpeed(speed: Float): String {
        return if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"
    }
}