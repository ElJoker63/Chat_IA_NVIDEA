package com.bdavidgm.glm_chat.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NvidiaGreen,
    onPrimary = Color.Black,
    secondary = DarkSurface,
    onSecondary = Color.White,
    tertiary = NvidiaGreen,
    background = DarkBackground,
    surface = DarkBackground,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.Gray
)

@Composable
fun GLMchatTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
