package com.bytecoders.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecoders.R
import com.bytecoders.data.local.CachedFlightEntity
import com.bytecoders.ui.FlightViewModel
import com.bytecoders.ui.components.SleekHeader
import com.bytecoders.util.UIConstants

@Composable
fun FlightRadarApp(
    viewModel: FlightViewModel
) {
    val context = LocalContext.current
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
                    tonalElevation = UIConstants.NAVIGATION_TONAL_ELEVATION,
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
