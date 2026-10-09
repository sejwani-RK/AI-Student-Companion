package com.example.ui.theme

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
    primary = AcademicBlueDarkPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = AcademicBlueDarkContainer,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = AcademicCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = AcademicPurple,
    onTertiary = Color.White,
    background = AcademicBackgroundDark,
    onBackground = Color(0xFFF1F5F9),
    surface = AcademicSurfaceDark,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = AcademicSurfaceDarkVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = AcademicCardBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = AcademicBluePrimary,
    onPrimary = Color.White,
    primaryContainer = AcademicBlueContainer,
    onPrimaryContainer = AcademicBlueOnContainer,
    secondary = AcademicBlueLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFF6FF),
    onSecondaryContainer = Color(0xFF1D4ED8),
    tertiary = AcademicCyan,
    onTertiary = Color.White,
    background = AcademicBackgroundLight,
    onBackground = AcademicSlateDark,
    surface = AcademicSurfaceLight,
    onSurface = AcademicSlateDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = AcademicSlateMedium,
    outline = AcademicCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent university branding by default
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
