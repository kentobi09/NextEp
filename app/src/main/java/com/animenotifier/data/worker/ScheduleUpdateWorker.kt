package com.animenotifier.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.animenotifier.data.local.AnimeDatabase
import com.animenotifier.data.notification.AlarmScheduler
import com.animenotifier.data.remote.AniListApiService

class ScheduleUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AnimeDatabase.getDatabase(applicationContext)
        val dao = database.animeDao()
        val apiService = AniListApiService()
        val alarmScheduler = AlarmScheduler(applicationContext)

        return try {
            val savedList = dao.getAllSavedAnimeList()
            for (saved in savedList) {
                val updatedMedia = apiService.getAnimeById(saved.id) ?: continue

                val nextEp = updatedMedia.nextAiringEpisode
                val updatedEntity = saved.copy(
                    title = updatedMedia.displayTitle(),
                    coverImage = updatedMedia.bestCoverImage(),
                    totalEpisodes = updatedMedia.episodes ?: saved.totalEpisodes,
                    nextEpisodeNumber = nextEp?.episode ?: saved.nextEpisodeNumber,
                    nextEpisodeAiringAt = nextEp?.airingAt ?: saved.nextEpisodeAiringAt,
                    status = updatedMedia.status ?: saved.status,
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
