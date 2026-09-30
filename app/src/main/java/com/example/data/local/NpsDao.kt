package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.NpsFeedbackEntity
import kotlinx.coroutines.flow.Flow

/**
 * [Performance Architecture]
 * NpsDao provides asynchronous, non-blocking storage of user NPS ratings.
 */
@Dao
interface NpsDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFeedback(feedback: NpsFeedbackEntity): Long

  @Query("SELECT * FROM nps_feedbacks ORDER BY submittedAt DESC LIMIT 1")
  fun getLatestFeedback(): Flow<NpsFeedbackEntity?>

  @Query("SELECT COUNT(*) FROM nps_feedbacks")
  suspend fun getFeedbackCount(): Int

  @Query("SELECT * FROM nps_feedbacks ORDER BY submittedAt DESC")
  fun getAllFeedbacks(): Flow<List<NpsFeedbackEntity>>
}
