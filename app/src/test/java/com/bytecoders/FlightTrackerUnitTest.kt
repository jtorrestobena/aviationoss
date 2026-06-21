package com.bytecoders

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bytecoders.data.local.AppDatabase
import com.bytecoders.data.local.CachedFlightEntity
import com.bytecoders.data.local.FlightDao
import com.bytecoders.data.model.AirlineDto
import com.bytecoders.data.model.AviationstackResponse
import com.bytecoders.data.model.DepartureDto
import com.bytecoders.data.model.FlightDataDto
import com.bytecoders.data.model.FlightDto
import com.bytecoders.data.remote.AviationstackApiService
import com.bytecoders.data.repository.FlightRepository
import com.bytecoders.data.repository.Resource
import com.bytecoders.data.utils.AirportUtils
import com.bytecoders.util.DateTimeUtils
import com.bytecoders.util.UiText
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

class FakeAviationstackApiService : AviationstackApiService {
    var responseToReturn: AviationstackResponse = AviationstackResponse(null, emptyList())
    var shouldThrowException = false
    var capturedApiKey: String? = null
    var capturedFlightNumber: String? = null
    var capturedFlightIata: String? = null
    var capturedAirlineIata: String? = null
    var capturedAirlineName: String? = null
    var capturedDepartureIata: String? = null

    override suspend fun getFlights(
        apiKey: String,
        flightNumber: String?,
        flightIata: String?,
        airlineIata: String?,
        airlineName: String?,
        departureIata: String?,
        arrivalIata: String?,
        limit: Int
    ): AviationstackResponse {
        capturedApiKey = apiKey
        capturedFlightNumber = flightNumber
        capturedFlightIata = flightIata
        capturedAirlineIata = airlineIata
        capturedAirlineName = airlineName
        capturedDepartureIata = departureIata

        if (shouldThrowException) {
            throw IOException("Simulated network error")
        }
        return responseToReturn
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FlightTrackerUnitTest {

    private lateinit var database: AppDatabase
    private lateinit var flightDao: FlightDao
    private lateinit var fakeApiService: FakeAviationstackApiService
    private lateinit var repository: FlightRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        flightDao = database.flightDao()
        fakeApiService = FakeAviationstackApiService()
        repository = FlightRepository(flightDao, fakeApiService)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testFormatTime_success() {
        // Test standard ISO string parse
        val isoString = "2026-06-21T04:20:00+00:00"
        val formatted = DateTimeUtils.formatTime(isoString)
        assertNotNull(formatted)

        // Blank/Null check
        assertEquals("--:--", DateTimeUtils.formatTime(""))
        assertEquals("--:--", DateTimeUtils.formatTime(null))
    }

    @Test
    fun testFormatTimeDetailed_success() {
        val isoString = "2026-06-21T10:15:00+00:00"
        val formatted = DateTimeUtils.formatTimeDetailed(isoString)
        assertNotNull(formatted)

        assertEquals("--", DateTimeUtils.formatTimeDetailed(""))
        assertEquals("--", DateTimeUtils.formatTimeDetailed(null))
    }

    @Test
    fun testGetAirportCoords_predefinedAndFallback() {
        // Predefined
        val sfo = AirportUtils.getAirportCoords("SFO")
        assertEquals(37.6213, sfo.first, 0.0001)
        assertEquals(-122.3790, sfo.second, 0.0001)

        val cdg = AirportUtils.getAirportCoords("CDG")
        assertEquals(49.0097, cdg.first, 0.0001)
        assertEquals(2.5479, cdg.second, 0.0001)

        // Case insensitivity
        val laxLower = AirportUtils.getAirportCoords("lax")
        assertEquals(33.9416, laxLower.first, 0.0001)

        // Empty/Null fallback hash-based default
        val emptyCoords = AirportUtils.getAirportCoords(null)
        // Fallback generates algorithmically valid coordinates, assert that they exist
        assertNotNull(emptyCoords.first)
        assertNotNull(emptyCoords.second)
    }

    @Test
    fun testRepository_liveSearch_successAndDatabaseCaching() = runBlocking {
        // Arrange
        val testDto = FlightDataDto(
            flightDate = "2026-06-21",
            flightStatus = "active",
            departure = DepartureDto("Frankfurt Airport", null, "FRA", null, "1", "Z15", 15, "2026-06-21T10:55:00+00:00", null, null),
            arrival = null,
            airline = null,
            flight = FlightDto("400", "LH400", null),
            aircraft = null,
            live = null
        )
        fakeApiService.responseToReturn = AviationstackResponse(null, listOf(testDto))

        // Act - Call repository with search criteria. Let's make sure apiKeyOverride is configured to bypass local property checks.
        val flowEvents = repository.searchFlightsLive(
            flightNumber = "400",
            apiKeyOverride = "faketoken123"
        ).toList()

        // Assert - Events Flow should emit Loading first, followed by Success
        assertTrue(flowEvents.first() is Resource.Loading)
        val successEvent = flowEvents[1] as Resource.Success
        val resultList = successEvent.data
        assertEquals(1, resultList.size)
        
        val flight = resultList.first()
        assertEquals("active", flight.flightStatus)
        assertEquals("FRA", flight.departureIata)
        assertEquals("LH400", flight.flightIata)

        // Verify that the retrieved flight has also been indexed into standard SQLite database
        val dbList = flightDao.getAllFlights().first()
        assertEquals(1, dbList.size)
        assertEquals("LH400", dbList.first().flightIata)
    }

    @Test
    fun testRepository_liveSearch_airlineNameSearch() = runBlocking {
        // Arrange
        val testDto = FlightDataDto(
            flightDate = "2026-06-21",
            flightStatus = "active",
            departure = DepartureDto("Madrid Airport", null, "MAD", null, "4", "K12", 10, "2026-06-21T12:00:00+00:00", null, null),
            arrival = null,
            airline = AirlineDto("Iberia", "IB", null),
            flight = FlightDto("3112", "IB3112", null),
            aircraft = null,
            live = null
        )
        fakeApiService.responseToReturn = AviationstackResponse(null, listOf(testDto))

        // Act - Call repository with the airline name "iberia"
        val flowEvents = repository.searchFlightsLive(
            flightNumber = "iberia",
            apiKeyOverride = "faketoken123"
        ).toList()

        // Assert - Events Flow should emit Loading first, followed by Success
        assertTrue(flowEvents.first() is Resource.Loading)
        val successEvent = flowEvents[1] as Resource.Success
        val resultList = successEvent.data
        assertEquals(1, resultList.size)

        // Ensure airlineName was parsed and captured on the API query parameters
        assertEquals("iberia", fakeApiService.capturedAirlineName)
        assertEquals(null, fakeApiService.capturedFlightNumber)
        assertEquals(null, fakeApiService.capturedFlightIata)
        assertEquals(null, fakeApiService.capturedAirlineIata)
    }

    @Test
    fun testRepository_liveSearch_errorFallback() = runBlocking {
        // Arrange
        fakeApiService.shouldThrowException = true

        // Act
        val flowEvents = repository.searchFlightsLive(
            flightNumber = "400",
            apiKeyOverride = "faketoken123"
        ).toList()

        // Assert
        assertTrue(flowEvents[0] is Resource.Loading)
        assertTrue(flowEvents[1] is Resource.Error)
        val errorEvent = flowEvents[1] as Resource.Error
        val uiText = errorEvent.uiText
        assertTrue(uiText is UiText.StringResource)
        assertEquals(R.string.network_error, (uiText as UiText.StringResource).resId)
    }

    private fun createTestFlight(
        id: String,
        flightDate: String? = null,
        flightStatus: String? = null,
        departureAirport: String? = null,
        departureIata: String? = null,
        departureTerminal: String? = null,
        departureGate: String? = null,
        departureDelay: Int? = null,
        departureScheduled: String? = null,
        departureEstimated: String? = null,
        departureActual: String? = null,
        arrivalAirport: String? = null,
        arrivalIata: String? = null,
        arrivalTerminal: String? = null,
        arrivalGate: String? = null,
        arrivalBaggage: String? = null,
        arrivalDelay: Int? = null,
        arrivalScheduled: String? = null,
        arrivalEstimated: String? = null,
        arrivalActual: String? = null,
        airlineName: String? = null,
        airlineIata: String? = null,
        flightNumber: String? = null,
        flightIata: String? = null,
        aircraftRegistration: String? = null,
        aircraftIata: String? = null,
        liveAltitude: Double? = null,
        liveSpeed: Double? = null,
        liveLatitude: Double? = null,
        liveLongitude: Double? = null,
        isBookmarked: Boolean = false,
        cachedAtEpochMs: Long = System.currentTimeMillis()
    ): CachedFlightEntity {
        return CachedFlightEntity(
            id = id,
            flightDate = flightDate,
            flightStatus = flightStatus,
            departureAirport = departureAirport,
            departureIata = departureIata,
            departureTerminal = departureTerminal,
            departureGate = departureGate,
            departureDelay = departureDelay,
            departureScheduled = departureScheduled,
            departureEstimated = departureEstimated,
            departureActual = departureActual,
            arrivalAirport = arrivalAirport,
            arrivalIata = arrivalIata,
            arrivalTerminal = arrivalTerminal,
            arrivalGate = arrivalGate,
            arrivalBaggage = arrivalBaggage,
            arrivalDelay = arrivalDelay,
            arrivalScheduled = arrivalScheduled,
            arrivalEstimated = arrivalEstimated,
            arrivalActual = arrivalActual,
            airlineName = airlineName,
            airlineIata = airlineIata,
            flightNumber = flightNumber,
            flightIata = flightIata,
            aircraftRegistration = aircraftRegistration,
            aircraftIata = aircraftIata,
            liveAltitude = liveAltitude,
            liveSpeed = liveSpeed,
            liveLatitude = liveLatitude,
            liveLongitude = liveLongitude,
            isBookmarked = isBookmarked,
            cachedAtEpochMs = cachedAtEpochMs
        )
    }

    @Test
    fun testRepository_upcomingDeparture_sortingAndExclusion() = runBlocking {
        // Pre-populate Database with multiple departures for same airport SFO
        val currentFlightId = "current_flight_id"
        val flight1 = createTestFlight(
            id = currentFlightId,
            flightDate = "2026-06-21",
            flightStatus = "active",
            departureAirport = "San Francisco International",
            departureIata = "SFO",
            departureScheduled = "2026-06-21T05:00:00+00:00",
            arrivalAirport = "Arrival A",
            arrivalIata = "AAA"
        )
        // Next upcoming flight: scheduled at 06:00
        val nextFlight = createTestFlight(
            id = "next_flight_id",
            flightDate = "2026-06-21",
            flightStatus = "scheduled",
            departureAirport = "San Francisco International",
            departureIata = "SFO",
            departureScheduled = "2026-06-21T06:00:00+00:00",
            arrivalAirport = "Arrival B",
            arrivalIata = "BBB"
        )
        // Later flight: scheduled at 09:30
        val laterFlight = createTestFlight(
            id = "later_flight_id",
            flightDate = "2026-06-21",
            flightStatus = "scheduled",
            departureAirport = "San Francisco International",
            departureIata = "SFO",
            departureScheduled = "2026-06-21T09:30:00+00:00",
            arrivalAirport = "Arrival C",
            arrivalIata = "CCC"
        )
        
        flightDao.insertFlights(listOf(flight1, nextFlight, laterFlight))

        // Act - Fetch next upcoming departure from SFO, excluding currentFlightId. 
        // We override API key as null or invalid to trigger the local database caching fallback.
        val flowEvents = repository.searchUpcomingDeparture(
            departureIata = "SFO",
            currentFlightId = currentFlightId,
            apiKeyOverride = null
        ).toList()

        // Assert 
        assertTrue(flowEvents[0] is Resource.Loading)
        val successEvent = flowEvents[1] as Resource.Success
        val upcoming = successEvent.data
        
        assertNotNull(upcoming)
        assertEquals("next_flight_id", upcoming?.id)
        assertEquals("2026-06-21T06:00:00+00:00", upcoming?.departureScheduled)
    }

    @Test
    fun testDatabase_bookmarkToggles() = runBlocking {
        val flight = createTestFlight(
            id = "test_bookmark_id",
            flightDate = "2026-06-21",
            flightStatus = "scheduled",
            departureAirport = "Departure Airport",
            departureIata = "DEP",
            arrivalAirport = "Arrival Airport",
            arrivalIata = "ARR",
            isBookmarked = false
        )
        flightDao.insertFlights(listOf(flight))

        // Verify loaded as non-bookmarked
        var allFlights = flightDao.getAllFlights().first()
        assertEquals(1, allFlights.size)
        assertEquals(false, allFlights.first().isBookmarked)

        // Toggle state to True
        flightDao.updateBookmarkState("test_bookmark_id", true)
        allFlights = flightDao.getAllFlights().first()
        assertEquals(true, allFlights.first().isBookmarked)

        // Verify fetched through bookmarked filter flow
        val bookmarkedResult = flightDao.getBookmarkedFlights().first()
        assertEquals(1, bookmarkedResult.size)
        assertEquals("test_bookmark_id", bookmarkedResult.first().id)
        
        // Toggle state back to false
        flightDao.updateBookmarkState("test_bookmark_id", false)
        assertTrue(flightDao.getBookmarkedFlights().first().isEmpty())
    }
}
