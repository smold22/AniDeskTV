package com.anidesk.tv.player

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.leanback.app.GuidedStepSupportFragment
import com.anidesk.tv.R

open class FakeGuidedStepFragment : GuidedStepSupportFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (isEnabled) {
                getGuidedRouter().exit()
                isEnabled = false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.player_guided_background)
        )
    }

    fun fakeOnAddSharedElementTransition(
        transaction: FragmentTransaction,
        disappearingFragment: GuidedStepSupportFragment
    ) {
        onAddSharedElementTransition(transaction, disappearingFragment)
    }

    protected fun getGuidedRouter(): GuidedRouter =
        (requireActivity() as PlayerActivity).guidedRouter
}