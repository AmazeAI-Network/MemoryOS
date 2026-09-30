package com.example.media

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.util.DevicePerformanceManager
import com.example.util.PerformanceTier
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Enterprise ImageLoader configuration for Coil.
 * Applies memory management and tier-based graceful degradation best practices to eliminate OOM risks:
 * 1. Dynamically scales memory cache (15% heap on low-RAM devices, 25% on mid/high tiers).
 * 2. Dedicated disk cache for decoded bitmaps and thumbnails (25MB on low-RAM, 50MB on mid/high).
 * 3. Enables hardware bitmaps on Android 8.0+ (Oreo) to store pixels in GPU VRAM rather than Java heap.
 * 4. Enables RGB_565 on entry-level devices to cut bitmap memory consumption by 50% and prevent GC pauses.
 */
object CoilConfig {

  fun createImageLoader(context: Context): ImageLoader {
    val tier = DevicePerformanceManager.getDeviceTier(context)
    val memoryPercent = if (tier == PerformanceTier.LOW) 0.15 else 0.25
    val diskCacheBytes = if (tier == PerformanceTier.LOW) 25L * 1024 * 1024 else 50L * 1024 * 1024
    val isRgb565Preferred = DevicePerformanceManager.shouldUseRgb565(context)

    return ImageLoader.Builder(context)
      .memoryCache {
        MemoryCache.Builder(context)
          .maxSizePercent(memoryPercent)
          .strongReferencesEnabled(true)
          .build()
      }
      .diskCache {
        DiskCache.Builder()
          .directory(File(context.cacheDir, "image_cache"))
          .maxSizeBytes(diskCacheBytes)
          .build()
      }
      .okHttpClient {
        OkHttpClient.Builder()
          .connectTimeout(12, TimeUnit.SECONDS)
          .readTimeout(12, TimeUnit.SECONDS)
          .build()
      }
      .crossfade(true)
      .crossfade(if (tier == PerformanceTier.LOW) 150 else 250)
      .allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) // Hardware bitmaps on Android 8+
      .allowRgb565(isRgb565Preferred) // 50% RAM savings on low-spec devices
      .build()
  }
}
