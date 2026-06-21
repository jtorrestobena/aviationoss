package com.bytecoders.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytecoders.R
import com.bytecoders.ui.FavoriteFlightStatus
import com.bytecoders.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FavoriteFlightNumberRow(
    flightNo: String,
    statusInfo: FavoriteFlightStatus?,
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

@Preview(showBackground = true)
@Composable
fun FavoriteFlightNumberRowPreview() {
    MyApplicationTheme {
        FavoriteFlightNumberRow(
            flightNo = "IB3112",
            statusInfo = FavoriteFlightStatus(
                flightNumber = "3112",
                status = "active",
                departureDelay = 10,
                arrivalDelay = 5,
                lastChecked = System.currentTimeMillis(),
                isPolling = false
            ),
            onRefresh = {},
            onDelete = {}
        )
    }
}
