package com.bytecoders.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytecoders.R
import com.bytecoders.ui.theme.MyApplicationTheme
import com.bytecoders.util.UIConstants

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
                animation = tween(durationMillis = UIConstants.SHIMMER_DURATION, easing = LinearEasing),
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
                .clip(RoundedCornerShape(UIConstants.CORNER_RADIUS_SMALL))
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
            animation = tween(durationMillis = UIConstants.RADAR_SWEEP_DURATION, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = UIConstants.PULSE_DURATION, easing = FastOutSlowInEasing),
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

@Preview(showBackground = true)
@Composable
fun FlightSkeletonListPreview() {
    MyApplicationTheme {
        FlightSkeletonList()
    }
}
