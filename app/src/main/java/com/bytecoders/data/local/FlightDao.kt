package com.bytecoders.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {
    @Query("SELECT * FROM cached_flights ORDER BY cachedAtEpochMs DESC")
    fun getAllFlights(): Flow<List<CachedFlightEntity>>

    @Query("SELECT * FROM cached_flights WHERE isBookmarked = 1 ORDER BY cachedAtEpochMs DESC")
    fun getBookmarkedFlights(): Flow<List<CachedFlightEntity>>

    @Query("""
        SELECT * FROM cached_flights 
        WHERE flightIata LIKE :query 
           OR airlineName LIKE :query 
           OR departureIata LIKE :query 
           OR arrivalIata LIKE :query 
           OR departureAirport LIKE :query
           OR arrivalAirport LIKE :query
        ORDER BY cachedAtEpochMs DESC
    """)
    fun searchCachedFlights(query: String): Flow<List<CachedFlightEntity>>

    @Query("SELECT id FROM cached_flights WHERE isBookmarked = 1")
    suspend fun getBookmarkedFlightIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlights(flights: List<CachedFlightEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlight(flight: CachedFlightEntity)

    @Query("UPDATE cached_flights SET isBookmarked = :isBookmarked WHERE id = :flightId")
    suspend fun updateBookmarkState(flightId: String, isBookmarked: Boolean)

    @Query("DELETE FROM cached_flights WHERE isBookmarked = 0 AND cachedAtEpochMs < :expiryTimeMs")
    suspend fun deleteOldCache(expiryTimeMs: Long)

    @Query("DELETE FROM cached_flights WHERE isBookmarked = 0")
    suspend fun clearCache()

    @Query("SELECT COUNT(*) FROM cached_flights WHERE isBookmarked = 0")
    suspend fun getCacheCount(): Int

    @Query("SELECT * FROM cached_flights WHERE departureIata = :departureIata")
    suspend fun getFlightsByDeparture(departureIata: String): List<CachedFlightEntity>
}
