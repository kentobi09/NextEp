package com.animenotifier.data.repository

import android.content.Context
import androidx.work.*
import com.animenotifier.data.local.AnimeDao
import com.animenotifier.data.local.AnimeEntity
import com.animenotifier.data.notification.AlarmScheduler
import com.animenotifier.data.remote.AniListApiService
import com.animenotifier.data.remote.AniListMedia
import com.animenotifier.data.worker.ScheduleUpdateWorker
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AnimeRepository(
    private val context: Context,
    private val dao: AnimeDao,
    private val apiService: AniListApiService,
    private val alarmScheduler: AlarmScheduler
) {

    val savedAnimeList: Flow<List<AnimeEntity>> = dao.getAllSavedAnime()

    suspend fun searchAnime(query: String): List<AniListMedia> {
        if (query.isBlank()) return emptyList()
        return apiService.searchAnime(query)
    }

    suspend fun getAiringToday(): List<AniListMedia> {
        return apiService.getAiringToday()
    }

    suspend fun getTrendingThisSeason(): List<AniListMedia> {
        return apiService.getTrendingThisSeason()
    }

    suspend fun getTopAiring(): List<AniListMedia> {
        return apiService.getTopAiring()
    }

    suspend fun getAnimeById(id: Int): AniListMedia? {
        return apiService.getAnimeById(id)
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
            watchedEpisodes = 0,
            totalEpisodes = media.episodes,
            nextEpisodeNumber = nextEp?.episode,
            nextEpisodeAiringAt = nextEp?.airingAt,
            airingDayOfWeek = dayOfWeek,
            status = media.status,
            siteUrl = media.siteUrl,
            notificationsEnabled = true,
            alertLeadTimeMinutes = 0
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
            val updated = apiService.getAnimeById(anime.id) ?: continue
            val nextEp = updated.nextAiringEpisode
            val dayOfWeek = nextEp?.airingAt?.let { calculateDayOfWeek(it) }

            val newEntity = anime.copy(
                title = updated.displayTitle(),
                coverImage = updated.bestCoverImage(),
                bannerImage = updated.bannerImage ?: anime.bannerImage,
                synopsis = updated.cleanDescription(),
                studio = updated.primaryStudio() ?: anime.studio,
                durationMinutes = updated.duration ?: anime.durationMinutes,
                totalEpisodes = updated.episodes ?: anime.totalEpisodes,
                nextEpisodeNumber = nextEp?.episode ?: anime.nextEpisodeNumber,
                nextEpisodeAiringAt = nextEp?.airingAt ?: anime.nextEpisodeAiringAt,
                airingDayOfWeek = dayOfWeek ?: anime.airingDayOfWeek,
                status = updated.status ?: anime.status,
                updatedAt = System.currentTimeMillis()
            )
            dao.insertOrUpdate(newEntity)
            if (newEntity.notificationsEnabled) {
                alarmScheduler.scheduleEpisodeAlarm(newEntity)
            }
        }
    }

    fun setupBackgroundWorker() {
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
