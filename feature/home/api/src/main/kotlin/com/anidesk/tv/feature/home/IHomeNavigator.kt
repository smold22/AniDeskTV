package com.anidesk.tv.feature.home

import androidx.navigation3.runtime.NavKey

interface IHomeNavigator {
    fun getHomeDest(): NavKey
}