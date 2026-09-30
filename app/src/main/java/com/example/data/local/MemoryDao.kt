package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
  @Query("SELECT * FROM memories WHERE isArchived = 0 ORDER BY createdAt DESC")
  fun getAllMemories(): Flow<List<MemoryEntity>>

  @Query("SELECT * FROM memories WHERE isArchived = 0 ORDER BY createdAt DESC")
  fun getAllMemoriesDesc(): Flow<List<MemoryEntity>>

  @Query("SELECT * FROM memories WHERE isArchived = 0 ORDER BY createdAt ASC")
  fun getAllMemoriesAsc(): Flow<List<MemoryEntity>>

  @Query("""
    SELECT * FROM memories 
    WHERE isArchived = 0 
    ORDER BY 
      CASE WHEN :ascending = 1 THEN createdAt END ASC,
      CASE WHEN :ascending = 0 THEN createdAt END DESC
  """)
  fun getAllMemoriesSorted(ascending: Boolean): Flow<List<MemoryEntity>>

  @Query("SELECT * FROM memories WHERE id = :id LIMIT 1")
  suspend fun getMemoryById(id: String): MemoryEntity?

  @Query("SELECT * FROM memories WHERE isFavorite = 1 AND isArchived = 0 ORDER BY createdAt DESC")
  fun getFavoriteMemories(): Flow<List<MemoryEntity>>

  @Query("SELECT * FROM memories WHERE memoryType = :type AND isArchived = 0 ORDER BY createdAt DESC")
  fun getMemoriesByType(type: String): Flow<List<MemoryEntity>>

  @Query("SELECT * FROM memories WHERE collectionId = :collectionId AND isArchived = 0 ORDER BY createdAt DESC")
  fun getMemoriesByCollection(collectionId: String): Flow<List<MemoryEntity>>

  @Query("""
    SELECT * FROM memories 
    WHERE isArchived = 0 AND (
      title LIKE '%' || :query || '%' OR 
      summary LIKE '%' || :query || '%' OR 
      ocrText LIKE '%' || :query || '%' OR 
      rawText LIKE '%' || :query || '%' OR 
      merchant LIKE '%' || :query || '%' OR 
      places LIKE '%' || :query || '%' OR 
      location LIKE '%' || :query || '%' OR
      tags LIKE '%' || :query || '%'
    )
    ORDER BY createdAt DESC
  """)
  fun searchMemories(query: String): Flow<List<MemoryEntity>>

  @Query("SELECT * FROM memories WHERE isArchived = 0")
  suspend fun getAllMemoriesSnapshot(): List<MemoryEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMemory(memory: MemoryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMemories(memories: List<MemoryEntity>)

  @Update
  suspend fun updateMemory(memory: MemoryEntity)

  @Query("DELETE FROM memories WHERE id = :id")
  suspend fun deleteMemoryById(id: String)

  @Query("UPDATE memories SET isFavorite = :isFavorite WHERE id = :id")
  suspend fun setFavorite(id: String, isFavorite: Boolean)

  @Query("SELECT COUNT(*) FROM memories WHERE isArchived = 0")
  suspend fun countMemories(): Int

  @Query("""
    DELETE FROM memories 
    WHERE id LIKE 'mem_tokyo%' 
       OR id LIKE 'mem_macbook%' 
       OR id LIKE 'mem_klaten%' 
       OR id LIKE 'mem_bandung%' 
       OR id LIKE 'mem_hanging%'
       OR title LIKE '%Coffee with Sarah%'
       OR title LIKE '%Flight SQ-958%'
       OR title LIKE '%Grand Hyatt Tokyo%'
       OR rawText LIKE '%Blue Bottle%'
       OR rawText LIKE '%Singapore to Tokyo Narita%'
       OR rawText LIKE '%Grand Hyatt Tokyo%'
       OR rawText LIKE '%Sample%'
  """)
  suspend fun deleteDummyMemories()

  @Query("DELETE FROM memories")
  suspend fun deleteAllMemories()
}
