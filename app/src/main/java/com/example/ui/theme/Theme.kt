package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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
  outline = WarmCreamBorder
)

/**
 * MemoryOS Dark Color Scheme
 * Inverted monochromatic luxury palette with deep obsidian tones.
 */
private val DarkColorScheme = darkColorScheme(
  primary = BrandWhite,
  onPrimary = BrandBlack,
  primaryContainer = SlateNavy,
  onPrimaryContainer = BrandWhite,
  secondary = WarmCreamDark,
  onSecondary = BrandBlack,
  secondaryContainer = SlateNavy,
  onSecondaryContainer = BrandWhite,
  tertiary = MutedEarthLight,
  onTertiary = BrandBlack,
  tertiaryContainer = SlateNavy,
  onTertiaryContainer = WarmCreamDark,
  background = BrandBlack,
  onBackground = BrandWhite,
  surface = SlateNavy,
  onSurface = BrandWhite,
  surfaceVariant = BrandBlack,
  onSurfaceVariant = WarmCreamBorder,
  outline = SlateNavy
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

