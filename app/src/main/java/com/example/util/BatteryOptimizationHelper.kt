package com.example.util

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

/**
 * Utility helper to inspect Android battery optimization / Doze restrictions,
 * evaluate if background WorkManager tasks and log cleanups are throttled,
 * and safely guide users to whitelist the application for unrestricted background execution.
 */
object BatteryOptimizationHelper {
  private const val TAG = "BatteryOptimization"
  private const val PREFS_NAME = "memoryos_battery_optimization_prefs"
  private const val KEY_WARNING_DISMISSED = "key_battery_warning_dismissed"
  private const val KEY_DISMISSED_TIMESTAMP = "key_battery_dismissed_timestamp"

  /**
   * Checks whether the application is already whitelisted from battery optimizations.
   * Returns true if battery optimization is ignored (unrestricted background execution allowed),
   * or false if the application is restricted by system Doze / App Standby buckets.
   */
  fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      try {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
      } catch (e: Exception) {
        Log.e(TAG, "Failed to query battery optimization status: ${e.message}", e)
        false
      }
    } else {
      true
    }
  }

  /**
   * Checks if the user has dismissed the battery optimization warning banner on the Home screen.
   */
  fun isWarningDismissed(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_WARNING_DISMISSED, false)
  }

  /**
   * Records that the user dismissed the battery optimization warning banner.
   */
  fun setWarningDismissed(context: Context, dismissed: Boolean) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
      .putBoolean(KEY_WARNING_DISMISSED, dismissed)
      .putLong(KEY_DISMISSED_TIMESTAMP, System.currentTimeMillis())
      .apply()
  }

  /**
   * Requests whitelist exclusion from battery optimizations, using graceful multi-tier fallbacks:
   * 1. Direct prompt via ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
   * 2. Battery optimization overview via ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
   * 3. App Details settings via ACTION_APPLICATION_DETAILS_SETTINGS (Battery -> Unrestricted)
   */
  @SuppressLint("BatteryLife")
  fun requestIgnoreBatteryOptimizations(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return true
    }

    // Tier 1: System prompt to whitelist directly
    try {
      val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.parse("package:${context.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      return true
    } catch (e: Exception) {
      Log.w(TAG, "Tier 1 request ignore battery optimizations failed: ${e.message}")
    }

    // Tier 2: System battery optimization list settings
    try {
      val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      return true
    } catch (e: Exception) {
      Log.w(TAG, "Tier 2 battery optimization settings failed: ${e.message}")
    }

    // Tier 3: App info screen fallback
    return try {
      val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      true
    } catch (e: ActivityNotFoundException) {
      Log.e(TAG, "Tier 3 application details settings failed: ${e.message}")
      false
    }
  }

  /**
   * Returns human-readable OEM guidance instructions tailored to the device manufacturer.
   */
  fun getManufacturerGuidance(): String {
    val manufacturer = Build.MANUFACTURER.lowercase()
    return when {
      manufacturer.contains("samsung") ->
        "Samsung: Go to Settings → Apps → MemoryOS → Battery → select 'Unrestricted'."
      manufacturer.contains("xiaomi") || manufacturer.contains("redmi") ->
        "Xiaomi: Go to Settings → Apps → Manage Apps → MemoryOS → Battery Saver → select 'No restrictions'."
      manufacturer.contains("huawei") || manufacturer.contains("honor") ->
        "Huawei: Go to Settings → Battery → App Launch → find MemoryOS → toggle to 'Manage manually' with background allowed."
      manufacturer.contains("oppo") || manufacturer.contains("realme") ->
        "Oppo/Realme: Go to Settings → Battery → App Battery Management → MemoryOS → Allow background activity."
      manufacturer.contains("oneplus") ->
        "OnePlus: Go to Settings → Battery → Battery Optimization → MemoryOS → Don't optimize."
      else ->
        "Settings → Apps → MemoryOS → App battery usage → set to 'Unrestricted'."
    }
  }
}
