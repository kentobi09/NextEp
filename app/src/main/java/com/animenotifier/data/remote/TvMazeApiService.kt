package com.animenotifier.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class TvMazeApiService {

    private val TAG = "TvMazeApiService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val baseUrl = "https://api.tvmaze.com"

    suspend fun searchShows(query: String): List<AniListMedia> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "$baseUrl/search/shows?q=$encoded"
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "NextEp/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (response.isSuccessful && !body.isNullOrBlank()) {
                    val searchResults = json.decodeFromString<List<TvMazeSearchResult>>(body)
                    return@withContext searchResults.map { it.show.toMediaItem() }
                }
            }
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "TVMaze search failed for query: $query", e)
            emptyList()
        }
    }

    suspend fun getShowById(id: Int): AniListMedia? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/shows/$id?embed=nextepisode"
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "NextEp/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (response.isSuccessful && !body.isNullOrBlank()) {
                    val show = json.decodeFromString<TvMazeShow>(body)
                    return@withContext show.toMediaItem()
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "TVMaze getShowById failed for id: $id", e)
            null
        }
    }

    suspend fun getAiringToday(): List<AniListMedia> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/schedule?country=US"
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "NextEp/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (response.isSuccessful && !body.isNullOrBlank()) {
                    val scheduleItems = json.decodeFromString<List<TvMazeScheduleItem>>(body)
                    val seenIds = mutableSetOf<Int>()
                    val list = mutableListOf<AniListMedia>()
                    for (item in scheduleItems) {
                        if (seenIds.add(item.show.id)) {
                            val media = item.show.toMediaItem(item)
                            list.add(media)
                        }
                    }
                    return@withContext list.take(25)
                }
            }
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "TVMaze getAiringToday failed", e)
            emptyList()
        }
    }

    private fun TvMazeShow.toMediaItem(scheduledEpisode: TvMazeScheduleItem? = null): AniListMedia {
        val namespacedId = -this.id
        val networkOrChannel = this.network?.name ?: this.webChannel?.name
        val cleanSummary = this.summary?.replace(Regex("<[^>]*>"), "")?.trim() ?: "No synopsis available."
        val cover = this.image?.original ?: this.image?.medium ?: ""

        val nextEpData = scheduledEpisode?.let { ep ->
            val airingSeconds = parseAirstampToSeconds(ep.airstamp, ep.airdate, ep.airtime)
            airingSeconds?.let { sec ->
                val now = System.currentTimeMillis() / 1000L

                AiringEpisode(
                    airingAt = sec,
                    timeUntilAiring = maxOf(0L, sec - now),
                    episode = ep.number ?: 1
                )
            }
        } ?: this._embedded?.nextepisode?.let { ep ->
            val airingSeconds = parseAirstampToSeconds(ep.airstamp, ep.airdate, ep.airtime)
            airingSeconds?.let { sec ->
                val now = System.currentTimeMillis() / 1000L
                AiringEpisode(
                    airingAt = sec,
                    timeUntilAiring = maxOf(0L, sec - now),
                    episode = ep.number ?: 1
                )
            }
        }

        val scoreSequence = this.rating?.average?.let { (it * 10).toInt() }


        return AniListMedia(
            id = namespacedId,
            title = MediaTitle(romaji = this.name, english = this.name),
            coverImage = MediaCoverImage(extraLarge = cover, large = cover, medium = cover),
            bannerImage = cover,
            description = cleanSummary,
            episodes = null,
            duration = this.averageRuntime ?: this.runtime,
            status = mapTvMazeStatus(this.status),
            siteUrl = this.officialSite,
            studios = networkOrChannel?.let { StudioConnection(nodes = listOf(StudioNode(name = it, isAnimationStudio = false))) },
            genres = this.genres,
            averageScore = scoreSequence,
            nextAiringEpisode = nextEpData,
            mediaType = "SERIES"
        )
    }

    private fun mapTvMazeStatus(status: String?): String {
        return when (status?.lowercase()) {
            "running" -> "RELEASING"
            "ended" -> "FINISHED"
            "in development" -> "NOT_YET_RELEASED"
            else -> status?.uppercase() ?: "RELEASING"
        }
    }

    private fun parseAirstampToSeconds(airstamp: String?, airdate: String?, airtime: String?): Long? {
        if (!airstamp.isNullOrBlank()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
                val date = sdf.parse(airstamp)
                if (date != null) return date.time / 1000L
            } catch (e: Exception) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
                    val date = sdf.parse(airstamp)
                    if (date != null) return date.time / 1000L
                } catch (ignored: Exception) {}
            }
        }

        if (!airdate.isNullOrBlank()) {
            try {
                val timePart = if (!airtime.isNullOrBlank()) airtime else "20:00"
                val combined = "$airdate $timePart"
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("America/New_York")
                }
                val date = sdf.parse(combined)
                if (date != null) return date.time / 1000L
            } catch (ignored: Exception) {}
        }

        return null
    }
}
