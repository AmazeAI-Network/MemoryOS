package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
  @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
  fun getUserProfile(userId: String = "local_user"): Flow<UserProfileEntity?>

  @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
  suspend fun getUserProfileSnapshot(userId: String = "local_user"): UserProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

  @Update
  suspend fun updateProfile(profile: UserProfileEntity)
}
