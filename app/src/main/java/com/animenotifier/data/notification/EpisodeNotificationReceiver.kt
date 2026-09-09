package com.animenotifier.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.animenotifier.MainActivity
import com.animenotifier.R

class EpisodeNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val animeId = intent.getIntExtra(AlarmScheduler.EXTRA_ANIME_ID, -1)
        val animeTitle = intent.getStringExtra(AlarmScheduler.EXTRA_ANIME_TITLE) ?: "Anime Alert"
        val episodeNum = intent.getIntExtra(AlarmScheduler.EXTRA_EPISODE_NUM, 1)
        val leadTimeMinutes = intent.getIntExtra(AlarmScheduler.EXTRA_LEAD_TIME_MINUTES, 0)

        createNotificationChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            animeId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (titleText, bodyText) = when {
            leadTimeMinutes >= 60 -> {
                val hours = leadTimeMinutes / 60
                "Episode Airing Soon! ⏰" to "$animeTitle Episode $episodeNum airs in $hours hour!"
            }
            leadTimeMinutes > 0 -> {
                "Episode Airing Soon! ⏰" to "$animeTitle Episode $episodeNum airs in $leadTimeMinutes minutes!"
            }
            else -> {
                "New Episode Airing! 🍿" to "$animeTitle Episode $episodeNum is out now!"
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titleText)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$bodyText Tap to view details."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(if (animeId != -1) animeId else System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Anime Episode Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new anime episode releases"
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "anime_episode_alerts"
    }
}
