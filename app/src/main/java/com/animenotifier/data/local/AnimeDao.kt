package com.animenotifier.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    @Query("SELECT * FROM saved_anime ORDER BY updatedAt DESC")
    fun getAllSavedAnime(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM saved_anime")
    suspend fun getAllSavedAnimeList(): List<AnimeEntity>

    @Query("SELECT * FROM saved_anime WHERE id = :id")
    suspend fun getAnimeById(id: Int): AnimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(anime: AnimeEntity)

    @Query("DELETE FROM saved_anime WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE saved_anime SET watchedEpisodes = watchedEpisodes + 1 WHERE id = :id")
    suspend fun incrementWatchedEpisode(id: Int)

    @Query("UPDATE saved_anime SET notificationsEnabled = :enabled, alertLeadTimeMinutes = :leadTimeMinutes WHERE id = :id")
    suspend fun updateNotificationSettings(id: Int, enabled: Boolean, leadTimeMinutes: Int)
}
