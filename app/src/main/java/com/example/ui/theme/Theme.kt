package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * MemoryOS Light Color Scheme
 * Founded strictly on Brand Cream (#F7F3ED) background,
 * Brand Black (#000000) typography/primary actions, and Pure White (#FFFFFF) surfaces.
 */
private val LightColorScheme = lightColorScheme(
  primary = BrandBlack,
  onPrimary = BrandWhite,
  primaryContainer = WarmCreamDark,
  onPrimaryContainer = BrandBlack,
  secondary = InkSecondary,
  onSecondary = BrandWhite,
  secondaryContainer = WarmCreamBorderSubtle,
  onSecondaryContainer = BrandBlack,
  tertiary = MutedEarth,
  onTertiary = BrandWhite,
  tertiaryContainer = MutedEarthLight,
  onTertiaryContainer = MutedEarth,
  background = MemoryOSCream,
  onBackground = BrandBlack,
  surface = BrandWhite,
  onSurface = BrandBlack,
  surfaceVariant = WarmCreamLight,
  onSurfaceVariant = InkSecondary,
  outline = WarmCreamBorder,
  outlineVariant = WarmCreamBorderSubtle
)

/**
 * MemoryOS Dark Color Scheme
 * Inverted monochromatic luxury palette with deep obsidian tones and clear contrast.
 */
private val DarkColorScheme = darkColorScheme(
  primary = BrandWhite,
  onPrimary = BrandBlack,
  primaryContainer = Color(0xFF2C2C2C),
  onPrimaryContainer = BrandWhite,
  secondary = Color(0xFFD6D6D6),
  onSecondary = BrandBlack,
  secondaryContainer = Color(0xFF252525),
  onSecondaryContainer = BrandWhite,
  tertiary = MutedEarthLight,
  onTertiary = BrandBlack,
  tertiaryContainer = Color(0xFF2C2C2C),
  onTertiaryContainer = Color(0xFFE0E0E0),
  background = Color(0xFF121212),
  onBackground = Color(0xFFF5F5F5),
  surface = Color(0xFF1E1E1E),
  onSurface = Color(0xFFF5F5F5),
  surfaceVariant = Color(0xFF282828),
  onSurfaceVariant = Color(0xFFB0B0B0),
  outline = Color(0xFF383838),
  outlineVariant = Color(0xFF2A2A2A)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Preserve bespoke brand palette inspired by user references
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

