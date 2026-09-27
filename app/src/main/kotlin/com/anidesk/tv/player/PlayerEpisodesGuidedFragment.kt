package com.anidesk.tv.player

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import com.anidesk.tv.R

class PlayerEpisodesGuidedFragment : BasePlayerGuidedFragment() {

    companion object {
        fun newInstance(extra: PlayerExtra): PlayerEpisodesGuidedFragment =
            PlayerEpisodesGuidedFragment().putExtra(extra)
    }

    private val viewModel: PlayerEpisodesViewModel by playerViewModel { getPlayerExtra() }

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)
        val data = viewModel.getPlayerData() ?: return
        data.episodes.forEach { episode ->
            actions.add(
                GuidedAction.Builder(requireContext())
                    .id(episode.position.toLong())
                    .title(episode.name)
                    .checked(episode.position == data.currentEpisode?.position)
                    .build()
            )
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        super.onGuidedActionClicked(action)
        viewModel.onEpisodeClick(action.id.toInt())
    }
}