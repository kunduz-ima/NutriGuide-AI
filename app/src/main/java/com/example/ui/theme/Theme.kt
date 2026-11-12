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

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF0F5257), // DeepForest
    onPrimary = Color.White,
    secondary = Color(0xFF0B6A6F), // LightEmerald
    onSecondary = Color.White,
    background = Color(0xFFEAF5F5), // LightPastelGreen
    onBackground = Color(0xFF0F5257), // DeepForest
    surface = Color.White,
    onSurface = Color(0xFF1E293B), // Slate 800 (Highly readable dark text)
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onSurfaceVariant = Color(0xFF334155), // Slate 700
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  // To completely resolve the white-on-white text visibility issue in dark themes,
  // we force LightColorScheme as the design of NutriGuide relies on specific pastel-green and white backgrounds.
  val colorScheme = LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
