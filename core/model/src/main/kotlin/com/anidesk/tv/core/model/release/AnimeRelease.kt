package com.anidesk.tv.core.model.release

/**
 * Общая модель карточки релиза, используемая на Home, в поиске, топе и расписании.
 * Строится из DTO Anixart (`Release`) в мапперах фич.
 */
data class AnimeRelease(
    val id: Int,
    val titleRu: String,
    val titleOriginal: String,
    val posterUrl: String,
    val year: Int?,
    val rating: Double,
    val genres: List<String> = emptyList(),
    val category: String = "",
    val episodesTotal: Int? = null,
    val episodeName: String = "",
    val isViewed: Boolean = false,
    val lastViewEpisodeName: String? = null,
)