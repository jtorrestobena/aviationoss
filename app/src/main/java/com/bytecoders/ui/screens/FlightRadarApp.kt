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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment as AlignmentComp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
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

    val navItems = listOf(
        Triple(0, Icons.Default.Home, R.string.nav_home),
        Triple(1, Icons.Default.Explore, R.string.nav_discover),
        Triple(2, Icons.Default.Star, R.string.nav_watchlist),
        Triple(3, Icons.Default.Settings, R.string.nav_settings)
    )

    val suiteItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            navItems.forEach { (index, icon, labelRes) ->
                item(
                    selected = activeTab == index,
                    onClick = { viewModel.currentTab.value = index },
                    icon = { 
                        Icon(
                            imageVector = icon, 
                            contentDescription = stringResource(labelRes),
                            modifier = Modifier.size(24.dp)
                        ) 
                    },
                    label = { 
                        Text(
                            text = stringResource(labelRes), 
                            fontSize = 12.sp, 
                            fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium
                        ) 
                    },
                    colors = suiteItemColors
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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

    Box(modifier = Modifier.fillMaxSize()) {
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

@Preview(name = "Phone Portrait", showSystemUi = true, device = Devices.PIXEL_4)
@Preview(name = "Phone Landscape", showSystemUi = true, device = "spec:width=891dp,height=411dp,orientation=landscape,dpi=420")
@Preview(name = "Tablet", showSystemUi = true, device = Devices.PIXEL_C)
@Preview(name = "Large Font", showSystemUi = true, fontScale = 1.5f)
@Composable
fun FlightRadarAppPreview() {
    // This is a placeholder for structural demonstration. 
    // In a real scenario, you'd pass a mock or a controlled state ViewModel.
}
