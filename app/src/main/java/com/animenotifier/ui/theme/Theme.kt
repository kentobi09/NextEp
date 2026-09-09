package com.animenotifier.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = TextPrimary,
    primaryContainer = SurfaceElevatedHigh,
    onPrimaryContainer = TextPrimary,
    secondary = AccentMonochrome,
    onSecondary = SurfaceRoot,
    background = SurfaceRoot,
    onBackground = TextPrimary,
    surface = SurfaceElevated,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevatedHigh,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AccentPrimary,
    onPrimary = TextPrimary,
    primaryContainer = LightSurfaceBorder,
    onPrimaryContainer = LightTextPrimary,
    secondary = LightTextPrimary,
    onSecondary = LightSurfaceElevated,
    background = LightSurfaceRoot,
    onBackground = LightTextPrimary,
    surface = LightSurfaceElevated,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceBorder,
    onSurfaceVariant = LightTextSecondary,
    outline = LightSurfaceBorder
)

@Composable
fun AnimeNotifierTheme(
    darkTheme: Boolean = true, // Default to true dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
