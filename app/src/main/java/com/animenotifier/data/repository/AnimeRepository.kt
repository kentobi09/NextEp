package com.animenotifier.data.repository

import android.content.Context
import androidx.work.*
import com.animenotifier.data.local.AnimeDao
import com.animenotifier.data.local.AnimeEntity
import com.animenotifier.data.notification.AlarmScheduler
import com.animenotifier.data.remote.AniListApiService
import com.animenotifier.data.remote.AniListMedia
import com.animenotifier.data.remote.TvMazeApiService
import com.animenotifier.data.worker.ScheduleUpdateWorker
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class MediaCategory(val label: String) {
    ALL("All"),
    ANIME("Anime"),
    SERIES("TV Series")
}

class AnimeRepository(
    private val context: Context,
    private val dao: AnimeDao,
    private val apiService: AniListApiService,
    private val tvMazeApiService: TvMazeApiService,
    private val alarmScheduler: AlarmScheduler
) {

    val savedAnimeList: Flow<List<AnimeEntity>> = dao.getAllSavedAnime()

    suspend fun search(
        query: String?,
        genre: String? = null,
        category: MediaCategory = MediaCategory.ALL
    ): List<AniListMedia> = coroutineScope {
        val cleanQuery = query?.trim()
        if (cleanQuery.isNullOrEmpty() && genre.isNullOrBlank()) return@coroutineScope emptyList()

        when (category) {
            MediaCategory.ANIME -> {
                apiService.searchAnime(cleanQuery, genre)
            }
            MediaCategory.SERIES -> {
                if (!cleanQuery.isNullOrEmpty()) {
                    tvMazeApiService.searchShows(cleanQuery)
                } else emptyList()
            }
            MediaCategory.ALL -> {
                val animeDeferred = async { apiService.searchAnime(cleanQuery, genre) }
                val tvDeferred = async {
                    if (!cleanQuery.isNullOrEmpty()) {
                        tvMazeApiService.searchShows(cleanQuery)
                    } else emptyList()
                }

                val animeResults = animeDeferred.await()
                val tvResults = tvDeferred.await()

                interleaveMedia(animeResults, tvResults)
            }
        }
    }

    suspend fun getAiringToday(category: MediaCategory = MediaCategory.ALL): List<AniListMedia> = coroutineScope {
        when (category) {
            MediaCategory.ANIME -> apiService.getAiringToday()
            MediaCategory.SERIES -> tvMazeApiService.getAiringToday()
            MediaCategory.ALL -> {
                val animeDeferred = async { apiService.getAiringToday() }
                val tvDeferred = async { tvMazeApiService.getAiringToday() }
                interleaveMedia(animeDeferred.await(), tvDeferred.await())
            }
        }
    }

    suspend fun getTrendingThisSeason(category: MediaCategory = MediaCategory.ALL): List<AniListMedia> = coroutineScope {
        when (category) {
            MediaCategory.ANIME -> apiService.getTrendingThisSeason()
            MediaCategory.SERIES -> tvMazeApiService.getAiringToday()
            MediaCategory.ALL -> {
                val animeDeferred = async { apiService.getTrendingThisSeason() }
                val tvDeferred = async { tvMazeApiService.getAiringToday() }
                interleaveMedia(animeDeferred.await(), tvDeferred.await())
            }
        }
    }

    suspend fun getTopAiring(category: MediaCategory = MediaCategory.ALL): List<AniListMedia> = coroutineScope {
        when (category) {
            MediaCategory.ANIME -> apiService.getTopAiring()
            MediaCategory.SERIES -> tvMazeApiService.getAiringToday()
            MediaCategory.ALL -> {
                val animeDeferred = async { apiService.getTopAiring() }
                val tvDeferred = async { tvMazeApiService.getAiringToday() }
                interleaveMedia(animeDeferred.await(), tvDeferred.await())
            }
        }
    }

    suspend fun getMediaById(id: Int): AniListMedia? {
        return if (id < 0) {
            tvMazeApiService.getShowById(-id)
        } else {
            apiService.getAnimeById(id)
        }
    }

    suspend fun getSavedAnimeById(id: Int): AnimeEntity? {
        return dao.getAnimeById(id)
    }

    suspend fun saveAnime(media: AniListMedia) {
        val nextEp = media.nextAiringEpisode
        val dayOfWeek = nextEp?.airingAt?.let { calculateDayOfWeek(it) }

        val entity = AnimeEntity(
            id = media.id,
            title = media.displayTitle(),
            coverImage = media.bestCoverImage(),
            bannerImage = media.bannerImage,
            synopsis = media.cleanDescription(),
            studio = media.primaryStudio(),
            durationMinutes = media.duration,
            genres = if (media.genres.isNotEmpty()) media.genres.joinToString(", ") else null,
            averageScore = media.averageScore,
            watchedEpisodes = 0,
            totalEpisodes = media.episodes,
            nextEpisodeNumber = nextEp?.episode,
            nextEpisodeAiringAt = nextEp?.airingAt,
            airingDayOfWeek = dayOfWeek,
            status = media.status,
            siteUrl = media.siteUrl,
            notificationsEnabled = true,
            alertLeadTimeMinutes = 0,
            mediaType = media.mediaType
        )

        dao.insertOrUpdate(entity)
        alarmScheduler.scheduleEpisodeAlarm(entity)
    }

    suspend fun removeAnime(id: Int) {
        alarmScheduler.cancelAlarm(id)
        dao.deleteById(id)
    }

    suspend fun incrementWatchedEpisode(id: Int) {
        dao.incrementWatchedEpisode(id)
    }

    suspend fun updateNotificationSettings(id: Int, enabled: Boolean, leadTimeMinutes: Int) {
        dao.updateNotificationSettings(id, enabled, leadTimeMinutes)
        val anime = dao.getAnimeById(id) ?: return
        if (enabled) {
            alarmScheduler.scheduleEpisodeAlarm(anime)
        } else {
            alarmScheduler.cancelAlarm(id)
        }
    }

    suspend fun refreshAllSchedules() {
        val list = dao.getAllSavedAnimeList()
        for (anime in list) {
            val updated = getMediaById(anime.id) ?: continue
            val nextEp = updated.nextAiringEpisode
            val dayOfWeek = nextEp?.airingAt?.let { calculateDayOfWeek(it) }

            val newEntity = anime.copy(
                title = updated.displayTitle(),
                coverImage = updated.bestCoverImage(),
                bannerImage = updated.bannerImage ?: anime.bannerImage,
                synopsis = updated.cleanDescription(),
                studio = updated.primaryStudio() ?: anime.studio,
                durationMinutes = updated.duration ?: anime.durationMinutes,
                genres = if (updated.genres.isNotEmpty()) updated.genres.joinToString(", ") else anime.genres,
                averageScore = updated.averageScore ?: anime.averageScore,
                totalEpisodes = updated.episodes ?: anime.totalEpisodes,
                nextEpisodeNumber = nextEp?.episode ?: anime.nextEpisodeNumber,
                nextEpisodeAiringAt = nextEp?.airingAt ?: anime.nextEpisodeAiringAt,
                airingDayOfWeek = dayOfWeek ?: anime.airingDayOfWeek,
                status = updated.status ?: anime.status,
                mediaType = updated.mediaType,
                updatedAt = System.currentTimeMillis()
            )
            dao.insertOrUpdate(newEntity)
            if (newEntity.notificationsEnabled) {
                alarmScheduler.scheduleEpisodeAlarm(newEntity)
            }
        }
    }

    fun setupBackgroundWorker() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<ScheduleUpdateWorker>(
                12, TimeUnit.HOURS
            )
            .setConstraints(constraints)
            .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "ScheduleUpdateWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun interleaveMedia(a: List<AniListMedia>, b: List<AniListMedia>): List<AniListMedia> {
        val result = mutableListOf<AniListMedia>()
        val maxLen = maxOf(a.size, b.size)
        for (i in 0 until maxLen) {
            if (i < a.size) result.add(a[i])
            if (i < b.size) result.add(b[i])
        }
        return result
    }

    private fun calculateDayOfWeek(timestampSeconds: Long): Int {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestampSeconds * 1000L
        }
        val calDay = calendar.get(Calendar.DAY_OF_WEEK)
        return when (calDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}
