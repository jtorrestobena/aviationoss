package com.bytecoders.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecoders.R
import com.bytecoders.data.local.CachedFlightEntity
import com.bytecoders.ui.FlightViewModel
import com.bytecoders.ui.components.FavoriteFlightNumberRow
import com.bytecoders.ui.components.FlightCardItem
import com.bytecoders.ui.theme.ColorActive
import com.bytecoders.util.UIConstants

@Composable
fun WatchlistScreen(
    viewModel: FlightViewModel,
    onFlightClick: (CachedFlightEntity) -> Unit,
    onSelectForTracking: (CachedFlightEntity) -> Unit,
    activeTrackingFlight: CachedFlightEntity?
) {
    val bookmarkedList by viewModel.bookmarkedFlights.collectAsStateWithLifecycle()
    val favoriteNumbers by viewModel.favoriteFlightNumbers.collectAsStateWithLifecycle()
    val favoriteStatuses by viewModel.favoriteFlightStatuses.collectAsStateWithLifecycle()
    val isPollingActive by viewModel.isPeriodicPollingActive.collectAsStateWithLifecycle()
    val nextPollTime by viewModel.nextPollTime.collectAsStateWithLifecycle()

    var newFlightInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Section A: Favorite Flight Alerts & Background Polling Panel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header title row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.flight_status_alerts),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Background Polling status details
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isPollingActive) ColorActive.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                )
                                .clickable { viewModel.togglePeriodicPolling() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isPollingActive) ColorActive else MaterialTheme.colorScheme.secondary)
                                )
                                Text(
                                    text = if (isPollingActive) stringResource(R.string.auto_poll_on) else stringResource(R.string.auto_poll_off),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPollingActive) ColorActive else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input Row to Add Flight No.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newFlightInput,
                            onValueChange = { newFlightInput = it },
                            placeholder = { Text(stringResource(R.string.fav_input_placeholder), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("fav_flight_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                        )

                        Button(
                            onClick = {
                                if (newFlightInput.isNotBlank()) {
                                    viewModel.addFavoriteFlightNumber(newFlightInput)
                                    newFlightInput = ""
                                    keyboardController?.hide()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("add_fav_button"),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.cd_add_favorite),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Polling Countdown indicator
                    if (isPollingActive && nextPollTime != null) {
                        val currentMs = System.currentTimeMillis()
                        val diffMs = (nextPollTime!! - currentMs).coerceAtLeast(0)
                        val diffSecs = (diffMs / 1000).toInt()
                        Text(
                            text = stringResource(R.string.next_poll_format, diffSecs),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // List of Favorite Flight Numbers Rows
                    if (favoriteNumbers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_fav_numbers),
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        // Header line with master Poll Now action
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.monitored_numbers_format, favoriteNumbers.size),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Text(
                                text = stringResource(R.string.poll_all),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { viewModel.pollAllFavorites() }
                                    .padding(vertical = 2.dp, horizontal = 4.dp)
                            )
                        }

                        favoriteNumbers.forEach { flightNo ->
                            val statusInfo = favoriteStatuses[flightNo]
                            FavoriteFlightNumberRow(
                                flightNo = flightNo,
                                statusInfo = statusInfo,
                                onRefresh = { viewModel.pollSingleFavorite(flightNo) },
                                onDelete = { viewModel.removeFavoriteFlightNumber(flightNo) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // Section B: VIP Bookmark List Header
        item {
            Text(
                text = stringResource(R.string.watched_vip_flights, bookmarkedList.size),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
            )
        }

        // Section C: Bookmarked list content
        if (bookmarkedList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.StarOutline,
                            contentDescription = stringResource(R.string.cd_empty_watchlist),
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_bookmarks),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = stringResource(R.string.no_bookmarks_sub),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        } else {
            items(bookmarkedList) { flight ->
                FlightCardItem(
                    flight = flight,
                    onClick = { onFlightClick(flight) },
                    onBookmarkToggle = { viewModel.toggleBookmark(flight) },
                    onSelectForTracking = { onSelectForTracking(flight) },
                    isActiveTracking = activeTrackingFlight?.id == flight.id
                )
            }
        }
    }
}
