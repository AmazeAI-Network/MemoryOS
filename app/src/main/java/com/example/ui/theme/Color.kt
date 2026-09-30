package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// MemoryOS Monochromatic Foundation & Official Brand Color System
// Monochromatic Foundation: Black (#000000), White (#FFFFFF)
// Official Brand Color: Cream (#F7F3ED)
// ============================================================================

// Core Monochromatic Foundation
val BrandBlack = Color(0xFF000000)
val BrandWhite = Color(0xFFFFFFFF)

// MemoryOS Official Brand Color (Cream #F7F3ED)
val MemoryOSCream = Color(0xFFF7F3ED)
val WarmCream = MemoryOSCream // Direct brand alias
val WarmCreamLight = Color(0xFFFAF7F2)
val WarmCreamDark = Color(0xFFECE7DE)
val WarmCreamSurface = BrandWhite
val WarmCreamBorder = Color(0xFFE8E2D8)
val WarmCreamBorderSubtle = Color(0xFFF0EBE2)

// Ink Scale (Monochromatic Typography & Icons Hierarchy)
val InkPrimary = BrandBlack
val InkSecondary = Color(0xFF3E3B38)
val InkMuted = Color(0xFF757069)
val InkDisabled = Color(0xFFB5B0A8)

// Selective, Muted Functional Accents (Restrained & Sophisticated)
val MutedSage = Color(0xFF385E4D)
val MutedSageLight = Color(0xFFECF3EF)
val MutedEarth = Color(0xFF7A5246)
val MutedEarthLight = Color(0xFFF6EFEA)
val MutedSlate = Color(0xFF4E5866)
val MutedSlateLight = Color(0xFFEEF1F5)

// Backward Compatibility Aliases aligned to the Monochromatic / Cream palette
val MidnightNavy = BrandBlack
val SlateNavy = Color(0xFF141414)
val DustyBlue = BrandBlack
val DustyBlueLight = Color(0xFFEFEAE1)
val TerracottaRed = MutedEarth
val TerracottaLight = MutedEarthLight
val WarmIvory = MemoryOSCream
val WarmCanvas = WarmCreamLight
val PureWhite = BrandWhite
val CardBackground = BrandWhite
val CardBorder = WarmCreamBorder

// Text Semantic Colors
val TextPrimary = InkPrimary
val TextSecondary = InkSecondary
val TextMuted = InkMuted

// Functional Accent Aliases
val EmeraldGreen = MutedSage
val EmeraldLight = MutedSageLight
val AmberOrange = Color(0xFF94682A)
val AmberLight = Color(0xFFF8F3E8)
val SoftPurple = Color(0xFF5A4D6B)
val SoftPurpleLight = Color(0xFFF2EFF7)
