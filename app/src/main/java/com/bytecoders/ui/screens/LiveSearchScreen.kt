package com.bytecoders.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AirplaneTicket
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecoders.R
import com.bytecoders.data.local.CachedFlightEntity
import com.bytecoders.data.repository.Resource
import com.bytecoders.ui.FlightViewModel
import com.bytecoders.ui.components.ActiveTrackingSection
import com.bytecoders.ui.components.FlightCardItem
import com.bytecoders.ui.components.FlightSkeletonList
import com.bytecoders.ui.components.FreeTierUsageSection
import com.bytecoders.ui.theme.ColorCancelled
import com.bytecoders.util.UIConstants

@Composable
fun LiveSearchScreen(
    viewModel: FlightViewModel,
    apiKeyConfigured: Boolean,
    onFlightClick: (CachedFlightEntity) -> Unit,
    onSelectForTracking: (CachedFlightEntity) -> Unit,
    activeTrackingFlight: CachedFlightEntity?,
    shieldRemaining: Int
) {
    val flightNo by viewModel.searchFlightNumber.collectAsStateWithLifecycle()
    val departure by viewModel.searchDepartureIata.collectAsStateWithLifecycle()
    val arrival by viewModel.searchArrivalIata.collectAsStateWithLifecycle()
    val searchState by viewModel.liveSearchState.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Query form (Sleek styled search capsule)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(UIConstants.CORNER_RADIUS_MEDIUM))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Flight number input
                    TextField(
                        value = flightNo,
                        onValueChange = { viewModel.searchFlightNumber.value = it },
                        placeholder = { Text(stringResource(R.string.search_placeholder), fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.cd_search), tint = MaterialTheme.colorScheme.secondary) },
                        trailingIcon = {
                            if (flightNo.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchFlightNumber.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cd_clear), tint = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(UIConstants.CORNER_RADIUS_MEDIUM),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (apiKeyConfigured) {
                                viewModel.searchFlightsLive()
                                keyboardController?.hide()
                            }
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("input_flight_number")
                    )

                    // Origin and Destination row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = departure,
                            onValueChange = { viewModel.searchDepartureIata.value = it },
                            placeholder = { Text(stringResource(R.string.origin_placeholder), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.FlightTakeoff, contentDescription = stringResource(R.string.cd_origin), modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("input_departure_iata")
                        )

                        TextField(
                            value = arrival,
                            onValueChange = { viewModel.searchArrivalIata.value = it },
                            placeholder = { Text(stringResource(R.string.dest_placeholder), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.FlightLand, contentDescription = stringResource(R.string.cd_dest), modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("input_arrival_iata")
                        )
                    }

                     Button(
                        onClick = {
                            viewModel.searchFlightsLive()
                            keyboardController?.hide()
                        },
                        enabled = apiKeyConfigured && (flightNo.isNotBlank() || departure.isNotBlank() || arrival.isNotBlank()),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_live_search"),
                        shape = RoundedCornerShape(UIConstants.CORNER_RADIUS_MEDIUM),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_query_live), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (!apiKeyConfigured) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.currentTab.value = 3 }
                        .testTag("missing_key_warning_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(UIConstants.CORNER_RADIUS_SMALL)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning API Key",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.api_key_missing_title),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = stringResource(R.string.api_key_missing_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Go to Settings",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Scrollable area for dashboard and results
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Active Tracking prominently featured dashboard display
            item {
                ActiveTrackingSection(
                    flight = activeTrackingFlight,
                    modifier = Modifier.padding(horizontal = 0.dp) // Reset padding since LazyColumn handles it
                )
            }

            // Progress statistics display
            item {
                FreeTierUsageSection(
                    shieldCount = shieldRemaining,
                    onResetShield = { viewModel.resetRequestShield() },
                    modifier = Modifier.padding(horizontal = 0.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }

            when (val state = searchState) {
                is Resource.Loading -> {
                    item {
                        FlightSkeletonList()
                    }
                }
                is Resource.Error -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillParentMaxHeight(0.7f)
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = stringResource(R.string.cd_error_icon),
                                tint = ColorCancelled,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = state.uiText.asString(),
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.discover_offline_tip),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is Resource.Success -> {
                    val list = state.data
                    if (list.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillParentMaxHeight(0.7f)
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.AirplaneTicket,
                                    contentDescription = stringResource(R.string.cd_empty_results),
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.no_search_results),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = stringResource(R.string.no_search_results_sub),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = stringResource(R.string.scan_results_format, list.size),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                        }
                        items(list) { flight ->
                            FlightCardItem(
                                flight = flight,
                                onClick = { onFlightClick(flight) },
                                onBookmarkToggle = { viewModel.toggleBookmark(flight) },
                                onSelectForTracking = { onSelectForTracking(flight) },
                                isActiveTracking = activeTrackingFlight?.flightIata == flight.flightIata
                            )
                        }
                    }
                }
            }
        }
    }
}
