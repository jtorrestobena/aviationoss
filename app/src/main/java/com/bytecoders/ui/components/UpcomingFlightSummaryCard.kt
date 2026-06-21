package com.bytecoders.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytecoders.R
import com.bytecoders.data.local.CachedFlightEntity
import com.bytecoders.data.repository.Resource
import com.bytecoders.ui.theme.*
import com.bytecoders.util.DateTimeUtils

@Composable
fun UpcomingFlightSummaryCard(
    upcomingState: Resource<CachedFlightEntity?>,
    onFlightClick: (CachedFlightEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        )
    ) {
        when (upcomingState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.querying_departures),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            is Resource.Error -> {
                Text(
                    text = stringResource(R.string.fetch_departures_error),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(14.dp)
                )
            }
            is Resource.Success -> {
                val flight = upcomingState.data
                if (flight == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_other_flights),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFlightClick(flight) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlightTakeoff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = flight.flightIata ?: "Flight ${flight.flightNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.to_arrival_format, flight.arrivalIata ?: stringResource(R.string.n_a), flight.arrivalAirport ?: stringResource(R.string.n_a)),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = DateTimeUtils.formatTimeDetailed(flight.departureScheduled),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val statusColor = when (flight.flightStatus?.lowercase()) {
                                "active" -> ColorActive
                                "scheduled" -> ColorScheduled
                                "landed" -> ColorLanded
                                "cancelled" -> ColorCancelled
                                else -> ColorScheduled
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(statusColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = (flight.flightStatus ?: stringResource(R.string.status_scheduled)).uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpcomingFlightSummaryCardPreview() {
    MyApplicationTheme {
        UpcomingFlightSummaryCard(
            upcomingState = Resource.Success(
                CachedFlightEntity(
                    id = "2",
                    flightIata = "IB3114",
                    flightNumber = "3114",
                    departureIata = "MAD",
                    arrivalIata = "LHR",
                    airlineName = "Iberia",
                    flightStatus = "scheduled",
                    departureScheduled = "2023-10-10T15:00:00Z",
                    flightDate = "2023-10-10",
                    departureAirport = "Adolfo Suarez",
                    arrivalAirport = "Heathrow",
                    departureTerminal = null,
                    departureGate = null,
                    departureDelay = null,
                    departureEstimated = null,
                    departureActual = null,
                    arrivalTerminal = null,
                    arrivalGate = null,
                    arrivalBaggage = null,
                    arrivalDelay = null,
                    arrivalScheduled = null,
                    arrivalEstimated = null,
                    arrivalActual = null,
                    airlineIata = null,
                    aircraftRegistration = null,
                    aircraftIata = null,
                    liveAltitude = null,
                    liveSpeed = null
                )
            ),
            onFlightClick = {}
        )
    }
}
