package com.animenotifier.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.animenotifier.data.local.AnimeDatabase
import com.animenotifier.data.notification.AlarmScheduler
import com.animenotifier.data.remote.AniListApiService
import com.animenotifier.data.remote.TvMazeApiService

class ScheduleUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AnimeDatabase.getDatabase(applicationContext)
        val dao = database.animeDao()
        val aniListApiService = AniListApiService()
        val tvMazeApiService = TvMazeApiService()
        val alarmScheduler = AlarmScheduler(applicationContext)

        return try {
            val savedList = dao.getAllSavedAnimeList()
            for (saved in savedList) {
                val updatedMedia = if (saved.id < 0) {
                    tvMazeApiService.getShowById(-saved.id)
                } else {
                    aniListApiService.getAnimeById(saved.id)
                } ?: continue

                val nextEp = updatedMedia.nextAiringEpisode
                val updatedEntity = saved.copy(
                    title = updatedMedia.displayTitle(),
                    coverImage = updatedMedia.bestCoverImage(),
                    bannerImage = updatedMedia.bannerImage ?: saved.bannerImage,
                    synopsis = updatedMedia.cleanDescription(),
                    studio = updatedMedia.primaryStudio() ?: saved.studio,
                    durationMinutes = updatedMedia.duration ?: saved.durationMinutes,
                    totalEpisodes = updatedMedia.episodes ?: saved.totalEpisodes,
                    nextEpisodeNumber = nextEp?.episode ?: saved.nextEpisodeNumber,
                    nextEpisodeAiringAt = nextEp?.airingAt ?: saved.nextEpisodeAiringAt,
                    status = updatedMedia.status ?: saved.status,
                    mediaType = updatedMedia.mediaType,
                    updatedAt = System.currentTimeMillis()
                )

                dao.insertOrUpdate(updatedEntity)
                if (updatedEntity.notificationsEnabled) {
                    alarmScheduler.scheduleEpisodeAlarm(updatedEntity)
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
