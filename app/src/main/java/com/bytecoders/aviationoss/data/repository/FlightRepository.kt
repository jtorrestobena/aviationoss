package com.bytecoders.aviationoss.data.repository

import com.bytecoders.aviationoss.BuildConfig
import com.bytecoders.aviationoss.data.local.CachedFlightEntity
import com.bytecoders.aviationoss.data.local.FlightDao
import com.bytecoders.aviationoss.data.model.FlightDataDto
import com.bytecoders.aviationoss.data.remote.AviationstackApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>()
}

class FlightRepository(
    private val flightDao: FlightDao,
    private val apiService: AviationstackApiService
) {
    // Expose flows from the DB
    val allCachedFlights: Flow<List<CachedFlightEntity>> = flightDao.getAllFlights()
    val bookmarkedFlights: Flow<List<CachedFlightEntity>> = flightDao.getBookmarkedFlights()

    fun searchLocalCache(query: String): Flow<List<CachedFlightEntity>> {
        return flightDao.searchCachedFlights("%$query%")
    }

    suspend fun toggleBookmark(flightId: String, isBookmarked: Boolean) {
        flightDao.updateBookmarkState(flightId, isBookmarked)
    }

    suspend fun clearNonBookmarkedCache() {
        flightDao.clearCache()
    }

    suspend fun getCacheCount(): Int {
        return flightDao.getCacheCount()
    }

    // Main search function targeting API first, then caching, then falling back to Cache/Demo on errors.
    fun searchFlightsLive(
        flightNumber: String? = null,
        departureIata: String? = null,
        arrivalIata: String? = null,
        apiKeyOverride: String? = null
    ): Flow<Resource<List<CachedFlightEntity>>> = flow {
        emit(Resource.Loading)

        val resolvedKey = when {
            !apiKeyOverride.isNullOrBlank() -> apiKeyOverride
            !BuildConfig.AVIATIONSTACK_API_KEY.contains("YOUR_") && 
            !BuildConfig.AVIATIONSTACK_API_KEY.contains("MY_") && 
            BuildConfig.AVIATIONSTACK_API_KEY.isNotBlank() -> BuildConfig.AVIATIONSTACK_API_KEY
            else -> null
        }

        if (resolvedKey == null) {
            // No API key configured. Emit error but we also suggest searching local/demo data
            emit(Resource.Error("API Key is missing. Please set AVIATIONSTACK_API_KEY in the Secrets panel in AI Studio or use the offline Cache Vault tab."))
            return@flow
        }

        val cleansedQuery = (flightNumber ?: "").replace("\\s".toRegex(), "").trim()
        
        var resolvedFlightNumber: String? = null
        var resolvedFlightIata: String? = null
        var resolvedAirlineIata: String? = null

        if (cleansedQuery.isNotEmpty()) {
            if (cleansedQuery.all { it.isDigit() }) {
                // Pure numeric flight number, e.g. "857"
                resolvedFlightNumber = cleansedQuery
            } else if (cleansedQuery.length >= 2 && cleansedQuery.substring(0, 2).all { it.isLetter() } && cleansedQuery.substring(2).all { it.isDigit() }) {
                // Airline IATA + number, e.g. "UA857"
                resolvedFlightIata = cleansedQuery.uppercase()
            } else if (cleansedQuery.length in 2..3 && cleansedQuery.all { it.isLetter() }) {
                // Pure line IATA letter query, e.g. "UA"
                resolvedAirlineIata = cleansedQuery.uppercase()
            } else {
                // Fallback: search as flight_iata
                resolvedFlightIata = cleansedQuery.uppercase()
            }
        }

        try {
            val response = apiService.getFlights(
                apiKey = resolvedKey,
                flightNumber = resolvedFlightNumber,
                flightIata = resolvedFlightIata,
                airlineIata = resolvedAirlineIata,
                departureIata = departureIata?.trim()?.uppercase()?.ifEmpty { null },
                arrivalIata = arrivalIata?.trim()?.uppercase()?.ifEmpty { null }
            )

            val flightsList = response.data
            if (flightsList == null) {
                emit(Resource.Error("Invalid or empty response from Aviationstack."))
                return@flow
            }

            // Convert to Local Room Entity and Cache them
            val entities = flightsList.map { dtoToEntity(it) }
            if (entities.isNotEmpty()) {
                flightDao.insertFlights(entities)
            }

            emit(Resource.Success(entities))
        } catch (e: Exception) {
            val friendlyMessage = when (e) {
                is IOException -> "A network error occurred. Please check your internet connection or Aviationstack status."
                else -> e.localizedMessage ?: "An unexpected error occurred during search."
            }
            emit(Resource.Error(friendlyMessage, e))
        }
    }

    private fun dtoToEntity(dto: FlightDataDto): CachedFlightEntity {
        val airlineIata = dto.airline?.iata ?: ""
        val flNumber = dto.flight?.number ?: ""
        val flDate = dto.flightDate ?: "N/A"
        val flightIataCode = dto.flight?.iata ?: "${airlineIata}${flNumber}".trim().ifEmpty { "FLIGHT" }
        
        // Composite unique key
        val uniqueId = "${airlineIata}_${flNumber}_${flDate}".ifEmpty { 
            "flight_gen_${System.currentTimeMillis()}_${(0..1000).random()}"
        }

        return CachedFlightEntity(
            id = uniqueId,
            flightDate = dto.flightDate,
            flightStatus = dto.flightStatus ?: "unknown",
            
            departureAirport = dto.departure?.airport ?: "Unknown Airport",
            departureIata = dto.departure?.iata ?: "N/A",
            departureTerminal = dto.departure?.terminal,
            departureGate = dto.departure?.gate,
            departureDelay = dto.departure?.delay,
            departureScheduled = dto.departure?.scheduled,
            departureEstimated = dto.departure?.estimated,
            departureActual = dto.departure?.actual,
            
            arrivalAirport = dto.arrival?.airport ?: "Unknown Airport",
            arrivalIata = dto.arrival?.iata ?: "N/A",
            arrivalTerminal = dto.arrival?.terminal,
            arrivalGate = dto.arrival?.gate,
            arrivalBaggage = dto.arrival?.baggage,
            arrivalDelay = dto.arrival?.delay,
            arrivalScheduled = dto.arrival?.scheduled,
            arrivalEstimated = dto.arrival?.estimated,
            arrivalActual = dto.arrival?.actual,
            
            airlineName = dto.airline?.name ?: "Unknown Airline",
            airlineIata = dto.airline?.iata,
            flightNumber = dto.flight?.number,
            flightIata = flightIataCode,
            
            aircraftRegistration = dto.aircraft?.registration,
            aircraftIata = dto.aircraft?.iata,
            
            liveAltitude = dto.live?.altitude,
            liveSpeed = dto.live?.speedHorizontal,
            
            isBookmarked = false,
            cachedAtEpochMs = System.currentTimeMillis()
        )
    }

    // Pre-populates the database with some beautiful high-quality mock data 
    // to search immediately in Cache Vault if the user is keyless.
    suspend fun populateDemoData() {
        val demoFlights = listOf(
            CachedFlightEntity(
                id = "AA_1004_2026-06-21",
                flightDate = "2026-06-21",
                flightStatus = "active",
                departureAirport = "San Francisco International",
                departureIata = "SFO",
                departureTerminal = "2",
                departureGate = "D11",
                departureDelay = 12,
                departureScheduled = "2026-06-21T04:20:00+00:00",
                departureEstimated = "2026-06-21T04:20:00+00:00",
                departureActual = "2026-06-21T04:32:00+00:00",
                arrivalAirport = "Dallas/Fort Worth International",
                arrivalIata = "DFW",
                arrivalTerminal = "C",
                arrivalGate = "C15",
                arrivalBaggage = "1",
                arrivalDelay = 25,
                arrivalScheduled = "2026-06-21T05:42:00+00:00",
                arrivalEstimated = "2026-06-21T06:07:00+00:00",
                arrivalActual = null,
                airlineName = "American Airlines",
                airlineIata = "AA",
                flightNumber = "1004",
                flightIata = "AA1004",
                aircraftRegistration = "N104NN",
                aircraftIata = "A321",
                liveAltitude = 34000.0,
                liveSpeed = 485.0,
                isBookmarked = false,
                cachedAtEpochMs = System.currentTimeMillis() - 1000 * 60 * 10 // 10 mins ago
            ),
            CachedFlightEntity(
                id = "BA_217_2026-06-20",
                flightDate = "2026-06-20",
                flightStatus = "landed",
                departureAirport = "London Heathrow",
                departureIata = "LHR",
                departureTerminal = "5",
                departureGate = "B36",
                departureDelay = 0,
                departureScheduled = "2026-06-20T11:15:00+00:00",
                departureEstimated = "2026-06-20T11:15:00+00:00",
                departureActual = "2026-06-20T11:15:00+00:00",
                arrivalAirport = "Dulles International",
                arrivalIata = "IAD",
                arrivalTerminal = "Main",
                arrivalGate = "C2",
                arrivalBaggage = "4",
                arrivalDelay = -15, // 15 mins early!
                arrivalScheduled = "2026-06-20T14:30:00+00:00",
                arrivalEstimated = "2026-06-20T14:15:00+00:00",
                arrivalActual = "2026-06-20T14:15:00+00:00",
                airlineName = "British Airways",
                airlineIata = "BA",
                flightNumber = "217",
                flightIata = "BA217",
                aircraftRegistration = "G-VIIC",
                aircraftIata = "B777",
                liveAltitude = null,
                liveSpeed = null,
                isBookmarked = true, // pre-bookmarked to show how bookmarks tab works!
                cachedAtEpochMs = System.currentTimeMillis() - 1000 * 60 * 60 * 5 // 5 hours ago
            ),
            CachedFlightEntity(
                id = "LH_400_2026-06-21",
                flightDate = "2026-06-21",
                flightStatus = "active",
                departureAirport = "Frankfurt Airport",
                departureIata = "FRA",
                departureTerminal = "1",
                departureGate = "Z15",
                departureDelay = 15,
                departureScheduled = "2026-06-21T10:55:00+00:00",
                departureEstimated = "2026-06-21T11:10:00+00:00",
                departureActual = "2026-06-21T11:10:00+00:00",
                arrivalAirport = "John F. Kennedy International",
                arrivalIata = "JFK",
                arrivalTerminal = "1",
                arrivalGate = "3",
                arrivalBaggage = "B3",
                arrivalDelay = 5,
                arrivalScheduled = "2026-06-21T13:10:00+00:00",
                arrivalEstimated = "2026-06-21T13:15:00+00:00",
                arrivalActual = null,
                airlineName = "Lufthansa",
                airlineIata = "LH",
                flightNumber = "400",
                flightIata = "LH400",
                aircraftRegistration = "D-ABYI",
                aircraftIata = "B748", // Beautiful Boeing 747-8!
                liveAltitude = 37000.0,
                liveSpeed = 505.0,
                isBookmarked = false,
                cachedAtEpochMs = System.currentTimeMillis() - 1000 * 60 * 30 // 30 mins ago
            ),
            CachedFlightEntity(
                id = "JL_2_2026-06-21",
                flightDate = "2026-06-21",
                flightStatus = "scheduled",
                departureAirport = "Tokyo Haneda",
                departureIata = "HND",
                departureTerminal = "3",
                departureGate = "112",
                departureDelay = 0,
                departureScheduled = "2026-06-21T19:45:00+00:00",
                departureEstimated = "2026-06-21T19:45:00+00:00",
                departureActual = null,
                arrivalAirport = "San Francisco International",
                arrivalIata = "SFO",
                arrivalTerminal = "I",
                arrivalGate = "A12",
                arrivalBaggage = null,
                arrivalDelay = 0,
                arrivalScheduled = "2026-06-21T13:00:00+00:00",
                arrivalEstimated = "2026-06-21T13:00:00+00:00",
                arrivalActual = null,
                airlineName = "Japan Airlines",
                airlineIata = "JL",
                flightNumber = "2",
                flightIata = "JL2",
                aircraftRegistration = "JA873J",
                aircraftIata = "B787",
                liveAltitude = null,
                liveSpeed = null,
                isBookmarked = false,
                cachedAtEpochMs = System.currentTimeMillis() - 1000 * 60 * 50 // 50 mins ago
            ),
            CachedFlightEntity(
                id = "SQ_22_2026-06-21",
                flightDate = "2026-06-21",
                flightStatus = "scheduled",
                departureAirport = "Singapore Changi",
                departureIata = "SIN",
                departureTerminal = "3",
                departureGate = "A15",
                departureDelay = 5,
                departureScheduled = "2026-06-21T23:35:00+00:00",
                departureEstimated = "2026-06-21T23:40:00+00:00",
                departureActual = null,
                arrivalAirport = "Newark Liberty International",
                arrivalIata = "EWR",
                arrivalTerminal = "B",
                arrivalGate = null,
                arrivalBaggage = null,
                arrivalDelay = 0,
                arrivalScheduled = "2026-06-22T06:00:00+00:00",
                arrivalEstimated = "2026-06-22T06:00:00+00:00",
                arrivalActual = null,
                airlineName = "Singapore Airlines",
                airlineIata = "SQ",
                flightNumber = "22",
                flightIata = "SQ22",
                aircraftRegistration = "9V-SGB",
                aircraftIata = "A359",
                liveAltitude = null,
                liveSpeed = null,
                isBookmarked = false,
                cachedAtEpochMs = System.currentTimeMillis() - 1000 * 60 * 120 // 2 hours ago
            ),
            CachedFlightEntity(
                id = "AF_66_2026-06-21",
                flightDate = "2026-06-21",
                flightStatus = "cancelled",
                departureAirport = "Paris Charles de Gaulle",
                departureIata = "CDG",
                departureTerminal = "2E",
                departureGate = "K32",
                departureDelay = null,
                departureScheduled = "2026-06-21T10:15:00+00:00",
                departureEstimated = null,
                departureActual = null,
                arrivalAirport = "Los Angeles International",
                arrivalIata = "LAX",
                arrivalTerminal = "B",
                arrivalGate = "150",
                arrivalBaggage = null,
                arrivalDelay = null,
                arrivalScheduled = "2026-06-21T13:05:00+00:00",
                arrivalEstimated = null,
                arrivalActual = null,
                airlineName = "Air France",
                airlineIata = "AF",
                flightNumber = "66",
                flightIata = "AF66",
                aircraftRegistration = "F-GSQT",
                aircraftIata = "B77W",
                liveAltitude = null,
                liveSpeed = null,
                isBookmarked = false,
                cachedAtEpochMs = System.currentTimeMillis() - 1000 * 60 * 8 // 8 mins ago
            )
        )
        flightDao.insertFlights(demoFlights)
    }
}
