package com.bytecoders.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecoders.R
import com.bytecoders.ui.FlightViewModel
import com.bytecoders.ui.theme.RadarTertiary

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
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
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
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(R.string.save_key))
                        }
                        
                        OutlinedButton(
                            onClick = {
                                apiKeyText = ""
                                viewModel.saveUserApiKey("")
                            },
                            modifier = Modifier.weight(1f).testTag("clear_api_key_btn"),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
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
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
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
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
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
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
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
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
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
