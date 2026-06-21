package com.bytecoders.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecoders.R
import com.bytecoders.data.local.CachedFlightEntity
import com.bytecoders.ui.FlightViewModel
import com.bytecoders.ui.components.FlightCardItem
import com.bytecoders.ui.theme.ColorCancelled
import com.bytecoders.util.UIConstants

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
                    .clip(RoundedCornerShape(UIConstants.CORNER_RADIUS_MEDIUM))
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
