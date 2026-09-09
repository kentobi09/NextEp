package com.animenotifier.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.animenotifier.data.local.AnimeEntity
import com.animenotifier.ui.AnimeViewModel
import com.animenotifier.ui.components.ExactAlarmPermissionBanner
import com.animenotifier.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AnimeViewModel,
    onNavigateToSearch: () -> Unit
) {
    val watchlist by viewModel.savedAnimeList.collectAsState()
    val heroAnime by viewModel.nextAiringHero.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showSettingsForAnime by remember { mutableStateOf<AnimeEntity?>(null) }

    val pullToRefreshState = rememberPullToRefreshState()
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refreshSchedules()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NEXTEP",
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
            ExactAlarmPermissionBanner()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                if (watchlist.isEmpty()) {
                    EmptyWatchlistState(onNavigateToSearch = onNavigateToSearch)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // Hero Airing Next Banner
                        heroAnime?.let { hero ->
                            item(key = "hero_banner") {
                                HeroAiringNextCard(
                                    anime = hero,
                                    onClick = { viewModel.openAnimeDetailFromEntity(hero) },
                                    onOpenSettings = { showSettingsForAnime = hero }
                                )
                            }
                        }

                        // Section Title
                        item(key = "section_header") {
                            Text(
                                text = "WATCHLIST",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }

                        // Watchlist Cards
                        items(watchlist, key = { it.id }) { anime ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                MinimalAnimeWatchlistCard(
                                    anime = anime,
                                    onClick = { viewModel.openAnimeDetailFromEntity(anime) },
                                    onOpenSettings = { showSettingsForAnime = anime }
                                )
                            }
                        }
                    }
                }

                PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = SurfaceElevated,
                    contentColor = AccentPrimary
                )
            }
        }
    }

    // Show Settings Dialog
    showSettingsForAnime?.let { anime ->
        MinimalNotificationSettingsDialog(
            anime = anime,
            onDismiss = { showSettingsForAnime = null },
            onSave = { enabled, leadTime ->
                viewModel.updateNotificationSettings(anime.id, enabled, leadTime)
                showSettingsForAnime = null
            },
            onDelete = {
                viewModel.removeSavedAnime(anime.id)
                showSettingsForAnime = null
            }
        )
    }
}

@Composable
fun HeroAiringNextCard(
    anime: AnimeEntity,
    onClick: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Background Image
            AsyncImage(
                model = anime.bannerImage ?: anime.coverImage,
                contentDescription = anime.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Scrim Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                SurfaceElevated.copy(alpha = 0.85f),
                                SurfaceElevated
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(StatusLive)
                        )
                        Text(
                            text = "AIRING NEXT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = StatusLive
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = anime.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val countdownText = remember(anime.nextEpisodeAiringAt, currentTimeMillis) {
                        calculateCountdownText(anime.nextEpisodeAiringAt, currentTimeMillis)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "EP ${anime.nextEpisodeNumber ?: "?"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = AccentPrimary
                        )

                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Text(
                            text = countdownText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MinimalAnimeWatchlistCard(
    anime: AnimeEntity,
    onClick: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Asymmetric Poster 2:3 Aspect Ratio
            AsyncImage(
                model = anime.coverImage,
                contentDescription = anime.title,
                modifier = Modifier
                    .width(72.dp)
                    .height(108.dp) // 2:3 ratio
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(108.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = anime.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Settings",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Micro-caps metadata tag
                    val isAiringSoon = anime.nextEpisodeAiringAt?.let {
                        (it * 1000L - currentTimeMillis) in 0..(24 * 3600 * 1000L)
                    } ?: false

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isAiringSoon) StatusLive else StatusUpcoming)
                        )

                        val nextEpText = anime.nextEpisodeNumber?.let { "EP $it" } ?: (anime.status ?: "AIRING")
                        Text(
                            text = nextEpText.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = TextSecondary
                        )

                        anime.nextEpisodeAiringAt?.let { ts ->
                            Text(text = "•", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = calculateCountdownText(ts, currentTimeMillis),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isAiringSoon) StatusLive else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun calculateCountdownText(airingAt: Long?, currentTimeMillis: Long): String {
    if (airingAt == null) return "TBA"
    val diff = (airingAt * 1000L) - currentTimeMillis
    if (diff <= 0) return "Airing now"

    val totalSeconds = diff / 1000
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (days > 0) {
        "${days}d ${hours}h"
    } else {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}

@Composable
fun EmptyWatchlistState(onNavigateToSearch: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "NO SAVED SHOWS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Track your favorite anime releases with exact alerts.",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateToSearch,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
        ) {
            Text(
                text = "EXPLORE ANIME",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}

@Composable
fun MinimalNotificationSettingsDialog(
    anime: AnimeEntity,
    onDismiss: () -> Unit,
    onSave: (enabled: Boolean, leadTime: Int) -> Unit,
    onDelete: () -> Unit
) {
    var enabled by remember { mutableStateOf(anime.notificationsEnabled) }
    var selectedLeadTime by remember { mutableStateOf(anime.alertLeadTimeMinutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ALERT PREFERENCES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = TextSecondary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = anime.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Push Notifications", fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = AccentPrimary,
                            uncheckedTrackColor = SurfaceElevatedHigh
                        )
                    )
                }

                if (enabled) {
                    Text(
                        text = "DELIVERY TIME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = TextSecondary
                    )

                    val options = listOf(
                        0 to "At air time",
                        15 to "15 minutes prior",
                        60 to "1 hour prior"
                    )

                    options.forEach { (leadTime, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLeadTime = leadTime }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = (selectedLeadTime == leadTime),
                                onClick = { selectedLeadTime = leadTime },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentPrimary)
                            )
                            Text(label, fontSize = 13.sp, color = TextPrimary)
                        }
                    }
                }

                HorizontalDivider(color = SurfaceBorder)

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPrimary),
                    border = BorderStroke(1.dp, AccentPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "REMOVE FROM WATCHLIST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(enabled, selectedLeadTime) },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
            ) {
                Text(
                    text = "SAVE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CANCEL",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(8.dp)
    )
}
