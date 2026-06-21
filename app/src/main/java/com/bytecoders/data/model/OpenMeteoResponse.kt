package com.bytecoders.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @field:Json(name = "latitude") val latitude: Double?,
    @field:Json(name = "longitude") val longitude: Double?,
    @field:Json(name = "current_weather") val currentWeather: CurrentWeatherDto?,
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @field:Json(name = "temperature") val temperature: Double?,
    @field:Json(name = "windspeed") val windspeed: Double?,
    @field:Json(name = "weathercode") val weathercode: Int?,
    @field:Json(name = "is_day") val isDay: Int?,
    @field:Json(name = "time") val time: String?,
)

object WeatherCode {
    const val CLEAR_SKY = 0
    const val MAINLY_CLEAR = 1
    const val PARTLY_CLOUDY = 2
    const val OVERCAST = 3
    const val FOG = 45
    const val DEPOSITING_RIME_FOG = 48
    const val DRIZZLE_LIGHT = 51
    const val DRIZZLE_MODERATE = 53
    const val DRIZZLE_DENSE = 55
    const val FREEZING_DRIZZLE_LIGHT = 56
    const val FREEZING_DRIZZLE_DENSE = 57
    const val RAIN_SLIGHT = 61
    const val RAIN_MODERATE = 63
    const val RAIN_HEAVY = 65
    const val FREEZING_RAIN_LIGHT = 66
    const val FREEZING_RAIN_HEAVY = 67
    const val SNOW_FALL_SLIGHT = 71
    const val SNOW_FALL_MODERATE = 73
    const val SNOW_FALL_HEAVY = 75
    const val SNOW_GRAINS = 77
    const val RAIN_SHOWERS_SLIGHT = 80
    const val RAIN_SHOWERS_MODERATE = 81
    const val RAIN_SHOWERS_VIOLENT = 82
    const val SNOW_SHOWERS_SLIGHT = 85
    const val SNOW_SHOWERS_HEAVY = 86
    const val THUNDERSTORM_SLIGHT_MODERATE = 95
    const val THUNDERSTORM_WITH_SLIGHT_HAIL = 96
    const val THUNDERSTORM_WITH_HEAVY_HAIL = 99
}
