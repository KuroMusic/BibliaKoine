package com.biblia.koine.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define Dark Colors (Pure Black Mode)
// Define Dark Colors (Pure Black Mode)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4AF37),      // Gold
    onPrimary = Color(0xFF000000),    // Black text on Gold
    primaryContainer = Color(0xFFD4AF37), // Gold for containers
    onPrimaryContainer = Color(0xFF000000),
    secondary = Color(0xFFD4AF37),    // Gold
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF4A3B00), // Dark Gold
    onSecondaryContainer = Color(0xFFF7EBCA), // Soft Light Gold Text
    tertiary = Color(0xFFEEEEEE),     // Light Gary
    onTertiary = Color(0xFF000000),
    background = Color(0xFF000000),   // Pure Black
    onBackground = Color(0xFFFFFFFF), // White Text
    surface = Color(0xFF121212),      // Dark Gray Surface
    onSurface = Color(0xFFFFFFFF),    // White Text
    surfaceVariant = Color(0xFF1E1E1E), // Slightly lighter for cards
    onSurfaceVariant = Color(0xFFB0B0B0), // Light Gray text
)

// Define Light Colors (White Mode)
private val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFFD4AF37),      // Gold
    onPrimary = Color(0xFFFFFFFF),    // White text on Gold
    primaryContainer = Color(0xFFD4AF37),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFD4AF37),    // Gold
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF7EBCA), // Soft Gold (Light)
    onSecondaryContainer = Color(0xFF4A3B00), // Dark Gold Text
    tertiary = Color(0xFF424242),     // Dark Gray
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),   // Pure White
    onBackground = Color(0xFF000000), // Pure Black Text
    surface = Color(0xFFF5F5F5),      // Light Gray Surface
    onSurface = Color(0xFF000000),    // Pure Black Text
    surfaceVariant = Color(0xFFFFFFFF), // White cards on Light Gray surface
    onSurfaceVariant = Color(0xFF424242), // Dark Gray text
)

@Composable
fun BibliaKoineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            // Fix Status Bar Contrast (Dark icons on Light theme)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
