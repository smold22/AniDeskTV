package com.anidesk.tv.player

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import com.anidesk.tv.R
import com.anidesk.tv.core.network.dto.Dubber

class PlayerDubberGuidedFragment : BasePlayerGuidedFragment() {

    companion object {
        fun newInstance(extra: PlayerExtra): PlayerDubberGuidedFragment =
            PlayerDubberGuidedFragment().putExtra(extra)
    }

    private val viewModel: PlayerDubberViewModel by playerViewModel { getPlayerExtra() }

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.load()
        subscribeTo(viewModel.dubbersState) {
            if (it.isNotEmpty()) setActions(buildActions(it))
        }
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)
        actions.addAll(buildActions(viewModel.getItems()))
    }

    private fun buildActions(dubbers: List<Dubber>): List<GuidedAction> =
        dubbers.map { dubber ->
            GuidedAction.Builder(requireContext())
                .id(dubber.id.toLong())
                .title(dubber.name)
                .checked(dubber.id == viewModel.getSelectedId())
                .build()
        }

    override fun onGuidedActionClicked(action: GuidedAction) {
        super.onGuidedActionClicked(action)
        viewModel.onDubberClick(action.id.toInt())
    }
}