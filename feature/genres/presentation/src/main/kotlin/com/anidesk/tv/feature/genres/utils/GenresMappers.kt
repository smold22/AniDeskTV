package com.anidesk.tv.feature.genres.utils

import com.anidesk.tv.core.model.release.AnimeRelease
import com.anidesk.tv.core.network.dto.Release

/** Общая карточка релиза из каталога/топа (как на поиске и главной). */
internal fun Release.toAnimeRelease(): AnimeRelease = AnimeRelease(
    id = id,
    titleRu = titleRu,
    titleOriginal = titleOriginal,
    posterUrl = image,
    year = year.toIntOrNull(),
    rating = grade,
    genres = genres.split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() },
    category = category?.name.orEmpty(),
    episodesTotal = episodesTotal,
    episodeName = lastViewEpisodeName,
    isViewed = isViewed,
    lastViewEpisodeName = lastViewEpisodeName,
)