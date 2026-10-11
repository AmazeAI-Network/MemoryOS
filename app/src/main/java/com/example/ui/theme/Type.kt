package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// ============================================================================
// MemoryOS Dual-Typography System
// Script Handwriting for Brand & Emotional Moments + Contemporary Sans-Serif for UI
// ============================================================================

// 1. Script Typography (Directly matching the handwritten "Me" & "MemoryOS" wordmark)
val MemoryScriptFontFamily = FontFamily(
  Font(R.font.alex_brush, FontWeight.Normal)
)

// 2. Contemporary Sans-Serif Typography (Plus Jakarta Sans for crisp editorial UI)
val MemorySansFontFamily = FontFamily(
  Font(R.font.plus_jakarta_sans, FontWeight.Normal)
)

// Aliases for general usage
val AppFontFamily = MemorySansFontFamily
val SFProDisplayFamily = MemorySansFontFamily
val SFProTextFamily = MemorySansFontFamily

// Expressive Script Styles for Brand Moments, Onboarding & Reflections
val ScriptLogoStyle = TextStyle(
  fontFamily = MemoryScriptFontFamily,
  fontWeight = FontWeight.Normal,
  fontSize = 32.sp,
  lineHeight = 36.sp,
  letterSpacing = 0.sp
)

val ScriptBrandDisplay = TextStyle(
  fontFamily = MemoryScriptFontFamily,
  fontWeight = FontWeight.Normal,
  fontSize = 38.sp,
  lineHeight = 44.sp,
  letterSpacing = 0.sp
)

val ScriptAccentHeading = TextStyle(
  fontFamily = MemoryScriptFontFamily,
  fontWeight = FontWeight.Normal,
  fontSize = 26.sp,
  lineHeight = 32.sp,
  letterSpacing = 0.sp
)

val ScriptQuoteStyle = TextStyle(
  fontFamily = MemoryScriptFontFamily,
  fontWeight = FontWeight.Normal,
  fontSize = 22.sp,
  lineHeight = 28.sp,
  letterSpacing = 0.sp
)

val ScriptEmblemStyle = TextStyle(
  fontFamily = MemoryScriptFontFamily,
  fontWeight = FontWeight.Normal,
  fontSize = 18.sp,
  lineHeight = 22.sp,
  letterSpacing = 0.sp
)

// Standard M3 Typography Scale powered by Plus Jakarta Sans
val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 38.sp,
    letterSpacing = (-0.4).sp
  ),
  displayMedium = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.3).sp
  ),
  displaySmall = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
    letterSpacing = (-0.2).sp
  ),
  headlineLarge = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.15).sp
  ),
  headlineMedium = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.1).sp
  ),
  headlineSmall = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.05).sp
  ),
  titleLarge = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp
  ),
  titleMedium = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.05.sp
  ),
  titleSmall = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 19.sp,
    letterSpacing = 0.1.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 23.sp,
    letterSpacing = 0.15.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.2.sp
  ),
  bodySmall = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 17.sp,
    letterSpacing = 0.25.sp
  ),
  labelLarge = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.15.sp
  ),
  labelMedium = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.25.sp
  ),
  labelSmall = TextStyle(
    fontFamily = MemorySansFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.3.sp
  )
)
