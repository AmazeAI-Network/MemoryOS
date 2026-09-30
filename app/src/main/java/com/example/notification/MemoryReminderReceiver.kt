package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MemoryReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (NotificationHelper.isReminderEnabled(context)) {
      NotificationHelper.showDailyReminderNotification(context)
      // Reschedule for next day
      NotificationHelper.scheduleDailyReminder(context)
    }
  }
}
