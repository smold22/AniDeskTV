package com.anidesk.tv.core.network.api

import com.anidesk.tv.core.network.dto.DubbersResponse
import com.anidesk.tv.core.network.dto.EpisodesResponse
import com.anidesk.tv.core.network.dto.LoginResponse
import com.anidesk.tv.core.network.dto.PageableResponse
import com.anidesk.tv.core.network.dto.ProfileResponse
import com.anidesk.tv.core.network.dto.Release
import com.anidesk.tv.core.network.dto.ReleaseFilterRequest
import com.anidesk.tv.core.network.dto.ReleaseResponse
import com.anidesk.tv.core.network.dto.ScheduleResponse
import com.anidesk.tv.core.network.dto.SearchRequest
import com.anidesk.tv.core.network.dto.SearchResponse
import com.anidesk.tv.core.network.dto.SourcesResponse
import com.anidesk.tv.core.network.dto.Type
import com.anidesk.tv.core.network.dto.TypesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Клиент API Anixart (порт anixartjs на Kotlin/Ktor).
 * Токен передаётся query-параметром `token`; хост можно менять в настройках.
 */
class AnixartApi(
    var baseUrl: String = DEFAULT_BASE_URL,
) {
    var token: String? = null

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                    explicitNulls = true
                    coerceInputValues = true
                }
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
        }
    }

    // ---------- Auth ----------

    suspend fun signIn(login: String, password: String): LoginResponse {
        val form = listOf("login" to login, "password" to password)
        return client.post("$baseUrl/auth/signIn") {
            header("User-Agent", USER_AGENT)
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(form.formUrlEncode())
        }.body()
    }

    // ---------- Profile ----------

    suspend fun profile(id: Int): ProfileResponse = apiGet("/profile/$id")

    // ---------- Release ----------

    suspend fun releaseInfo(id: Int): ReleaseResponse =
        apiGet("/release/$id", mapOf("extended_mode" to "true"))

    /** @param page 1-индексированная страница (в API страницы начинаются с 0). */
    suspend fun relatedReleases(relatedId: Int, page: Int): PageableResponse<Release> =
        apiGet("/related/$relatedId/${page - 1}", headers = mapOf("API-Version" to "v2"))

    /** @param page 1-индексированная страница (в API страницы начинаются с 0). */
    suspend fun filterReleases(
        page: Int,
        sort: Int = 0,
        statusId: Int? = null,
        categoryId: Int? = null,
        country: String? = null,
        startYear: Int? = null,
        endYear: Int? = null,
        season: Int? = null,
        genres: List<String> = emptyList(),
        types: List<Int> = emptyList(),
        ageRatings: List<Int> = emptyList(),
    ): PageableResponse<Release> =
        apiPostJson(
            "/filter/${page - 1}",
            query = mapOf("extended_mode" to "true"),
            body = ReleaseFilterRequest(
                sort = sort,
                statusId = statusId,
                categoryId = categoryId,
                country = country,
                startYear = startYear,
                endYear = endYear,
                season = season,
                genres = genres,
                types = types,
                ageRatings = ageRatings,
            ),
        )

    suspend fun getDubbers(releaseId: Int): DubbersResponse = apiGet("/episode/$releaseId")

    suspend fun getDubberSources(releaseId: Int, dubberId: Int): SourcesResponse =
        apiGet("/episode/$releaseId/$dubberId")

    suspend fun getEpisodes(
        releaseId: Int,
        dubberId: Int,
        sourceId: Int,
        sort: Int = 1,
    ): EpisodesResponse =
        apiGet("/episode/$releaseId/$dubberId/$sourceId", mapOf("sort" to sort.toString()))

    suspend fun markEpisodeAsWatched(releaseId: Int, sourceId: Int, position: Int) {
        apiGet<Unit>("/episode/watch/$releaseId/$sourceId/$position")
    }

    suspend fun addToHistory(releaseId: Int, sourceId: Int, position: Int) {
        apiGet<Unit>("/history/add/$releaseId/$sourceId/$position")
    }

    // ---------- Feed / Schedule ----------

    suspend fun schedule(): ScheduleResponse = apiGet("/schedule")

    // ---------- Search ----------

    /** @param page 1-индексированная страница (в API страницы начинаются с 0). */
    suspend fun searchReleases(page: Int, query: String): List<Release> {
        val response: SearchResponse = apiPostJson(
            "/search/releases/${page - 1}",
            headers = mapOf("API-Version" to "v2"),
            body = SearchRequest(query = query),
        )
        return response.releases
    }

    suspend fun types(): List<Type> = apiGet<TypesResponse>("/type/all").types

    // ---------- Discover ----------

    suspend fun discoverWatching(page: Int): PageableResponse<Release> =
        apiGet("/discover/watching/$page")

    suspend fun discoverRecommendations(page: Int): PageableResponse<Release> =
        apiGet("/discover/recommendations/$page", mapOf("previous_page" to "-1"))

    // ---------- Закладки / История ----------

    /** @param page 0-индексированная страница (в API страницы таких списков начинаются с 0). */
    suspend fun profileList(type: Int, page: Int, sort: Int = 1): PageableResponse<Release> =
        apiGet("/profile/list/all/$type/$page", mapOf("sort" to sort.toString()))

    suspend fun history(page: Int): PageableResponse<Release> = apiGet("/history/$page")

    // ---------- Internals ----------

    private suspend inline fun <reified T> apiGet(
        path: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): T {
        val response = client.get("$baseUrl$path") {
            header("User-Agent", USER_AGENT)
            token?.let { url.parameters.append("token", it) }
            params.forEach { (key, value) -> url.parameters.append(key, value) }
            headers.forEach { (key, value) -> header(key, value) }
        }
        return response.body()
    }

    private suspend inline fun <reified T> apiPostJson(
        path: String,
        query: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        body: Any,
    ): T {
        val response = client.post("$baseUrl$path") {
            header("User-Agent", USER_AGENT)
            token?.let { url.parameters.append("token", it) }
            query.forEach { (key, value) -> url.parameters.append(key, value) }
            headers.forEach { (key, value) -> header(key, value) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return response.body()
    }

    companion object {
        const val USER_AGENT =
            "AnixartApp/9.0 BETA 3-25021818 (Android 9; SDK 28; x86_64; ROG ASUS AI2201_B; ru)"
        const val DEFAULT_BASE_URL = "https://api-s.anixsekai.com"
    }
}