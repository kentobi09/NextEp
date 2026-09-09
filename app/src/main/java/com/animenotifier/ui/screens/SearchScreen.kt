package com.animenotifier.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
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
                    .background(SurfaceRoot)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                // Clean icon-free text field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    placeholder = {
                        Text(
                            text = "Search titles, studios...",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated,
                        focusedBorderColor = TextPrimary,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chips = listOf(
                        DiscoveryChip.TRENDING,
                        DiscoveryChip.TOP_AIRING,
                        DiscoveryChip.AIRING_TODAY
                    )

                    items(chips.size) { index ->
                        val chip = chips[index]
                        val isSelected = (activeChip == chip)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AccentPrimary else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) AccentPrimary else SurfaceBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.onChipSelected(chip) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = chip.label.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }
        },
        containerColor = SurfaceRoot
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentPrimary,
                    strokeWidth = 2.dp
                )
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No anime found." else "Explore popular releases above.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(searchResults, key = { it.id }) { media ->
                        val isSaved = savedIds.contains(media.id)
                        MinimalSearchResultCard(
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
fun MinimalSearchResultCard(
    media: AniListMedia,
    isSaved: Boolean,
    onToggleSave: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 2:3 Aspect Ratio Portrait Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, if (isSaved) AccentPrimary.copy(alpha = 0.6f) else SurfaceBorder, RoundedCornerShape(8.dp))
                .background(SurfaceElevated)
        ) {
            AsyncImage(
                model = media.bestCoverImage(),
                contentDescription = media.displayTitle(),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Minimalist Single Tap Toggle Button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (isSaved) AccentPrimary else SurfaceRoot.copy(alpha = 0.85f))
                    .border(1.dp, if (isSaved) AccentPrimary else SurfaceBorder, CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleSave()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = if (isSaved) "Saved" else "Save",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = media.displayTitle(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Metadata micro-caps
        val meta = media.nextAiringEpisode?.let { "EP ${it.episode}" } ?: (media.status ?: "FINISHED")
        Text(
            text = meta.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextSecondary
        )
    }
}
