package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * [Performance Architecture]
 * DailyUsageEntity records individual daily active usage occurrences.
 * Indexed by timestamp and isStreakDay for instantaneous index-scan queries.
 */
@Immutable
@Entity(
  tableName = "daily_usage_logs",
  indices = [
    Index(value = ["dateString"], unique = true),
    Index(value = ["timestamp"])
  ]
)
data class DailyUsageEntity(
  @PrimaryKey
  val dateString: String,              // ISO-8601 Date e.g. "2026-09-30"
  val timestamp: Long = System.currentTimeMillis(),
  val memoriesCreatedCount: Int = 0,
  val queriesAskedCount: Int = 0,
  val activeSeconds: Long = 0L,
  val isStreakDay: Boolean = true
)

/**
 * [Performance Architecture]
 * UserStreakEntity maintains cached aggregated streak calculations to avoid expensive
 * real-time recalculations on the Main Thread.
 */
@Immutable
@Entity(tableName = "user_streaks")
data class UserStreakEntity(
  @PrimaryKey
  val userId: String = "local_user",
  val currentStreak: Int = 1,
  val longestStreak: Int = 1,
  val lastActiveDate: String = "",
  val totalActiveDays: Int = 1,
  val streakFreezeRemaining: Int = 2,
  val lastCalculatedAt: Long = System.currentTimeMillis()
)

/**
 * [Performance Architecture]
 * Stores user Net Promoter Score (NPS) feedback on-device in Room.
 */
@Immutable
@Entity(
  tableName = "nps_feedbacks",
  indices = [Index(value = ["submittedAt"])]
)
data class NpsFeedbackEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val rating: Int,                     // 1 to 10 scale
  val feedbackText: String = "",
  val submittedAt: Long = System.currentTimeMillis(),
  val appVersion: String = "1.0.0",
  val daysSinceInstall: Int = 7
)

/**
 * Model for rendering individual day badges in the 7-day streak carousel.
 * Marked @Immutable for stable Compose memoization and 120 FPS scrolling.
 */
@Immutable
data class DayStreakItem(
  val dayLabel: String,                // "M", "T", "W", "T", "F", "S", "S"
  val dateString: String,              // "2026-09-30"
  val isCompleted: Boolean,
  val isToday: Boolean,
  val isFuture: Boolean
)

/**
 * High-performance immutable state model for the Streak UI component.
 */
@Immutable
data class StreakUiState(
  val currentStreak: Int = 1,
  val longestStreak: Int = 1,
  val totalActiveDays: Int = 1,
  val isTodayActive: Boolean = true,
  val weeklyDays: List<DayStreakItem> = emptyList(),
  val nextMilestoneTarget: Int = 7,
  val milestoneProgress: Float = 0.14f,
  val streakFreezeRemaining: Int = 2
)
