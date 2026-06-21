package com.bytecoders.aviationoss.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @Json(name = "latitude") val latitude: Double?,
    @Json(name = "longitude") val longitude: Double?,
    @Json(name = "current_weather") val currentWeather: CurrentWeatherDto?
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @Json(name = "temperature") val temperature: Double?,
    @Json(name = "windspeed") val windspeed: Double?,
    @Json(name = "weathercode") val weathercode: Int?,
    @Json(name = "is_day") val isDay: Int?,
    @Json(name = "time") val time: String?
)
