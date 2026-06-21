package com.bytecoders.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AviationstackResponse(
    @Json(name = "pagination") val pagination: PaginationDto?,
    @Json(name = "data") val data: List<FlightDataDto>?
)

@JsonClass(generateAdapter = true)
data class PaginationDto(
    @Json(name = "limit") val limit: Int?,
    @Json(name = "offset") val offset: Int?,
    @Json(name = "count") val count: Int?,
    @Json(name = "total") val total: Int?
)

@JsonClass(generateAdapter = true)
data class FlightDataDto(
    @Json(name = "flight_date") val flightDate: String?,
    @Json(name = "flight_status") val flightStatus: String?,
    @Json(name = "departure") val departure: DepartureDto?,
    @Json(name = "arrival") val arrival: ArrivalDto?,
    @Json(name = "airline") val airline: AirlineDto?,
    @Json(name = "flight") val flight: FlightDto?,
    @Json(name = "aircraft") val aircraft: AircraftDto?,
    @Json(name = "live") val live: LiveDto?
)

@JsonClass(generateAdapter = true)
data class DepartureDto(
    @Json(name = "airport") val airport: String?,
    @Json(name = "timezone") val timezone: String?,
    @Json(name = "iata") val iata: String?,
    @Json(name = "icao") val icao: String?,
    @Json(name = "terminal") val terminal: String?,
    @Json(name = "gate") val gate: String?,
    @Json(name = "delay") val delay: Int?,
    @Json(name = "scheduled") val scheduled: String?,
    @Json(name = "estimated") val estimated: String?,
    @Json(name = "actual") val actual: String?
)

@JsonClass(generateAdapter = true)
data class ArrivalDto(
    @Json(name = "airport") val airport: String?,
    @Json(name = "timezone") val timezone: String?,
    @Json(name = "iata") val iata: String?,
    @Json(name = "icao") val icao: String?,
    @Json(name = "terminal") val terminal: String?,
    @Json(name = "gate") val gate: String?,
    @Json(name = "baggage") val baggage: String?,
    @Json(name = "delay") val delay: Int?,
    @Json(name = "scheduled") val scheduled: String?,
    @Json(name = "estimated") val estimated: String?,
    @Json(name = "actual") val actual: String?
)

@JsonClass(generateAdapter = true)
data class AirlineDto(
    @Json(name = "name") val name: String?,
    @Json(name = "iata") val iata: String?,
    @Json(name = "icao") val icao: String?
)

@JsonClass(generateAdapter = true)
data class FlightDto(
    @Json(name = "number") val number: String?,
    @Json(name = "iata") val iata: String?,
    @Json(name = "icao") val icao: String?
)

@JsonClass(generateAdapter = true)
data class AircraftDto(
    @Json(name = "registration") val registration: String?,
    @Json(name = "iata") val iata: String?,
    @Json(name = "icao") val icao: String?
)

@JsonClass(generateAdapter = true)
data class LiveDto(
    @Json(name = "updated") val updated: String?,
    @Json(name = "latitude") val latitude: Double?,
    @Json(name = "longitude") val longitude: Double?,
    @Json(name = "altitude") val altitude: Double?,
    @Json(name = "direction") val direction: Double?,
    @Json(name = "speed_horizontal") val speedHorizontal: Double?,
    @Json(name = "speed_vertical") val speedVertical: Double?,
    @Json(name = "is_ground") val isGround: Boolean?
)
