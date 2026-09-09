package com.animenotifier.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, String>? = null
)

@Serializable
data class AniListResponse<T>(
    val data: T? = null
)

@Serializable
data class PageData(
    val Page: MediaPage? = null
)

@Serializable
data class MediaData(
    val Media: AniListMedia? = null
)

@Serializable
data class MediaPage(
    val media: List<AniListMedia> = emptyList()
)

@Serializable
data class StudioNode(
    val name: String,
    val isAnimationStudio: Boolean = false
)

@Serializable
data class StudioConnection(
    val nodes: List<StudioNode> = emptyList()
)

@Serializable
data class AniListMedia(
    val id: Int,
    val title: MediaTitle? = null,
    val coverImage: MediaCoverImage? = null,
    val bannerImage: String? = null,
    val description: String? = null,
    val episodes: Int? = null,
    val duration: Int? = null,
    val status: String? = null,
    val siteUrl: String? = null,
    val studios: StudioConnection? = null,
    val nextAiringEpisode: AiringEpisode? = null
) {
    fun displayTitle(): String {
        return title?.english ?: title?.romaji ?: "Unknown Title"
    }

    fun bestCoverImage(): String {
        return coverImage?.extraLarge ?: coverImage?.large ?: coverImage?.medium ?: ""
    }

    fun cleanDescription(): String {
        return description?.replace(Regex("<[^>]*>"), "")?.trim() ?: "No synopsis available."
    }

    fun primaryStudio(): String? {
        return studios?.nodes?.firstOrNull { it.isAnimationStudio }?.name
            ?: studios?.nodes?.firstOrNull()?.name
    }
}

@Serializable
data class MediaTitle(
    val romaji: String? = null,
    val english: String? = null
)

@Serializable
data class MediaCoverImage(
    val extraLarge: String? = null,
    val large: String? = null,
    val medium: String? = null
)

@Serializable
data class AiringEpisode(
    val airingAt: Long, // Unix timestamp in seconds
    val timeUntilAiring: Long,
    val episode: Int
)
