package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.CachedFlightEntity
import com.example.data.remote.AviationstackApiService
import com.example.data.repository.FlightRepository
import com.example.data.repository.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FlightViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val flightDao = db.flightDao()

    // Base URL is read from .env (which includes the BuildConfig via maps secrets plugin), 
    // defaulting to http variant if not configured to prevent HTTPS errors on free tier.
    private val baseUrl = try {
        BuildConfig.AVIATIONSTACK_BASE_URL.ifBlank { "http://api.aviationstack.com/" }
    } catch (e: Exception) {
        "http://api.aviationstack.com/"
    }

    private val apiService = AviationstackApiService.create(baseUrl)
    private val repository = FlightRepository(flightDao, apiService)

    // SharedPreferences to track live requests count
    private val sharedPrefs = application.getSharedPreferences("aviation_prefs", Context.MODE_PRIVATE)

    // UI Input states
    val searchFlightNumber = MutableStateFlow("")
    val searchDepartureIata = MutableStateFlow("")
    val searchArrivalIata = MutableStateFlow("")

    // Tab state: 0 = Live search, 1 = Cache Vault, 2 = Watchlist
    val currentTab = MutableStateFlow(0)

    // Query state inside local cache page
    val cacheSearchQuery = MutableStateFlow("")

    // Live search state
    private val _liveSearchState = MutableStateFlow<Resource<List<CachedFlightEntity>>>(Resource.Success(emptyList()))
    val liveSearchState: StateFlow<Resource<List<CachedFlightEntity>>> = _liveSearchState.asStateFlow()

    // Flow of flights from Database
    val allCachedFlights: StateFlow<List<CachedFlightEntity>> = repository.allCachedFlights
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedFlights: StateFlow<List<CachedFlightEntity>> = repository.bookmarkedFlights
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered cached flights based on query
    val filteredCachedFlights: StateFlow<List<CachedFlightEntity>> = cacheSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allCachedFlights
            } else {
                repository.searchLocalCache(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // API Key availability info
    val isApiKeyConfigured: Boolean
        get() = !BuildConfig.AVIATIONSTACK_API_KEY.contains("YOUR_") &&
                !BuildConfig.AVIATIONSTACK_API_KEY.contains("MY_") &&
                BuildConfig.AVIATIONSTACK_API_KEY.isNotBlank()

    // Free Tier Live Search shield tracking
    private val _remainingFreeRequests = MutableStateFlow(100)
    val remainingFreeRequests: StateFlow<Int> = _remainingFreeRequests.asStateFlow()

    init {
        // Populate preloaded high-fidelity flights on first start if database is empty
        viewModelScope.launch {
            if (repository.getCacheCount() == 0) {
                repository.populateDemoData()
            }
        }
        // Load consumed requests counter
        val consumed = sharedPrefs.getInt("consumed_calls", 0)
        _remainingFreeRequests.value = (100 - consumed).coerceAtLeast(0)
    }

    fun searchFlightsLive() {
        val fNo = searchFlightNumber.value
        val dep = searchDepartureIata.value
        val arr = searchArrivalIata.value

        viewModelScope.launch {
            repository.searchFlightsLive(
                flightNumber = fNo,
                departureIata = dep,
                arrivalIata = arr
            ).collect { resource ->
                _liveSearchState.value = resource
                if (resource is Resource.Success) {
                    // Successful live search! Log request consumption
                    incrementConsumedCalls()
                }
            }
        }
    }

    private fun incrementConsumedCalls() {
        val current = sharedPrefs.getInt("consumed_calls", 0)
        val updated = current + 1
        sharedPrefs.edit().putInt("consumed_calls", updated).apply()
        _remainingFreeRequests.value = (100 - updated).coerceAtLeast(0)
    }

    fun toggleBookmark(flight: CachedFlightEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(flight.id, !flight.isBookmarked)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearNonBookmarkedCache()
        }
    }

    fun resetRequestShield() {
         sharedPrefs.edit().putInt("consumed_calls", 0).apply()
        _remainingFreeRequests.value = 100
    }
}
