package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyUsageEntity
import com.example.data.model.UserStreakEntity
import kotlinx.coroutines.flow.Flow

/**
 * [Performance Architecture]
 * UserStreakDao executes all operations on Dispatchers.IO via Room's internal executor.
 */
@Dao
interface UserStreakDao {

  @Query("SELECT * FROM user_streaks WHERE userId = :userId LIMIT 1")
  fun getUserStreak(userId: String = "local_user"): Flow<UserStreakEntity?>

  @Query("SELECT * FROM user_streaks WHERE userId = :userId LIMIT 1")
  suspend fun getUserStreakSnapshot(userId: String = "local_user"): UserStreakEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateStreak(streak: UserStreakEntity)

  @Query("SELECT * FROM daily_usage_logs ORDER BY dateString DESC LIMIT :limit")
  fun getRecentUsageLogs(limit: Int = 30): Flow<List<DailyUsageEntity>>

  @Query("SELECT * FROM daily_usage_logs ORDER BY dateString DESC LIMIT :limit")
  suspend fun getRecentUsageLogsSnapshot(limit: Int = 30): List<DailyUsageEntity>

  @Query("SELECT * FROM daily_usage_logs WHERE dateString = :dateString LIMIT 1")
  suspend fun getDailyUsage(dateString: String): DailyUsageEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateDailyUsage(usage: DailyUsageEntity)

  @Query("SELECT COUNT(*) FROM daily_usage_logs WHERE isStreakDay = 1")
  fun getTotalActiveDaysCount(): Flow<Int>
}
