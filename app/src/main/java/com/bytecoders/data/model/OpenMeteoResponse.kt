package com.bytecoders.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @field:Json(name = "latitude") val latitude: Double?,
    @field:Json(name = "longitude") val longitude: Double?,
    @field:Json(name = "current_weather") val currentWeather: CurrentWeatherDto?
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @field:Json(name = "temperature") val temperature: Double?,
    @field:Json(name = "windspeed") val windspeed: Double?,
    @field:Json(name = "weathercode") val weathercode: Int?,
    @field:Json(name = "is_day") val isDay: Int?,
    @field:Json(name = "time") val time: String?
)
