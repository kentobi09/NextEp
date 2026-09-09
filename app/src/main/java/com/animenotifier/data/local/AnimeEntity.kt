package com.animenotifier.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_anime")
data class AnimeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val coverImage: String,
    val bannerImage: String? = null,
    val synopsis: String? = null,
    val studio: String? = null,
    val durationMinutes: Int? = null,
    val watchedEpisodes: Int = 0,
    val totalEpisodes: Int? = null,
    val nextEpisodeNumber: Int? = null,
    val nextEpisodeAiringAt: Long? = null, // Unix timestamp in seconds
    val airingDayOfWeek: Int? = null,      // 1 = Monday, ..., 7 = Sunday
    val status: String? = null,            // RELEASING, FINISHED, NOT_YET_RELEASED, etc.
    val siteUrl: String? = null,
    val notificationsEnabled: Boolean = true,
    val alertLeadTimeMinutes: Int = 0,     // 0 = At release time, 15 = 15 min before, 60 = 1 hr before
    val updatedAt: Long = System.currentTimeMillis()
)
