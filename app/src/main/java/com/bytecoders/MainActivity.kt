package com.bytecoders

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecoders.data.local.CachedFlightEntity
import com.bytecoders.data.model.OpenMeteoResponse
import com.bytecoders.data.repository.Resource
import com.bytecoders.ui.FlightViewModel
import com.bytecoders.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val viewModel = ViewModelProvider(this)[FlightViewModel::class.java]
        
        setContent {
            MyApplicationTheme {
                FlightRadarApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FlightRadarApp(
    viewModel: FlightViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.toastChannelFlow.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val activeTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val shieldRemaining by viewModel.remainingFreeRequests.collectAsStateWithLifecycle()
    val isApiKeyConfigured by viewModel.isApiKeyConfigured.collectAsStateWithLifecycle()
    val cachedFlightsList by viewModel.filteredCachedFlights.collectAsStateWithLifecycle()
    val allCachedFlights by viewModel.allCachedFlights.collectAsStateWithLifecycle()

    var selectedFlightForDetail by remember { mutableStateOf<CachedFlightEntity?>(null) }
    var activeTrackingFlightState by remember { mutableStateOf<CachedFlightEntity?>(null) }
    var showProfileScreen by remember { mutableStateOf(false) }

    // Resolve the reactive flight for detail to ensure bookmark toggles reflect immediately
    val currentDetailFlight = selectedFlightForDetail?.let { selected ->
        allCachedFlights.find { it.id == selected.id } ?: selected
    }

    // Resolve the active flight being tracked prominently on the dashboard.
    val activeTrackingFlight = activeTrackingFlightState 
        ?: cachedFlightsList.firstOrNull { it.isBookmarked }
        ?: cachedFlightsList.firstOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("sleek_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { viewModel.currentTab.value = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                        label = { Text(stringResource(R.string.nav_home), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { viewModel.currentTab.value = 1 },
                        icon = { Icon(Icons.Default.Explore, contentDescription = stringResource(R.string.nav_discover)) },
                        label = { Text(stringResource(R.string.nav_discover), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { viewModel.currentTab.value = 2 },
                        icon = { Icon(Icons.Default.Star, contentDescription = stringResource(R.string.nav_watchlist)) },
                        label = { Text(stringResource(R.string.nav_watchlist), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { viewModel.currentTab.value = 3 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                        label = { Text(stringResource(R.string.nav_settings), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Title Header with aviation emblem logo
                SleekHeader(
                    onProfileClick = { showProfileScreen = true }
                )

                // Screen Selector Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (activeTab) {
                        0 -> LiveSearchScreen(
                            viewModel = viewModel,
                            apiKeyConfigured = isApiKeyConfigured,
                            onFlightClick = { selectedFlightForDetail = it },
                            onSelectForTracking = { activeTrackingFlightState = it },
                            activeTrackingFlight = activeTrackingFlight,
                            shieldRemaining = shieldRemaining
                        )
                        1 -> CacheVaultScreen(
                            viewModel = viewModel,
                            onFlightClick = { selectedFlightForDetail = it },
                            onSelectForTracking = { activeTrackingFlightState = it },
                            activeTrackingFlight = activeTrackingFlight
                        )
                        2 -> WatchlistScreen(
                            viewModel = viewModel,
                            onFlightClick = { selectedFlightForDetail = it },
                            onSelectForTracking = { activeTrackingFlightState = it },
                            activeTrackingFlight = activeTrackingFlight
                        )
                        3 -> SettingsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showProfileScreen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            ProfileConfigScreen(
                apiKeyConfigured = isApiKeyConfigured,
                shieldCount = shieldRemaining,
                onDismiss = { showProfileScreen = false },
                onResetShield = { viewModel.resetRequestShield() }
            )
        }

        AnimatedVisibility(
            visible = selectedFlightForDetail != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            currentDetailFlight?.let { flight ->
                LaunchedEffect(flight.id) {
                    viewModel.fetchUpcomingFlight(flight.departureIata ?: "", flight.id)
                    viewModel.fetchDestinationWeather(flight.arrivalIata ?: "")
                }
                val upcomingState by viewModel.upcomingFlightForDetail.collectAsStateWithLifecycle()
                val weatherState by viewModel.destinationWeather.collectAsStateWithLifecycle()
                FlightDetailScreen(
                    flight = flight,
                    upcomingState = upcomingState,
                    weatherState = weatherState,
                    onDismiss = { selectedFlightForDetail = null },
                    onToggleBookmark = { viewModel.toggleBookmark(flight) },
                    onUpcomingFlightClick = { nextFlight ->
                        selectedFlightForDetail = nextFlight
                    }
                )
            }
        }
    }
}

@Composable
fun SleekHeader(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = stringResource(R.string.cd_takeoff_logo),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 21.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = stringResource(R.string.cd_profile),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ActiveTrackingSection(
    flight: CachedFlightEntity?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(28.dp),
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
                            .clip(RoundedCornerShape(12.dp))
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

                Spacer(modifier = Modifier.height(18.dp))

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

                Spacer(modifier = Modifier.height(18.dp))

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
                        text = stringResource(R.string.arrival_format, formatTime(flight.arrivalScheduled)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun FreeTierUsageSection(
    shieldCount: Int,
    onResetShield: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.free_tier_usage),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = stringResource(R.string.free_tier_reqs_format, shieldCount),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onResetShield() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (shieldCount.toFloat() / 100f).coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

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
                    .clip(RoundedCornerShape(24.dp))
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
                        shape = RoundedCornerShape(24.dp),
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
                        shape = RoundedCornerShape(24.dp),
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
                    shape = RoundedCornerShape(16.dp)
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

@Composable
fun CacheVaultScreen(
    viewModel: FlightViewModel,
    onFlightClick: (CachedFlightEntity) -> Unit,
    onSelectForTracking: (CachedFlightEntity) -> Unit,
    activeTrackingFlight: CachedFlightEntity?
) {
    val cachedList by viewModel.filteredCachedFlights.collectAsStateWithLifecycle()
    val query by viewModel.cacheSearchQuery.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Vault control search bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AllInbox,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.cache_vault_title),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        TextButton(
                            onClick = { viewModel.clearHistory() },
                            colors = ButtonDefaults.textButtonColors(contentColor = ColorCancelled),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.clear_cache), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    TextField(
                        value = query,
                        onValueChange = { viewModel.cacheSearchQuery.value = it },
                        placeholder = { Text(stringResource(R.string.filter_cache_placeholder), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.cd_filter), tint = MaterialTheme.colorScheme.secondary) },
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
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("input_cache_filter")
                    )
                }
            }
        }

        // Vault list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (cachedList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = stringResource(R.string.cd_empty_vault),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.vault_empty),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = stringResource(R.string.vault_empty_sub),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.recent_searches),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }

                    items(cachedList) { flight ->
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

@Composable
fun FavoriteFlightNumberRow(
    flightNo: String,
    statusInfo: com.bytecoders.ui.FavoriteFlightStatus?,
    onRefresh: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = flightNo,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Status pill/color if polled
                    if (statusInfo?.status != null) {
                        val status = statusInfo.status
                        val (color, label) = when {
                            status == "not_found" -> Pair(MaterialTheme.colorScheme.secondary, stringResource(R.string.status_not_found))
                            status.lowercase() == "cancelled" -> Pair(ColorCancelled, stringResource(R.string.status_cancelled_upper))
                            status.lowercase() == "active" -> Pair(ColorActive, stringResource(R.string.status_active_upper))
                            status.lowercase() == "delayed" -> Pair(ColorCancelled, stringResource(R.string.status_delayed_upper))
                            else -> Pair(ColorScheduled, status.uppercase())
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(color.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Detail updates text
                val detailText = when {
                    statusInfo?.isPolling == true -> stringResource(R.string.refreshing_data)
                    statusInfo?.status == "not_found" -> stringResource(R.string.no_current_schedule)
                    statusInfo?.status != null -> {
                        val totalDelay = (statusInfo.departureDelay ?: 0) + (statusInfo.arrivalDelay ?: 0)
                        val delayStr = if (totalDelay > 0) stringResource(R.string.delayed_format, totalDelay) else stringResource(R.string.on_time)
                        val updateTime = if (statusInfo.lastChecked != null) {
                            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(statusInfo.lastChecked))
                            stringResource(R.string.at_time_format, timeStr)
                        } else ""
                        stringResource(R.string.updated_format, delayStr, updateTime)
                    }
                    else -> stringResource(R.string.pending_status)
                }

                Text(
                    text = detailText,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Actions row: refresh, remove
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (statusInfo?.isPolling == true) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.cd_poll_status),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_remove_favorite),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

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
                .clip(RoundedCornerShape(12.dp))
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

@Composable
fun ProfileConfigScreen(
    apiKeyConfigured: Boolean,
    shieldCount: Int,
    onDismiss: () -> Unit,
    onResetShield: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = stringResource(R.string.cd_lock_shield_logo),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.profile_config_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (apiKeyConfigured) ColorActive.copy(alpha = 0.12f)
                        else ColorWarning.copy(alpha = 0.12f)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (apiKeyConfigured) stringResource(R.string.live_api_secured) else stringResource(R.string.offline_vault_active),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (apiKeyConfigured) ColorActive else ColorWarning
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (apiKeyConfigured) {
                    stringResource(R.string.shield_desc_secured, shieldCount)
                } else {
                    stringResource(R.string.shield_desc_offline)
                },
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { 
                        onResetShield()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.reset_shield))
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

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
                        DetailPairRow(stringResource(R.string.sched_label), formatTimeDetailed(flight.departureScheduled))
                        DetailPairRow(stringResource(R.string.est_label), formatTimeDetailed(flight.departureEstimated))
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
                        DetailPairRow(stringResource(R.string.sched_label), formatTimeDetailed(flight.arrivalScheduled))
                        DetailPairRow(stringResource(R.string.est_label), formatTimeDetailed(flight.arrivalEstimated))
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

                val weatherInfo = when (val res = weatherState) {
                    is Resource.Success -> {
                        val code = res.data?.currentWeather?.weathercode
                        when (code) {
                            0 -> Pair(stringResource(R.string.weather_clear), Icons.Default.WbSunny)
                            1, 2, 3 -> Pair(stringResource(R.string.weather_partly_cloudy), Icons.Default.Cloud)
                            45, 48 -> Pair(stringResource(R.string.weather_foggy), Icons.Default.Cloud)
                            51, 53, 55 -> Pair(stringResource(R.string.weather_drizzle), Icons.Default.Cloud)
                            61, 63, 65 -> Pair(stringResource(R.string.weather_rainy), Icons.Default.Cloud)
                            71, 73, 75 -> Pair(stringResource(R.string.weather_snowy), Icons.Default.Cloud)
                            80, 81, 82 -> Pair(stringResource(R.string.weather_showers), Icons.Default.Cloud)
                            95, 96, 99 -> Pair(stringResource(R.string.weather_thunderstorm), Icons.Default.Warning)
                            else -> Pair(stringResource(R.string.weather_overcast), Icons.Default.Cloud)
                        }
                    }
                    else -> Pair(stringResource(R.string.weather_overcast), Icons.Default.Cloud)
                }

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
fun DetailPairRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun formatTime(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "--:--"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val date = parser.parse(isoString) ?: return "--:--"
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = parser.parse(isoString) ?: return "--:--"
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(date)
        } catch (e2: Exception) {
            isoString.substringAfter("T").take(5)
        }
    }
}

fun formatTimeDetailed(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "--"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val date = parser.parse(isoString) ?: return "--"
        val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = parser.parse(isoString) ?: return "--"
            val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
            formatter.format(date)
        } catch (e2: Exception) {
            isoString.replace("T", " ").take(16)
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: FlightViewModel
) {
    var apiKeyText by remember { mutableStateOf(viewModel.getUserApiKey()) }
    var keyVisible by remember { mutableStateOf(false) }
    val isApiKeyConfigured by viewModel.isApiKeyConfigured.collectAsStateWithLifecycle()
    val shieldRemaining by viewModel.remainingFreeRequests.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.settings_sub),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Section: API Key Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("api_key_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "API Key Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.api_config_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = stringResource(R.string.api_config_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Key Input Field
                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        modifier = Modifier.fillMaxWidth().testTag("api_key_input_field"),
                        label = { Text(stringResource(R.string.api_key_label)) },
                        placeholder = { Text(stringResource(R.string.api_key_placeholder)) },
                        singleLine = true,
                        visualTransformation = if (keyVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = stringResource(R.string.visibility_toggle)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveUserApiKey(apiKeyText.trim())
                            },
                            modifier = Modifier.weight(1f).testTag("save_api_key_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(R.string.save_key))
                        }
                        
                        OutlinedButton(
                            onClick = {
                                apiKeyText = ""
                                viewModel.saveUserApiKey("")
                            },
                            modifier = Modifier.weight(1f).testTag("clear_api_key_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Text(stringResource(R.string.clear_key))
                        }
                    }

                    // Key Status Badge
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isApiKeyConfigured) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isApiKeyConfigured) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "Config Status",
                                tint = if (isApiKeyConfigured) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = if (isApiKeyConfigured) stringResource(R.string.api_active_msg) else stringResource(R.string.api_mock_msg),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isApiKeyConfigured) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        // Section: Request Shield Control
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("shield_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Shield Icon",
                            tint = RadarTertiary
                        )
                        Text(
                            text = stringResource(R.string.shield_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = stringResource(R.string.shield_desc_format, shieldRemaining),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { viewModel.resetRequestShield() },
                        modifier = Modifier.fillMaxWidth().testTag("reset_shield_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.reset_daily_shield), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: About & Sign up link
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.how_to_register_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.how_to_register_steps),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun shimmerBrush(
    targetValue: Float = 1000f,
    showShimmer: Boolean = true
): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "shimmer_transition")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translation"
        )

        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        SolidColor(MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
fun FlightSkeletonCard(brush: Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container skeleton
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Identification info skeleton
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Flight ID/number bar
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            // Sub-info bar (cities, carrier)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Right side badge and action
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status Tag skeleton
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
            // Bookmark icon skeleton
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
        }
    }
}

@Composable
fun FlightSkeletonList() {
    val brush = shimmerBrush()
    
    // Rotating radar visual for expressive material feedback
    val transition = rememberInfiniteTransition(label = "radar_sweep")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("flight_skeleton_loading"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Expressive Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Spinning customized expressive loading radar indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = stringResource(R.string.cd_radar_sweep),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer(rotationZ = rotation)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.sync_radar_telemetry),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.interrogating_transponders),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Render multiple cards for skeleton list representation
        repeat(4) {
            FlightSkeletonCard(brush = brush)
        }
    }
}

fun getAirportCoords(iata: String?): Pair<Double, Double> {
    val upper = iata?.uppercase()?.trim() ?: ""
    return when (upper) {
        "SFO" -> Pair(37.6213, -122.3790)
        "DFW" -> Pair(32.8998, -97.0403)
        "LHR" -> Pair(51.4700, -0.4543)
        "IAD" -> Pair(38.9531, -77.4565)
        "FRA" -> Pair(50.0379, 8.5622)
        "JFK" -> Pair(40.6413, -73.7781)
        "HND" -> Pair(35.5494, 139.7798)
        "LAX" -> Pair(33.9416, -118.4085)
        "ORD" -> Pair(41.9742, -87.9073)
        "CDG" -> Pair(49.0097, 2.5479)
        "AMS" -> Pair(52.3105, 4.7683)
        "SIN" -> Pair(1.3644, 103.9915)
        "DXB" -> Pair(25.2532, 55.3657)
        "HKG" -> Pair(22.3080, 113.9185)
        "SYD" -> Pair(-33.9461, 151.1772)
        "ATL" -> Pair(33.6407, -84.4277)
        "DEN" -> Pair(39.8561, -104.6737)
        "PEK" -> Pair(40.0799, 116.5971)
        "PVG" -> Pair(31.1443, 121.8083)
        "CAN" -> Pair(23.3924, 113.2988)
        "IST" -> Pair(41.2752, 28.7519)
        "DEL" -> Pair(28.5562, 77.1001)
        "BOM" -> Pair(19.0896, 72.8656)
        "NRT" -> Pair(35.7720, 140.3929)
        "KIX" -> Pair(34.4320, 135.2304)
        "ICN" -> Pair(37.4602, 126.4407)
        "MAD" -> Pair(40.4839, -3.5680)
        "FCO" -> Pair(41.8003, 12.2389)
        "MUC" -> Pair(48.3538, 11.7861)
        "BCN" -> Pair(41.2974, 2.0833)
        "YVR" -> Pair(49.1967, -123.1815)
        "YYZ" -> Pair(43.6777, -79.6248)
        "MEL" -> Pair(-37.6690, 144.8410)
        "MIA" -> Pair(25.7959, -80.2870)
        "SEA" -> Pair(47.4502, -122.3088)
        "BOS" -> Pair(42.3656, -71.0096)
        "EWR" -> Pair(40.6895, -74.1745)
        "CLT" -> Pair(35.2140, -80.9431)
        "PHX" -> Pair(33.4352, -112.0101)
        "IAH" -> Pair(29.9902, -95.3368)
        "MCO" -> Pair(28.4281, -81.3160)
        "EZE" -> Pair(-34.8222, -58.5358)
        "GRU" -> Pair(-23.4318, -46.4678)
        "CPH" -> Pair(55.6180, 12.6508)
        "ARN" -> Pair(59.6519, 17.9186)
        "HEL" -> Pair(60.3172, 24.9633)
        "OSL" -> Pair(60.1976, 11.1004)
        "LIS" -> Pair(38.7756, -9.1355)
        "ATH" -> Pair(37.9356, 23.9484)
        "DOH" -> Pair(25.2611, 51.5651)
        "RUH" -> Pair(24.9576, 46.6988)
        "JED" -> Pair(21.6796, 39.1565)
        "TPE" -> Pair(25.0797, 121.2342)
        "BKK" -> Pair(13.6899, 100.7501)
        "KUL" -> Pair(2.7456, 101.7072)
        "MNL" -> Pair(14.5086, 121.0194)
        "CGK" -> Pair(-6.1256, 106.6559)
        "BNE" -> Pair(-27.3842, 153.1175)
        "AKL" -> Pair(-37.0081, 174.7917)
        "CPT" -> Pair(-33.9715, 18.6021)
        "JNB" -> Pair(-26.1367, 28.2411)
        "MEX" -> Pair(19.4363, -99.0721)
        "DUB" -> Pair(53.4264, -6.2701)
        "MAN" -> Pair(53.3588, -2.2749)
        "BRU" -> Pair(50.9008, 4.4844)
        "ZRH" -> Pair(47.4582, 8.5555)
        "VIE" -> Pair(48.1103, 16.5697)
        "GVA" -> Pair(46.2370, 6.1092)
        else -> {
            val code = upper.take(3)
            val baseLat = 20.0 + (code.getOrNull(0)?.code?.rem(40) ?: 0) - 20.0
            val baseLng = (code.getOrNull(1)?.code?.rem(180) ?: 0) * if ((code.getOrNull(2)?.code?.rem(2) ?: 0) == 0) 1.0 else -1.0
            Pair(baseLat, baseLng)
        }
    }
}

@Composable
fun FlightRouteMap(
    flight: CachedFlightEntity,
    modifier: Modifier = Modifier
) {
    val depIata = flight.departureIata ?: stringResource(R.string.n_a)
    val arrIata = flight.arrivalIata ?: stringResource(R.string.n_a)
    val depCoords = getAirportCoords(depIata)
    val arrCoords = getAirportCoords(arrIata)

    val showLive = flight.liveLatitude != null && flight.liveLongitude != null
    val liveLat = flight.liveLatitude ?: 0.0
    val liveLng = flight.liveLongitude ?: 0.0
    val flightIataStr = flight.flightIata ?: flight.flightNumber ?: "Flight"

    val htmlContent = remember(flight) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <style>
                html, body {
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    background-color: #12131a;
                }
                #map {
                    width: 100%;
                    height: 100%;
                    background-color: #12131a;
                }
                .leaflet-container {
                    background-color: #12131a !important;
                }
                .custom-tooltip {
                    background: #1e202c !important;
                    border: 1px solid #4a5470 !important;
                    color: #ffffff !important;
                    font-family: 'Roboto', sans-serif;
                    font-size: 10px;
                    font-weight: bold;
                    border-radius: 6px;
                    padding: 2px 6px;
                    box-shadow: 0 4px 6px rgba(0,0,0,0.3);
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <script>
                var map = L.map('map', {
                    zoomControl: false,
                    attributionControl: false
                });

                L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
                    maxZoom: 19
                }).addTo(map);

                var depLatLng = [${depCoords.first}, ${depCoords.second}];
                var arrLatLng = [${arrCoords.first}, ${arrCoords.second}];

                L.circleMarker(depLatLng, {
                    radius: 7,
                    fillColor: '#3b82f6',
                    color: '#ffffff',
                    weight: 2,
                    fillOpacity: 0.95
                }).addTo(map).bindTooltip('$depIata', { permanent: true, direction: 'top', className: 'custom-tooltip' });

                L.circleMarker(arrLatLng, {
                    radius: 7,
                    fillColor: '#10b981',
                    color: '#ffffff',
                    weight: 2,
                    fillOpacity: 0.95
                }).addTo(map).bindTooltip('$arrIata', { permanent: true, direction: 'bottom', className: 'custom-tooltip' });

                var path = L.polyline([depLatLng, arrLatLng], {
                    color: '#6366f1',
                    weight: 3,
                    dashArray: '4, 6',
                    opacity: 0.75
                }).addTo(map);

                var bounds = L.latLngBounds([depLatLng, arrLatLng]);

                if ($showLive) {
                    var liveLatLng = [$liveLat, $liveLng];
                    L.circleMarker(liveLatLng, {
                        radius: 8,
                        fillColor: '#f97316',
                        color: '#ffffff',
                        weight: 2,
                        fillOpacity: 1.0
                    }).addTo(map).bindTooltip('$flightIataStr ✈', { permanent: true, direction: 'right', className: 'custom-tooltip' });
                    
                    bounds.extend(liveLatLng);
                }

                map.fitBounds(bounds, { padding: [35, 35] });
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = modifier
            .testTag("flight_route_map")
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    webViewClient = WebViewClient()
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://unpkg.com", htmlContent, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

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
                                text = formatTimeDetailed(flight.departureScheduled),
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
