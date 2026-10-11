package com.example

import android.app.Application
import coil.Coil
import com.example.crash.GlobalCrashHandler
import com.example.media.CoilConfig
import com.example.notification.NotificationHelper
import com.example.security.EncryptedPreferencesManager
import com.example.sync.SyncQueueManager
import com.example.worker.MaintenanceScheduler

class MemoryApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    // 1. Install Custom Global Uncaught Exception Handler for crash resilience
    GlobalCrashHandler.install(this)

    // 2. Configure Coil image loading with optimized memory/disk cache and hardware bitmaps
    Coil.setImageLoader(CoilConfig.createImageLoader(this))

    // 3. Initialize hardware-backed EncryptedSharedPreferences
    EncryptedPreferencesManager.getInstance(this)

    // 4. Setup local daily engagement reminders channel and schedule
    NotificationHelper.createNotificationChannel(this)
    if (NotificationHelper.isReminderEnabled(this)) {
      NotificationHelper.scheduleDailyReminder(this)
    }

    // 6. Schedule WorkManager periodic maintenance for cleaning logs and caches
    try {
      MaintenanceScheduler.schedulePeriodicMaintenance(this)
    } catch (_: Exception) {}

    // 7. Initialize Offline-First Sync Queue Manager
    SyncQueueManager.getInstance(this)

    // 8. Initialize RevenueCat Billing Manager
    com.example.billing.BillingManager.initialize(this)

    // 9. Initialize Supabase Auth & Cloud Database Client
    com.example.data.supabase.SupabaseAuthManager.initialize(this)
  }
}
