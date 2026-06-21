package com.bytecoders.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bytecoders.BuildConfig
import com.bytecoders.R
import com.bytecoders.getAirportCoords
import com.bytecoders.data.local.AppDatabase
import com.bytecoders.data.local.CachedFlightEntity
import com.bytecoders.data.remote.AviationstackApiService
import com.bytecoders.data.remote.OpenMeteoApiService
import com.bytecoders.data.model.OpenMeteoResponse
import com.bytecoders.data.repository.FlightRepository
import com.bytecoders.data.repository.Resource
import com.bytecoders.util.UiText
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.channels.Channel

data class FavoriteFlightStatus(
    val flightNumber: String,
    val status: String?,          // e.g. "active", "scheduled", "cancelled", etc.
    val departureDelay: Int?,     // minutes
    val arrivalDelay: Int?,       // minutes
    val lastChecked: Long? = null,
    val isPolling: Boolean = false
)

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
    private val weatherApiService = OpenMeteoApiService.create()
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
    val liveSearchState: StateFlow<Resource<List<CachedFlightEntity>>> = combine(
        _liveSearchState,
        repository.bookmarkedFlights
    ) { state, bookmarked ->
        if (state is Resource.Success) {
            val bookmarkedIds = bookmarked.map { it.id }.toSet()
            val updatedList = state.data.map { flight ->
                flight.copy(isBookmarked = bookmarkedIds.contains(flight.id))
            }
            Resource.Success(updatedList)
        } else {
            state
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Success(emptyList()))

    // Upcoming flight for details state flow
    private val _upcomingFlightForDetail = MutableStateFlow<Resource<CachedFlightEntity?>>(Resource.Success(null))
    val upcomingFlightForDetail: StateFlow<Resource<CachedFlightEntity?>> = combine(
        _upcomingFlightForDetail,
        repository.bookmarkedFlights
    ) { state, bookmarked ->
        if (state is Resource.Success && state.data != null) {
            val isBookmarked = bookmarked.any { it.id == state.data.id }
            Resource.Success(state.data.copy(isBookmarked = isBookmarked))
        } else {
            state
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Success(null))

    // Destination weather for details state flow
    private val _destinationWeather = MutableStateFlow<Resource<OpenMeteoResponse?>>(Resource.Success(null))
    val destinationWeather: StateFlow<Resource<OpenMeteoResponse?>> = _destinationWeather.asStateFlow()

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

    // API Key flow tracking user entered keys.
    val userCustomApiKey = MutableStateFlow("")

    // API Key availability info
    val isApiKeyConfigured: StateFlow<Boolean> = userCustomApiKey
        .map { key ->
            true // Always true due to fallback testing API key
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun getUserApiKey(): String {
        return sharedPrefs.getString("user_api_key", "") ?: ""
    }

    fun saveUserApiKey(key: String) {
        sharedPrefs.edit().putString("user_api_key", key).apply()
        userCustomApiKey.value = key
    }

    // Free Tier Live Search shield tracking
    private val _remainingFreeRequests = MutableStateFlow(100)
    val remainingFreeRequests: StateFlow<Int> = _remainingFreeRequests.asStateFlow()

    // Favorites and Polling State
    private val _favoriteFlightNumbers = MutableStateFlow<Set<String>>(emptySet())
    val favoriteFlightNumbers: StateFlow<Set<String>> = _favoriteFlightNumbers.asStateFlow()

    private val _favoriteFlightStatuses = MutableStateFlow<Map<String, FavoriteFlightStatus>>(emptyMap())
    val favoriteFlightStatuses: StateFlow<Map<String, FavoriteFlightStatus>> = _favoriteFlightStatuses.asStateFlow()

    private val _isPeriodicPollingActive = MutableStateFlow(true)
    val isPeriodicPollingActive: StateFlow<Boolean> = _isPeriodicPollingActive.asStateFlow()

    private val _nextPollTime = MutableStateFlow<Long?>(null)
    val nextPollTime: StateFlow<Long?> = _nextPollTime.asStateFlow()

    private val _lastNotifiedStates = MutableStateFlow<Map<String, String>>(emptyMap())

    val toastChannel = Channel<String>(Channel.BUFFERED)
    val toastChannelFlow = toastChannel.receiveAsFlow()

    private val pollingIntervalMs = 45_000L

    init {
        // Load custom user API key
        userCustomApiKey.value = getUserApiKey()

        // Populate preloaded high-fidelity flights on first start if database is empty
        viewModelScope.launch {
            if (repository.getCacheCount() == 0) {
                repository.populateDemoData()
            }
        }
        // Load consumed requests counter
        val consumed = sharedPrefs.getInt("consumed_calls", 0)
        _remainingFreeRequests.value = (100 - consumed).coerceAtLeast(0)

        // Load favorites and start background scheduler
        loadFavorites()
        startPeriodicPolling()
    }

    fun searchFlightsLive() {
        val fNo = searchFlightNumber.value
        val dep = searchDepartureIata.value
        val arr = searchArrivalIata.value
        val customKey = getUserApiKey().ifBlank { null }

        viewModelScope.launch {
            repository.searchFlightsLive(
                flightNumber = fNo,
                departureIata = dep,
                arrivalIata = arr,
                apiKeyOverride = customKey
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

    fun fetchUpcomingFlight(departureIata: String, currentFlightId: String) {
        if (departureIata.isBlank() || departureIata == "N/A") {
            _upcomingFlightForDetail.value = Resource.Success(null)
            return
        }

        _upcomingFlightForDetail.value = Resource.Loading
        val customKey = getUserApiKey().ifBlank { null }

        viewModelScope.launch {
            repository.searchUpcomingDeparture(departureIata, currentFlightId, customKey).collect { resource ->
                _upcomingFlightForDetail.value = resource
            }
        }
    }

    fun fetchDestinationWeather(arrivalIata: String?) {
        if (arrivalIata.isNullOrBlank()) {
            _destinationWeather.value = Resource.Success(null)
            return
        }

        _destinationWeather.value = Resource.Loading
        viewModelScope.launch {
            try {
                val coords = getAirportCoords(arrivalIata)
                val response = weatherApiService.getCurrentWeather(latitude = coords.first, longitude = coords.second)
                _destinationWeather.value = Resource.Success(response)
            } catch (e: Exception) {
                val errorText = UiText.StringResource(R.string.weather_load_error, e.localizedMessage ?: e.message ?: "")
                _destinationWeather.value = Resource.Error(errorText, e)
            }
        }
    }

    fun loadFavorites() {
        val favs = sharedPrefs.getStringSet("favorite_flight_numbers", emptySet()) ?: emptySet()
        _favoriteFlightNumbers.value = favs
        val currentStatuses = _favoriteFlightStatuses.value.toMutableMap()
        favs.forEach { flightNo ->
            if (!currentStatuses.containsKey(flightNo)) {
                currentStatuses[flightNo] = FavoriteFlightStatus(flightNumber = flightNo, status = null, departureDelay = null, arrivalDelay = null)
            }
        }
        currentStatuses.keys.retainAll(favs)
        _favoriteFlightStatuses.value = currentStatuses
    }

    fun addFavoriteFlightNumber(flightNo: String) {
        val clean = flightNo.trim().uppercase()
        if (clean.isBlank()) return
        val current = _favoriteFlightNumbers.value.toSet()
        val updated = current + clean
        sharedPrefs.edit().putStringSet("favorite_flight_numbers", updated).apply()
        _favoriteFlightNumbers.value = updated
        
        val currentStatuses = _favoriteFlightStatuses.value.toMutableMap()
        if (!currentStatuses.containsKey(clean)) {
            currentStatuses[clean] = FavoriteFlightStatus(flightNumber = clean, status = null, departureDelay = null, arrivalDelay = null)
            _favoriteFlightStatuses.value = currentStatuses
        }
        pollSingleFavorite(clean)
    }

    fun removeFavoriteFlightNumber(flightNo: String) {
        val clean = flightNo.trim().uppercase()
        val current = _favoriteFlightNumbers.value.toSet()
        val updated = current - clean
        sharedPrefs.edit().putStringSet("favorite_flight_numbers", updated).apply()
        _favoriteFlightNumbers.value = updated
        
        val currentStatuses = _favoriteFlightStatuses.value.toMutableMap()
        currentStatuses.remove(clean)
        _favoriteFlightStatuses.value = currentStatuses
    }

    fun togglePeriodicPolling() {
        _isPeriodicPollingActive.value = !_isPeriodicPollingActive.value
    }

    fun pollSingleFavorite(flightNo: String) {
        viewModelScope.launch {
            val currentStatuses = _favoriteFlightStatuses.value.toMutableMap()
            val oldStatus = currentStatuses[flightNo]
            currentStatuses[flightNo] = oldStatus?.copy(isPolling = true) ?: FavoriteFlightStatus(flightNumber = flightNo, status = null, departureDelay = null, arrivalDelay = null, isPolling = true)
            _favoriteFlightStatuses.value = currentStatuses

            val customKey = getUserApiKey().ifBlank { null }
            val resolvedKey = customKey ?: when {
                !BuildConfig.AVIATIONSTACK_API_KEY.contains("YOUR_") &&
                !BuildConfig.AVIATIONSTACK_API_KEY.contains("MY_") &&
                BuildConfig.AVIATIONSTACK_API_KEY.isNotBlank() -> BuildConfig.AVIATIONSTACK_API_KEY
                else -> null
            }

            if (resolvedKey == null) {
                val updatedStatuses = _favoriteFlightStatuses.value.toMutableMap()
                updatedStatuses[flightNo] = (updatedStatuses[flightNo] ?: FavoriteFlightStatus(flightNo, null, null, null)).copy(isPolling = false)
                _favoriteFlightStatuses.value = updatedStatuses
                return@launch
            }

            try {
                val hasLetters = flightNo.any { it.isLetter() }
                val response = if (hasLetters) {
                    apiService.getFlights(apiKey = resolvedKey, flightIata = flightNo)
                } else {
                    apiService.getFlights(apiKey = resolvedKey, flightNumber = flightNo)
                }

                val flightDataList = response.data
                if (flightDataList != null && flightDataList.isNotEmpty()) {
                    val flightData = flightDataList.first()
                    val status = flightData.flightStatus ?: "unknown"
                    val depDelay = flightData.departure?.delay
                    val arrDelay = flightData.arrival?.delay

                    val isCancelled = status.lowercase() == "cancelled"
                    val isDelayedStatus = status.lowercase() == "delayed"
                    val activeDepDelay = depDelay ?: 0
                    val activeArrDelay = arrDelay ?: 0
                    val isDelayedByTime = activeDepDelay > 0 || activeArrDelay > 0

                    val updatedStatuses = _favoriteFlightStatuses.value.toMutableMap()
                    updatedStatuses[flightNo] = FavoriteFlightStatus(
                        flightNumber = flightNo,
                        status = status,
                        departureDelay = depDelay,
                        arrivalDelay = arrDelay,
                        lastChecked = System.currentTimeMillis(),
                        isPolling = false
                    )
                    _favoriteFlightStatuses.value = updatedStatuses

                    // Trigger alert if state changed to delayed/cancelled
                    val currentSignature = "status:${status}_depDelay:${activeDepDelay}_arrDelay:${activeArrDelay}"
                    val lastSignature = _lastNotifiedStates.value[flightNo]

                    if (currentSignature != lastSignature) {
                        if (isCancelled) {
                            val msg = getApplication<Application>().getString(R.string.flight_cancelled_toast, flightNo)
                            toastChannel.send(msg)
                        } else if (isDelayedStatus || isDelayedByTime) {
                            val context = getApplication<Application>()
                            val reason = when {
                                activeDepDelay > 0 -> context.getString(R.string.flight_delayed_dep_toast, activeDepDelay)
                                activeArrDelay > 0 -> context.getString(R.string.flight_delayed_arr_toast, activeArrDelay)
                                else -> context.getString(R.string.flight_delayed_generic)
                            }
                            val msg = context.getString(R.string.flight_status_update_toast, flightNo, reason)
                            toastChannel.send(msg)
                        }
                        val notifiedMap = _lastNotifiedStates.value.toMutableMap()
                        notifiedMap[flightNo] = currentSignature
                        _lastNotifiedStates.value = notifiedMap
                    }
                } else {
                    val updatedStatuses = _favoriteFlightStatuses.value.toMutableMap()
                    updatedStatuses[flightNo] = FavoriteFlightStatus(
                        flightNumber = flightNo,
                        status = "not_found",
                        departureDelay = null,
                        arrivalDelay = null,
                        lastChecked = System.currentTimeMillis(),
                        isPolling = false
                    )
                    _favoriteFlightStatuses.value = updatedStatuses
                }
            } catch (e: Exception) {
                val updatedStatuses = _favoriteFlightStatuses.value.toMutableMap()
                updatedStatuses[flightNo] = (updatedStatuses[flightNo] ?: FavoriteFlightStatus(flightNo, null, null, null)).copy(
                    isPolling = false,
                    lastChecked = System.currentTimeMillis()
                )
                _favoriteFlightStatuses.value = updatedStatuses
            }
        }
    }

    fun pollAllFavorites() {
        _favoriteFlightNumbers.value.forEach { flightNo ->
            pollSingleFavorite(flightNo)
        }
    }

    private fun startPeriodicPolling() {
        viewModelScope.launch {
            while (true) {
                if (_isPeriodicPollingActive.value) {
                    _nextPollTime.value = System.currentTimeMillis() + pollingIntervalMs
                    delay(pollingIntervalMs)
                    if (_isPeriodicPollingActive.value) {
                        pollAllFavorites()
                    }
                } else {
                    _nextPollTime.value = null
                    delay(2000)
                }
            }
        }
    }
}
