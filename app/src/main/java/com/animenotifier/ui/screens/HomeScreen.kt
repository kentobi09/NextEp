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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AnimeViewModel,
    onNavigateToSearch: () -> Unit
) {
    val watchlist by viewModel.savedAnimeList.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showSettingsForAnime by remember { mutableStateOf<AnimeEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(ElectricIndigo, NeonCoral)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "AnimeNotifier",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshSchedules() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Schedules",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ExactAlarmPermissionBanner()

            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = ElectricIndigo,
                    trackColor = DarkSlate
                )
            }

            if (watchlist.isEmpty()) {
                EmptyWatchlistState(onNavigateToSearch = onNavigateToSearch)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(watchlist, key = { it.id }) { anime ->
                        AnimeWatchlistCard(
                            anime = anime,
                            onIncrementWatched = { viewModel.incrementWatched(anime.id) },
                            onOpenSettings = { showSettingsForAnime = anime },
                            onDelete = { viewModel.removeSavedAnime(anime.id) }
                        )
                    }
                }
            }
        }
    }

    // Show Notification Settings Dialog
    showSettingsForAnime?.let { anime ->
        NotificationSettingsDialog(
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
fun AnimeWatchlistCard(
    anime: AnimeEntity,
    onIncrementWatched: () -> Unit,
    onOpenSettings: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cover Image
            AsyncImage(
                model = anime.coverImage,
                contentDescription = anime.title,
                modifier = Modifier
                    .width(90.dp)
                    .height(130.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )

            // Content Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = anime.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Settings",
                                tint = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Status Badge & Site Link
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        anime.status?.let { status ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = status.replace("_", " "),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyberCyan
                                )
                            }
                        }

                        anime.siteUrl?.let { url ->
                            Text(
                                text = "AniList ↗",
                                fontSize = 10.sp,
                                color = ElectricIndigoLight,
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }

                // Live Countdown Ticker
                LiveCountdownBadge(
                    airingAt = anime.nextEpisodeAiringAt,
                    episodeNumber = anime.nextEpisodeNumber
                )

                // Watched Progress Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val progressText = buildString {
                        append("Watched: Ep ${anime.watchedEpisodes}")
                        if (anime.totalEpisodes != null) {
                            append(" / ${anime.totalEpisodes}")
                        }
                    }

                    Text(
                        text = progressText,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = onIncrementWatched,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo.copy(alpha = 0.2f)),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add watched episode",
                                tint = ElectricIndigo,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "+1 Ep",
                                fontSize = 11.sp,
                                color = ElectricIndigo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveCountdownBadge(
    airingAt: Long?,
    episodeNumber: Int?
) {
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    if (airingAt == null || episodeNumber == null) {
        Text(
            text = "No upcoming episode date",
            fontSize = 11.sp,
            color = TextMuted
        )
        return
    }

    val targetMillis = airingAt * 1000L
    val diffMillis = targetMillis - currentTimeMillis

    if (diffMillis <= 0) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(EmeraldGlow.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Ep $episodeNumber: Airing Now! 🍿",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldGlow
            )
        }
    } else {
        val totalSeconds = diffMillis / 1000
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val countdownStr = buildString {
            if (days > 0) append("${days}d ")
            append(String.format("%02dh %02dm %02ds", hours, minutes, seconds))
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(ElectricIndigo.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = ElectricIndigo,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Ep $episodeNumber in $countdownStr",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ElectricIndigo
                )
            }
        }
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
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ElectricIndigo.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null,
                tint = ElectricIndigo,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your Watchlist is Empty",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Search and add your favorite anime to receive live countdowns & episode notifications.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateToSearch,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Search Anime Now", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun NotificationSettingsDialog(
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
                text = "Show Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = anime.title,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Notifications", fontSize = 14.sp)
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ElectricIndigo)
                    )
                }

                if (enabled) {
                    Text("Remind Me:", fontSize = 14.sp, fontWeight = FontWeight.Medium)

                    val options = listOf(
                        0 to "At Exact Airing Time",
                        15 to "15 Minutes Before",
                        60 to "1 Hour Before"
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
                                colors = RadioButtonDefaults.colors(selectedColor = ElectricIndigo)
                            )
                            Text(label, fontSize = 13.sp)
                        }
                    }
                }

                HorizontalDivider(color = BorderSubtle)

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCoral),
                    border = BorderStroke(1.dp, NeonCoral.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Remove from Watchlist", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(enabled, selectedLeadTime) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
