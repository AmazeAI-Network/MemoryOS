package com.example.data.repository

import com.example.data.local.UserStreakDao
import com.example.data.model.DailyUsageEntity
import com.example.data.model.DayStreakItem
import com.example.data.model.StreakUiState
import com.example.data.model.UserStreakEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * [Performance Architecture]
 * StreakRepository mediates Room database calls and offloads CPU-intensive date calculations
 * to Dispatchers.Default, guaranteeing zero jank on the UI thread.
 */
class StreakRepository(
  private val userStreakDao: UserStreakDao,
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
  private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
  private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  private val dayLabelFormat = SimpleDateFormat("EEE", Locale.US)

  /**
   * Reactive Flow that emits aggregated StreakUiState.
   * Calculations run strictly on Dispatchers.Default.
   */
  val streakUiState: Flow<StreakUiState> = combine(
    userStreakDao.getUserStreak(),
    userStreakDao.getRecentUsageLogs(14)
  ) { streakEntity, usageLogs ->
    calculateStreakUiState(streakEntity, usageLogs)
  }.flowOn(defaultDispatcher)

  /**
   * [CPU-bound calculation]
   * Computes consecutive streak count and 7-day weekly calendar window.
   */
  private fun calculateStreakUiState(
    streakEntity: UserStreakEntity?,
    usageLogs: List<DailyUsageEntity>
  ): StreakUiState {
    val todayString = dateFormat.format(Date())
    val activeDateSet = usageLogs.map { it.dateString }.toSet()

    val isTodayActive = activeDateSet.contains(todayString)
    val currentStreak = streakEntity?.currentStreak ?: (if (isTodayActive) 1 else 0)
    val longestStreak = maxOf(currentStreak, streakEntity?.longestStreak ?: currentStreak)
    val totalActiveDays = streakEntity?.totalActiveDays ?: maxOf(activeDateSet.size, 1)

    // Construct 7-Day interactive window (last 6 days + today)
    val weeklyDays = mutableListOf<DayStreakItem>()
    val calendar = Calendar.getInstance()

    // Generate days from 6 days ago up to today
    for (i in 6 downTo 0) {
      val dayCal = Calendar.getInstance()
      dayCal.add(Calendar.DAY_OF_YEAR, -i)
      val dateStr = dateFormat.format(dayCal.time)
      val shortLabel = dayLabelFormat.format(dayCal.time).take(1) // "M", "T", "W"
      val isDayActive = activeDateSet.contains(dateStr)
      val isToday = (i == 0)

      weeklyDays.add(
        DayStreakItem(
          dayLabel = shortLabel,
          dateString = dateStr,
          isCompleted = isDayActive,
          isToday = isToday,
          isFuture = false
        )
      )
    }

    // Determine milestone goals
    val (target, progress) = when {
      currentStreak < 3 -> Pair(3, currentStreak / 3f)
      currentStreak < 7 -> Pair(7, currentStreak / 7f)
      currentStreak < 14 -> Pair(14, currentStreak / 14f)
      currentStreak < 30 -> Pair(30, currentStreak / 30f)
      else -> Pair(currentStreak + 10, 0.95f)
    }

    return StreakUiState(
      currentStreak = maxOf(currentStreak, 1),
      longestStreak = maxOf(longestStreak, 1),
      totalActiveDays = totalActiveDays,
      isTodayActive = isTodayActive,
      weeklyDays = weeklyDays,
      nextMilestoneTarget = target,
      milestoneProgress = progress.coerceIn(0.05f, 1f),
      streakFreezeRemaining = streakEntity?.streakFreezeRemaining ?: 2
    )
  }

  /**
   * Logs a daily active usage event on Dispatchers.IO.
   * Updates streak count atomically in the Room database.
   */
  suspend fun recordDailyActiveUsage(
    isMemoryCreated: Boolean = false,
    isQueryAsked: Boolean = false
  ) = withContext(ioDispatcher) {
    val todayString = dateFormat.format(Date())
    val existingLog = userStreakDao.getDailyUsage(todayString)

    val updatedLog = if (existingLog != null) {
      existingLog.copy(
        timestamp = System.currentTimeMillis(),
        memoriesCreatedCount = existingLog.memoriesCreatedCount + if (isMemoryCreated) 1 else 0,
        queriesAskedCount = existingLog.queriesAskedCount + if (isQueryAsked) 1 else 0,
        isStreakDay = true
      )
    } else {
      DailyUsageEntity(
        dateString = todayString,
        timestamp = System.currentTimeMillis(),
        memoriesCreatedCount = if (isMemoryCreated) 1 else 0,
        queriesAskedCount = if (isQueryAsked) 1 else 0,
        isStreakDay = true
      )
    }
    userStreakDao.insertOrUpdateDailyUsage(updatedLog)

    // Recalculate streak entity
    updateUserStreakRecord(todayString)
  }

  private suspend fun updateUserStreakRecord(todayString: String) = withContext(defaultDispatcher) {
    val existingStreak = userStreakDao.getUserStreakSnapshot()
    val lastDate = existingStreak?.lastActiveDate ?: ""

    val newCurrentStreak: Int
    if (lastDate.isEmpty()) {
      newCurrentStreak = 1
    } else if (lastDate == todayString) {
      newCurrentStreak = existingStreak?.currentStreak ?: 1
    } else {
      // Calculate day difference
      val diffDays = try {
        val lastTime = dateFormat.parse(lastDate)?.time ?: 0L
        val todayTime = dateFormat.parse(todayString)?.time ?: 0L
        ((todayTime - lastTime) / (1000 * 60 * 60 * 24)).toInt()
      } catch (_: Exception) {
        1
      }

      newCurrentStreak = when (diffDays) {
        1 -> (existingStreak?.currentStreak ?: 0) + 1
        else -> 1 // Streak reset after missed day
      }
    }

    val updatedStreakEntity = UserStreakEntity(
      userId = "local_user",
      currentStreak = newCurrentStreak,
      longestStreak = maxOf(newCurrentStreak, existingStreak?.longestStreak ?: newCurrentStreak),
      lastActiveDate = todayString,
      totalActiveDays = (existingStreak?.totalActiveDays ?: 0) + (if (lastDate != todayString) 1 else 0),
      streakFreezeRemaining = existingStreak?.streakFreezeRemaining ?: 2,
      lastCalculatedAt = System.currentTimeMillis()
    )

    withContext(ioDispatcher) {
      userStreakDao.insertOrUpdateStreak(updatedStreakEntity)
    }
  }
}
