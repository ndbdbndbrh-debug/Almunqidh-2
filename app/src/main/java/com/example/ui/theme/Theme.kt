package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PharmaDarkColorScheme = darkColorScheme(
    primary = PurpleElectric,
    onPrimary = TextPrimary,
    primaryContainer = PurpleGlow,
    onPrimaryContainer = TextPrimary,
    secondary = RoyalBlueBright,
    onSecondary = TextPrimary,
    secondaryContainer = RoyalBlueMedium,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentCyan,
    onTertiary = TextPrimary,
    background = RoyalBlueDeep,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGlass,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceCardBorder,
    error = AccentRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = PharmaDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = RoyalBlueDeep.toArgb()
            window.navigationBarColor = RoyalBlueDeep.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
