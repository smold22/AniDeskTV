package com.anidesk.tv.player

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import com.anidesk.tv.R

class PlayerQualityGuidedFragment : BasePlayerGuidedFragment() {

    companion object {
        fun newInstance(extra: PlayerExtra): PlayerQualityGuidedFragment =
            PlayerQualityGuidedFragment().putExtra(extra)
    }

    private val viewModel: PlayerQualityViewModel by playerViewModel { getPlayerExtra() }

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)
        viewModel.updateAvailable(viewModel.getPlayerData())
        val items = viewModel.getItems()
        val selected = viewModel.getSelectedItem()
        items.forEach { quality ->
            actions.add(
                GuidedAction.Builder(requireContext())
                    .id(quality.toLong())
                    .title(if (quality == 0) "Авто" else "${quality}p")
                    .checked(quality == selected)
                    .build()
            )
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        super.onGuidedActionClicked(action)
        viewModel.onQualityClick(action.id.toInt())
    }
}