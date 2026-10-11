package com.example.analytics

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Privacy-Preserving On-Device Product Telemetry & Retention Tracker.
 * Tracks usage milestones, retention loops, daily capture streaks, and draft persistence
 * strictly on-device without leaking sensitive memory contents.
 */
object AppAnalytics {
  private const val TAG = "AppAnalytics"
  private const val PREFS_NAME = "memoryos_analytics_prefs"

  // Milestone Keys
  private const val KEY_FIRST_LAUNCH = "is_first_launch"
  private const val KEY_INSTALL_TIMESTAMP = "install_timestamp"
  private const val KEY_FIRST_MEMORY_TIMESTAMP = "first_memory_timestamp"
  private const val KEY_AHA_MOMENT_REACHED = "aha_moment_reached"
  private const val KEY_TOTAL_MEMORIES_CREATED = "total_memories_created"
  private const val KEY_TOTAL_QUERIES_ASKED = "total_queries_asked"
  private const val KEY_LAST_ACTIVE_DATE = "last_active_date"
  private const val KEY_CURRENT_STREAK = "current_streak"
  private const val KEY_NPS_SUBMITTED = "nps_submitted"
  private const val KEY_NPS_DISMISSED_TIMESTAMP = "nps_dismissed_timestamp"
  private const val KEY_NPS_LAST_SCORE = "nps_last_score"
  private const val SEVEN_DAYS_MS = 7L * 24L * 60L * 60L * 1000L

  // Draft Keys
  private const val KEY_DRAFT_TEXT = "draft_memory_text"
  private const val KEY_DRAFT_TYPE = "draft_memory_type"

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun getInstallTimestamp(context: Context): Long {
    val prefs = getPrefs(context)
    var timestamp = prefs.getLong(KEY_INSTALL_TIMESTAMP, 0L)
    if (timestamp == 0L) {
      timestamp = try {
        context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
      } catch (_: Exception) {
        System.currentTimeMillis()
      }
      prefs.edit().putLong(KEY_INSTALL_TIMESTAMP, timestamp).apply()
    }
    return timestamp
  }

  fun shouldTriggerNps(context: Context): Boolean {
    val prefs = getPrefs(context)
    val hasSubmitted = prefs.getBoolean(KEY_NPS_SUBMITTED, false)
    if (hasSubmitted) return false

    val installTime = getInstallTimestamp(context)
    val elapsed = System.currentTimeMillis() - installTime
    val isAfter7Days = elapsed >= SEVEN_DAYS_MS

    val lastDismissed = prefs.getLong(KEY_NPS_DISMISSED_TIMESTAMP, 0L)
    val dismissedCooldown = (System.currentTimeMillis() - lastDismissed) >= (3L * 24L * 60L * 60L * 1000L)

    return isAfter7Days && (lastDismissed == 0L || dismissedCooldown)
  }

  fun recordNpsSubmitted(context: Context, score: Int, feedback: String) {
    getPrefs(context).edit()
      .putBoolean(KEY_NPS_SUBMITTED, true)
      .putInt(KEY_NPS_LAST_SCORE, score)
      .putLong("nps_submitted_at", System.currentTimeMillis())
      .apply()
    trackEvent("nps_rating_submitted", mapOf("score" to score, "has_feedback" to feedback.isNotBlank()))
  }

  fun markNpsDismissed(context: Context) {
    getPrefs(context).edit()
      .putLong(KEY_NPS_DISMISSED_TIMESTAMP, System.currentTimeMillis())
      .apply()
    trackEvent("nps_rating_dismissed")
  }

  fun isFirstLaunch(context: Context): Boolean {
    val prefs = getPrefs(context)
    val isFirst = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    if (isFirst) {
      prefs.edit().putLong(KEY_INSTALL_TIMESTAMP, System.currentTimeMillis()).apply()
    }
    return isFirst
  }

  fun markFirstLaunchComplete(context: Context) {
    getPrefs(context).edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    trackEvent("app_onboarding_completed")
  }

  fun hasReachedAhaMoment(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_AHA_MOMENT_REACHED, false)
  }

  fun markAhaMomentReached(context: Context) {
    getPrefs(context).edit().putBoolean(KEY_AHA_MOMENT_REACHED, true).apply()
    trackEvent("aha_moment_reached")
  }

  fun recordMemoryCreated(context: Context) {
    val prefs = getPrefs(context)
    val total = prefs.getInt(KEY_TOTAL_MEMORIES_CREATED, 0) + 1
    val edit = prefs.edit().putInt(KEY_TOTAL_MEMORIES_CREATED, total)
    if (total == 1) {
      val timeToValue = System.currentTimeMillis() - prefs.getLong(KEY_INSTALL_TIMESTAMP, System.currentTimeMillis())
      edit.putLong(KEY_FIRST_MEMORY_TIMESTAMP, System.currentTimeMillis())
      trackEvent("first_memory_created", mapOf("time_to_value_ms" to timeToValue))
    }
    edit.apply()
    updateDailyActiveStreak(context)
    trackEvent("memory_created", mapOf("total_count" to total))
  }

  fun recordQueryAsked(context: Context) {
    val prefs = getPrefs(context)
    val total = prefs.getInt(KEY_TOTAL_QUERIES_ASKED, 0) + 1
    prefs.edit().putInt(KEY_TOTAL_QUERIES_ASKED, total).apply()
    trackEvent("grounded_query_asked", mapOf("total_queries" to total))
  }

  fun updateDailyActiveStreak(context: Context): Int {
    val prefs = getPrefs(context)
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val lastDate = prefs.getString(KEY_LAST_ACTIVE_DATE, null)
    var streak = prefs.getInt(KEY_CURRENT_STREAK, 0)

    if (lastDate == null) {
      streak = 1
      prefs.edit().putString(KEY_LAST_ACTIVE_DATE, today).putInt(KEY_CURRENT_STREAK, 1).apply()
    } else if (lastDate != today) {
      val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
      try {
        val lastDateTime = dateFormat.parse(lastDate)?.time ?: 0
        val diffDays = (Date().time - lastDateTime) / (1000 * 60 * 60 * 24)
        streak = if (diffDays <= 1) streak + 1 else 1
      } catch (_: Exception) {
        streak = 1
      }
      prefs.edit().putString(KEY_LAST_ACTIVE_DATE, today).putInt(KEY_CURRENT_STREAK, streak).apply()
    }
    return streak
  }

  fun getCurrentStreak(context: Context): Int {
    val streak = getPrefs(context).getInt(KEY_CURRENT_STREAK, 0)
    return if (streak > 0) streak else 1
  }

  // --- Draft Persistence ---

  fun saveDraft(context: Context, text: String, type: String) {
    if (text.isBlank()) {
      clearDraft(context)
    } else {
      getPrefs(context).edit()
        .putString(KEY_DRAFT_TEXT, text)
        .putString(KEY_DRAFT_TYPE, type)
        .apply()
    }
  }

  fun getDraft(context: Context): Pair<String, String>? {
    val prefs = getPrefs(context)
    val text = prefs.getString(KEY_DRAFT_TEXT, null)
    val type = prefs.getString(KEY_DRAFT_TYPE, "NOTE") ?: "NOTE"
    return if (!text.isNullOrBlank()) Pair(text, type) else null
  }

  fun clearDraft(context: Context) {
    getPrefs(context).edit()
      .remove(KEY_DRAFT_TEXT)
      .remove(KEY_DRAFT_TYPE)
      .apply()
  }

  fun trackEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
    // Structured diagnostic log for local debugging & telemetry compliance
    Log.d(TAG, "Event: $eventName | params: $params")
  }
}
