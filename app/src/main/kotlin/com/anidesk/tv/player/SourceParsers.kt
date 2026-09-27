package com.anidesk.tv.player

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Парсеры видеоисточников (порт anixartjs/kodikwrapper LinkParser на Kotlin/OkHttp).
 * Возвращает карту: качество -> прямой URL (HLS).
 */
object SourceParsers {

    private const val TAG = "SourceParsers"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun parse(episodeUrl: String, sourceName: String): Map<String, String> =
        withContext(Dispatchers.IO) {
            val result = try {
                directMedia(episodeUrl)?.let { return@withContext mapOf("720" to it) }
                val lower = sourceName.lowercase()
                when {
                    lower.contains("kodik") -> kodik(episodeUrl)
                    lower.contains("anilibria") || lower.contains("liberty") || lower.contains("libria") ->
                        anilibria(episodeUrl)

                    lower.contains("sibnet") -> {
                        val link = sibnet(episodeUrl) ?: return@withContext emptyMap()
                        mapOf("720" to link)
                    }
                    lower.contains("rutube") -> ruTube(episodeUrl)
                    lower.contains("studiomir") || lower.contains("tsm") -> studiomir(episodeUrl)
                    isProxyParserUrl(episodeUrl) -> proxyParser(episodeUrl)
                    else -> emptyMap()
                }
            } catch (e: Exception) {
                Log.e(TAG, "parse failed for source=$sourceName url=$episodeUrl", e)
                emptyMap()
            }
            Log.i(TAG, "source=$sourceName url=$episodeUrl -> ${result.keys}")
            result
        }

    /** Прямые медиа-URL (mp4/m3u8 или CDN Alloha/CVH-хосты) играются без парсинга. */
    private fun directMedia(url: String): String? {
        val host = runCatching { url.toHttpUrl().host }.getOrNull() ?: return null
        if (DIRECT_CDN_HOSTS.any { host.contains(it) }) return url
        val path = runCatching { url.toHttpUrl().encodedPath }.getOrNull().orEmpty()
        if (path.endsWith(".mp4") || path.endsWith(".m3u8") || path.endsWith(".ts")) return url
        return null
    }

    private fun fetch(url: String, headers: Map<String, String> = emptyMap()): String? =
        runCatching {
            val builder = Request.Builder().url(url)
            headers.forEach { (k, v) -> builder.header(k, v) }
            client.newCall(builder.build()).execute().use { it.body?.string() }
        }.getOrNull()

    // ---------- RuTube ----------

    private fun ruTube(url: String): Map<String, String> {
        val id = Regex("""(?:/video/|/embed/|/short/|[?&]id=)([0-9a-f]{32}|[0-9a-f]{24})""")
            .find(url)?.groupValues?.get(1)
            ?: Regex("""(\d{6,})""").find(url)?.groupValues?.get(1)
            ?: return emptyMap()
        val api = "https://rutube.ru/api/play/options/$id/?no_404=true"
        val body = fetch(api, mapOf("User-Agent" to DESKTOP_UA)) ?: return emptyMap()
        val root = runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull() ?: return emptyMap()
        val balancer = root["video_balancer"]?.jsonObject
            ?: root["video_balancer"]?.jsonPrimitive?.contentOrNull?.let { raw ->
                runCatching { json.parseToJsonElement(raw).jsonObject }.getOrNull()
            }
            ?: return emptyMap()
        val m3u8 = balancer["m3u8"]?.jsonPrimitive?.contentOrNull
            ?: balancer["default"]?.jsonPrimitive?.contentOrNull
            ?: return emptyMap()
        val result = LinkedHashMap<String, String>()
        result["auto"] = m3u8
        return result
    }

    // ---------- Kodik ----------

    private val kodikUrlRegex = Regex("""/([a-z]+)/(\d+)/([0-9a-z]+)/(\d+p)(?:.*)""")

    private fun kodik(url: String): Map<String, String> {
        val match = kodikUrlRegex.find(url) ?: return emptyMap()
        val type = match.groupValues[1]
        val id = match.groupValues[2]
        val hash = match.groupValues[3]
        val host = runCatching { url.toHttpUrl().host }.getOrNull() ?: "kodikplayer.com"

        // Актуальный способ: GET /ftor?type=..&id=..&hash=.. (без urlParams-подписей).
        val builder = "https://$host/ftor".toHttpUrl().newBuilder()
        builder.addQueryParameter("type", type)
        builder.addQueryParameter("id", id)
        builder.addQueryParameter("hash", hash)

        val response = runCatching {
            client.newCall(Request.Builder().url(builder.build()).header("Referer", "").build())
                .execute()
        }.getOrNull()

        if (response != null && response.header("Content-Type")?.contains("application/json") == true) {
            response.use { it.body?.string() }?.let { body ->
                return parseKodikLinks(body)
            }
        }

        // Запасной способ (старый): подписи urlParams со страницы эпизода.
        return kodikLegacy(url, host)
    }

    private fun kodikLegacy(url: String, host: String): Map<String, String> {
        val page = fetch(url) ?: return emptyMap()

        fun find(regex: Regex): String? = regex.find(page)?.groupValues?.get(1)

        val urlParamsRaw = find(Regex("""var\s+urlParams\s*=\s*'([^']*)'""")) ?: return emptyMap()
        val hash = find(Regex("""\w+\.hash\s*=\s*'([^']*)'""")) ?: return emptyMap()
        val id = find(Regex("""\w+\.id\s*=\s*'([^']*)'""")) ?: return emptyMap()
        val type = find(Regex("""\w+\.type\s*=\s*'([^']*)'""")) ?: return emptyMap()

        val urlParams = runCatching { json.parseToJsonElement(urlParamsRaw).jsonObject }
            .getOrNull() ?: Json.parseToJsonElement("{}").jsonObject

        val builder: HttpUrl.Builder = "https://$host/ftor".toHttpUrl().newBuilder()
        builder.addQueryParameter("type", type)
        builder.addQueryParameter("hash", hash)
        builder.addQueryParameter("id", id)
        for ((key, value) in urlParams) {
            if (value is JsonPrimitive) builder.addQueryParameter(key, value.content)
        }

        val response = runCatching {
            client.newCall(Request.Builder().url(builder.build()).header("Referer", "").build())
                .execute()
        }.getOrNull() ?: return emptyMap()

        response.use {
            val contentType = it.header("Content-Type") ?: ""
            if (!contentType.contains("application/json")) return emptyMap()
            val body = it.body?.string().orEmpty()
            return parseKodikLinks(body)
        }
    }

    private fun parseKodikLinks(body: String): Map<String, String> {
        val root = runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull()
            ?: return emptyMap()

        val links = root["links"]?.jsonObject ?: return emptyMap()
        val result = mutableMapOf<String, String>()
        for ((quality, sourcesJson) in links) {
            val array = sourcesJson.jsonArray
            if (array.isEmpty()) continue
            val src = array[0].jsonObject["src"]?.jsonPrimitive?.contentOrNull ?: continue
            val url = if (isPlainKodikUrl(src)) src else decryptKodik(src)
            if (url.isEmpty()) continue
            result[quality] = if (url.startsWith("//")) "https:$url" else url
        }
        return result
    }

    private fun isPlainKodikUrl(src: String): Boolean {
        val regex =
            Regex("""//(get|cloud)\.(kodik-storage|solodcdn)\.com/useruploads/.*?/.*?/(240|360|480|720|1080)\.mp4:hls:manifest\.m3u8""")
        return regex.containsMatchIn(src)
    }

    private fun decryptKodik(src: String): String {
        val shifted = src.map { ch ->
            when (ch) {
                in 'A'..'Z' -> ((ch.code - 'A'.code + 18) % 26 + 'A'.code).toChar()
                in 'a'..'z' -> ((ch.code - 'a'.code + 18) % 26 + 'a'.code).toChar()
                else -> ch
            }
        }.joinToString("")
        return runCatching {
            Base64.decode(shifted, Base64.DEFAULT).toString(Charsets.UTF_8)
        }.getOrDefault("")
    }

    // ---------- Sibnet ----------

    private fun sibnet(url: String): String? {
        val page = fetch(url, mapOf("User-Agent" to DESKTOP_UA, "Referer" to url)) ?: return null
        val match = Regex("""src:\s*(".*?")""").find(page) ?: return null
        val path = match.groupValues[1].replace("\"", "")
        val full = if (path.startsWith("http")) path else "https://video.sibnet.ru$path"

        return runCatching {
            val response = client.newCall(
                Request.Builder()
                    .url(full)
                    .header("User-Agent", DESKTOP_UA)
                    .header("Referer", url)
                    .header("Origin", "https://video.sibnet.ru")
                    .build()
            ).execute()
            response.use {
                if (it.isSuccessful) it.request.url.toString() else null
            }
        }.getOrNull()
    }

    // ---------- AniLibria / Liberty ----------

    private fun anilibria(url: String): Map<String, String> {
        val id = Regex("""id=(\d+)""").find(url)?.groupValues?.get(1) ?: return emptyMap()
        val episode = Regex("""ep=(\d+)""").find(url)?.groupValues?.get(1)?.toIntOrNull()
            ?: return emptyMap()

        val body = fetch("https://aniliberty.top/api/v1/anime/releases/$id") ?: return emptyMap()
        val root = runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull()
            ?: return emptyMap()
        val episodes = root["episodes"]?.jsonArray ?: return emptyMap()

        val target = episodes.firstOrNull {
            it.jsonObject["ordinal"]?.jsonPrimitive?.intOrNull == episode
        } ?: return emptyMap()

        val obj = target.jsonObject
        return buildMap {
            for ((quality, key) in listOf("1080" to "hls_1080", "720" to "hls_720", "480" to "hls_480")) {
                obj[key]?.jsonPrimitive?.contentOrNull?.let { put(quality, it) }
            }
        }
    }

    // ---------- TSM / StudioMir ----------

    private fun studiomir(url: String): Map<String, String> {
        val ani = Regex("""[?&]ani=(\d+)""").find(url)?.groupValues?.get(1) ?: return emptyMap()
        val ep = Regex("""[?&]ep=(\d+)""").find(url)?.groupValues?.get(1)?.toIntOrNull() ?: return emptyMap()
        val body = fetch("https://api.studiomir.club/api?ani=$ani&apikey=$STUDIOMIR_API_KEY")
            ?: return emptyMap()
        val root = runCatching { json.parseToJsonElement(body).jsonArray }.getOrNull()
            ?: return emptyMap()
        val players = root.firstOrNull()?.jsonObject?.get("players")?.jsonObject ?: return emptyMap()
        val tsm = players["tsm"]?.jsonArray ?: return emptyMap()
        val target = tsm.firstOrNull {
            val obj = it.jsonObject
            obj["type"]?.jsonPrimitive?.contentOrNull == "TV" &&
                obj["episode"]?.jsonPrimitive?.intOrNull == ep
        } ?: return emptyMap()
        val hls = target.jsonObject["hls"]?.jsonObject ?: return emptyMap()
        return buildMap {
            hls.forEach { (quality, value) ->
                val q = quality.removeSuffix("p")
                if (q.isNotEmpty() && q.all(Char::isDigit)) put(q, value.jsonPrimitive.content)
            }
        }
    }

    // ---------- Anixora / прокси-парсеры (Alloha, CVH) ----------
    //
    // API-прокси (например baproxy-demo.ds1nc.ru) отдаёт эпизоды балансеров [Anixora] Alloha и
    // [Anixora] CVH в виде: https://host/cp/parser/cvh?token=<jwt>. GET по этому URL возвращает
    // {"code":0,"result":{"master":"...m3u8","quality":{"360":"...","480":"...",...}}}
    // с прямыми ссылками (okcdn.ru для CVH, hlsp-прокси для Alloha).

    private fun isProxyParserUrl(url: String): Boolean {
        val path = runCatching { url.toHttpUrl().encodedPath }.getOrNull().orEmpty()
        return path.contains("/cp/parser/")
    }

    private fun proxyParser(url: String): Map<String, String> {
        val body = fetch(url) ?: return emptyMap()
        val root = runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull()
            ?: return emptyMap()
        val result = root["result"]?.jsonObject ?: return emptyMap()
        val resultMap = LinkedHashMap<String, String>()
        result["quality"]?.jsonObject?.forEach { (quality, value) ->
            value.jsonPrimitive.contentOrNull?.let { resultMap[quality] = it }
        }
        result["master"]?.jsonPrimitive?.contentOrNull?.let { resultMap["auto"] = it }
        return resultMap
    }

    private const val DESKTOP_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    private const val STUDIOMIR_API_KEY = "80b2d3e9c4ff27eb2e924c4d38f7daec"

    /** CDN-хосты Alloha/CVH (и им подобные), с которых видео играется напрямую. */
    private val DIRECT_CDN_HOSTS = listOf(
        "csst.online",
        "sstrge.online",
        "secvideo1.online",
        "anixora",
        "alloha",
        "torlook",
        "vidcdn",
    )
}