package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_flights")
data class CachedFlightEntity(
    @PrimaryKey val id: String, // Constructed as airline_iata + flight_number + flight_date / timestamp
    val flightDate: String?,
    val flightStatus: String?,
    
    // Departure
    val departureAirport: String?,
    val departureIata: String?,
    val departureTerminal: String?,
    val departureGate: String?,
    val departureDelay: Int?,
    val departureScheduled: String?,
    val departureEstimated: String?,
    val departureActual: String?,
    
    // Arrival
    val arrivalAirport: String?,
    val arrivalIata: String?,
    val arrivalTerminal: String?,
    val arrivalGate: String?,
    val arrivalBaggage: String?,
    val arrivalDelay: Int?,
    val arrivalScheduled: String?,
    val arrivalEstimated: String?,
    val arrivalActual: String?,
    
    // Airline & Flight details
    val airlineName: String?,
    val airlineIata: String?,
    val flightNumber: String?,
    val flightIata: String?,
    
    // Aircraft
    val aircraftRegistration: String?,
    val aircraftIata: String?,
    
    // Live State
    val liveAltitude: Double?,
    val liveSpeed: Double?,
    
    // Bookmarking & Cache tracking
    val isBookmarked: Boolean = false,
    val cachedAtEpochMs: Long = System.currentTimeMillis()
)
