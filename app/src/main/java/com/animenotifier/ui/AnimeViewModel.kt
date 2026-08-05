package com.animenotifier.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.animenotifier.data.local.AnimeDatabase
import com.animenotifier.data.local.AnimeEntity
import com.animenotifier.data.notification.AlarmScheduler
import com.animenotifier.data.remote.AniListApiService
import com.animenotifier.data.remote.AniListMedia
import com.animenotifier.data.repository.AnimeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class DiscoveryChip {
    SEARCH, AIRING_TODAY, TRENDING
}

class AnimeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AnimeDatabase.getDatabase(application)
    private val apiService = AniListApiService()
    private val alarmScheduler = AlarmScheduler(application)
    private val repository = AnimeRepository(
        application,
        database.animeDao(),
        apiService,
        alarmScheduler
    )

    val savedAnimeList: StateFlow<List<AnimeEntity>> = repository.savedAnimeList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    private var searchJob: Job? = null

    init {
        repository.setupBackgroundWorker()
        loadDiscoveryData(DiscoveryChip.TRENDING)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            _activeChip.value = DiscoveryChip.SEARCH
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(300) // 300ms debounce
                _isSearching.value = true
                val results = repository.searchAnime(query)
                _searchResults.value = results
                _isSearching.value = false
            }
        } else {
            if (_activeChip.value == DiscoveryChip.SEARCH) {
                onChipSelected(DiscoveryChip.TRENDING)
            }
        }
    }

    fun onChipSelected(chip: DiscoveryChip) {
        _activeChip.value = chip
        if (chip != DiscoveryChip.SEARCH) {
            _searchQuery.value = ""
        }
        loadDiscoveryData(chip)
    }

    private fun loadDiscoveryData(chip: DiscoveryChip) {
        viewModelScope.launch {
            _isSearching.value = true
            val results = when (chip) {
                DiscoveryChip.AIRING_TODAY -> repository.getAiringToday()
                DiscoveryChip.TRENDING -> repository.getTrendingThisSeason()
                DiscoveryChip.SEARCH -> _searchResults.value
            }
            _searchResults.value = results
            _isSearching.value = false
        }
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
