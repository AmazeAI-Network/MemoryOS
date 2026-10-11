package com.example.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.example.ui.viewmodel.Screen
import com.example.util.DevicePerformanceManager
import com.example.util.PerformanceTier

// High-speed, natural easing curve: cubic-bezier(0.4, 0.0, 0.2, 1)
val MotionDecelerateEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

// Targeted instantaneous duration (< 300ms)
const val NAV_ENTER_DURATION_MS = 260
const val NAV_EXIT_DURATION_MS = 190

/**
 * High-performance Page Transition HOC / Wrapper.
 *
 * Implements Pillars 1, 2, & 5:
 * - Sub-300ms total transition time (< 0.5s limit) for instant responsiveness.
 * - Hardware acceleration via [graphicsLayer] to maintain 60-120 FPS without dropped frames.
 * - Graceful degradation: Automatically switches to ultra-fast lightweight alpha crossfades
 *   on low-spec / entry-level devices (<= 3GB RAM) to eliminate GPU overdraw and lag.
 * - Direction-aware transitions for mid and flagship tiers.
 */
@Composable
fun FastPageTransition(
  targetScreen: Screen,
  modifier: Modifier = Modifier,
  content: @Composable (Screen) -> Unit
) {
  val context = LocalContext.current
  val tier = DevicePerformanceManager.getDeviceTier(context)

  AnimatedContent(
    targetState = targetScreen,
    transitionSpec = {
      // Graceful degradation for low-spec devices:
      // Pure fast alpha crossfade (< 120ms) eliminates complex multi-axis matrix transforms
      if (tier == PerformanceTier.LOW) {
        return@AnimatedContent fadeIn(
          animationSpec = tween(durationMillis = 120)
        ).togetherWith(
          fadeOut(animationSpec = tween(durationMillis = 100))
        )
      }

      val isEnteringDetail = targetState is Screen.MemoryDetail ||
        targetState is Screen.CollectionDetail ||
        targetState is Screen.Settings

      val isExitingDetail = initialState is Screen.MemoryDetail ||
        initialState is Screen.CollectionDetail ||
        initialState is Screen.Settings

      when {
        // Entering a detail screen or modal screen (slide up + subtle scale up)
        isEnteringDetail -> {
          (slideInVertically(
            animationSpec = tween(durationMillis = NAV_ENTER_DURATION_MS, easing = MotionDecelerateEasing),
            initialOffsetY = { fullHeight -> (fullHeight * 0.12f).toInt() }
          ) + fadeIn(
            animationSpec = tween(durationMillis = NAV_ENTER_DURATION_MS, easing = MotionDecelerateEasing)
          ) + scaleIn(
            initialScale = 0.96f,
            animationSpec = tween(durationMillis = NAV_ENTER_DURATION_MS, easing = MotionDecelerateEasing)
          )).togetherWith(
            fadeOut(
              animationSpec = tween(durationMillis = NAV_EXIT_DURATION_MS, easing = FastOutSlowInEasing)
            ) + scaleOut(
              targetScale = 0.98f,
              animationSpec = tween(durationMillis = NAV_EXIT_DURATION_MS)
            )
          )
        }

        // Exiting from a detail screen back to root (slide down + fade)
        isExitingDetail -> {
          (fadeIn(
            animationSpec = tween(durationMillis = NAV_ENTER_DURATION_MS, easing = MotionDecelerateEasing)
          ) + scaleIn(
            initialScale = 0.97f,
            animationSpec = tween(durationMillis = NAV_ENTER_DURATION_MS, easing = MotionDecelerateEasing)
          )).togetherWith(
            slideOutVertically(
              animationSpec = tween(durationMillis = NAV_EXIT_DURATION_MS, easing = FastOutSlowInEasing),
              targetOffsetY = { fullHeight -> (fullHeight * 0.10f).toInt() }
            ) + fadeOut(
              animationSpec = tween(durationMillis = NAV_EXIT_DURATION_MS, easing = FastOutSlowInEasing)
            )
          )
        }

        // Primary tab navigation (Home, Ask, Memories, Collections, Profile)
        else -> {
          val initialIndex = getTabIndex(initialState)
          val targetIndex = getTabIndex(targetState)
          val forward = targetIndex >= initialIndex

          (slideInHorizontally(
            animationSpec = tween(durationMillis = 240, easing = MotionDecelerateEasing),
            initialOffsetX = { fullWidth -> if (forward) (fullWidth * 0.08f).toInt() else (-fullWidth * 0.08f).toInt() }
          ) + fadeIn(
            animationSpec = tween(durationMillis = 220, easing = MotionDecelerateEasing)
          )).togetherWith(
            slideOutHorizontally(
              animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
              targetOffsetX = { fullWidth -> if (forward) (-fullWidth * 0.06f).toInt() else (fullWidth * 0.06f).toInt() }
            ) + fadeOut(
              animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
            )
          )
        }
      }
    },
    label = "FastPageTransition",
    modifier = modifier.graphicsLayer() // Native hardware layer acceleration
  ) { screen ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .graphicsLayer()
    ) {
      content(screen)
    }
  }
}

private fun getTabIndex(screen: Screen): Int = when (screen) {
  Screen.Home -> 0
  Screen.Ask -> 1
  Screen.Memories -> 2
  Screen.Collections -> 3
  Screen.Profile -> 4
  else -> 0
}
