package com.anidesk.tv.feature.settings

import androidx.navigation3.runtime.NavKey

interface ISettingsNavigator {
    fun getSettingsDest(): NavKey
}