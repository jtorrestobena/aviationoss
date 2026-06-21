package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CachedFlightEntity
import com.example.data.repository.Resource
import com.example.ui.FlightViewModel
import com.example.ui.theme.*
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
    val activeTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val shieldRemaining by viewModel.remainingFreeRequests.collectAsStateWithLifecycle()
    val isApiKeyConfigured = viewModel.isApiKeyConfigured
    val cachedFlightsList by viewModel.filteredCachedFlights.collectAsStateWithLifecycle()

    var selectedFlightForDetail by remember { mutableStateOf<CachedFlightEntity?>(null) }
    var activeTrackingFlightState by remember { mutableStateOf<CachedFlightEntity?>(null) }

    // Resolve the active flight being tracked prominently on the dashboard.
    // Default to the first bookmarked or first cached flight on launch to ensure an instantly populated beautiful view!
    val activeTrackingFlight = activeTrackingFlightState 
        ?: cachedFlightsList.firstOrNull { it.isBookmarked }
        ?: cachedFlightsList.firstOrNull()

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
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
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
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Discover") },
                    label = { Text("Discover", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
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
                    icon = { Icon(Icons.Default.Star, contentDescription = "Watchlist") },
                    label = { Text("Watchlist", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
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
            var showProfileDialog by remember { mutableStateOf(false) }

            // Title Header with aviation emblem logo
            SleekHeader(
                onProfileClick = { showProfileDialog = true }
            )

            if (showProfileDialog) {
                ProfileConfigDialog(
                    apiKeyConfigured = isApiKeyConfigured,
                    shieldCount = shieldRemaining,
                    onDismiss = { showProfileDialog = false },
                    onResetShield = { viewModel.resetRequestShield() }
                )
            }

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
                }
            }
        }
    }

    // Detail cockpit popup scan
    selectedFlightForDetail?.let { flight ->
        FlightDetailDialog(
            flight = flight,
            onDismiss = { selectedFlightForDetail = null },
            onToggleBookmark = { viewModel.toggleBookmark(flight) }
        )
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
                    contentDescription = "Takeoff Logo",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = "Avionstack",
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
                contentDescription = "Credentials Profile",
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
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
                    text = "Awaiting Radar Scan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Search flight details below or browse cached database entries.",
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
                            text = "ACTIVE TRACKING",
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
                            text = (flight.flightStatus ?: "EN ROUTE").uppercase(),
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
                            text = flight.departureIata ?: "JFK",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = flight.departureAirport?.split(",")?.firstOrNull()?.split(" ")?.firstOrNull() ?: "New York",
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
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
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
                            text = flight.arrivalIata ?: "LAX",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = flight.arrivalAirport?.split(",")?.firstOrNull()?.split(" ")?.firstOrNull() ?: "Los Angeles",
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
                        text = "Data source: cached_server_ams_1",
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Arrival: ${formatTime(flight.arrivalScheduled)}",
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Free Tier Usage",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "$shieldCount / 100 reqs",
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
                        placeholder = { Text("Search flight number or airline", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.secondary) },
                        trailingIcon = {
                            if (flightNo.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchFlightNumber.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.secondary)
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
                            placeholder = { Text("Origin (SFO)", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.FlightTakeoff, contentDescription = "Origin", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary) },
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
                            placeholder = { Text("Dest (LAX)", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.FlightLand, contentDescription = "Dest", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary) },
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
                            Text("Query Aviationstack Live", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Tracking prominently featured dashboard display
        ActiveTrackingSection(flight = activeTrackingFlight)

        // Progress statistics display
        FreeTierUsageSection(
            shieldCount = shieldRemaining,
            onResetShield = { viewModel.resetRequestShield() }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Live Search query results list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            when (val state = searchState) {
                is Resource.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Contacting radar servers...",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 13.sp
                        )
                    }
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Error icon",
                            tint = ColorCancelled,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "💡 Tap Discover to view offline cached flights instantly.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is Resource.Success -> {
                    val list = state.data
                    if (list.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AirplaneTicket,
                                contentDescription = "Flights results empty",
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Search Results",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Enter flight configurations above to run scanning telemetry.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp)
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
                                    text = "SCAN RESULTS (${list.size})",
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
                                text = "Offline Cache Vault",
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
                            Text("Clear Cache", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    TextField(
                        value = query,
                        onValueChange = { viewModel.cacheSearchQuery.value = it },
                        placeholder = { Text("Filter cached database (SFO, DL2419, Delta)", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.secondary) },
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
                        contentDescription = "Empty vault",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vault Empty",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Searches map matches automatic cache storage.",
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
                            text = "RECENT SEARCHES",
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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (bookmarkedList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.StarOutline,
                        contentDescription = "Empty Watchlist",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Watchlist is Empty",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Tap the star icon on any flight cards to populate your tracking VIP list.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
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
                            text = "WATCHED VIP FLIGHTS (${bookmarkedList.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }

                    items(bookmarkedList) { flight ->
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
                text = "${flight.departureIata} → ${flight.arrivalIata} • ${flight.airlineName ?: "Carrier"}",
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
                    text = (flight.flightStatus ?: "SCHEDULED").uppercase(),
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
                    contentDescription = "Watchlist trigger",
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
fun ProfileConfigDialog(
    apiKeyConfigured: Boolean,
    shieldCount: Int,
    onDismiss: () -> Unit,
    onResetShield: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Lock Shield logo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Aviationstack Cockpit",
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
                        text = if (apiKeyConfigured) "LIVE API SECURED" else "OFFLINE CACHE VAULT ACTIVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (apiKeyConfigured) ColorActive else ColorWarning
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (apiKeyConfigured) {
                        "Avionstack searches are actively proxied through local SQlite caches under credential shield limits. Remaining free query count is $shieldCount/100."
                    } else {
                        "No credentials active. Avionstack is running offline. Enjoy interactive flight telemetry and bookmarks securely."
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
                        Text("Reset Shield")
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun FlightDetailDialog(
    flight: CachedFlightEntity,
    onDismiss: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    val statusColor = when (flight.flightStatus?.lowercase()) {
        "active" -> ColorActive
        "scheduled" -> ColorScheduled
        "landed" -> ColorLanded
        "cancelled" -> ColorCancelled
        else -> ColorScheduled
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = flight.flightIata ?: "Flight ${flight.flightNumber}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = flight.airlineName ?: "Unknown carrier",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Row {
                        IconButton(onClick = onToggleBookmark) {
                            Icon(
                                imageVector = if (flight.isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Watchlist toggler",
                                tint = if (flight.isBookmarked) RadarTertiary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(24.dp))
                        }
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
                        text = "Aviation Status:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = (flight.flightStatus ?: "SCHEDULED").uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Departure & Arrival Details columns
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DEPARTURE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(flight.departureIata ?: "N/A", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(flight.departureAirport ?: "N/A", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailPairRow("Terminal", flight.departureTerminal ?: "--")
                        DetailPairRow("Gate", flight.departureGate ?: "--")
                        DetailPairRow("Delay", if (flight.departureDelay == null) "On Time" else "${flight.departureDelay}m")
                        DetailPairRow("Sched", formatTimeDetailed(flight.departureScheduled))
                        DetailPairRow("Est", formatTimeDetailed(flight.departureEstimated))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("ARRIVAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(flight.arrivalIata ?: "N/A", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(flight.arrivalAirport ?: "N/A", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailPairRow("Terminal", flight.arrivalTerminal ?: "--")
                        DetailPairRow("Gate", flight.arrivalGate ?: "--")
                        DetailPairRow("Baggage", flight.arrivalBaggage ?: "--")
                        DetailPairRow("Sched", formatTimeDetailed(flight.arrivalScheduled))
                        DetailPairRow("Est", formatTimeDetailed(flight.arrivalEstimated))
                    }
                }

                // Aircraft systems telemetry info
                if (flight.aircraftIata != null || flight.liveAltitude != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                    Text("TELEMETRY SCANNER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        flight.aircraftIata?.let { model ->
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Equipment", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(model, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                flight.aircraftRegistration?.let { reg ->
                                    Text("Reg: $reg", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }

                        if (flight.liveAltitude != null || flight.liveSpeed != null) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("Radar Metrics", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                flight.liveAltitude?.let { alt ->
                                    Text("Altitude: ${alt.toInt()} ft", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                flight.liveSpeed?.let { speed ->
                                    Text("Speed: ${speed.toInt()} kts", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Close Telemetry Scan", fontWeight = FontWeight.Bold)
                }
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
