package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MemoryEntity
import com.example.data.model.MemoryFtsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room Full-Text Search (FTS4) and multi-dimensional memory filtering.
 */
@Dao
interface SearchDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFts(fts: MemoryFtsEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllFts(ftsList: List<MemoryFtsEntity>)

  @Query("DELETE FROM memories_fts WHERE memoryId = :memoryId")
  suspend fun deleteFts(memoryId: String)

  @Query("DELETE FROM memories_fts")
  suspend fun clearFts()

  /**
   * Fast full-text search matching query tokens using SQLite FTS4 MATCH operator.
   */
  @Query("""
    SELECT memories.* FROM memories
    JOIN memories_fts ON memories.id = memories_fts.memoryId
    WHERE memories_fts MATCH :ftsQuery AND memories.isArchived = 0
    ORDER BY memories.createdAt DESC
  """)
  fun searchMemoriesFts(ftsQuery: String): Flow<List<MemoryEntity>>

  /**
   * Direct LIKE fallback query for broad substring matches.
   */
  @Query("""
    SELECT * FROM memories
    WHERE isArchived = 0 AND (
      title LIKE '%' || :query || '%' OR
      summary LIKE '%' || :query || '%' OR
      rawText LIKE '%' || :query || '%' OR
      ocrText LIKE '%' || :query || '%' OR
      tags LIKE '%' || :query || '%' OR
      category LIKE '%' || :query || '%' OR
      location LIKE '%' || :query || '%' OR
      merchant LIKE '%' || :query || '%'
    )
    ORDER BY createdAt DESC
  """)
  fun searchMemoriesDirect(query: String): Flow<List<MemoryEntity>>

  /**
   * Multi-dimensional filtering by category ('Work', 'Personal', 'Ideas') and tag.
   */
  @Query("""
    SELECT * FROM memories
    WHERE isArchived = 0
      AND (:category IS NULL OR :category = '' OR :category = 'ALL' OR category = :category COLLATE NOCASE)
      AND (:tag IS NULL OR :tag = '' OR tags LIKE '%' || :tag || '%')
    ORDER BY createdAt DESC
  """)
  fun filterMemoriesByCategoryAndTag(category: String?, tag: String?): Flow<List<MemoryEntity>>

  /**
   * Multi-dimensional filtering by category ('Work', 'Personal', 'Ideas') and tag with date sorting.
   */
  @Query("""
    SELECT * FROM memories
    WHERE isArchived = 0
      AND (:category IS NULL OR :category = '' OR :category = 'ALL' OR category = :category COLLATE NOCASE)
      AND (:tag IS NULL OR :tag = '' OR tags LIKE '%' || :tag || '%')
    ORDER BY 
      CASE WHEN :ascending = 1 THEN createdAt END ASC,
      CASE WHEN :ascending = 0 THEN createdAt END DESC
  """)
  fun filterMemoriesByCategoryAndTagSorted(category: String?, tag: String?, ascending: Boolean): Flow<List<MemoryEntity>>

  /**
   * Retrieve distinct categories present in the database.
   */
  @Query("SELECT DISTINCT category FROM memories WHERE isArchived = 0 AND category != '' ORDER BY category ASC")
  fun getDistinctCategories(): Flow<List<String>>

  /**
   * Snapshot count of indexed FTS records.
   */
  @Query("SELECT COUNT(*) FROM memories_fts")
  suspend fun countFtsEntries(): Int
}
