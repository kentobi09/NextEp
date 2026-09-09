package com.animenotifier.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    val selectedAnimeDetail by viewModel.selectedAnimeDetail.collectAsState()

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
                            onClick = { viewModel.selectAnimeForDetail(anime) }
                        )
                    }
                }
            }
        }
    }

    // ModalBottomSheet for Anime Details
    selectedAnimeDetail?.let { anime ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectAnimeForDetail(null) },
            containerColor = SurfaceElevated,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SurfaceBorder)
                )
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            AnimeDetailSheetContent(
                anime = anime,
                onClose = { viewModel.selectAnimeForDetail(null) }
            )
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

@Composable
fun AnimeDetailSheetContent(
    anime: AnimeEntity,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = anime.coverImage,
                contentDescription = anime.title,
                modifier = Modifier
                    .width(84.dp)
                    .height(126.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = anime.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )

                anime.studio?.let { studio ->
                    Text(
                        text = "STUDIO: ${studio.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                }

                anime.durationMinutes?.let { mins ->
                    Text(
                        text = "RUNTIME: $mins MIN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                }

                anime.status?.let { status ->
                    Text(
                        text = "STATUS: ${status.replace("_", " ").uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = StatusLive
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SYNOPSIS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = anime.synopsis ?: "No synopsis available.",
            fontSize = 13.sp,
            color = TextPrimary,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        anime.siteUrl?.let { url ->
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
            ) {
                Text(
                    text = "VIEW ON ANILIST",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
