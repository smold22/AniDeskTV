package com.anidesk.tv.player

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.leanback.media.PlaybackBaseControlGlue
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import androidx.leanback.R as LeanbackR

@UnstableApi
class VideoPlayerGlue(
    context: Context,
    private val playerAdapter: LeanbackPlayerAdapter,
) : PlaybackTransportControlGlue<LeanbackPlayerAdapter>(context, playerAdapter) {

    interface OnActionClickedListener {

        fun onPrevious()
        fun onNext()
        fun onQualityClick()
        fun onSpeedClick()
        fun onEpisodesClick()
        fun onDubberClick()
    }

    var actionListener: OnActionClickedListener? = null

    private val previousAction = Action(
        PlaybackBaseControlGlue.ACTION_SKIP_TO_PREVIOUS.toLong(),
        "",
        "",
        ContextCompat.getDrawable(context, LeanbackR.drawable.lb_ic_skip_previous),
    )
    private val nextAction = Action(
        PlaybackBaseControlGlue.ACTION_SKIP_TO_NEXT.toLong(),
        "",
        "",
        ContextCompat.getDrawable(context, LeanbackR.drawable.lb_ic_skip_next),
    )
    private val rewindAction = Action(
        PlaybackBaseControlGlue.ACTION_REWIND.toLong(),
        "",
        "",
        ContextCompat.getDrawable(context, LeanbackR.drawable.lb_ic_fast_rewind),
    )
    private val fastForwardAction = Action(
        PlaybackBaseControlGlue.ACTION_FAST_FORWARD.toLong(),
        "",
        "",
        ContextCompat.getDrawable(context, LeanbackR.drawable.lb_ic_fast_forward),
    )

    private val qualityAction = QualityAction(context)
    private val speedAction = SpeedAction(context)
    private val skipAction = SkipAction(context)
    private val episodesAction = EpisodesAction(context)
    private val dubbersAction = DubbersAction(context)

    private var secondaryActionsAdapter: ArrayObjectAdapter? = null

    /** Текущий интервал пропуска, мс — обновляется из PlayerViewModel */
    var skipIntervalMs: Long = 85_000L

    /**
     * Поставил ли пользователь видео на паузу сам (кнопка Play/Pause, диалог
     * продолжения, уход со экрана). Пока false — перемотка обязана продолжить
     * воспроизведение, иначе пришлось бы жать Play.
     */
    var pausedByUser: Boolean = false
        private set

    override fun onCreatePrimaryActions(primaryActionsAdapter: ArrayObjectAdapter) {
        super.onCreatePrimaryActions(primaryActionsAdapter)
        primaryActionsAdapter.add(previousAction)
        primaryActionsAdapter.add(rewindAction)
        primaryActionsAdapter.add(fastForwardAction)
        primaryActionsAdapter.add(nextAction)
        primaryActionsAdapter.add(episodesAction)
        primaryActionsAdapter.add(dubbersAction)
    }

    override fun onCreateSecondaryActions(secondaryActionsAdapter: ArrayObjectAdapter) {
        super.onCreateSecondaryActions(secondaryActionsAdapter)
        this.secondaryActionsAdapter = secondaryActionsAdapter
        secondaryActionsAdapter.add(qualityAction)
        secondaryActionsAdapter.add(speedAction)
        secondaryActionsAdapter.add(skipAction)
    }

    override fun onActionClicked(action: Action) {
        if (action.id == PlaybackBaseControlGlue.ACTION_PLAY_PAUSE.toLong()) {
            // Super переключит play/pause, а мы запоминаем intent пользователя,
            // чтобы перемотка его не перебивала.
            pausedByUser = playerAdapter.isPlaying
        }
        when {
            action == qualityAction -> actionListener?.onQualityClick()
            action == speedAction -> actionListener?.onSpeedClick()
            action == skipAction -> seekBy(skipIntervalMs)
            action == episodesAction -> actionListener?.onEpisodesClick()
            action == dubbersAction -> actionListener?.onDubberClick()
            action == rewindAction -> seekBy(-SEEK_STEP)
            action == fastForwardAction -> seekBy(SEEK_STEP)
            action.id == previousAction.id -> actionListener?.onPrevious()
            action.id == nextAction.id -> actionListener?.onNext()
            else -> super.onActionClicked(action)
        }
    }

    fun markPausedByUser() {
        pausedByUser = true
    }

    fun markResumedByUser() {
        pausedByUser = false
    }

    private fun seekBy(deltaMs: Long) {
        val duration = playerAdapter.duration
        val target = (playerAdapter.currentPosition + deltaMs)
            .coerceIn(0L, if (duration > 0) duration else Long.MAX_VALUE)
        playerAdapter.seekTo(target)
        if (!pausedByUser) {
            playerAdapter.play()
        }
    }

    companion object {
        private const val SEEK_STEP = 10_000L
    }

    fun setQualityLabel(label: String) {
        qualityAction.setQuality(label)
        secondaryActionsAdapter?.let { notifyItemChanged(it, qualityAction) }
    }
}