package com.example.worker

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Enterprise WorkManager task for periodic maintenance and cache sanitization.
 * Cleans up expired crash logs, transient feedback screenshots, old downloaded APKs,
 * and stale cache files to prevent storage bloat and ensure optimal performance
 * during long-running background operations.
 */
class MaintenanceWorker(
  appContext: Context,
  workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    Log.i(TAG, "Starting periodic storage & log maintenance task...")

    var totalBytesReclaimed = 0L
    var totalFilesRemoved = 0

    val now = System.currentTimeMillis()
    val sevenDaysAgo = now - TimeUnit.DAYS.toMillis(7)
    val oneDayAgo = now - TimeUnit.DAYS.toMillis(1)
    val twoDaysAgo = now - TimeUnit.DAYS.toMillis(2)

    try {
      // 1. Clean up old crash reports (> 7 days, or preserve only 5 latest)
      val crashDir = File(applicationContext.filesDir, "crashes")
      if (crashDir.exists() && crashDir.isDirectory) {
        val crashFiles = crashDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
        crashFiles.forEachIndexed { index, file ->
          if (index >= 5 || file.lastModified() < sevenDaysAgo) {
            val length = file.length()
            if (file.delete()) {
              totalBytesReclaimed += length
              totalFilesRemoved++
            }
          }
        }
      }

      // 2. Clean up temporary bug reporting screenshots (> 24 hours)
      val cacheDir = applicationContext.cacheDir
      if (cacheDir.exists() && cacheDir.isDirectory) {
        cacheDir.listFiles()?.forEach { file ->
          if (file.isFile && file.name.startsWith("feedback_screenshot_") && file.lastModified() < oneDayAgo) {
            val length = file.length()
            if (file.delete()) {
              totalBytesReclaimed += length
              totalFilesRemoved++
            }
          }
        }
      }

      // 3. Clean up stale downloaded update APKs (> 2 days)
      val downloadsDir = applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
      if (downloadsDir != null && downloadsDir.exists()) {
        downloadsDir.listFiles()?.forEach { file ->
          if (file.isFile && file.name.endsWith(".apk") && file.lastModified() < twoDaysAgo) {
            val length = file.length()
            if (file.delete()) {
              totalBytesReclaimed += length
              totalFilesRemoved++
            }
          }
        }
      }

      // 4. Clean up temporary HTTP/Image cache orphaned files (.tmp)
      val httpCache = File(cacheDir, "http_cache")
      if (httpCache.exists() && httpCache.isDirectory) {
        httpCache.listFiles()?.forEach { file ->
          if (file.name.endsWith(".tmp") && file.lastModified() < oneDayAgo) {
            val length = file.length()
            if (file.delete()) {
              totalBytesReclaimed += length
              totalFilesRemoved++
            }
          }
        }
      }

      // Record maintenance run summary into SharedPreferences
      val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      prefs.edit().apply {
        putLong(KEY_LAST_RUN_TIMESTAMP, now)
        putLong(KEY_LAST_BYTES_RECLAIMED, totalBytesReclaimed)
        putInt(KEY_LAST_FILES_REMOVED, totalFilesRemoved)
        putString(KEY_LAST_STATUS, "SUCCESS")
        apply()
      }

      Log.i(
        TAG,
        "Maintenance completed successfully: Reclaimed $totalBytesReclaimed bytes across $totalFilesRemoved files."
      )

      val outputData = workDataOf(
        OUTPUT_BYTES_RECLAIMED to totalBytesReclaimed,
        OUTPUT_FILES_REMOVED to totalFilesRemoved
      )

      Result.success(outputData)
    } catch (e: Exception) {
      Log.e(TAG, "Error occurred during maintenance cleanup: ${e.message}", e)
      Result.failure()
    }
  }

  companion object {
    private const val TAG = "MaintenanceWorker"
    const val PREFS_NAME = "memoryos_maintenance_prefs"
    const val KEY_LAST_RUN_TIMESTAMP = "last_run_timestamp"
    const val KEY_LAST_BYTES_RECLAIMED = "last_bytes_reclaimed"
    const val KEY_LAST_FILES_REMOVED = "last_files_removed"
    const val KEY_LAST_STATUS = "last_status"

    const val OUTPUT_BYTES_RECLAIMED = "output_bytes_reclaimed"
    const val OUTPUT_FILES_REMOVED = "output_files_removed"
  }
}
