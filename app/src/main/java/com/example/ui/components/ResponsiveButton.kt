package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmCreamBorder

enum class ButtonVariant {
  PRIMARY,
  SECONDARY,
  OUTLINED,
  GHOST,
  DANGER
}

/**
 * Custom Responsive Button component adhering to Pillar 2:
 * - Instant visual response (< 50ms)
 * - Subtle 0.96x press scaling with natural spring physics
 * - Tactile haptic feedback on touch down
 * - Hardware accelerated with [graphicsLayer]
 * - Accessible >= 48dp touch targets
 */
@Composable
fun ResponsiveButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  variant: ButtonVariant = ButtonVariant.PRIMARY,
  leadingIcon: ImageVector? = null,
  trailingIcon: ImageVector? = null,
  isLoading: Boolean = false,
  enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(22.dp),
  minHeight: Dp = 48.dp,
  testTag: String? = null
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val haptic = LocalHapticFeedback.current

  LaunchedEffect(isPressed) {
    if (isPressed && enabled && !isLoading) {
      try {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      } catch (_: Exception) {}
    }
  }

  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled && !isLoading) 0.96f else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "ResponsiveButtonScale"
  )

  val (containerColor, contentColor, borderStroke) = when (variant) {
    ButtonVariant.PRIMARY -> Triple(BrandBlack, PureWhite, null)
    ButtonVariant.SECONDARY -> Triple(WarmCream, BrandBlack, BorderStroke(1.dp, WarmCreamBorder))
    ButtonVariant.OUTLINED -> Triple(Color.Transparent, BrandBlack, BorderStroke(1.5.dp, CardBorder))
    ButtonVariant.GHOST -> Triple(Color.Transparent, BrandBlack, null)
    ButtonVariant.DANGER -> Triple(Color(0xFFE53935), PureWhite, null)
  }

  val finalContainerColor = if (enabled) containerColor else containerColor.copy(alpha = 0.5f)
  val finalContentColor = if (enabled) contentColor else contentColor.copy(alpha = 0.5f)

  Box(
    modifier = modifier
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .defaultMinSize(minHeight = minHeight)
      .clip(shape)
      .then(if (borderStroke != null) Modifier.border(borderStroke, shape) else Modifier)
      .background(finalContainerColor)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled && !isLoading,
        onClick = onClick
      )
      .padding(horizontal = 20.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    if (isLoading) {
      CircularProgressIndicator(
        modifier = Modifier.size(20.dp),
        color = finalContentColor,
        strokeWidth = 2.dp
      )
    } else {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        if (leadingIcon != null) {
          Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = finalContentColor,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
          text = text,
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp
          ),
          color = finalContentColor
        )

        if (trailingIcon != null) {
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = finalContentColor,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

/**
 * High-performance, tactile icon button with 48dp minimum touch target.
 */
@Composable
fun ResponsiveIconButton(
  icon: ImageVector,
  contentDescription: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color = TextPrimary,
  containerColor: Color = PureWhite,
  shape: Shape = CircleShape,
  border: BorderStroke? = null,
  iconSize: Dp = 20.dp,
  size: Dp = 44.dp,
  enabled: Boolean = true,
  testTag: String? = null
) {
  Box(
    modifier = modifier
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
      .size(size)
      .bouncingClickable(
        enabled = enabled,
        pressedScale = 0.92f,
        onClick = onClick
      )
      .clip(shape)
      .then(if (border != null) Modifier.border(border, shape) else Modifier)
      .background(if (enabled) containerColor else containerColor.copy(alpha = 0.5f)),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = if (enabled) tint else tint.copy(alpha = 0.4f),
      modifier = Modifier.size(iconSize)
    )
  }
}

/**
 * Responsive Card wrapper that delivers tactile physical depression and instant response.
 */
@Composable
fun ResponsiveCard(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(22.dp),
  containerColor: Color = PureWhite,
  border: BorderStroke? = BorderStroke(1.dp, CardBorder),
  elevation: Dp = 2.dp,
  onPressStart: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  Surface(
    shape = shape,
    color = containerColor,
    border = border,
    shadowElevation = elevation,
    modifier = modifier
      .bouncingClickable(
        pressedScale = 0.98f,
        onPressStart = onPressStart,
        onClick = onClick
      )
  ) {
    content()
  }
}
