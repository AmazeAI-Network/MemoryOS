package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import java.util.Calendar

object NotificationHelper {
  const val CHANNEL_ID = "daily_memory_engagement_reminders"
  const val CHANNEL_NAME = "Daily Memory Reminders"
  const val CHANNEL_DESC = "Daily notifications to remind you to capture and review memory snippets."

  const val EXTRA_OPEN_VOICE_CAPTURE = "extra_open_voice_capture"
  const val PREF_NAME = "memory_os_notifications"
  const val KEY_REMINDER_ENABLED = "key_reminder_enabled"
  const val KEY_REMINDER_HOUR = "key_reminder_hour"
  const val KEY_REMINDER_MINUTE = "key_reminder_minute"

  private const val NOTIFICATION_ID = 2026
  private const val ALARM_REQUEST_CODE = 4040

  private val ENGAGING_PROMPTS = listOf(
    Pair("Time for your daily memory check-in", "What was the highlight of your day? Tap to capture a quick voice memory."),
    Pair("Capture today's moments", "Visited a new place, met someone, or bought something? Save it before you forget."),
    Pair("Reflect on your day", "Take 20 seconds to record today's memories and thoughts into your memory database."),
    Pair("Daily Memory Check-in", "Keep your memory bank updated. Tap here to record a quick voice snippet."),
    Pair("Evening Reflection", "Review your day's achievements and notes. Your future self will thank you.")
  )

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
  }

  fun isReminderEnabled(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_REMINDER_ENABLED, true)
  }

  fun setReminderEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
    if (enabled) {
      scheduleDailyReminder(context)
    } else {
      cancelDailyReminder(context)
    }
  }

  fun getReminderTime(context: Context): Pair<Int, Int> {
    val prefs = getPrefs(context)
    val hour = prefs.getInt(KEY_REMINDER_HOUR, 20) // 8:00 PM default
    val minute = prefs.getInt(KEY_REMINDER_MINUTE, 0)
    return Pair(hour, minute)
  }

  fun setReminderTime(context: Context, hour: Int, minute: Int) {
    getPrefs(context).edit()
      .putInt(KEY_REMINDER_HOUR, hour)
      .putInt(KEY_REMINDER_MINUTE, minute)
      .apply()
    if (isReminderEnabled(context)) {
      scheduleDailyReminder(context)
    }
  }

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = CHANNEL_DESC
        enableVibration(true)
        setShowBadge(true)
      }
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      manager?.createNotificationChannel(channel)
    }
  }

  fun scheduleDailyReminder(context: Context) {
    createNotificationChannel(context)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val (hour, minute) = getReminderTime(context)

    val calendar = Calendar.getInstance().apply {
      timeInMillis = System.currentTimeMillis()
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      if (before(Calendar.getInstance())) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }

    val intent = Intent(context, MemoryReminderReceiver::class.java).apply {
      action = "com.example.notification.ACTION_DAILY_REMINDER"
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      ALARM_REQUEST_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      alarmManager.setInexactRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
      )
    } catch (_: SecurityException) {
      alarmManager.set(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
      )
    }
  }

  fun cancelDailyReminder(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, MemoryReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      ALARM_REQUEST_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
  }

  fun showDailyReminderNotification(context: Context) {
    createNotificationChannel(context)

    val promptIndex = (System.currentTimeMillis() / (1000 * 60 * 60 * 24)).toInt() % ENGAGING_PROMPTS.size
    val (title, content) = ENGAGING_PROMPTS[kotlin.math.abs(promptIndex)]

    // PendingIntent that opens MainActivity and immediately prompts voice capture
    val tapIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra(EXTRA_OPEN_VOICE_CAPTURE, true)
    }

    val tapPendingIntent = PendingIntent.getActivity(
      context,
      0,
      tapIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_popup_reminder)
      .setContentTitle(title)
      .setContentText(content)
      .setStyle(NotificationCompat.BigTextStyle().bigText(content))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(tapPendingIntent)
      .setAutoCancel(true)

    try {
      NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    } catch (_: SecurityException) {
      // Permission might not be granted yet
    }
  }

  fun triggerTestNotification(context: Context) {
    createNotificationChannel(context)

    val tapIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra(EXTRA_OPEN_VOICE_CAPTURE, true)
    }

    val tapPendingIntent = PendingIntent.getActivity(
      context,
      0,
      tapIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_popup_reminder)
      .setContentTitle("Daily Memory Reminder ✨")
      .setContentText("What's on your mind today? Tap here to save a 15-second voice snippet.")
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText("What's on your mind today? Tap here to record a quick voice snippet or review your recent memory entries.")
      )
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setContentIntent(tapPendingIntent)
      .setAutoCancel(true)

    try {
      NotificationManagerCompat.from(context).notify(NOTIFICATION_ID + 1, builder.build())
    } catch (_: SecurityException) {
      // Permission not granted
    }
  }
}
