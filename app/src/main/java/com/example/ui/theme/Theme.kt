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
  primary = BthEmerald,
  onPrimary = Color.Black,
  primaryContainer = BthTeal,
  onPrimaryContainer = Color.White,
  secondary = BthCyan,
  onSecondary = Color.Black,
  tertiary = SpeedTurbo,
  background = BthDarkBackground,
  onBackground = Color(0xFFE9EDEF),
  surface = BthDarkSurface,
  onSurface = Color(0xFFE9EDEF),
  surfaceVariant = BthDarkSurfaceVariant,
  onSurfaceVariant = Color(0xFF8696A0),
  outline = Color(0xFF374248)
)

private val LightColorScheme = lightColorScheme(
  primary = BthLightPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD9FDD3),
  onPrimaryContainer = Color(0xFF003822),
  secondary = BthTeal,
  onSecondary = Color.White,
  tertiary = SpeedTurbo,
  background = BthLightBackground,
  onBackground = Color(0xFF111B21),
  surface = BthLightSurface,
  onSurface = Color(0xFF111B21),
  surfaceVariant = Color(0xFFE2E5E9),
  onSurfaceVariant = Color(0xFF54656F),
  outline = Color(0xFFD1D7DB)
)

@Composable
fun BthChatTheme(
  darkTheme: Boolean = true, // Default to sleek dark mode for high-tech P2P vibe
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
