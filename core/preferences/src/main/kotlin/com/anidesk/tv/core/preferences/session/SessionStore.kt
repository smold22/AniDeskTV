package com.anidesk.tv.core.preferences.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "anidesk_session")

class SessionStore(private val context: Context) {

    private val TOKEN = stringPreferencesKey("token")
    private val PROFILE_ID = intPreferencesKey("profile_id")
    private val AUTH_SKIPPED = booleanPreferencesKey("auth_skipped")

    val token: Flow<String?> = context.sessionDataStore.data.map { it[TOKEN] }

    val profileId: Flow<Int?> = context.sessionDataStore.data.map { it[PROFILE_ID] }

    /** Вход пропущен — приложение работает без токена (только локальное продолжение просмотра). */
    val authSkipped: Flow<Boolean> = context.sessionDataStore.data.map { it[AUTH_SKIPPED] ?: false }

    suspend fun save(profileId: Int, token: String) {
        context.sessionDataStore.edit {
            it[PROFILE_ID] = profileId
            it[TOKEN] = token
        }
    }

    suspend fun setAuthSkipped(value: Boolean) {
        context.sessionDataStore.edit { it[AUTH_SKIPPED] = value }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
    }
}