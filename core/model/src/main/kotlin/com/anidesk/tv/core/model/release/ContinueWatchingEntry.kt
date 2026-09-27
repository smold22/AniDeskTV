package com.anidesk.tv.core.model.release

import kotlinx.serialization.Serializable

/**
 * Локальная запись "продолжить просмотр": метаданные релиза + последняя позиция
 * воспроизведения. Хранится в DataStore (не зависит от токена).
 */
@Serializable
data class ContinueWatchingEntry(
    val releaseId: Int,
    val titleRu: String,
    val titleOriginal: String = "",
    val posterUrl: String = "",
    val sourceId: Int,
    val episodePosition: Int,
    val episodeName: String = "",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val lastWatchedAt: Long = 0L,
)