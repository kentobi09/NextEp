package com.animenotifier.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class TvMazeSearchResult(
    val score: Double? = null,
    val show: TvMazeShow
)

@Serializable
data class TvMazeShow(
    val id: Int,
    val name: String,
    val type: String? = null,
    val language: String? = null,
    val genres: List<String> = emptyList(),
    val status: String? = null,
    val runtime: Int? = null,
    val averageRuntime: Int? = null,
    val premiered: String? = null,
    val ended: String? = null,
    val officialSite: String? = null,
    val schedule: TvMazeSchedule? = null,
    val rating: TvMazeRating? = null,
    val network: TvMazeNetwork? = null,
    val webChannel: TvMazeWebChannel? = null,
    val image: TvMazeImage? = null,
    val summary: String? = null,
    val _embedded: TvMazeEmbedded? = null
)

@Serializable
data class TvMazeSchedule(
    val time: String? = null,
    val days: List<String> = emptyList()
)

@Serializable
data class TvMazeRating(
    val average: Double? = null
)

@Serializable
data class TvMazeNetwork(
    val id: Int? = null,
    val name: String? = null
)

@Serializable
data class TvMazeWebChannel(
    val id: Int? = null,
    val name: String? = null
)

@Serializable
data class TvMazeImage(
    val medium: String? = null,
    val original: String? = null
)

@Serializable
data class TvMazeEmbedded(
    val nextepisode: TvMazeEpisode? = null
)

@Serializable
data class TvMazeEpisode(
    val id: Int,
    val name: String? = null,
    val season: Int? = null,
    val number: Int? = null,
    val airdate: String? = null,
    val airtime: String? = null,
    val airstamp: String? = null,
    val runtime: Int? = null,
    val summary: String? = null
)

@Serializable
data class TvMazeScheduleItem(
    val id: Int,
    val name: String? = null,
    val season: Int? = null,
    val number: Int? = null,
    val airdate: String? = null,
    val airtime: String? = null,
    val airstamp: String? = null,
    val runtime: Int? = null,
    val show: TvMazeShow
)
