package com.animenotifier.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.animenotifier.data.local.AnimeDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val database = AnimeDatabase.getDatabase(context)
            val alarmScheduler = AlarmScheduler(context)

            CoroutineScope(Dispatchers.IO).launch {
                val savedAnimeList = database.animeDao().getAllSavedAnimeList()
                for (anime in savedAnimeList) {
                    if (anime.notificationsEnabled && anime.nextEpisodeAiringAt != null) {
                        alarmScheduler.scheduleEpisodeAlarm(anime)
                    }
                }
            }
        }
    }
}
