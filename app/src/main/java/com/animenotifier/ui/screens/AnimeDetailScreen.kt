package com.animenotifier.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.animenotifier.data.local.AnimeEntity
import com.animenotifier.data.remote.AniListMedia
import com.animenotifier.ui.AnimeViewModel
import com.animenotifier.ui.theme.*
import kotlinx.coroutines.delay

data class AnimeDetailModel(
    val id: Int,
    val title: String,
    val coverImage: String,
    val bannerImage: String?,
    val synopsis: String?,
    val studio: String?,
    val durationMinutes: Int?,
    val genres: List<String>,
    val averageScore: Int?,
    val status: String?,
    val totalEpisodes: Int?,
    val nextEpisodeNumber: Int?,
    val nextEpisodeAiringAt: Long?,
    val siteUrl: String?,
    val mediaType: String = "ANIME",
    val originalMedia: AniListMedia? = null
)

fun AniListMedia.toDetailModel(): AnimeDetailModel = AnimeDetailModel(
    id = id,
    title = displayTitle(),
    coverImage = bestCoverImage(),
    bannerImage = bannerImage,
    synopsis = cleanDescription(),
    studio = primaryStudio(),
    durationMinutes = duration,
    genres = genres,
    averageScore = averageScore,
    status = status,
    totalEpisodes = episodes,
    nextEpisodeNumber = nextAiringEpisode?.episode,
    nextEpisodeAiringAt = nextAiringEpisode?.airingAt,
    siteUrl = siteUrl,
    mediaType = mediaType,
    originalMedia = this
)

fun AnimeEntity.toDetailModel(): AnimeDetailModel = AnimeDetailModel(
    id = id,
    title = title,
    coverImage = coverImage,
    bannerImage = bannerImage,
    synopsis = synopsis,
    studio = studio,
    durationMinutes = durationMinutes,
    genres = genres?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
    averageScore = averageScore,
    status = status,
    totalEpisodes = totalEpisodes,
    nextEpisodeNumber = nextEpisodeNumber,
    nextEpisodeAiringAt = nextEpisodeAiringAt,
    siteUrl = siteUrl,
    mediaType = mediaType,
    originalMedia = null
)

fun AnimeDetailModel.toEntity(): AnimeEntity {
    val dayOfWeek = nextEpisodeAiringAt?.let {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = it * 1000L
        when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> 1
            java.util.Calendar.TUESDAY -> 2
            java.util.Calendar.WEDNESDAY -> 3
            java.util.Calendar.THURSDAY -> 4
            java.util.Calendar.FRIDAY -> 5
            java.util.Calendar.SATURDAY -> 6
            java.util.Calendar.SUNDAY -> 7
            else -> 1
        }
    }
    return AnimeEntity(
        id = id,
        title = title,
        coverImage = coverImage,
        bannerImage = bannerImage,
        synopsis = synopsis,
        studio = studio,
        durationMinutes = durationMinutes,
        genres = if (genres.isNotEmpty()) genres.joinToString(", ") else null,
        averageScore = averageScore,
        watchedEpisodes = 0,
        totalEpisodes = totalEpisodes,
        nextEpisodeNumber = nextEpisodeNumber,
        nextEpisodeAiringAt = nextEpisodeAiringAt,
        airingDayOfWeek = dayOfWeek,
        status = status,
        siteUrl = siteUrl,
        notificationsEnabled = true,
        alertLeadTimeMinutes = 0,
        mediaType = mediaType
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailScreen(
    detail: AnimeDetailModel,
    viewModel: AnimeViewModel,
    onBack: () -> Unit,
    onGenreClick: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val watchlist by viewModel.savedAnimeList.collectAsState()

    val savedEntity = remember(watchlist, detail.id) {
        watchlist.find { it.id == detail.id }
    }
    val isSaved = savedEntity != null

    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    Scaffold(
        containerColor = SurfaceRoot,
        bottomBar = {
            // Bottom Action Bar
            Surface(
                color = SurfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleSaveDetail(detail)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaved) SurfaceElevatedHigh else AccentPrimary
                        ),
                        border = if (isSaved) BorderStroke(1.dp, SurfaceBorder) else null
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSaved) "IN WATCHLIST" else "ADD TO WATCHLIST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Backdrop Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                AsyncImage(
                    model = detail.bannerImage ?: detail.coverImage,
                    contentDescription = detail.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Seamless gradient fade to SurfaceRoot
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    SurfaceRoot.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    SurfaceRoot.copy(alpha = 0.9f),
                                    SurfaceRoot
                                )
                            )
                        )
                )

                // Top Floating Bar (Back Button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceRoot.copy(alpha = 0.75f))
                            .border(1.dp, SurfaceBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Anime Header Info (Poster + Title + Metadata)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 2:3 Aspect Ratio Poster Art
                AsyncImage(
                    model = detail.coverImage,
                    contentDescription = detail.title,
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .background(SurfaceElevated),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        detail.averageScore?.let { score ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "$score%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        detail.status?.let { status ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, SurfaceBorder, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = status.replace("_", " ").uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = if (status.contains("RELEASING", true)) StatusLive else TextSecondary
                                )
                            }
                        }
                    }

                    detail.studio?.let { studio ->
                        val studioOrNetworkLabel = if (detail.mediaType == "SERIES") "NETWORK" else "STUDIO"
                        Text(
                            text = "$studioOrNetworkLabel: ${studio.uppercase()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Genre Chips
            if (detail.genres.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(detail.genres) { genre ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(6.dp))
                                .clickable { onGenreClick(genre) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = genre.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Upcoming Episode Countdown Card
            detail.nextEpisodeAiringAt?.let { airingAt ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-16).dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NEXT EPISODE ${detail.nextEpisodeNumber ?: ""}".trim(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = AccentPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatCountdown(airingAt, currentTimeMillis),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = AccentPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Synopsis Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-8).dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "SYNOPSIS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = detail.synopsis ?: "No synopsis provided.",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Metadata Details Grid
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailRow("TOTAL EPISODES", "${detail.totalEpisodes ?: "Unknown"}")
                        detail.durationMinutes?.let { DetailRow("EPISODE LENGTH", "$it Minutes") }
                        detail.studio?.let {
                            val rowLabel = if (detail.mediaType == "SERIES") "NETWORK / PLATFORM" else "ANIMATION STUDIO"
                            DetailRow(rowLabel, it)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

private fun formatCountdown(airingAt: Long, currentTimeMillis: Long): String {
    val diff = (airingAt * 1000L) - currentTimeMillis
    if (diff <= 0) return "Airing now! 🍿"

    val totalSeconds = diff / 1000
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (days > 0) {
        "Airs in ${days}d ${hours}h ${minutes}m"
    } else {
        String.format("Airs in %02dh %02dm %02ds", hours, minutes, seconds)
    }
}
