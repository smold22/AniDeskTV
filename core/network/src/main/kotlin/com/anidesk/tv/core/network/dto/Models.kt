package com.anidesk.tv.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PageableResponse<T>(
    @SerialName("code") val code: Int = 0,
    @SerialName("content") val content: List<T> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0,
    @SerialName("total_page_count") val totalPageCount: Int = 0,
    @SerialName("current_page") val currentPage: Int = 0,
)

@Serializable
data class Release(
    @SerialName("id") val id: Int,
    @SerialName("title_ru") val titleRu: String = "",
    @SerialName("title_original") val titleOriginal: String = "",
    @SerialName("title_alt") val titleAlt: String = "",
    @SerialName("poster") val poster: String = "",
    @SerialName("image") val image: String = "",
    @SerialName("year") val year: String = "",
    @SerialName("genres") val genres: String = "",
    @SerialName("country") val country: String = "",
    @SerialName("director") val director: String = "",
    @SerialName("author") val author: String = "",
    @SerialName("translators") val translators: String = "",
    @SerialName("studio") val studio: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("note") val note: String? = null,
    @SerialName("related") val related: RelatedRelease? = null,
    @SerialName("category") val category: ReleaseCategory? = null,
    @SerialName("status") val status: ReleaseStatus? = null,
    @SerialName("rating") val rating: Double = 0.0,
    @SerialName("grade") val grade: Double = 0.0,
    @SerialName("duration") val duration: Int = 0,
    @SerialName("season") val season: Int = 0,
    @SerialName("broadcast") val broadcast: Int = 0,
    @SerialName("screenshots") val screenshots: List<String> = emptyList(),
    @SerialName("screenshot_images") val screenshotImages: List<String> = emptyList(),
    @SerialName("title") val title: String = "",
    @SerialName("episodes_released") val episodesReleased: Int? = null,
    @SerialName("episodes_total") val episodesTotal: Int? = null,
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("aired_on_date") val airedOnDate: Long = 0,
    @SerialName("creation_date") val creationDate: Long = 0,
    @SerialName("age_rating") val ageRating: Int = 0,
    @SerialName("is_adult") val isAdult: Boolean = false,
    @SerialName("is_play_disabled") val isPlayDisabled: Boolean = false,
    @SerialName("is_view_blocked") val isViewBlocked: Boolean = false,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @SerialName("your_vote") val yourVote: Int = 0,
    @SerialName("comment_count") val commentCount: Int = 0,
    @SerialName("comments_count") val commentsCount: Int = 0,
    @SerialName("collection_count") val collectionCount: Int = 0,
    @SerialName("profile_list_status") val profileListStatus: Int? = null,
    @SerialName("status_id") val statusId: Int = 0,
    @SerialName("last_view_timestamp") val lastViewTimestamp: Long = 0,
    @SerialName("last_view_episode_name") val lastViewEpisodeName: String = "",
    @SerialName("last_view_episode_type_name") val lastViewEpisodeTypeName: String = "",
    @SerialName("is_viewed") val isViewed: Boolean = false,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
    @SerialName("related_releases") val relatedReleases: List<RelatedRelease> = emptyList(),
)

@Serializable
data class ReleaseStatus(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
)

@Serializable
data class ReleaseCategory(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
)

@Serializable
data class RelatedRelease(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String = "",
    @SerialName("name_ru") val nameRu: String = "",
    @SerialName("image") val image: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("release_count") val releaseCount: Int = 0,
)

@Serializable
data class ReleaseResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("release") val release: Release? = null,
)

@Serializable
data class Dubber(
    @SerialName("@id") val atId: Int = 0,
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String = "",
    @SerialName("icon") val icon: String = "",
    @SerialName("workers") val workers: String = "",
    @SerialName("is_sub") val isSub: Boolean = false,
    @SerialName("episode_count") val episodeCount: Int = 0,
    @SerialName("view_count") val viewCount: Int = 0,
    @SerialName("pinned") val pinned: Boolean = false,
)

@Serializable
data class DubbersResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("types") val types: List<Dubber> = emptyList(),
)

@Serializable
data class Source(
    @SerialName("@id") val atId: Int = 0,
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("episode_count") val episodeCount: Int = 0,
)

@Serializable
data class SourcesResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("sources") val sources: List<Source> = emptyList(),
)

@Serializable
data class Episode(
    @SerialName("@id") val atId: Int = 0,
    @SerialName("id") val id: Long? = null,
    @SerialName("position") val position: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("url") val url: String = "",
    @SerialName("iframe") val iframe: Boolean = false,
    @SerialName("addedDate") val addedDate: Long = 0,
    @SerialName("is_filter") val isFilter: Boolean = false,
    @SerialName("is_watched") val isWatched: Boolean = false,
    @SerialName("source") val source: JsonElement? = null,
)

@Serializable
data class EpisodesResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("episodes") val episodes: List<Episode> = emptyList(),
)

@Serializable
data class ReleaseFilterRequest(
    @SerialName("sort") val sort: Int = 0,
    @SerialName("status_id") val statusId: Int? = null,
    @SerialName("category_id") val categoryId: Int? = null,
    @SerialName("country") val country: String? = null,
    @SerialName("start_year") val startYear: Int? = null,
    @SerialName("end_year") val endYear: Int? = null,
    @SerialName("season") val season: Int? = null,
    @SerialName("genres") val genres: List<String> = emptyList(),
    @SerialName("types") val types: List<Int> = emptyList(),
    @SerialName("age_ratings") val ageRatings: List<Int> = emptyList(),
)

@Serializable
data class SearchRequest(
    @SerialName("query") val query: String = "",
    @SerialName("searchBy") val searchBy: Int = 0,
)

@Serializable
data class SearchResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("releases") val releases: List<Release> = emptyList(),
)

@Serializable
data class Type(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String = "",
)

@Serializable
data class TypesResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("types") val types: List<Type> = emptyList(),
)

@Serializable
data class ScheduleResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("monday") val monday: List<Release> = emptyList(),
    @SerialName("tuesday") val tuesday: List<Release> = emptyList(),
    @SerialName("wednesday") val wednesday: List<Release> = emptyList(),
    @SerialName("thursday") val thursday: List<Release> = emptyList(),
    @SerialName("friday") val friday: List<Release> = emptyList(),
    @SerialName("saturday") val saturday: List<Release> = emptyList(),
    @SerialName("sunday") val sunday: List<Release> = emptyList(),
)

@Serializable
data class Profile(
    @SerialName("id") val id: Int,
    @SerialName("login") val login: String = "",
    @SerialName("avatar") val avatar: String = "",
    @SerialName("status") val status: String = "",
    @SerialName("privilege_level") val privilegeLevel: Int = 0,
    @SerialName("is_sponsor") val isSponsor: Boolean = false,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("is_online") val isOnline: Boolean = false,
    @SerialName("register_date") val registerDate: Long = 0,
    @SerialName("last_activity_time") val lastActivityTime: Long = 0,
)

@Serializable
data class ProfileToken(
    @SerialName("id") val id: String = "",
    @SerialName("token") val token: String = "",
)

@Serializable
data class LoginResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("profile") val profile: Profile? = null,
    @SerialName("profileToken") val profileToken: ProfileToken? = null,
)

@Serializable
data class ProfileResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("profile") val profile: Profile? = null,
    @SerialName("is_my_profile") val isMyProfile: Boolean = false,
)