package com.animenotifier.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.animenotifier.data.local.AnimeEntity

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleEpisodeAlarm(anime: AnimeEntity) {
        val airingAtSeconds = anime.nextEpisodeAiringAt ?: return
        val episodeNum = anime.nextEpisodeNumber ?: return
        if (!anime.notificationsEnabled) return

        val leadTimeMillis = anime.alertLeadTimeMinutes * 60 * 1000L
        val alarmTimeMillis = (airingAtSeconds * 1000L) - leadTimeMillis

        // Only schedule if alarm time is in the future
        if (alarmTimeMillis <= System.currentTimeMillis()) return

        if (!canScheduleExactAlarms()) {
            return
        }

        val intent = Intent(context, EpisodeNotificationReceiver::class.java).apply {
            putExtra(EXTRA_ANIME_ID, anime.id)
            putExtra(EXTRA_ANIME_TITLE, anime.title)
            putExtra(EXTRA_EPISODE_NUM, episodeNum)
            putExtra(EXTRA_COVER_IMAGE, anime.coverImage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            anime.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelAlarm(animeId: Int) {
        val intent = Intent(context, EpisodeNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            animeId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        const val EXTRA_ANIME_ID = "extra_anime_id"
        const val EXTRA_ANIME_TITLE = "extra_anime_title"
        const val EXTRA_EPISODE_NUM = "extra_episode_num"
        const val EXTRA_COVER_IMAGE = "extra_cover_image"
    }
}
