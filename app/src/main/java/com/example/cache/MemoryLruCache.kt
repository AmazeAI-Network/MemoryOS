package com.example.cache

import android.util.LruCache
import com.example.data.model.ExtractedMemoryData
import com.example.data.model.GroundedAnswer
import com.example.data.model.SemanticSearchResult
import java.util.concurrent.atomic.AtomicInteger

/**
 * Enterprise Multi-Tier In-Memory LRU Cache.
 * Dramatically accelerates repeated user queries, semantic searches, and entity extractions
 * while maintaining strict heap budget limits.
 */
object MemoryLruCache {

  private const val MAX_CACHE_ENTRIES = 50

  private val answerCache = object : LruCache<String, GroundedAnswer>(MAX_CACHE_ENTRIES) {
    override fun sizeOf(key: String, value: GroundedAnswer): Int = 1
  }

  private val semanticCache = object : LruCache<String, SemanticSearchResult>(MAX_CACHE_ENTRIES) {
    override fun sizeOf(key: String, value: SemanticSearchResult): Int = 1
  }

  private val extractionCache = object : LruCache<String, ExtractedMemoryData>(MAX_CACHE_ENTRIES) {
    override fun sizeOf(key: String, value: ExtractedMemoryData): Int = 1
  }

  private val hitCount = AtomicInteger(0)
  private val missCount = AtomicInteger(0)

  fun getAnswer(query: String): GroundedAnswer? {
    val normalizedKey = query.trim().lowercase()
    val cached = answerCache.get(normalizedKey)
    if (cached != null) {
      hitCount.incrementAndGet()
    } else {
      missCount.incrementAndGet()
    }
    return cached
  }

  fun putAnswer(query: String, answer: GroundedAnswer) {
    val normalizedKey = query.trim().lowercase()
    answerCache.put(normalizedKey, answer)
  }

  fun getSemanticSearch(query: String): SemanticSearchResult? {
    val normalizedKey = query.trim().lowercase()
    val cached = semanticCache.get(normalizedKey)
    if (cached != null) {
      hitCount.incrementAndGet()
    } else {
      missCount.incrementAndGet()
    }
    return cached
  }

  fun putSemanticSearch(query: String, result: SemanticSearchResult) {
    val normalizedKey = query.trim().lowercase()
    semanticCache.put(normalizedKey, result)
  }

  fun getExtractedData(rawText: String): ExtractedMemoryData? {
    val hashKey = rawText.trim().hashCode().toString()
    val cached = extractionCache.get(hashKey)
    if (cached != null) {
      hitCount.incrementAndGet()
    } else {
      missCount.incrementAndGet()
    }
    return cached
  }

  fun putExtractedData(rawText: String, data: ExtractedMemoryData) {
    val hashKey = rawText.trim().hashCode().toString()
    extractionCache.put(hashKey, data)
  }

  /**
   * Invalidates cached Q&A and search results when user modifies or deletes a memory.
   */
  fun invalidateQueryCaches() {
    answerCache.evictAll()
    semanticCache.evictAll()
  }

  fun clearAll() {
    answerCache.evictAll()
    semanticCache.evictAll()
    extractionCache.evictAll()
    hitCount.set(0)
    missCount.set(0)
  }

  fun getStats(): CacheStats {
    val totalEntries = answerCache.size() + semanticCache.size() + extractionCache.size()
    val hits = hitCount.get()
    val misses = missCount.get()
    val total = hits + misses
    val hitRate = if (total > 0) (hits.toFloat() / total * 100).toInt() else 0

    return CacheStats(
      totalEntries = totalEntries,
      hitCount = hits,
      missCount = misses,
      hitRatePercent = hitRate,
      estimatedMemoryKb = (totalEntries * 4L)
    )
  }
}

data class CacheStats(
  val totalEntries: Int,
  val hitCount: Int,
  val missCount: Int,
  val hitRatePercent: Int,
  val estimatedMemoryKb: Long
)
