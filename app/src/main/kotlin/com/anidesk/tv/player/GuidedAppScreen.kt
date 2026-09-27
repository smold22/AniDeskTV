package com.anidesk.tv.player

import androidx.fragment.app.FragmentFactory
import com.github.terrakok.cicerone.androidx.FragmentScreen

abstract class GuidedAppScreen : FragmentScreen {
    abstract override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment
}