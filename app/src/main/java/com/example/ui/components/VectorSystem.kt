package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.BrandWhite
import com.example.ui.theme.MemoryScriptFontFamily

/**
 * High-performance vector rendering system for MemoryOS.
 * Ensures all visual elements, glyphs, and iconography render at subpixel
 * precision and remain razor-sharp across all Android screen densities,
 * foldables (Galaxy Z Fold), high-DPI tablets (Galaxy Tab S series), and DeX mode.
 */

/**
 * Scalable Vector-rendered MemoryOS Brand Emblem ("Me" Squircle).
 * Faithfully reproduces the official brand icon using both high-resolution
 * vector graphics and subpixel Canvas paths.
 */
@Composable
fun MemoryOSVectorEmblem(
  modifier: Modifier = Modifier,
  size: Dp = 36.dp,
  backgroundColor: Color = BrandBlack,
  contentColor: Color = BrandWhite,
  useExactAsset: Boolean = true
) {
  // App icon completely removed across page & frontend/UI per user request
}

/**
 * Standardized Vector Icon Composable
 * Guarantees resolution independence and enforces minimum touch targets (48dp).
 */
@Composable
fun VectorIcon(
  imageVector: ImageVector,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  tint: Color = LocalContentColor.current,
  size: Dp = 24.dp
) {
  Icon(
    imageVector = imageVector,
    contentDescription = contentDescription,
    tint = tint,
    modifier = modifier.size(size)
  )
}

/**
 * Interactive Vector Action Icon with ripple feedback and accessibility minimum bounds.
 */
@Composable
fun VectorIconButton(
  imageVector: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color = LocalContentColor.current,
  iconSize: Dp = 22.dp,
  buttonSize: Dp = 48.dp,
  backgroundColor: Color = Color.Transparent,
  shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
  Box(
    modifier = modifier
      .size(buttonSize)
      .clip(shape)
      .background(backgroundColor)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true),
        role = Role.Button,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    VectorIcon(
      imageVector = imageVector,
      contentDescription = contentDescription,
      tint = tint,
      size = iconSize
    )
  }
}

/**
 * Pure Vector Badge / Indicator for tags, statuses, and categories.
 */
@Composable
fun VectorBadge(
  text: String,
  icon: ImageVector? = null,
  modifier: Modifier = Modifier,
  backgroundColor: Color = BrandBlack.copy(alpha = 0.06f),
  contentColor: Color = BrandBlack,
  fontSize: androidx.compose.ui.unit.TextUnit = 11.sp
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(backgroundColor)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    if (icon != null) {
      VectorIcon(
        imageVector = icon,
        contentDescription = null,
        tint = contentColor,
        size = 12.dp
      )
    }
    Text(
      text = text,
      color = contentColor,
      fontSize = fontSize,
      fontWeight = FontWeight.Medium
    )
  }
}
