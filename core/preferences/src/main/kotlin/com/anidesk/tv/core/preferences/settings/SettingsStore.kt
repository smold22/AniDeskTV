package com.anidesk.tv.core.preferences.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anidesk.tv.core.model.release.ContinueWatchingEntry
import com.anidesk.tv.core.model.settings.MainSettingsSnapshot
import com.anidesk.tv.core.model.settings.PosterCardSize
import com.anidesk.tv.core.model.settings.PosterQuality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "anidesk_settings")

/** Размер обложек по умолчанию, пока пользователь не выбрал свой в настройках. */
private val DEFAULT_POSTER_CARD_SIZE = PosterCardSize.LARGE

class SettingsStore(private val context: Context) {

    private val POSTER_QUALITY = intPreferencesKey("poster_quality")
    private val POSTER_CARD_SIZE = intPreferencesKey("poster_card_size")

    private val DEFAULT_QUALITY = intPreferencesKey("default_quality")
    private val AUTO_PLAY = booleanPreferencesKey("auto_play")
    private val PLAYBACK_SPEED = intPreferencesKey("playback_speed")
    private val REWIND_TIME = intPreferencesKey("rewind_time")
    private val API_ENDPOINT = stringPreferencesKey("api_endpoint")

    private val PLAYBACK_POSITIONS = stringSetPreferencesKey("playback_positions")
    private val DUBBER_SOURCES = stringSetPreferencesKey("dubber_sources")
    private val CONTINUE_WATCHING = stringSetPreferencesKey("continue_watching")

    val posterQuality: Flow<PosterQuality> =
        context.settingsDataStore.data.map { PosterQuality.entries.getOrElse(it[POSTER_QUALITY] ?: PosterQuality.STANDARD.ordinal) { PosterQuality.STANDARD } }

    val posterCardSize: Flow<PosterCardSize> =
        context.settingsDataStore.data.map { PosterCardSize.entries.getOrElse(it[POSTER_CARD_SIZE] ?: DEFAULT_POSTER_CARD_SIZE.ordinal) { DEFAULT_POSTER_CARD_SIZE } }

    val mainSettingsSnapshot: Flow<MainSettingsSnapshot> =
        context.settingsDataStore.data.map {
            MainSettingsSnapshot(
                posterQuality = it.let { prefs -> PosterQuality.entries.getOrElse(prefs[POSTER_QUALITY] ?: PosterQuality.STANDARD.ordinal) { PosterQuality.STANDARD } },
                posterCardSize = it.let { prefs -> PosterCardSize.entries.getOrElse(prefs[POSTER_CARD_SIZE] ?: DEFAULT_POSTER_CARD_SIZE.ordinal) { DEFAULT_POSTER_CARD_SIZE } },
            )
        }

    suspend fun setPosterQuality(value: PosterQuality) {
        context.settingsDataStore.edit { it[POSTER_QUALITY] = value.ordinal }
    }

    suspend fun setPosterCardSize(value: PosterCardSize) {
        context.settingsDataStore.edit { it[POSTER_CARD_SIZE] = value.ordinal }
    }

    /** 0 = авто, 1080, 720, 480, 360 */
    val defaultQuality: Flow<Int> = context.settingsDataStore.data.map { it[DEFAULT_QUALITY] ?: 0 }

    /** Автовоспроизведение следующей серии */
    val autoPlay: Flow<Boolean> = context.settingsDataStore.data.map { it[AUTO_PLAY] ?: true }

    /** Индекс скорости воспроизведения в SPEED_OPTIONS (по умолчанию 3 = 1.0x) */
    val playbackSpeed: Flow<Int> = context.settingsDataStore.data.map { it[PLAYBACK_SPEED] ?: 3 }

    /** Интервал пропуска в секундах */
    val rewindTime: Flow<Int> = context.settingsDataStore.data.map { it[REWIND_TIME] ?: 85 }

    /** Выбранный хост API Anixart */
    val apiEndpoint: Flow<String> =
        context.settingsDataStore.data.map { it[API_ENDPOINT] ?: DEFAULT_API_ENDPOINT }

    suspend fun setDefaultQuality(value: Int) {
        context.settingsDataStore.edit { it[DEFAULT_QUALITY] = value }
    }

    suspend fun setAutoPlay(value: Boolean) {
        context.settingsDataStore.edit { it[AUTO_PLAY] = value }
    }

    suspend fun setPlaybackSpeed(value: Int) {
        context.settingsDataStore.edit { it[PLAYBACK_SPEED] = value }
    }

    suspend fun setRewindTime(value: Int) {
        context.settingsDataStore.edit { it[REWIND_TIME] = value }
    }

    suspend fun setApiEndpoint(value: String) {
        context.settingsDataStore.edit { it[API_ENDPOINT] = value }
    }

    /** Текущая позиция воспроизведения: ключ `releaseId:sourceId:episodePosition`. */
    suspend fun getPlaybackPosition(key: String): Long {
        val data = context.settingsDataStore.data.first()
        return data[PLAYBACK_POSITIONS]
            ?.firstOrNull { it.startsWith("$key=") }
            ?.substringAfter('=')
            ?.toLongOrNull()
            ?: 0L
    }

    suspend fun setPlaybackPosition(key: String, ms: Long) {
        context.settingsDataStore.edit { prefs ->
            val entries = prefs[PLAYBACK_POSITIONS]?.toMutableSet() ?: mutableSetOf()
            entries.removeAll { it.startsWith("$key=") }
            entries.add("$key=$ms")
            prefs[PLAYBACK_POSITIONS] = entries
        }
    }

    suspend fun clearPlaybackPosition(key: String) {
        context.settingsDataStore.edit { prefs ->
            val entries = prefs[PLAYBACK_POSITIONS]?.toMutableSet() ?: return@edit
            entries.removeAll { it.startsWith("$key=") }
            prefs[PLAYBACK_POSITIONS] = entries
        }
    }

    suspend fun getDubberSource(releaseId: Int): Pair<Int?, Int?> {
        val data = context.settingsDataStore.data.first()
        val entries = data[DUBBER_SOURCES] ?: return null to null
        val dubber = entries.firstOrNull { it.startsWith("$releaseId:dubber=") }
            ?.substringAfter('=')?.toIntOrNull()
        val source = entries.firstOrNull { it.startsWith("$releaseId:source=") }
            ?.substringAfter('=')?.toIntOrNull()
        return dubber to source
    }

    suspend fun setDubberSource(releaseId: Int, dubberId: Int, sourceId: Int) {
        context.settingsDataStore.edit { prefs ->
            val entries = prefs[DUBBER_SOURCES]?.toMutableSet() ?: mutableSetOf()
            entries.removeAll { it.startsWith("$releaseId:") }
            entries.add("$releaseId:dubber=$dubberId")
            entries.add("$releaseId:source=$sourceId")
            prefs[DUBBER_SOURCES] = entries
        }
    }

    /** Записанные продолжения просмотра (last-watched releases). */
    val continueWatching: Flow<List<ContinueWatchingEntry>> =
        context.settingsDataStore.data.map { prefs ->
            (prefs[CONTINUE_WATCHING] ?: emptySet())
                .mapNotNull { raw -> runCatching { Json.decodeFromString<ContinueWatchingEntry>(raw) }.getOrNull() }
                .sortedByDescending { it.lastWatchedAt }
        }

    suspend fun putContinueWatching(entry: ContinueWatchingEntry) {
        context.settingsDataStore.edit { prefs ->
            val entries = prefs[CONTINUE_WATCHING]?.toMutableSet() ?: mutableSetOf()
            entries.removeAll { raw ->
                Json.decodeFromString<ContinueWatchingEntry>(raw).releaseId == entry.releaseId
            }
            entries.add(Json.encodeToString(ContinueWatchingEntry.serializer(), entry))
            prefs[CONTINUE_WATCHING] = entries
        }
    }

    suspend fun removeContinueWatching(releaseId: Int) {
        context.settingsDataStore.edit { prefs ->
            val entries = prefs[CONTINUE_WATCHING] ?: return@edit
            prefs[CONTINUE_WATCHING] = entries.filterNot { entry ->
                runCatching { Json.decodeFromString<ContinueWatchingEntry>(entry).releaseId == releaseId }
                    .getOrDefault(false)
            }.toSet()
        }
    }

    companion object {
        const val DEFAULT_API_ENDPOINT = "api-s.anixsekai.com"

        /** Доступные эндпоинты API (host -> подпись). */
        val API_ENDPOINTS = listOf(
            "api-s.anixsekai.com" to "api-s.anixsekai.com (основной)",
            "api.anixart.app" to "api.anixart.app",
            "api.anixart.tv" to "api.anixart.tv (заблокирован в РФ)",
            "api.anixsekai.com" to "api.anixsekai.com",
            "baproxy-demo.ds1nc.ru" to "baproxy-demo.ds1nc.ru (прокси)",
        )

        fun apiBaseUrl(host: String): String = "https://$host"
    }
}