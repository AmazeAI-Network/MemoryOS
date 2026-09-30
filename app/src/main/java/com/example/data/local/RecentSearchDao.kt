package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local recent search queries persisted in Room.
 */
@Dao
interface RecentSearchDao {

  @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 20")
  fun getAllRecentSearches(): Flow<List<RecentSearchEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSearch(search: RecentSearchEntity)

  @Query("DELETE FROM recent_searches WHERE `query` = :query")
  suspend fun deleteSearch(query: String)

  @Query("DELETE FROM recent_searches")
  suspend fun clearAllRecentSearches()
}
