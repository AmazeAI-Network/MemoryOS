package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a locally persisted recent search query.
 */
@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
  @PrimaryKey val query: String,
  val timestamp: Long = System.currentTimeMillis()
)
