package com.animenotifier.data.remote

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

class AniListApiService {

    private val TAG = "AniListApiService"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 12000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 12000
        }
        defaultRequest {
            header(
                HttpHeaders.UserAgent,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            )
            header("Origin", "https://anilist.co")
            header("Referer", "https://anilist.co/")
            header(HttpHeaders.Accept, "application/json")
        }
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
        studios(isMain: true) {
          nodes { name isAnimationStudio }
        }
        nextAiringEpisode { airingAt timeUntilAiring episode }
    """.trimIndent()

    suspend fun searchAnime(searchQuery: String): List<AniListMedia> {
        val query = """
            query (${'$'}search: String) {
              Page(page: 1, perPage: 25) {
                media(search: ${'$'}search, type: ANIME, sort: [POPULARITY_DESC]) {
                  $mediaFields
                }
              }
            }
        """.trimIndent()

        val variables = buildJsonObject {
            put("search", searchQuery)
        }

        val requestBody = buildJsonObject {
            put("query", query)
            put("variables", variables)
        }

        return try {
            val response: AniListResponse<PageData> = client.post(anilistUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val results = response.data?.Page?.media
            if (!results.isNullOrEmpty()) {
                results
            } else {
                fallbackKitsuSearch(searchQuery)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AniList search failed, attempting Kitsu fallback", e)
            fallbackKitsuSearch(searchQuery)
        }
    }

    suspend fun getAiringToday(): List<AniListMedia> {
        val query = """
            query {
              Page(page: 1, perPage: 25) {
                media(status: RELEASING, type: ANIME, sort: [POPULARITY_DESC]) {
                  $mediaFields
                }
              }
            }
        """.trimIndent()

        val requestBody = buildJsonObject {
            put("query", query)
        }

        return try {
            val response: AniListResponse<PageData> = client.post(anilistUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val results = response.data?.Page?.media
            if (!results.isNullOrEmpty()) {
                results
            } else {
                fallbackKitsuAiringToday()
            }
        } catch (e: Exception) {
            Log.e(TAG, "AniList getAiringToday failed, attempting Kitsu fallback", e)
            fallbackKitsuAiringToday()
        }
    }

    suspend fun getTrendingThisSeason(): List<AniListMedia> {
        val query = """
            query {
              Page(page: 1, perPage: 25) {
                media(type: ANIME, sort: [TRENDING_DESC]) {
                  $mediaFields
                }
              }
            }
        """.trimIndent()

        val requestBody = buildJsonObject {
            put("query", query)
        }

        return try {
            val response: AniListResponse<PageData> = client.post(anilistUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val results = response.data?.Page?.media
            if (!results.isNullOrEmpty()) {
                results
            } else {
                fallbackKitsuTrending()
            }
        } catch (e: Exception) {
            Log.e(TAG, "AniList getTrendingThisSeason failed, attempting Kitsu fallback", e)
            fallbackKitsuTrending()
        }
    }

    suspend fun getTopAiring(): List<AniListMedia> {
        val query = """
            query {
              Page(page: 1, perPage: 25) {
                media(status: RELEASING, type: ANIME, sort: [SCORE_DESC]) {
                  $mediaFields
                }
              }
            }
        """.trimIndent()

        val requestBody = buildJsonObject {
            put("query", query)
        }

        return try {
            val response: AniListResponse<PageData> = client.post(anilistUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val results = response.data?.Page?.media
            if (!results.isNullOrEmpty()) {
                results
            } else {
                fallbackKitsuAiringToday()
            }
        } catch (e: Exception) {
            Log.e(TAG, "AniList getTopAiring failed, attempting Kitsu fallback", e)
            fallbackKitsuAiringToday()
        }
    }

    suspend fun getAnimeById(id: Int): AniListMedia? {
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

        val requestBody = buildJsonObject {
            put("query", query)
            put("variables", variables)
        }

        return try {
            val response: AniListResponse<MediaData> = client.post(anilistUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            response.data?.Media
        } catch (e: Exception) {
            Log.e(TAG, "AniList getAnimeById failed", e)
            null
        }
    }

    // --- Resilient Fallback Helpers using Kitsu API ---

    private suspend fun fallbackKitsuSearch(searchQuery: String): List<AniListMedia> {
        return try {
            val encodedQuery = searchQuery.encodeURLQueryComponent()
            val url = "$kitsuBaseUrl/anime?filter[text]=$encodedQuery&page[limit]=20"
            val responseString: String = client.get(url) {
                header(HttpHeaders.Accept, "application/vnd.api+json")
            }.body()

            parseKitsuResponse(responseString)
        } catch (e: Exception) {
            Log.e(TAG, "Kitsu search fallback failed", e)
            emptyList()
        }
    }

    private suspend fun fallbackKitsuTrending(): List<AniListMedia> {
        return try {
            val url = "$kitsuBaseUrl/trending/anime?limit=20"
            val responseString: String = client.get(url) {
                header(HttpHeaders.Accept, "application/vnd.api+json")
            }.body()

            parseKitsuResponse(responseString)
        } catch (e: Exception) {
            Log.e(TAG, "Kitsu trending fallback failed", e)
            emptyList()
        }
    }

    private suspend fun fallbackKitsuAiringToday(): List<AniListMedia> {
        return try {
            val url = "$kitsuBaseUrl/anime?filter[status]=current&sort=-userCount&page[limit]=20"
            val responseString: String = client.get(url) {
                header(HttpHeaders.Accept, "application/vnd.api+json")
            }.body()

            parseKitsuResponse(responseString)
        } catch (e: Exception) {
            Log.e(TAG, "Kitsu airing fallback failed", e)
            emptyList()
        }
    }

    private fun parseKitsuResponse(jsonString: String): List<AniListMedia> {
        return try {
            val json = Json { ignoreUnknownKeys = true }
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
                    studios = null,
                    nextAiringEpisode = if (status == "CURRENT") {
                        // Generate weekly schedule estimate (e.g. 3 days from now)
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
