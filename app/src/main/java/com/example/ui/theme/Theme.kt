package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SleekDarkPrimary,
    secondary = SleekDarkSecondaryText,
    tertiary = SleekDarkPrimaryContainer,
    background = SleekDarkBackground,
    surface = SleekDarkSurface,
    onPrimary = SleekDarkOnPrimary,
    onSecondary = SleekDarkOnPrimaryContainer,
    onBackground = SleekDarkOnPrimaryContainer,
    onSurface = SleekDarkOnPrimaryContainer,
    primaryContainer = SleekDarkPrimaryContainer,
    onPrimaryContainer = SleekDarkOnPrimaryContainer
)

private val LightColorScheme = lightColorScheme(
    primary = SleekPrimary,
    secondary = SleekSecondaryText,
    tertiary = SleekPrimaryContainer,
    background = SleekBackground,
    surface = SleekSurface,
    onPrimary = SleekOnPrimary,
    onSecondary = SleekOnPrimaryContainer,
    onBackground = SleekOnPrimaryContainer,
    onSurface = SleekOnPrimaryContainer,
    primaryContainer = SleekPrimaryContainer,
    onPrimaryContainer = SleekOnPrimaryContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic colors to enforce the beautiful premium Radar / Aviator custom styled color branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
