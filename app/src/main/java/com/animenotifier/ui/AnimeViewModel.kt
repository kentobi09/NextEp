package com.animenotifier.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.animenotifier.data.local.AnimeDatabase
import com.animenotifier.data.local.AnimeEntity
import com.animenotifier.data.notification.AlarmScheduler
import com.animenotifier.data.remote.AniListApiService
import com.animenotifier.data.remote.AniListMedia
import com.animenotifier.data.remote.TvMazeApiService
import com.animenotifier.data.repository.AnimeRepository
import com.animenotifier.data.repository.MediaCategory
import com.animenotifier.ui.screens.AnimeDetailModel
import com.animenotifier.ui.screens.toDetailModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class DiscoveryChip(val label: String) {
    TRENDING("Trending This Season"),
    TOP_AIRING("Top Airing"),
    AIRING_TODAY("Airing Today"),
    SEARCH("Search")
}

class AnimeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AnimeDatabase.getDatabase(application)
    private val apiService = AniListApiService()
    private val tvMazeApiService = TvMazeApiService()
    private val alarmScheduler = AlarmScheduler(application)
    private val repository = AnimeRepository(
        application,
        database.animeDao(),
        apiService,
        tvMazeApiService,
        alarmScheduler
    )

    val savedAnimeList: StateFlow<List<AnimeEntity>> = repository.savedAnimeList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingAiringList: StateFlow<List<AnimeEntity>> = savedAnimeList.map { list ->
        val now = System.currentTimeMillis() / 1000L
        val upcoming = list.filter { it.nextEpisodeAiringAt != null && it.nextEpisodeAiringAt > now }
            .sortedBy { it.nextEpisodeAiringAt!! }
        if (upcoming.isNotEmpty()) {
            upcoming
        } else {
            list.filter { it.nextEpisodeAiringAt != null }
                .ifEmpty { list.take(1) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MediaCategory.ALL)
    val selectedCategory: StateFlow<MediaCategory> = _selectedCategory.asStateFlow()

    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()

    private val _searchResults = MutableStateFlow<List<AniListMedia>>(emptyList())
    val searchResults: StateFlow<List<AniListMedia>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _activeChip = MutableStateFlow(DiscoveryChip.TRENDING)
    val activeChip: StateFlow<DiscoveryChip> = _activeChip.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedScheduleDay = MutableStateFlow(currentDayOfWeek())
    val selectedScheduleDay: StateFlow<Int> = _selectedScheduleDay.asStateFlow()

    private val _activeDetail = MutableStateFlow<AnimeDetailModel?>(null)
    val activeDetail: StateFlow<AnimeDetailModel?> = _activeDetail.asStateFlow()

    private var searchJob: Job? = null


    init {
        repository.setupBackgroundWorker()
        loadDiscoveryData(DiscoveryChip.TRENDING, _selectedCategory.value)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _activeChip.value = DiscoveryChip.SEARCH
        executeSearch(query, _selectedGenre.value, _selectedCategory.value)
    }

    fun onCategorySelected(category: MediaCategory) {
        _selectedCategory.value = category
        if (_searchQuery.value.isNotBlank() || _selectedGenre.value != null) {
            executeSearch(_searchQuery.value, _selectedGenre.value, category)
        } else {
            loadDiscoveryData(_activeChip.value, category)
        }
    }

    fun onGenreSelected(genre: String?) {
        _selectedGenre.value = if (_selectedGenre.value == genre) null else genre
        _activeChip.value = DiscoveryChip.SEARCH
        executeSearch(_searchQuery.value, _selectedGenre.value, _selectedCategory.value)
    }


    private fun executeSearch(query: String?, genre: String?, category: MediaCategory) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            _isSearching.value = true
            val results = repository.search(query, genre, category)
            _searchResults.value = results
            _isSearching.value = false
        }
    }


    fun onChipSelected(chip: DiscoveryChip) {
        _activeChip.value = chip
        if (chip != DiscoveryChip.SEARCH) {
            _searchQuery.value = ""
            _selectedGenre.value = null
        }
        loadDiscoveryData(chip, _selectedCategory.value)
    }


    private fun loadDiscoveryData(chip: DiscoveryChip, category: MediaCategory) {
        viewModelScope.launch {
            _isSearching.value = true
            val results = when (chip) {
                DiscoveryChip.AIRING_TODAY -> repository.getAiringToday(category)
                DiscoveryChip.TRENDING -> repository.getTrendingThisSeason(category)
                DiscoveryChip.TOP_AIRING -> repository.getTopAiring(category)
                DiscoveryChip.SEARCH -> _searchResults.value
            }
            _searchResults.value = results
            _isSearching.value = false
        }
    }


    fun openAnimeDetail(media: AniListMedia) {
        _activeDetail.value = media.toDetailModel()
    }


    fun openAnimeDetailFromEntity(entity: AnimeEntity) {
        _activeDetail.value = entity.toDetailModel()
    }


    fun closeAnimeDetail() {
        _activeDetail.value = null
    }


    fun toggleSaveAnime(media: AniListMedia) {
        viewModelScope.launch {
            val isAlreadySaved = savedAnimeList.value.any { it.id == media.id }
            if (isAlreadySaved) {
                repository.removeAnime(media.id)
            } else {
                repository.saveAnime(media)
            }
        }
    }


    fun removeSavedAnime(animeId: Int) {
        viewModelScope.launch {
            repository.removeAnime(animeId)
        }
    }


    fun incrementWatched(animeId: Int) {
        viewModelScope.launch {
            repository.incrementWatchedEpisode(animeId)
        }
    }


    fun updateNotificationSettings(animeId: Int, enabled: Boolean, leadTimeMinutes: Int) {
        viewModelScope.launch {
            repository.updateNotificationSettings(animeId, enabled, leadTimeMinutes)
        }
    }

    fun refreshSchedules() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshAllSchedules()
            _isRefreshing.value = false
        }
    }


    fun setSelectedScheduleDay(day: Int) {
        _selectedScheduleDay.value = day
    }

    private fun currentDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}
