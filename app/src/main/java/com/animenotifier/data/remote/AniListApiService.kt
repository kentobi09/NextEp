package com.animenotifier.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AniListApiService {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
    }

    private val apiUrl = "https://graphql.anilist.co"

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
            val response: AniListResponse<PageData> = client.post(apiUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            response.data?.Page?.media ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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
            val response: AniListResponse<PageData> = client.post(apiUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            response.data?.Page?.media ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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
            val response: AniListResponse<PageData> = client.post(apiUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            response.data?.Page?.media ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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
            val response: AniListResponse<PageData> = client.post(apiUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            response.data?.Page?.media ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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
            val response: AniListResponse<MediaData> = client.post(apiUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            response.data?.Media
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
