package com.anidesk.tv.feature.settings.navigator

import androidx.navigation3.runtime.NavKey
import com.anidesk.tv.feature.settings.ISettingsNavigator
import javax.inject.Inject

class SettingsNavigator @Inject constructor() : ISettingsNavigator {
    override fun getSettingsDest(): NavKey = SettingsDestination
}