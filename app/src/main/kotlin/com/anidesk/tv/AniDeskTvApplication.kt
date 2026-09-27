package com.anidesk.tv

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AniDeskTvApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}