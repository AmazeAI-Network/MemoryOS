package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.BrandWhite
import com.example.ui.theme.MemoryScriptFontFamily
import com.example.ui.theme.ScriptBrandDisplay
import com.example.ui.theme.ScriptLogoStyle
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmCreamBorder

/**
 * MemoryOS Official Brand Emblem ("Me" Squircle)
 * App icon completely removed across page & frontend/UI per user request.
 */
@Composable
fun MemoryOSEmblem(
  modifier: Modifier = Modifier,
  size: Dp = 36.dp,
  backgroundColor: Color = BrandBlack,
  contentColor: Color = BrandWhite
) {
  // App icon completely removed across UI
}

/**
 * Horizontal Brand Header: Distinctive Script Wordmark
 */
@Composable
fun MemoryOSBrandHeader(
  modifier: Modifier = Modifier,
  logoSize: Dp = 32.dp,
  textColor: Color = BrandBlack,
  showTagline: Boolean = false
) {
  Column(
    modifier = modifier
  ) {
    Text(
      text = "MemoryOS",
      fontFamily = MemoryScriptFontFamily,
      fontSize = (logoSize.value * 0.95f).sp,
      color = textColor,
      lineHeight = (logoSize.value * 1.0f).sp
    )
    if (showTagline) {
      Text(
        text = "Your Personal Memory Space",
        style = MaterialTheme.typography.labelSmall,
        color = textColor.copy(alpha = 0.65f),
        fontSize = 10.sp
      )
    }
  }
}

/**
 * Complete Brand Stack (Script wordmark + tagline)
 * App icon completely removed.
 */
@Composable
fun MemoryOSCompleteLogo(
  modifier: Modifier = Modifier,
  iconSize: Dp = 72.dp,
  textColor: Color = BrandBlack,
  tagline: String? = "Your Personal Memory Space"
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "MemoryOS",
      fontFamily = MemoryScriptFontFamily,
      fontSize = 38.sp,
      color = textColor,
      lineHeight = 42.sp
    )
    if (tagline != null) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = tagline,
        style = MaterialTheme.typography.bodySmall,
        color = textColor.copy(alpha = 0.6f),
        letterSpacing = 0.5.sp
      )
    }
  }
}

/**
 * Subtle Brand Signature Motif for Personal Reflections & Memories
 * App icon completely removed.
 */
@Composable
fun MeMotifSignature(
  text: String,
  modifier: Modifier = Modifier,
  textColor: Color = BrandBlack
) {
  Text(
    text = text,
    fontFamily = MemoryScriptFontFamily,
    fontSize = 18.sp,
    color = textColor,
    modifier = modifier
  )
}
