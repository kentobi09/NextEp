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

val AnimeGenres = listOf(
    "Action",
    "Adventure",
    "Comedy",
    "Drama",
    "Fantasy",
    "Horror",
    "Mystery",
    "Romance",
    "Sci-Fi",
    "Slice of Life",
    "Sports",
    "Supernatural",
    "Thriller"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: AnimeViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val activeChip by viewModel.activeChip.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
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
                // Clean search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    placeholder = {
                        Text(
                            text = "Search anime, TV series, movies...",
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

                Spacer(modifier = Modifier.height(10.dp))

                // Media Category Filter Pills: [ ALL | ANIME | TV SERIES ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.animenotifier.data.repository.MediaCategory.values().forEach { category ->
                        val isSelected = (selectedCategory == category)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AccentPrimary else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) AccentPrimary else SurfaceBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.onCategorySelected(category) }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.label.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Discovery Category Chips
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
                        val isSelected = (activeChip == chip && selectedGenre == null && searchQuery.isEmpty())
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

                Spacer(modifier = Modifier.height(8.dp))

                // Genre Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(AnimeGenres) { genre ->
                        val isSelected = (selectedGenre == genre)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) AccentPrimary.copy(alpha = 0.85f) else SurfaceElevatedHigh)
                                .border(
                                    1.dp,
                                    if (isSelected) AccentPrimary else SurfaceBorder,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { viewModel.onGenreSelected(genre) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = genre.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
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
                        text = if (searchQuery.isNotBlank() || selectedGenre != null) {
                            "No anime found matching criteria."
                        } else {
                            "Explore popular releases or filter by genre above."
                        },
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
                            onClick = { viewModel.openAnimeDetail(media) },
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
    onClick: () -> Unit,
    onToggleSave: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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

            // Media Type Tag (SERIES vs ANIME)
            val isSeries = media.mediaType == "SERIES"
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SurfaceRoot.copy(alpha = 0.85f))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isSeries) "SERIES" else "ANIME",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (isSeries) Color(0xFF60A5FA) else AccentPrimary
                )
            }

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

        // Metadata micro-caps (genre or episode)
        val meta = if (media.genres.isNotEmpty()) {
            media.genres.take(2).joinToString(" • ")
        } else {
            media.nextAiringEpisode?.let { "EP ${it.episode}" } ?: (media.status ?: "FINISHED")
        }
        Text(
            text = meta.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
