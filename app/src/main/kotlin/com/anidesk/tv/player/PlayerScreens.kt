package com.anidesk.tv.player

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.github.terrakok.cicerone.androidx.FragmentScreen

class PlayerScreen(private val extra: PlayerExtra) : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return PlayerFragment.newInstance(extra)
    }
}

class PlayerQualityGuidedScreen(private val extra: PlayerExtra) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return PlayerQualityGuidedFragment.newInstance(extra)
    }
}

class PlayerSpeedGuidedScreen(private val extra: PlayerExtra) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return PlayerSpeedGuidedFragment.newInstance(extra)
    }
}

class PlayerEpisodesGuidedScreen(private val extra: PlayerExtra) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return PlayerEpisodesGuidedFragment.newInstance(extra)
    }
}

class PlayerDubberGuidedScreen(private val extra: PlayerExtra) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return PlayerDubberGuidedFragment.newInstance(extra)
    }
}