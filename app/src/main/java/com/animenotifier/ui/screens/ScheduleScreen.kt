package com.animenotifier.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.animenotifier.data.local.AnimeEntity
import com.animenotifier.ui.AnimeViewModel
import com.animenotifier.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: AnimeViewModel) {
    val watchlist by viewModel.savedAnimeList.collectAsState()
    val selectedDay by viewModel.selectedScheduleDay.collectAsState()

    val daysOfWeek = listOf(
        1 to "MON",
        2 to "TUE",
        3 to "WED",
        4 to "THU",
        5 to "FRI",
        6 to "SAT",
        7 to "SUN"
    )

    // Filter anime airing on the selected day and sort chronologically by airing timestamp
    val filteredAnime = remember(watchlist, selectedDay) {
        watchlist
            .filter { it.airingDayOfWeek == selectedDay }
            .sortedBy { it.nextEpisodeAiringAt ?: Long.MAX_VALUE }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AIRING SCHEDULE",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceRoot
                )
            )
        },
        containerColor = SurfaceRoot
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Minimal Week-Strip Day Selector
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(daysOfWeek) { (dayInt, dayLabel) ->
                    val isSelected = (selectedDay == dayInt)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) AccentPrimary else SurfaceElevated)
                            .border(1.dp, if (isSelected) AccentPrimary else SurfaceBorder, RoundedCornerShape(6.dp))
                            .clickable { viewModel.setSelectedScheduleDay(dayInt) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredAnime.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved anime airing on ${daysOfWeek.find { it.first == selectedDay }?.second}.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAnime, key = { it.id }) { anime ->
                        MinimalScheduleAnimeCard(
                            anime = anime,
                            onClick = { viewModel.openAnimeDetailFromEntity(anime) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MinimalScheduleAnimeCard(
    anime: AnimeEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = anime.coverImage,
                contentDescription = anime.title,
                modifier = Modifier
                    .width(52.dp)
                    .height(78.dp) // 2:3 ratio
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anime.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                val timeText = anime.nextEpisodeAiringAt?.let { timestamp ->
                    val date = Date(timestamp * 1000L)
                    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                    sdf.format(date)
                } ?: "TBA"

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isSeries = anime.mediaType == "SERIES"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSeries) Color(0xFF1E3A8A) else AccentPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (isSeries) "SERIES" else "ANIME",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = if (isSeries) Color(0xFF93C5FD) else AccentPrimary
                        )
                    }

                    Text(
                        text = "EP ${anime.nextEpisodeNumber ?: "?"}".uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = AccentPrimary
                    )

                    Text(text = "•", fontSize = 10.sp, color = TextSecondary)

                    Text(
                        text = timeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    anime.studio?.let { studioName ->
                        Text(text = "•", fontSize = 10.sp, color = TextSecondary)
                        Text(
                            text = studioName.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
