package com.bytecoders.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AviationstackResponse(
    @field:Json(name = "pagination") val pagination: PaginationDto?,
    @field:Json(name = "data") val data: List<FlightDataDto>?
)

@JsonClass(generateAdapter = true)
data class PaginationDto(
    @field:Json(name = "limit") val limit: Int?,
    @field:Json(name = "offset") val offset: Int?,
    @field:Json(name = "count") val count: Int?,
    @field:Json(name = "total") val total: Int?
)

@JsonClass(generateAdapter = true)
data class FlightDataDto(
    @field:Json(name = "flight_date") val flightDate: String?,
    @field:Json(name = "flight_status") val flightStatus: String?,
    @field:Json(name = "departure") val departure: DepartureDto?,
    @field:Json(name = "arrival") val arrival: ArrivalDto?,
    @field:Json(name = "airline") val airline: AirlineDto?,
    @field:Json(name = "flight") val flight: FlightDto?,
    @field:Json(name = "aircraft") val aircraft: AircraftDto?,
    @field:Json(name = "live") val live: LiveDto?
)

@JsonClass(generateAdapter = true)
data class DepartureDto(
    @field:Json(name = "airport") val airport: String?,
    @field:Json(name = "timezone") val timezone: String?,
    @field:Json(name = "iata") val iata: String?,
    @field:Json(name = "icao") val icao: String?,
    @field:Json(name = "terminal") val terminal: String?,
    @field:Json(name = "gate") val gate: String?,
    @field:Json(name = "delay") val delay: Int?,
    @field:Json(name = "scheduled") val scheduled: String?,
    @field:Json(name = "estimated") val estimated: String?,
    @field:Json(name = "actual") val actual: String?
)

@JsonClass(generateAdapter = true)
data class ArrivalDto(
    @field:Json(name = "airport") val airport: String?,
    @field:Json(name = "timezone") val timezone: String?,
    @field:Json(name = "iata") val iata: String?,
    @field:Json(name = "icao") val icao: String?,
    @field:Json(name = "terminal") val terminal: String?,
    @field:Json(name = "gate") val gate: String?,
    @field:Json(name = "baggage") val baggage: String?,
    @field:Json(name = "delay") val delay: Int?,
    @field:Json(name = "scheduled") val scheduled: String?,
    @field:Json(name = "estimated") val estimated: String?,
    @field:Json(name = "actual") val actual: String?
)

@JsonClass(generateAdapter = true)
data class AirlineDto(
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "iata") val iata: String?,
    @field:Json(name = "icao") val icao: String?
)

@JsonClass(generateAdapter = true)
data class FlightDto(
    @field:Json(name = "number") val number: String?,
    @field:Json(name = "iata") val iata: String?,
    @field:Json(name = "icao") val icao: String?
)

@JsonClass(generateAdapter = true)
data class AircraftDto(
    @field:Json(name = "registration") val registration: String?,
    @field:Json(name = "iata") val iata: String?,
    @field:Json(name = "icao") val icao: String?
)

@JsonClass(generateAdapter = true)
data class LiveDto(
    @field:Json(name = "updated") val updated: String?,
    @field:Json(name = "latitude") val latitude: Double?,
    @field:Json(name = "longitude") val longitude: Double?,
    @field:Json(name = "altitude") val altitude: Double?,
    @field:Json(name = "direction") val direction: Double?,
    @field:Json(name = "speed_horizontal") val speedHorizontal: Double?,
    @field:Json(name = "speed_vertical") val speedVertical: Double?,
    @field:Json(name = "is_ground") val isGround: Boolean?
)
