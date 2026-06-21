package com.bytecoders.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FlightTakeoff
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
import com.bytecoders.ui.theme.MyApplicationTheme
import com.bytecoders.util.UIConstants

@Composable
fun SleekHeader(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(UIConstants.HEADER_HEIGHT)
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
                    .size(UIConstants.LOGO_SIZE)
                    .clip(RoundedCornerShape(UIConstants.CORNER_RADIUS_SMALL))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = stringResource(R.string.cd_takeoff_logo),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(UIConstants.ICON_SIZE_MEDIUM)
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
            modifier = Modifier.size(UIConstants.LOGO_SIZE)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = stringResource(R.string.cd_profile),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(UIConstants.ICON_SIZE_LARGE)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SleekHeaderPreview() {
    MyApplicationTheme {
        SleekHeader(onProfileClick = {})
    }
}
