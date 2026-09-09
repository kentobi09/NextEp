package com.animenotifier.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class AniListApiService {

    private val TAG = "AniListApiService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val anilistUrl = "https://graphql.anilist.co"
    private val kitsuBaseUrl = "https://kitsu.io/api/edge"

    private val mediaFields = """
        id
        title { romaji english }
        coverImage { extraLarge large medium }
        bannerImage
        description(asHtml: false)
        episodes
        duration
        status
        siteUrl
        genres
        averageScore
        studios(isMain: true) {
          nodes { name isAnimationStudio }
        }
        nextAiringEpisode { airingAt timeUntilAiring episode }
    """.trimIndent()

    suspend fun searchAnime(searchQuery: String?, genre: String? = null): List<AniListMedia> = withContext(Dispatchers.IO) {
        val query = """
            query (${'$'}search: String, ${'$'}genre: String) {
              Page(page: 1, perPage: 25) {
                media(search: ${'$'}search, genre: ${'$'}genre, type: ANIME, sort: [POPULARITY_DESC]) {
                  $mediaFields
                }
              }
            }
        """.trimIndent()

        val variables = buildJsonObject {
            if (!searchQuery.isNullOrBlank()) {
                put("search", searchQuery)
            }
            if (!genre.isNullOrBlank()) {
                put("genre", genre)
            }
        }

        val requestPayload = buildJsonObject {
            put("query", query)
            put("variables", variables)
        }.toString()

        try {
            val request = Request.Builder()
                .url(anilistUrl)
                .post(requestPayload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Origin", "https://anilist.co")
                .addHeader("Referer", "https://anilist.co/")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val parsed = json.decodeFromString<AniListResponse<PageData>>(body)
                        val media = parsed.data?.Page?.media
                        if (!media.isNullOrEmpty()) {
                            return@withContext media
                        }
                    }
                }
            }
            fallbackKitsuSearch(searchQuery, genre)
        } catch (e: Exception) {
            Log.e(TAG, "AniList search failed, using Kitsu fallback", e)
            fallbackKitsuSearch(searchQuery, genre)
        }
    }

    suspend fun getAiringToday(): List<AniListMedia> = withContext(Dispatchers.IO) {
        val query = """
            query {
              Page(page: 1, perPage: 25) {
                media(status: RELEASING, type: ANIME, sort: [POPULARITY_DESC]) {
                  $mediaFields
                }
              }
            }
        """.trimIndent()

        val requestPayload = buildJsonObject {
            put("query", query)
        }.toString()

        try {
            val request = Request.Builder()
                .url(anilistUrl)
                .post(requestPayload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Origin", "https://anilist.co")
                .addHeader("Referer", "https://anilist.co/")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val parsed = json.decodeFromString<AniListResponse<PageData>>(body)
                        val media = parsed.data?.Page?.media
                        if (!media.isNullOrEmpty()) {
                            return@withContext media
                        }
                    }
                }
            }
            fallbackKitsuAiringToday()
        } catch (e: Exception) {
            Log.e(TAG, "AniList getAiringToday failed, using Kitsu fallback", e)
            fallbackKitsuAiringToday()
        }
    }

    suspend fun getTrendingThisSeason(): List<AniListMedia> = withContext(Dispatchers.IO) {
        val query = """
            query {
              Page(page: 1, perPage: 25) {
                media(type: ANIME, sort: [TRENDING_DESC]) {
                  $mediaFields
                }
              }
            }
        """.trimIndent()

        val requestPayload = buildJsonObject {
            put("query", query)
        }.toString()

        try {
            val request = Request.Builder()
                .url(anilistUrl)
                .post(requestPayload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Origin", "https://anilist.co")
                .addHeader("Referer", "https://anilist.co/")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val parsed = json.decodeFromString<AniListResponse<PageData>>(body)
                        val media = parsed.data?.Page?.media
                        if (!media.isNullOrEmpty()) {
                            return@withContext media
                        }
                    }
                }
            }
            fallbackKitsuTrending()
        } catch (e: Exception) {
            Log.e(TAG, "AniList getTrendingThisSeason failed, using Kitsu fallback", e)
            fallbackKitsuTrending()
        }
    }

    suspend fun getTopAiring(): List<AniListMedia> = withContext(Dispatchers.IO) {
        val query = """
            query {
              Page(page: 1, perPage: 25) {
                media(status: RELEASING, type: ANIME, sort: [SCORE_DESC]) {
                  $mediaFields
                }
              }
            }
        """.trimIndent()

        val requestPayload = buildJsonObject {
            put("query", query)
        }.toString()

        try {
            val request = Request.Builder()
                .url(anilistUrl)
                .post(requestPayload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Origin", "https://anilist.co")
                .addHeader("Referer", "https://anilist.co/")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val parsed = json.decodeFromString<AniListResponse<PageData>>(body)
                        val media = parsed.data?.Page?.media
                        if (!media.isNullOrEmpty()) {
                            return@withContext media
                        }
                    }
                }
            }
            fallbackKitsuAiringToday()
        } catch (e: Exception) {
            Log.e(TAG, "AniList getTopAiring failed, using Kitsu fallback", e)
            fallbackKitsuAiringToday()
        }
    }

    suspend fun getAnimeById(id: Int): AniListMedia? = withContext(Dispatchers.IO) {
        val query = """
            query (${'$'}id: Int) {
              Media(id: ${'$'}id, type: ANIME) {
                $mediaFields
              }
            }
        """.trimIndent()

        val variables = buildJsonObject {
            put("id", id)
        }

        val requestPayload = buildJsonObject {
            put("query", query)
            put("variables", variables)
        }.toString()

        try {
            val request = Request.Builder()
                .url(anilistUrl)
                .post(requestPayload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Origin", "https://anilist.co")
                .addHeader("Referer", "https://anilist.co/")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val parsed = json.decodeFromString<AniListResponse<MediaData>>(body)
                        return@withContext parsed.data?.Media
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "AniList getAnimeById failed", e)
            null
        }
    }

    // --- Resilient Fallback Helpers using Kitsu API ---

    private fun fallbackKitsuSearch(searchQuery: String?, genre: String?): List<AniListMedia> {
        return try {
            val queryParams = mutableListOf<String>()
            if (!searchQuery.isNullOrBlank()) {
                queryParams.add("filter[text]=${URLEncoder.encode(searchQuery, "UTF-8")}")
            }
            if (!genre.isNullOrBlank()) {
                queryParams.add("filter[categories]=${URLEncoder.encode(genre.lowercase(), "UTF-8")}")
            }
            queryParams.add("page[limit]=20")

            val url = "$kitsuBaseUrl/anime?${queryParams.joinToString("&")}"
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/vnd.api+json")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    parseKitsuResponse(body)
                } else emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kitsu search fallback failed", e)
            emptyList()
        }
    }

    private fun fallbackKitsuTrending(): List<AniListMedia> {
        return try {
            val url = "$kitsuBaseUrl/trending/anime?limit=20"
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/vnd.api+json")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    parseKitsuResponse(body)
                } else emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kitsu trending fallback failed", e)
            emptyList()
        }
    }

    private fun fallbackKitsuAiringToday(): List<AniListMedia> {
        return try {
            val url = "$kitsuBaseUrl/anime?filter[status]=current&sort=-userCount&page[limit]=20"
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/vnd.api+json")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    parseKitsuResponse(body)
                } else emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kitsu airing fallback failed", e)
            emptyList()
        }
    }

    private fun parseKitsuResponse(jsonString: String): List<AniListMedia> {
        return try {
            val root = json.parseToJsonElement(jsonString).jsonObject
            val dataArray = root["data"]?.jsonArray ?: return emptyList()

            dataArray.mapNotNull { item ->
                val obj = item.jsonObject
                val idStr = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val id = idStr.toIntOrNull() ?: idStr.hashCode()

                val attrs = obj["attributes"]?.jsonObject ?: return@mapNotNull null
                val title = attrs["canonicalTitle"]?.jsonPrimitive?.content
                    ?: attrs["titles"]?.jsonObject?.get("en")?.jsonPrimitive?.content
                    ?: "Unknown Title"

                val posterObj = attrs["posterImage"]?.jsonObject
                val cover = posterObj?.get("large")?.jsonPrimitive?.content
                    ?: posterObj?.get("medium")?.jsonPrimitive?.content
                    ?: posterObj?.get("original")?.jsonPrimitive?.content
                    ?: ""

                val bannerObj = attrs["coverImage"]?.jsonObject
                val banner = bannerObj?.get("large")?.jsonPrimitive?.content
                    ?: bannerObj?.get("original")?.jsonPrimitive?.content

                val synopsis = attrs["synopsis"]?.jsonPrimitive?.content
                    ?: attrs["description"]?.jsonPrimitive?.content

                val episodeCount = attrs["episodeCount"]?.jsonPrimitive?.intOrNull
                val episodeLength = attrs["episodeLength"]?.jsonPrimitive?.intOrNull
                val status = attrs["status"]?.jsonPrimitive?.content?.uppercase()
                val score = attrs["averageRating"]?.jsonPrimitive?.content?.toDoubleOrNull()?.toInt()

                AniListMedia(
                    id = id,
                    title = MediaTitle(romaji = title, english = title),
                    coverImage = MediaCoverImage(extraLarge = cover, large = cover, medium = cover),
                    bannerImage = banner,
                    description = synopsis,
                    episodes = episodeCount,
                    duration = episodeLength,
                    status = status ?: "RELEASING",
                    siteUrl = "https://kitsu.io/anime/$idStr",
                    genres = emptyList(),
                    averageScore = score,
                    studios = null,
                    nextAiringEpisode = if (status == "CURRENT") {
                        val now = System.currentTimeMillis() / 1000L
                        AiringEpisode(airingAt = now + 86400 * 2, timeUntilAiring = 86400 * 2, episode = (episodeCount ?: 12))
                    } else null
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing Kitsu response", e)
            emptyList()
        }
    }
}
