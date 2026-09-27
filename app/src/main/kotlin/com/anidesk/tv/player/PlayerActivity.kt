package com.anidesk.tv.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.anidesk.tv.R
import com.anidesk.tv.core.network.api.AnixartApi
import com.anidesk.tv.core.network.di.AnixartApiProvider
import com.anidesk.tv.core.preferences.settings.SettingsStore
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class PlayerActivity : FragmentActivity() {

    companion object {
        private const val EXTRA_RELEASE_ID = "release_id"
        private const val EXTRA_DUBBER_ID = "dubber_id"
        private const val EXTRA_SOURCE_ID = "source_id"
        private const val EXTRA_POSITION = "position"
        private const val EXTRA_SOURCE_NAME = "source_name"

        fun start(
            context: Context,
            releaseId: Int,
            dubberId: Int,
            sourceId: Int,
            position: Int,
            sourceName: String,
        ) {
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_RELEASE_ID, releaseId)
                putExtra(EXTRA_DUBBER_ID, dubberId)
                putExtra(EXTRA_SOURCE_ID, sourceId)
                putExtra(EXTRA_POSITION, position)
                putExtra(EXTRA_SOURCE_NAME, sourceName)
            }
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var apiProvider: AnixartApiProvider

    @Inject
    lateinit var settingsStore: SettingsStore

    val api: AnixartApi by lazy { runBlocking { apiProvider.get() } }
    val playerController: PlayerController by lazy { PlayerController() }

    private val cicerone: Cicerone<GuidedRouter> by lazy { Cicerone.create(GuidedRouter()) }
    val guidedRouter: GuidedRouter get() = cicerone.router
    val router: Router get() = cicerone.router
    private val navigator by lazy { GuidedStepNavigator(this, R.id.fragmentContainer) }

    private val extra: PlayerExtra by lazy {
        PlayerExtra(
            releaseId = intent.getIntExtra(EXTRA_RELEASE_ID, 0),
            dubberId = intent.getIntExtra(EXTRA_DUBBER_ID, 0),
            sourceId = intent.getIntExtra(EXTRA_SOURCE_ID, 0),
            startPosition = intent.getIntExtra(EXTRA_POSITION, 0),
            sourceName = intent.getStringExtra(EXTRA_SOURCE_NAME) ?: "Kodik",
        )
    }

    private val selectionPanelState = mutableStateOf<SelectionPanelType?>(null)
    private var selectionPanelView: View? = null

    fun openSelectionPanel(type: SelectionPanelType) {
        Log.i("PlayerActivity", "openSelectionPanel $type")
        selectionPanelState.value = type
        selectionPanelView?.let {
            it.visibility = View.VISIBLE
            it.isFocusable = true
            it.requestFocus()
            it.post { it.requestFocus() }
        }
    }

    fun closeSelectionPanel() {
        selectionPanelState.value = null
        selectionPanelView?.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val selectionPanelView = findViewById<ComposeView>(R.id.selectionPanel)
        this.selectionPanelView = selectionPanelView
        selectionPanelView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        selectionPanelView.setContent {
            val panelType = selectionPanelState.value
            val data = playerController.data.value
            val speedIndex by settingsStore.playbackSpeed.collectAsState(initial = 3)
            if (panelType != null) {
                TvSelectionPanel(
                    type = panelType,
                    data = data,
                    playbackSpeedIndex = speedIndex,
                    onSelect = { type, id -> applySelection(type, id) },
                    onClose = { closeSelectionPanel() },
                )
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (selectionPanelState.value != null) {
                    closeSelectionPanel()
                    return
                }
                val fm = supportFragmentManager
                if (fm.backStackEntryCount > 0) {
                    fm.popBackStackImmediate()
                    if (fm.backStackEntryCount == 0) {
                        finish()
                    }
                } else {
                    finish()
                }
            }
        })

        if (savedInstanceState == null) {
            router.navigateTo(PlayerScreen(extra))
        }
    }

    private fun applySelection(type: SelectionPanelType, id: Long) {
        Log.i("PlayerActivity", "applySelection $type id=$id")
        when (type) {
            SelectionPanelType.QUALITY -> {
                playerController.selectQualityRelay.emit(id.toInt())
                lifecycleScope.launch { settingsStore.setDefaultQuality(id.toInt()) }
            }
            SelectionPanelType.SPEED -> {
                lifecycleScope.launch { settingsStore.setPlaybackSpeed(id.toInt()) }
            }
            SelectionPanelType.EPISODES -> {
                val sourceId = playerController.data.value?.currentSourceId ?: 0
                playerController.selectEpisodeRelay.emit(
                    PlayerController.EpisodeId(extra.releaseId, sourceId, id.toInt())
                )
            }
            SelectionPanelType.DUBBERS -> {
                playerController.data.value?.dubbers
                    ?.firstOrNull { it.id == id.toInt() }
                    ?.let { playerController.selectDubberRelay.emit(it) }
            }
        }
        closeSelectionPanel()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        cicerone.getNavigatorHolder().setNavigator(navigator)
    }

    override fun onPause() {
        cicerone.getNavigatorHolder().removeNavigator()
        super.onPause()
    }
}