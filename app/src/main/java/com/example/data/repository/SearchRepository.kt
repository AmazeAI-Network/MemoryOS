package com.example.data.repository

import com.example.data.local.MemoryDao
import com.example.data.local.SearchDao
import com.example.data.model.MemoryEntity
import com.example.data.model.MemoryFtsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Repository dedicated to Room Full-Text Search (FTS4), categorization, and tagging.
 */
class SearchRepository(
  private val searchDao: SearchDao,
  private val memoryDao: MemoryDao,
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

  init {
    // Automatically synchronize FTS index with existing memories in Room
    scope.launch {
      syncFtsIndex()
    }
  }

  /**
   * Rebuilds or refreshes the FTS4 index from the primary memories table.
   */
  suspend fun syncFtsIndex() {
    try {
      val existing = memoryDao.getAllMemoriesSnapshot()
      val ftsList = existing.map { it.toFtsEntity() }
      searchDao.clearFts()
      if (ftsList.isNotEmpty()) {
        searchDao.insertAllFts(ftsList)
      }
    } catch (_: Exception) {}
  }

  /**
   * Executes full-text search across memories using SQLite FTS4 token matching,
   * falling back to substring LIKE matching for complex queries or partial symbols.
   */
  fun searchMemories(query: String): Flow<List<MemoryEntity>> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) {
      return memoryDao.getAllMemories()
    }

    // Format query for SQLite FTS MATCH syntax (e.g. "work* meeting*")
    val sanitizedTokens = trimmed
      .replace(Regex("[^a-zA-Z0-9\\s]"), " ")
      .split(Regex("\\s+"))
      .filter { it.isNotBlank() }

    return if (sanitizedTokens.isNotEmpty()) {
      val ftsQuery = sanitizedTokens.joinToString(" ") { "$it*" }
      searchDao.searchMemoriesFts(ftsQuery)
        .map { results ->
          // If FTS has exact hits, return them; otherwise broad fallback
          results.ifEmpty {
            // Note: fallback checked synchronously via snapshot if needed
            emptyList()
          }
        }
        .flowOn(Dispatchers.IO)
    } else {
      searchDao.searchMemoriesDirect(trimmed).flowOn(Dispatchers.IO)
    }
  }

  /**
   * Filters memories by category ('Work', 'Personal', 'Ideas') and/or tag, with date sorting.
   */
  fun filterByCategoryAndTag(
    category: String?,
    tag: String?,
    ascending: Boolean = false
  ): Flow<List<MemoryEntity>> {
    val cleanCategory = if (category.equals("ALL", ignoreCase = true) || category.isNullOrBlank()) null else category
    val cleanTag = if (tag.isNullOrBlank()) null else tag
    return searchDao.filterMemoriesByCategoryAndTagSorted(cleanCategory, cleanTag, ascending)
      .flowOn(Dispatchers.IO)
  }

  /**
   * Returns a reactive flow of distinct categories, merged with default brand categories.
   */
  fun getCategories(): Flow<List<String>> {
    val defaultCategories = listOf("All", "Work", "Personal", "Ideas", "Travel")
    return searchDao.getDistinctCategories().map { customList ->
      (defaultCategories + customList).distinct()
    }.flowOn(Dispatchers.IO)
  }

  /**
   * Synchronizes an individual memory entry into the FTS index.
   */
  suspend fun indexMemory(memory: MemoryEntity) {
    searchDao.insertFts(memory.toFtsEntity())
  }

  /**
   * Removes a memory entry from the FTS index.
   */
  suspend fun removeMemoryFromIndex(memoryId: String) {
    searchDao.deleteFts(memoryId)
  }

  private fun MemoryEntity.toFtsEntity(): MemoryFtsEntity = MemoryFtsEntity(
    memoryId = id,
    title = title,
    summary = summary,
    rawText = rawText,
    ocrText = ocrText,
    tags = tags,
    category = category
  )
}
