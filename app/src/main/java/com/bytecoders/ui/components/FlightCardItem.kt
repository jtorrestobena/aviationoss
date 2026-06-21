package com.bytecoders.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytecoders.R
import com.bytecoders.data.local.CachedFlightEntity
import com.bytecoders.ui.theme.*
import com.bytecoders.util.UIConstants

@Composable
fun FlightCardItem(
    flight: CachedFlightEntity,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onSelectForTracking: () -> Unit,
    isActiveTracking: Boolean,
    modifier: Modifier = Modifier
) {
    val statusColor = when (flight.flightStatus?.lowercase()) {
        "active" -> ColorActive
        "scheduled" -> ColorScheduled
        "landed" -> ColorLanded
        "cancelled" -> ColorCancelled
        else -> ColorScheduled
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isActiveTracking) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable { 
                onSelectForTracking()
                onClick()
            }
            .padding(14.dp)
            .testTag("flight_card_${flight.flightIata}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container with background history indicator
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(UIConstants.CORNER_RADIUS_SMALL))
                .background(
                    if (isActiveTracking) MaterialTheme.colorScheme.background
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (flight.isBookmarked) Icons.Default.Star else Icons.Default.History,
                contentDescription = null,
                tint = if (flight.isBookmarked) RadarTertiary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Identification info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = flight.flightIata ?: "Flight ${flight.flightNumber}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${flight.departureIata} → ${flight.arrivalIata} • ${flight.airlineName ?: stringResource(R.string.carrier_default)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Bookmark, state tag and action indicators
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = (flight.flightStatus ?: stringResource(R.string.status_scheduled)).uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            IconButton(
                onClick = onBookmarkToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (flight.isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = stringResource(R.string.cd_watchlist_trigger),
                    tint = if (flight.isBookmarked) RadarTertiary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FlightCardItemPreview() {
    MyApplicationTheme {
        FlightCardItem(
            flight = CachedFlightEntity(
                id = "1",
                flightIata = "IB3112",
                flightNumber = "3112",
                departureIata = "MAD",
                arrivalIata = "LHR",
                airlineName = "Iberia",
                flightStatus = "active",
                flightDate = "2023-10-10",
                departureAirport = "Adolfo Suarez",
                arrivalAirport = "Heathrow",
                departureTerminal = null,
                departureGate = null,
                departureDelay = null,
                departureScheduled = null,
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
            ),
            onClick = {},
            onBookmarkToggle = {},
            onSelectForTracking = {},
            isActiveTracking = false
        )
    }
}
