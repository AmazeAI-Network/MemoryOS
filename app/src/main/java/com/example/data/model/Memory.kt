package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
  tableName = "memories",
  indices = [
    Index(value = ["category"]),
    Index(value = ["createdAt"]),
    Index(value = ["isArchived", "createdAt"])
  ]
)
data class MemoryEntity(
  @PrimaryKey
  val id: String,
  val title: String,
  val memoryType: String, // TRAVEL, RECEIPT, TICKET, RESERVATION, NOTE, DOCUMENT, PHOTO
  val sourceType: String, // SCREENSHOT, CAMERA, IMPORT, NOTE
  val summary: String,
  val ocrText: String = "",
  val rawText: String = "",
  val tags: String = "", // Comma-delimited keywords e.g. "Meeting, Launch, UI"
  val people: String? = null,
  val places: String? = null,
  val merchant: String? = null,
  val amount: Double? = null,
  val currency: String? = null,
  val eventDate: String? = null,
  val location: String? = null,
  val category: String = "Personal", // Primary categories: 'Work', 'Personal', 'Ideas', 'Travel', 'Finance'
  val confidence: String = "HIGH",
  val isFavorite: Boolean = false,
  val isArchived: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val thumbnailUri: String? = null,
  val collectionId: String? = null
)

/**
 * SQLite FTS4 virtual table for lightning-fast full-text search across memories.
 */
@Immutable
@Entity(tableName = "memories_fts")
@Fts4
data class MemoryFtsEntity(
  val memoryId: String,
  val title: String,
  val summary: String,
  val rawText: String,
  val ocrText: String = "",
  val tags: String = "",
  val category: String = "Personal"
)

@Immutable
@Entity(tableName = "collections")
data class CollectionEntity(
  @PrimaryKey
  val id: String,
  val title: String,
  val description: String,
  val coverImage: String = "",
  val memoryCount: Int = 0,
  val createdAt: Long = System.currentTimeMillis()
)

@Immutable
data class SourceCitation(
  val memoryId: String,
  val title: String,
  val reason: String,
  val date: String? = null,
  val category: String = "Memory"
)

@Immutable
data class GroundedAnswer(
  val query: String,
  val answer: String,
  val confidence: String, // HIGH, MEDIUM, LOW
  val sources: List<SourceCitation> = emptyList(),
  val uncertaintyNote: String? = null
)

@Immutable
data class ExtractedMemoryData(
  val title: String,
  val summary: String,
  val memoryType: String,
  val category: String,
  val merchant: String? = null,
  val amount: Double? = null,
  val currency: String? = null,
  val eventDate: String? = null,
  val location: String? = null,
  val tags: List<String> = emptyList()
)

@Immutable
data class SemanticMatch(
  val memory: MemoryEntity,
  val relevanceScore: Int, // 0 - 100%
  val reason: String,
  val highlightSnippet: String
)

@Immutable
data class SemanticSearchResult(
  val query: String,
  val semanticSummary: String,
  val matches: List<SemanticMatch>,
  val executionTimeMs: Long = 0
)

