package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CollectionEntity
import com.example.data.model.DailyUsageEntity
import com.example.data.model.MemoryEntity
import com.example.data.model.MemoryFtsEntity
import com.example.data.model.NpsFeedbackEntity
import com.example.data.model.RecentSearchEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.UserStreakEntity

@Database(
  entities = [
    MemoryEntity::class,
    CollectionEntity::class,
    UserProfileEntity::class,
    MemoryFtsEntity::class,
    RecentSearchEntity::class,
    DailyUsageEntity::class,
    UserStreakEntity::class,
    NpsFeedbackEntity::class
  ],
  version = 5,
  exportSchema = false
)
abstract class MemoryDatabase : RoomDatabase() {
  abstract fun memoryDao(): MemoryDao
  abstract fun collectionDao(): CollectionDao
  abstract fun userProfileDao(): UserProfileDao
  abstract fun searchDao(): SearchDao
  abstract fun recentSearchDao(): RecentSearchDao
  abstract fun userStreakDao(): UserStreakDao
  abstract fun npsDao(): NpsDao

  companion object {
    @Volatile
    private var INSTANCE: MemoryDatabase? = null

    fun getDatabase(context: Context): MemoryDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          MemoryDatabase::class.java,
          "memoryos_database"
        )
        .fallbackToDestructiveMigration(true)
        .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
