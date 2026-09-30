package com.example.data.repository

import com.example.data.local.CollectionDao
import com.example.data.local.MemoryDao
import com.example.data.local.SearchDao
import com.example.data.model.CollectionEntity
import com.example.data.model.ExtractedMemoryData
import com.example.data.model.GroundedAnswer
import com.example.data.model.MemoryEntity
import com.example.data.model.MemoryFtsEntity
import com.example.data.model.SemanticSearchResult
import com.example.data.remote.OpenRouterAIService
import com.example.data.local.UserProfileDao
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MemoryRepository(
  private val memoryDao: MemoryDao,
  private val collectionDao: CollectionDao,
  private val searchDao: SearchDao? = null,
  private val userProfileDao: UserProfileDao? = null,
  private val openRouterService: OpenRouterAIService = OpenRouterAIService(),
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

  init {
    // Purge any lingering dummy records to ensure a pristine user experience
    scope.launch {
      memoryDao.deleteDummyMemories()
      collectionDao.deleteDummyCollections()
    }
  }

  val userProfile: Flow<UserProfileEntity?> =
    userProfileDao?.getUserProfile("local_user") ?: flowOf(null)

  suspend fun saveUserProfile(profile: UserProfileEntity) {
    userProfileDao?.insertOrUpdateProfile(profile)
  }

  val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()
  val favoriteMemories: Flow<List<MemoryEntity>> = memoryDao.getFavoriteMemories()
  val allCollections: Flow<List<CollectionEntity>> = collectionDao.getAllCollections()

  fun getAllMemoriesSorted(ascending: Boolean): Flow<List<MemoryEntity>> =
    memoryDao.getAllMemoriesSorted(ascending)

  fun getAllMemoriesAsc(): Flow<List<MemoryEntity>> = memoryDao.getAllMemoriesAsc()
  fun getAllMemoriesDesc(): Flow<List<MemoryEntity>> = memoryDao.getAllMemoriesDesc()

  fun getMemoriesByType(type: String): Flow<List<MemoryEntity>> = memoryDao.getMemoriesByType(type)

  fun getMemoriesByCollection(collectionId: String): Flow<List<MemoryEntity>> =
    memoryDao.getMemoriesByCollection(collectionId)

  fun searchMemories(query: String): Flow<List<MemoryEntity>> = memoryDao.searchMemories(query)

  suspend fun getMemoryById(id: String): MemoryEntity? = withContext(Dispatchers.IO) {
    memoryDao.getMemoryById(id)
  }

  suspend fun insertMemory(memory: MemoryEntity) = withContext(Dispatchers.IO) {
    memoryDao.insertMemory(memory)
    searchDao?.insertFts(
      MemoryFtsEntity(
        memoryId = memory.id,
        title = memory.title,
        summary = memory.summary,
        rawText = memory.rawText,
        ocrText = memory.ocrText,
        tags = memory.tags,
        category = memory.category
      )
    )
  }

  suspend fun createMemoryWithAI(
    rawText: String,
    memoryType: String,
    sourceType: String = "NOTE",
    category: String = "Personal",
    userTags: String = ""
  ): MemoryEntity = withContext(Dispatchers.IO) {
    val extracted = openRouterService.extractMemoryInfo(rawText, memoryType)

    val finalCategory = if (category.isNotBlank() && category != "Personal") category else extracted.category
    val mergedTags = (extracted.tags + userTags.split(",").map { it.trim().removePrefix("#") })
      .filter { it.isNotBlank() }
      .distinct()
      .joinToString(", ")

    val newMemory = MemoryEntity(
      id = "mem_" + UUID.randomUUID().toString().take(8),
      title = extracted.title,
      memoryType = memoryType,
      sourceType = sourceType,
      summary = extracted.summary,
      rawText = rawText,
      tags = mergedTags,
      merchant = extracted.merchant,
      amount = extracted.amount,
      currency = extracted.currency,
      eventDate = extracted.eventDate,
      location = extracted.location,
      category = finalCategory,
      createdAt = System.currentTimeMillis()
    )

    insertMemory(newMemory)
    newMemory
  }

  suspend fun updateMemory(memory: MemoryEntity) = withContext(Dispatchers.IO) {
    memoryDao.updateMemory(memory)
    searchDao?.insertFts(
      MemoryFtsEntity(
        memoryId = memory.id,
        title = memory.title,
        summary = memory.summary,
        rawText = memory.rawText,
        ocrText = memory.ocrText,
        tags = memory.tags,
        category = memory.category
      )
    )
  }

  suspend fun deleteMemory(id: String) = withContext(Dispatchers.IO) {
    memoryDao.deleteMemoryById(id)
    searchDao?.deleteFts(id)
  }

  suspend fun toggleFavorite(id: String, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
    memoryDao.setFavorite(id, !currentFavorite)
  }

  suspend fun askMemory(query: String): GroundedAnswer = withContext(Dispatchers.IO) {
    val snapshot = memoryDao.getAllMemoriesSnapshot()
    openRouterService.answerQuestion(query, snapshot)
  }

  suspend fun semanticSearch(query: String): SemanticSearchResult = withContext(Dispatchers.IO) {
    val snapshot = memoryDao.getAllMemoriesSnapshot()
    openRouterService.semanticSearch(query, snapshot)
  }

  suspend fun clearAllData() = withContext(Dispatchers.IO) {
    memoryDao.deleteAllMemories()
    collectionDao.deleteAllCollections()
  }

  suspend fun checkCloudSync(): Boolean = withContext(Dispatchers.IO) {
    true
  }
}

