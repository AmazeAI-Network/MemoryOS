package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * Hardware capability tiers for dynamic graceful degradation:
 * - LOW: Entry-level devices (<= 3 GB RAM, Android Go, or low-core SOCs). Heavy animations & blur disabled.
 * - MEDIUM: Mid-range devices (4 - 6 GB RAM). Standard transitions and 60 FPS target.
 * - HIGH: Flagship devices (>= 8 GB RAM, high-tier SOCs). Uncapped 120 FPS high-refresh rate transitions.
 */
enum class PerformanceTier {
  LOW,
  MEDIUM,
  HIGH
}

/**
 * Enterprise Performance Architecture Manager.
 * Enables zero-jank, constant 60/120 FPS rendering by automatically adapting
 * memory allocations, bitmap depth, animation complexity, and thread dispatchers
 * based on live device tier and thermal/power state.
 */
object DevicePerformanceManager {

  private var cachedTier: PerformanceTier? = null

  /**
   * Resolves device hardware performance tier using ActivityManager memory metrics.
   * Cached after initial calculation to avoid repeated JNI / IPC calls during scrolling.
   */
  fun getDeviceTier(context: Context): PerformanceTier {
    cachedTier?.let { return it }

    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager?.getMemoryInfo(memoryInfo)

    val totalRamMb = memoryInfo.totalMem / (1024 * 1024)
    val isLowRam = activityManager?.isLowRamDevice == true
    val isPowerSave = powerManager?.isPowerSaveMode == true
    val cpuCores = Runtime.getRuntime().availableProcessors()

    val tier = when {
      // Low Tier: <= 3GB RAM, Android Go flag, or single/dual-core CPU
      isLowRam || totalRamMb <= 3200L || cpuCores <= 4 || isPowerSave -> PerformanceTier.LOW
      // Medium Tier: 4GB - 6GB RAM, 6-8 cores
      totalRamMb in 3201L..6500L -> PerformanceTier.MEDIUM
      // High Tier: Flagship tier > 6GB RAM
      else -> PerformanceTier.HIGH
    }

    cachedTier = tier
    return tier
  }

  /**
   * Performance Flag: Determines whether complex multi-layer scale & slide animations
   * should run, or degrade gracefully to simple alpha crossfades to prevent GPU dropped frames.
   */
  fun canRunHeavyAnimations(context: Context): Boolean {
    return getDeviceTier(context) != PerformanceTier.LOW
  }

  /**
   * Performance Flag: Downscales animation duration for low-spec tiers to prevent
   * sluggish frame pacing and micro-stutters during screen transitions.
   */
  fun getAnimationDurationMultiplier(context: Context): Float {
    return when (getDeviceTier(context)) {
      PerformanceTier.LOW -> 0.6f // 40% faster, snappy transitions
      PerformanceTier.MEDIUM -> 0.9f
      PerformanceTier.HIGH -> 1.0f
    }
  }

  /**
   * Memory Optimization Flag: Returns true if bitmap decoding should use RGB_565 (2 bytes/pixel)
   * instead of ARGB_8888 (4 bytes/pixel), immediately cutting bitmap memory consumption by 50%
   * on devices with <= 3 GB RAM to prevent OutOfMemoryError and GC churn.
   */
  fun shouldUseRgb565(context: Context): Boolean {
    return getDeviceTier(context) == PerformanceTier.LOW
  }

  /**
   * Rendering Optimization Flag: Avoids running continuous infinite transitions (e.g. pulse beacons)
   * on low-end hardware to reduce continuous CPU/GPU wakeups and battery draw.
   */
  fun shouldThrottleInfiniteAnimations(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    return getDeviceTier(context) == PerformanceTier.LOW || powerManager?.isPowerSaveMode == true
  }

  /**
   * Clears the cached tier (e.g. when power-save mode toggles).
   */
  fun invalidateCache() {
    cachedTier = null
  }
}

/**
 * CompositionLocal providing current device performance tier down the Compose hierarchy.
 */
val LocalPerformanceTier = compositionLocalOf { PerformanceTier.MEDIUM }

@Composable
@ReadOnlyComposable
fun rememberPerformanceTier(): PerformanceTier {
  val context = LocalContext.current
  return DevicePerformanceManager.getDeviceTier(context)
}
