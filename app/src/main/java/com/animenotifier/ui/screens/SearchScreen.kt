package com.animenotifier.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.animenotifier.data.remote.AniListMedia
import com.animenotifier.ui.AnimeViewModel
import com.animenotifier.ui.DiscoveryChip
import com.animenotifier.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: AnimeViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val activeChip by viewModel.activeChip.collectAsState()
    val watchlist by viewModel.savedAnimeList.collectAsState()

    val savedIds = remember(watchlist) { watchlist.map { it.id }.toSet() }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    placeholder = { Text("Search anime (e.g. Solo Leveling)...", color = TextMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = ElectricIndigo
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSlate,
                        unfocusedContainerColor = DarkSlate,
                        focusedBorderColor = ElectricIndigo,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Discovery Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = (activeChip == DiscoveryChip.TRENDING),
                            onClick = { viewModel.onChipSelected(DiscoveryChip.TRENDING) },
                            label = { Text("🔥 Trending") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricIndigo,
                                selectedLabelColor = Color.White,
                                containerColor = DarkSlate,
                                labelColor = TextSecondary
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = (activeChip == DiscoveryChip.AIRING_TODAY),
                            onClick = { viewModel.onChipSelected(DiscoveryChip.AIRING_TODAY) },
                            label = { Text("🍿 Airing Today") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCoral,
                                selectedLabelColor = Color.White,
                                containerColor = DarkSlate,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ElectricIndigo
                )
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No anime found for '$searchQuery'." else "Search for your favorite anime above.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(searchResults, key = { it.id }) { media ->
                        val isSaved = savedIds.contains(media.id)
                        AnimeSearchResultCard(
                            media = media,
                            isSaved = isSaved,
                            onToggleSave = { viewModel.toggleSaveAnime(media) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeSearchResultCard(
    media: AniListMedia,
    isSaved: Boolean,
    onToggleSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = if (isSaved) ElectricIndigo else BorderSubtle,
                shape = RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = media.bestCoverImage(),
                    contentDescription = media.displayTitle(),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Single Tap Add / Remove Toggle Button
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSaved) EmeraldGlow else DarkSlate.copy(alpha = 0.85f)
                        )
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = if (isSaved) "Saved" else "Save",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = media.displayTitle(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                media.nextAiringEpisode?.let { ep ->
                    Text(
                        text = "Ep ${ep.episode} upcoming",
                        fontSize = 11.sp,
                        color = ElectricIndigo,
                        fontWeight = FontWeight.SemiBold
                    )
                } ?: run {
                    Text(
                        text = media.status ?: "Finished",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
