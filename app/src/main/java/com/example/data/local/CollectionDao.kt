package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
  @Query("SELECT * FROM collections ORDER BY createdAt DESC")
  fun getAllCollections(): Flow<List<CollectionEntity>>

  @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
  suspend fun getCollectionById(id: String): CollectionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCollection(collection: CollectionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCollections(collections: List<CollectionEntity>)

  @Query("SELECT COUNT(*) FROM collections")
  suspend fun countCollections(): Int

  @Query("DELETE FROM collections WHERE id LIKE 'col_%'")
  suspend fun deleteDummyCollections()

  @Query("DELETE FROM collections")
  suspend fun deleteAllCollections()
}
