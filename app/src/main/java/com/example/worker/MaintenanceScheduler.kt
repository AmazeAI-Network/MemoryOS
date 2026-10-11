package com.example.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

data class MaintenanceStats(
  val lastRunTimestamp: Long,
  val lastBytesReclaimed: Long,
  val lastFilesRemoved: Int,
  val lastStatus: String
) {
  val formattedLastRun: String
    get() {
      if (lastRunTimestamp == 0L) return "Never run yet"
      val sdf = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
      return sdf.format(Date(lastRunTimestamp))
    }

  val formattedBytesReclaimed: String
    get() {
      if (lastBytesReclaimed <= 0) return "0 KB"
      val kb = lastBytesReclaimed / 1024.0
      val mb = kb / 1024.0
      return if (mb >= 1.0) {
        String.format(Locale.US, "%.1f MB", mb)
      } else {
        String.format(Locale.US, "%.0f KB", kb)
      }
    }
}

object MaintenanceScheduler {
  private const val PERIODIC_WORK_NAME = "memoryos_periodic_maintenance"
  private const val IMMEDIATE_WORK_NAME = "memoryos_immediate_maintenance"

  /**
   * Schedules a 24-hour recurring WorkManager task to clean up old logs, crash dumps, and caches.
   */
  fun schedulePeriodicMaintenance(context: Context) {
    try {
      val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .setRequiresStorageNotLow(true)
        .build()

      val periodicWorkRequest = PeriodicWorkRequestBuilder<MaintenanceWorker>(
        repeatInterval = 24,
        repeatIntervalTimeUnit = TimeUnit.HOURS,
        flexTimeInterval = 2,
        flexTimeIntervalUnit = TimeUnit.HOURS
      )
        .setConstraints(constraints)
        .addTag("maintenance")
        .build()

      WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        PERIODIC_WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        periodicWorkRequest
      )
    } catch (_: Exception) {
      // Gracefully ignored in test environments where WorkManager is not initialized
    }
  }

  /**
   * Triggers an immediate one-time cleanup pass and returns a Flow of WorkInfo.
   */
  fun triggerImmediateMaintenance(context: Context): Flow<WorkInfo?> {
    val workRequest = OneTimeWorkRequestBuilder<MaintenanceWorker>()
      .addTag("immediate_maintenance")
      .build()

    val workManager = WorkManager.getInstance(context)
    workManager.enqueueUniqueWork(
      IMMEDIATE_WORK_NAME,
      ExistingWorkPolicy.REPLACE,
      workRequest
    )

    return workManager.getWorkInfoByIdFlow(workRequest.id)
  }

  /**
   * Reads the latest maintenance execution summary from SharedPreferences.
   */
  fun getLastMaintenanceStats(context: Context): MaintenanceStats {
    val prefs = context.getSharedPreferences(MaintenanceWorker.PREFS_NAME, Context.MODE_PRIVATE)
    return MaintenanceStats(
      lastRunTimestamp = prefs.getLong(MaintenanceWorker.KEY_LAST_RUN_TIMESTAMP, 0L),
      lastBytesReclaimed = prefs.getLong(MaintenanceWorker.KEY_LAST_BYTES_RECLAIMED, 0L),
      lastFilesRemoved = prefs.getInt(MaintenanceWorker.KEY_LAST_FILES_REMOVED, 0),
      lastStatus = prefs.getString(MaintenanceWorker.KEY_LAST_STATUS, "IDLE") ?: "IDLE"
    )
  }
}
