package com.bytecoders.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytecoders.R
import com.bytecoders.data.local.CachedFlightEntity
import com.bytecoders.data.model.OpenMeteoResponse
import com.bytecoders.data.model.WeatherCode
import com.bytecoders.data.repository.Resource
import com.bytecoders.ui.components.DetailPairRow
import com.bytecoders.ui.components.FlightRouteMap
import com.bytecoders.ui.components.UpcomingFlightSummaryCard
import com.bytecoders.ui.theme.*
import com.bytecoders.util.DateTimeUtils

@Composable
fun FlightDetailScreen(
    flight: CachedFlightEntity,
    upcomingState: Resource<CachedFlightEntity?>,
    weatherState: Resource<OpenMeteoResponse?>,
    onDismiss: () -> Unit,
    onToggleBookmark: () -> Unit,
    onUpcomingFlightClick: (CachedFlightEntity) -> Unit
) {
    BackHandler(onBack = onDismiss)
    val statusColor = when (flight.flightStatus?.lowercase()) {
        "active" -> ColorActive
        "scheduled" -> ColorScheduled
        "landed" -> ColorLanded
        "cancelled" -> ColorCancelled
        else -> ColorScheduled
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back), modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = flight.flightIata ?: "Flight ${flight.flightNumber}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = flight.airlineName ?: stringResource(R.string.unknown_carrier),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = if (flight.isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = stringResource(R.string.cd_watchlist_toggler),
                        tint = if (flight.isBookmarked) RadarTertiary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))

                // Progress status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.08f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.aviation_status_label),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = (flight.flightStatus ?: stringResource(R.string.status_scheduled)).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Departure & Arrival Details columns
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.departure_label), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(flight.departureIata ?: stringResource(R.string.n_a), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(flight.departureAirport ?: stringResource(R.string.n_a), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailPairRow(stringResource(R.string.terminal_label), flight.departureTerminal ?: stringResource(R.string.not_available_dash))
                        DetailPairRow(stringResource(R.string.gate_label), flight.departureGate ?: stringResource(R.string.not_available_dash))
                        DetailPairRow(stringResource(R.string.delay_label), if (flight.departureDelay == null) stringResource(R.string.on_time) else "${flight.departureDelay}m")
                        DetailPairRow(stringResource(R.string.sched_label), DateTimeUtils.formatTimeDetailed(flight.departureScheduled))
                        DetailPairRow(stringResource(R.string.est_label), DateTimeUtils.formatTimeDetailed(flight.departureEstimated))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.arrival_label), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(flight.arrivalIata ?: stringResource(R.string.n_a), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(flight.arrivalAirport ?: stringResource(R.string.n_a), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailPairRow(stringResource(R.string.terminal_label), flight.arrivalTerminal ?: stringResource(R.string.not_available_dash))
                        DetailPairRow(stringResource(R.string.gate_label), flight.arrivalGate ?: stringResource(R.string.not_available_dash))
                        DetailPairRow(stringResource(R.string.baggage_label), flight.arrivalBaggage ?: stringResource(R.string.not_available_dash))
                        DetailPairRow(stringResource(R.string.sched_label), DateTimeUtils.formatTimeDetailed(flight.arrivalScheduled))
                        DetailPairRow(stringResource(R.string.est_label), DateTimeUtils.formatTimeDetailed(flight.arrivalEstimated))
                    }
                }

                // Destination Weather Section
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.weather_scanner_title),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val weatherInfo = mapWeatherCode(weatherState)

                when (weatherState) {
                    is Resource.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.querying_weather), fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    is Resource.Error -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = stringResource(R.string.cd_error),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = weatherState.uiText.asString(),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    is Resource.Success -> {
                        val response = weatherState.data
                        if (response?.currentWeather != null) {
                            val cur = response.currentWeather
                            val (desc, icon) = weatherInfo
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = stringResource(R.string.cd_weather_icon),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = desc,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.current_conditions),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(R.string.temp_celsius_format, cur.temperature ?: stringResource(R.string.not_available_dash)),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.wind_format, cur.windspeed ?: stringResource(R.string.not_available_dash)),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.weather_unavailable),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                // Map Section
                Spacer(modifier = Modifier.height(14.dp))
                FlightRouteMap(
                    flight = flight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                // Aircraft systems telemetry info
                if (flight.aircraftIata != null || flight.liveAltitude != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                    Text(stringResource(R.string.telemetry_scanner_title), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        flight.aircraftIata?.let { model ->
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.equipment_label), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(model, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                flight.aircraftRegistration?.let { reg ->
                                    Text(stringResource(R.string.reg_format, reg), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }

                        if (flight.liveAltitude != null || flight.liveSpeed != null) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(stringResource(R.string.radar_metrics_label), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                flight.liveAltitude?.let { alt ->
                                    Text(stringResource(R.string.altitude_format, alt.toInt()), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                flight.liveSpeed?.let { speed ->
                                    Text(stringResource(R.string.speed_format, speed.toInt()), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                // Upcoming flight section
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                Text(stringResource(R.string.next_departure_format, flight.departureIata ?: ""), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                UpcomingFlightSummaryCard(
                    upcomingState = upcomingState,
                    onFlightClick = onUpcomingFlightClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(stringResource(R.string.close_telemetry), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

@Composable
private fun mapWeatherCode(weatherState: Resource<OpenMeteoResponse?>): Pair<String, ImageVector> {
    return when (val res = weatherState) {
        is Resource.Success -> {
            val code = res.data?.currentWeather?.weathercode
            when (code) {
                WeatherCode.CLEAR_SKY -> Pair(stringResource(R.string.weather_clear), Icons.Default.WbSunny)
                WeatherCode.MAINLY_CLEAR, WeatherCode.PARTLY_CLOUDY, WeatherCode.OVERCAST ->
                    Pair(stringResource(R.string.weather_partly_cloudy), Icons.Default.Cloud)
                WeatherCode.FOG, WeatherCode.DEPOSITING_RIME_FOG ->
                    Pair(stringResource(R.string.weather_foggy), Icons.Default.Cloud)
                WeatherCode.DRIZZLE_LIGHT, WeatherCode.DRIZZLE_MODERATE, WeatherCode.DRIZZLE_DENSE,
                WeatherCode.FREEZING_DRIZZLE_LIGHT, WeatherCode.FREEZING_DRIZZLE_DENSE ->
                    Pair(stringResource(R.string.weather_drizzle), Icons.Default.Cloud)
                WeatherCode.RAIN_SLIGHT, WeatherCode.RAIN_MODERATE, WeatherCode.RAIN_HEAVY,
                WeatherCode.FREEZING_RAIN_LIGHT, WeatherCode.FREEZING_RAIN_HEAVY ->
                    Pair(stringResource(R.string.weather_rainy), Icons.Default.Cloud)
                WeatherCode.SNOW_FALL_SLIGHT, WeatherCode.SNOW_FALL_MODERATE, WeatherCode.SNOW_FALL_HEAVY,
                WeatherCode.SNOW_GRAINS, WeatherCode.SNOW_SHOWERS_SLIGHT, WeatherCode.SNOW_SHOWERS_HEAVY ->
                    Pair(stringResource(R.string.weather_snowy), Icons.Default.Cloud)
                WeatherCode.RAIN_SHOWERS_SLIGHT, WeatherCode.RAIN_SHOWERS_MODERATE, WeatherCode.RAIN_SHOWERS_VIOLENT ->
                    Pair(stringResource(R.string.weather_showers), Icons.Default.Cloud)
                WeatherCode.THUNDERSTORM_SLIGHT_MODERATE, WeatherCode.THUNDERSTORM_WITH_SLIGHT_HAIL, WeatherCode.THUNDERSTORM_WITH_HEAVY_HAIL ->
                    Pair(stringResource(R.string.weather_thunderstorm), Icons.Default.Warning)
                else -> Pair(stringResource(R.string.weather_overcast), Icons.Default.Cloud)
            }
        }
        else -> Pair(stringResource(R.string.weather_overcast), Icons.Default.Cloud)
    }
}
