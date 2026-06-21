package com.bytecoders.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytecoders.R
import com.bytecoders.data.local.CachedFlightEntity
import com.bytecoders.ui.theme.MyApplicationTheme
import com.bytecoders.util.DateTimeUtils
import com.bytecoders.util.UIConstants

@Composable
fun ActiveTrackingSection(
    flight: CachedFlightEntity?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(UIConstants.CORNER_RADIUS_LARGE),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (flight == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.awaiting_radar_scan),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.awaiting_radar_scan_sub),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.active_tracking_title),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = flight.flightIata ?: "FLIGHT ${flight.flightNumber}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(UIConstants.CORNER_RADIUS_SMALL))
                            .background(MaterialTheme.colorScheme.onPrimaryContainer)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = (flight.flightStatus ?: stringResource(R.string.status_en_route)).uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(UIConstants.SECTION_SPACING))

                // Progress trajectory JFK -> [PLANE] -> LAX
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = flight.departureIata ?: stringResource(R.string.n_a),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = flight.departureAirport?.split(",")?.firstOrNull()?.split(" ")?.firstOrNull() ?: stringResource(R.string.fallback_new_york),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                        ) {
                            drawLine(
                                color = Color(0xFF001C38).copy(alpha = 0.15f),
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .size(16.dp)
                                    .graphicsLayer(rotationZ = 90f)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = flight.arrivalIata ?: stringResource(R.string.n_a),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = flight.arrivalAirport?.split(",")?.firstOrNull()?.split(" ")?.firstOrNull() ?: stringResource(R.string.fallback_los_angeles),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(UIConstants.SECTION_SPACING))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.data_source_cached),
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        text = stringResource(R.string.arrival_format, DateTimeUtils.formatTime(flight.arrivalScheduled)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActiveTrackingSectionPreview() {
    MyApplicationTheme {
        ActiveTrackingSection(
            flight = CachedFlightEntity(
                id = "1",
                flightDate = "2024-06-21",
                flightStatus = "active",
                departureAirport = "John F. Kennedy",
                departureIata = "JFK",
                departureTerminal = "4",
                departureGate = "B23",
                departureDelay = 10,
                departureScheduled = "2024-06-21T10:00:00Z",
                departureEstimated = "2024-06-21T10:10:00Z",
                departureActual = "2024-06-21T10:15:00Z",
                arrivalAirport = "Los Angeles International",
                arrivalIata = "LAX",
                arrivalTerminal = "5",
                arrivalGate = "54A",
                arrivalBaggage = "B7",
                arrivalDelay = 5,
                arrivalScheduled = "2024-06-21T13:00:00Z",
                arrivalEstimated = "2024-06-21T13:05:00Z",
                arrivalActual = null,
                airlineName = "American Airlines",
                airlineIata = "AA",
                flightNumber = "123",
                flightIata = "AA123",
                aircraftRegistration = "N123AA",
                aircraftIata = "A321",
                liveAltitude = 35000.0,
                liveSpeed = 450.0
            )
        )
    }
}
