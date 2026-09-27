package com.anidesk.tv.core.network.di

import com.anidesk.tv.core.network.api.AnixartApi
import com.anidesk.tv.core.preferences.session.SessionStore
import com.anidesk.tv.core.preferences.settings.SettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ленивый синглтон [AnixartApi]: применяет сохранённые токен и выбранный API-хост
 * при первой выдаче. Последующие изменения настроек применяются через
 * [refreshFromSettings].
 */
@Singleton
class AnixartApiProvider @Inject constructor(
    private val sessionStore: SessionStore,
    private val settingsStore: SettingsStore,
) {
    private val mutex = Mutex()
    @Volatile
    private var instance: AnixartApi? = null

    suspend fun get(): AnixartApi = mutex.withLock {
        instance?.let { return it }
        val api = AnixartApi(
            baseUrl = SettingsStore.apiBaseUrl(settingsStore.apiEndpoint.first()),
        )
        api.token = sessionStore.token.first()
        instance = api
        api
    }

    suspend fun refreshFromSettings() {
        val api = instance ?: return
        mutex.withLock {
            api.token = sessionStore.token.first()
            api.baseUrl = SettingsStore.apiBaseUrl(settingsStore.apiEndpoint.first())
        }
    }
}