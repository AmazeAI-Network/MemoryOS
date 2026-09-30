package com.example.ui.components

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

/**
 * High-performance, hardware-accelerated interactive touch feedback modifier.
 *
 * Micro-interactions & Touch Feedback Pillar:
 * 1. Instant visual response (< 50ms): Immediate scale transition down to [pressedScale] (default 0.96x).
 * 2. Natural spring physics: Spring easing (bouncy, low stiffness) for tactile physical feel.
 * 3. Haptic feedback: Subtle sensory tap on press to confirm touch.
 * 4. Hardware acceleration: Changes applied entirely on the GPU via [graphicsLayer], preventing
 *    unnecessary recompositions or layout recalculations across parent views (60-120 FPS).
 */
fun Modifier.bouncingClickable(
  enabled: Boolean = true,
  pressedScale: Float = 0.96f,
  enableHaptics: Boolean = true,
  onClickLabel: String? = null,
  onPressStart: (() -> Unit)? = null,
  onClick: () -> Unit
): Modifier = composed {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val haptic = LocalHapticFeedback.current
  val view = LocalView.current

  // Instant tactile feedback on press trigger
  LaunchedEffect(isPressed) {
    if (isPressed) {
      onPressStart?.invoke()
      if (enableHaptics) {
        try {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {
          view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
      }
    }
  }

  // Smooth, snappy spring animation for scale
  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled) pressedScale else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "BouncingScale"
  )

  this
    .graphicsLayer {
      scaleX = scale
      scaleY = scale
    }
    .clickable(
      interactionSource = interactionSource,
      indication = null, // Custom physical scale provides instant feedback without delayed ripples
      enabled = enabled,
      onClickLabel = onClickLabel,
      onClick = onClick
    )
}

/**
 * Subtle touch response modifier for cards and surface containers.
 */
fun Modifier.surfaceTouchResponse(
  enabled: Boolean = true,
  onClick: () -> Unit
): Modifier = bouncingClickable(
  enabled = enabled,
  pressedScale = 0.985f,
  enableHaptics = true,
  onClick = onClick
)
