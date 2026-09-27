package com.anidesk.tv.player

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import com.anidesk.tv.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerFragment : BasePlayerFragment() {

    companion object {
        private const val ARG_EXTRA = "player_extra"

        fun newInstance(extra: PlayerExtra): PlayerFragment = PlayerFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_EXTRA + "_release", extra.releaseId)
                putInt(ARG_EXTRA + "_dubber", extra.dubberId)
                putInt(ARG_EXTRA + "_source", extra.sourceId)
                putInt(ARG_EXTRA + "_position", extra.startPosition)
                putString(ARG_EXTRA + "_name", extra.sourceName)
            }
        }
    }

    private val extra: PlayerExtra by lazy {
        val args = requireArguments()
        PlayerExtra(
            releaseId = args.getInt(ARG_EXTRA + "_release"),
            dubberId = args.getInt(ARG_EXTRA + "_dubber"),
            sourceId = args.getInt(ARG_EXTRA + "_source"),
            startPosition = args.getInt(ARG_EXTRA + "_position"),
            sourceName = args.getString(ARG_EXTRA + "_name") ?: "Kodik",
        )
    }

    private val viewModel: PlayerViewModel by playerViewModel { extra }

    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playerGlue?.actionListener = object : VideoPlayerGlue.OnActionClickedListener {

            override fun onPrevious() = viewModel.onPrevClick(getPosition())
            override fun onNext() = viewModel.onNextClick(getPosition())
            override fun onQualityClick() =
                (requireActivity() as PlayerActivity).openSelectionPanel(SelectionPanelType.QUALITY)

            override fun onSpeedClick() =
                (requireActivity() as PlayerActivity).openSelectionPanel(SelectionPanelType.SPEED)

            override fun onEpisodesClick() =
                (requireActivity() as PlayerActivity).openSelectionPanel(SelectionPanelType.EPISODES)

            override fun onDubberClick() =
                (requireActivity() as PlayerActivity).openSelectionPanel(SelectionPanelType.DUBBERS)
        }
        progressBarManager.initialDelay = 0
        progressBarManager.show()

        subscribeTo(viewModel.videoData.filterNotNull()) {
            progressBarManager.hide()
            setPlayerHeaders(it.headers)
            playerGlue?.apply {
                title = it.title
                subtitle = it.subtitle
                seekTo(it.seek)
                preparePlayer(it.url)
            }
        }

        subscribeTo(viewModel.playAction.filterNotNull()) {
            if (it) {
                playerGlue?.markResumedByUser()
                playerGlue?.play()
            } else {
                playerGlue?.markPausedByUser()
                playerGlue?.pause()
            }
        }

        subscribeTo(viewModel.speedState.filterNotNull()) {
            player?.playbackParameters = PlaybackParameters(it)
        }

        subscribeTo(viewModel.qualityState.filterNotNull()) {
            playerGlue?.setQualityLabel(if (it == "auto") "Авто" else "${it}p")
        }

        subscribeTo((requireActivity() as PlayerActivity).playerController.skipIntervalMs) {
            playerGlue?.skipIntervalMs = it
        }

        subscribeTo(viewModel.loading) {
            if (it) {
                progressBarManager.show()
            } else {
                progressBarManager.hide()
            }
        }

        subscribeTo(viewModel.resumeRequest) { request ->
            if (request != null) {
                viewModel.resumeRequest.value = null
                showResumeDialog(request)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            var ticks = 0
            while (isActive) {
                val position = getPosition()
                (requireActivity() as PlayerActivity).playerController.currentPositionMs = position
                if (++ticks % 10 == 0) viewModel.savePosition(position)
                delay(500)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseClick(getPosition())
    }

    override fun onCompletePlaying() {
        viewModel.onComplete(getPosition())
    }

    override fun onPreparePlaying() {
        viewModel.onPrepare(getDuration())
    }

    private fun showResumeDialog(request: ResumeRequest) {
        val dialog = AlertDialog.Builder(requireContext(), R.style.AppTheme_Player_ResumeDialog)
            .setTitle("Воспроизведение")
            .setMessage("Продолжить с ${formatTime(request.positionMs)}?")
            .setPositiveButton("Продолжить") { _, _ ->
                player?.seekTo(request.positionMs)
                viewModel.playAction.emit(true)
            }
            .setNegativeButton("Начать сначала") { _, _ ->
                viewModel.clearPosition(request.positionKey)
                viewModel.playAction.emit(true)
            }
            .setOnCancelListener {
                viewModel.playAction.emit(true)
            }
            .create()
        viewModel.playAction.emit(false)
        dialog.show()
    }

    private fun getPosition(): Long = player?.currentPosition ?: 0

    private fun getDuration(): Long = player?.duration ?: 0

    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        return "%d:%02d".format(totalSec / 60, totalSec % 60)
    }
}