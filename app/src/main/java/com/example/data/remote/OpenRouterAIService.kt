package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ExtractedMemoryData
import com.example.data.model.GroundedAnswer
import com.example.data.model.MemoryEntity
import com.example.data.model.SemanticMatch
import com.example.data.model.SemanticSearchResult
import com.example.data.model.SourceCitation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenRouterAIService {

  companion object {
    private const val TAG = "OpenRouterAIService"
    private const val OPENROUTER_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions"
    private const val DEFAULT_MODEL = "google/gemini-2.0-flash-001"
    private const val FALLBACK_MODEL = "meta-llama/llama-3.3-70b-instruct"
    const val DEFAULT_API_KEY = "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"
  }

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  // OpenRouter API Key resolved from BuildConfig or fallback preset
  private val apiKey: String
    get() {
      val keyFromConfig = try {
        BuildConfig::class.java.getField("OPENROUTER_API_KEY").get(null) as? String
      } catch (_: Exception) {
        ""
      }
      return if (!keyFromConfig.isNullOrBlank() && keyFromConfig != "null") {
        keyFromConfig
      } else {
        DEFAULT_API_KEY
      }
    }

  /**
   * Grounded Question Answering over Personal Memories using OpenRouter
   */
  suspend fun answerQuestion(
    query: String,
    memories: List<MemoryEntity>
  ): GroundedAnswer = withContext(Dispatchers.IO) {
    if (memories.isEmpty()) {
      return@withContext GroundedAnswer(
        query = query,
        answer = "I couldn't find any saved memories in your library yet. Save your first voice snippet, receipt, or note to query your personal memory bank!",
        confidence = "LOW",
        sources = emptyList(),
        uncertaintyNote = "No memories stored in database"
      )
    }

    // Build grounded memory context
    val contextBuilder = StringBuilder()
    memories.take(30).forEach { mem ->
      contextBuilder.append("--- MEMORY ID: ${mem.id} ---\n")
      contextBuilder.append("Title: ${mem.title}\n")
      contextBuilder.append("Type: ${mem.memoryType} | Category: ${mem.category}\n")
      if (!mem.merchant.isNullOrBlank()) contextBuilder.append("Merchant: ${mem.merchant}\n")
      if (mem.amount != null) contextBuilder.append("Amount: ${mem.currency ?: "$"} ${mem.amount}\n")
      if (!mem.eventDate.isNullOrBlank()) contextBuilder.append("Date: ${mem.eventDate}\n")
      if (!mem.location.isNullOrBlank()) contextBuilder.append("Location: ${mem.location}\n")
      if (!mem.people.isNullOrBlank()) contextBuilder.append("People: ${mem.people}\n")
      if (mem.tags.isNotBlank()) contextBuilder.append("Tags: ${mem.tags}\n")
      contextBuilder.append("Summary: ${mem.summary}\n")
      if (mem.rawText.isNotBlank()) contextBuilder.append("Notes: ${mem.rawText}\n")
      contextBuilder.append("\n")
    }

    val systemPrompt = """
      You are MemoryOS Intelligence, a personal memory assistant.
      Your mandate: Answer user queries directly and concisely strictly based on the provided personal memories.
      
      RULES:
      1. Ground your answer ONLY in the memory details provided. Never hallucinate facts.
      2. If the memory provides the exact answer, state it clearly in 1-2 friendly, polished sentences.
      3. For any supporting memory used, list its memoryId and a short reason why it matches.
      4. If the memories do NOT contain enough information, respond with answer: "I couldn't find enough details in your saved memories to answer that question confidently." and confidence: "LOW".
      
      Respond ONLY in valid JSON matching this exact structure:
      {
        "answer": "Concise factual answer here",
        "confidence": "HIGH" | "MEDIUM" | "LOW",
        "sources": [
          {
            "memoryId": "mem_id_here",
            "reason": "Matched hotel name and booking date"
          }
        ]
      }
    """.trimIndent()

    val userContent = "User Query: $query\n\nAvailable Memories:\n$contextBuilder"

    try {
      val requestJson = JSONObject().apply {
        put("model", DEFAULT_MODEL)
        put("temperature", 0.1)
        put("max_tokens", 800)
        put("messages", JSONArray().apply {
          put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
          })
          put(JSONObject().apply {
            put("role", "user")
            put("content", userContent)
          })
        })
      }

      val request = Request.Builder()
        .url(OPENROUTER_ENDPOINT)
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("HTTP-Referer", "https://memoryos.app")
        .addHeader("X-Title", "MemoryOS")
        .addHeader("Content-Type", "application/json")
        .post(requestJson.toString().toRequestBody(jsonMediaType))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string()

      if (response.isSuccessful && !responseBody.isNullOrBlank()) {
        val root = JSONObject(responseBody)
        val choices = root.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
          val message = choices.getJSONObject(0).optJSONObject("message")
          val rawContent = message?.optString("content") ?: ""

          val parsedAnswer = parseAnswerJson(rawContent, query, memories)
          if (parsedAnswer != null) {
            return@withContext parsedAnswer
          }
        }
      } else {
        Log.w(TAG, "OpenRouter response code: ${response.code} body: $responseBody")
      }
    } catch (e: Exception) {
      Log.e(TAG, "OpenRouter call failed: ${e.message}", e)
    }

    // High quality offline fallback
    fallbackAnswerQuestion(query, memories)
  }

  /**
   * Semantic Search across Memories via OpenRouter
   */
  suspend fun semanticSearch(
    query: String,
    memories: List<MemoryEntity>
  ): SemanticSearchResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    if (memories.isEmpty()) {
      return@withContext SemanticSearchResult(
        query = query,
        semanticSummary = "No memories stored in library.",
        matches = emptyList(),
        executionTimeMs = 0
      )
    }

    val contextBuilder = StringBuilder()
    memories.take(40).forEach { mem ->
      contextBuilder.append("ID: ${mem.id} | Title: ${mem.title} | Type: ${mem.memoryType} | Merchant: ${mem.merchant ?: ""} | Summary: ${mem.summary} | Tags: ${mem.tags}\n")
    }

    val systemPrompt = """
      You are the MemoryOS Semantic Search Engine.
      Rank and score user memories against natural language queries using conceptual meaning, synonyms, and context.
      
      Respond ONLY in valid JSON matching this schema:
      {
        "semanticSummary": "Found 2 memories matching your query about ...",
        "matches": [
          {
            "id": "mem_id",
            "score": 95,
            "reason": "Direct match for ...",
            "snippet": "Short excerpt highlighting match"
          }
        ]
      }
    """.trimIndent()

    val userContent = "Search Query: $query\n\nMemories Library:\n$contextBuilder"

    try {
      val requestJson = JSONObject().apply {
        put("model", DEFAULT_MODEL)
        put("temperature", 0.0)
        put("max_tokens", 800)
        put("messages", JSONArray().apply {
          put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
          })
          put(JSONObject().apply {
            put("role", "user")
            put("content", userContent)
          })
        })
      }

      val request = Request.Builder()
        .url(OPENROUTER_ENDPOINT)
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("HTTP-Referer", "https://memoryos.app")
        .addHeader("X-Title", "MemoryOS")
        .addHeader("Content-Type", "application/json")
        .post(requestJson.toString().toRequestBody(jsonMediaType))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string()

      if (response.isSuccessful && !responseBody.isNullOrBlank()) {
        val root = JSONObject(responseBody)
        val choices = root.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
          val message = choices.getJSONObject(0).optJSONObject("message")
          val rawContent = message?.optString("content") ?: ""

          val parsed = parseSemanticJson(rawContent, query, memories, System.currentTimeMillis() - startTime)
          if (parsed != null && parsed.matches.isNotEmpty()) {
            return@withContext parsed
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "OpenRouter semantic search error: ${e.message}")
    }

    // Heuristic semantic fallback
    fallbackSemanticSearch(query, memories, System.currentTimeMillis() - startTime)
  }

  /**
   * Entity and Metadata Extraction from Voice or Text Snippet
   */
  suspend fun extractMemoryInfo(
    rawText: String,
    memoryType: String
  ): ExtractedMemoryData = withContext(Dispatchers.IO) {
    if (rawText.isBlank()) {
      return@withContext ExtractedMemoryData(
        title = "Quick Note",
        summary = "Empty memory",
        memoryType = memoryType,
        category = "Personal"
      )
    }

    val systemPrompt = """
      You are the MemoryOS Entity Extractor.
      Analyze the user's input text (which may be a spoken memory, receipt note, travel plan, or meeting thought).
      Extract key fields and output ONLY a valid JSON object matching:
      {
        "title": "Short descriptive title (max 5 words)",
        "summary": "Polished one-sentence summary of the memory",
        "memoryType": "TRAVEL" | "RECEIPT" | "TICKET" | "RESERVATION" | "NOTE" | "DOCUMENT" | "PHOTO",
        "category": "Travel" | "Finance" | "Work" | "Personal" | "Health" | "Home",
        "merchant": "Merchant / Vendor name or null",
        "amount": 12.50 or null,
        "currency": "USD" or null,
        "eventDate": "YYYY-MM-DD or readable date string or null",
        "location": "City, place, or address or null",
        "tags": ["tag1", "tag2", "tag3"]
      }
    """.trimIndent()

    try {
      val requestJson = JSONObject().apply {
        put("model", DEFAULT_MODEL)
        put("temperature", 0.1)
        put("max_tokens", 500)
        put("messages", JSONArray().apply {
          put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
          })
          put(JSONObject().apply {
            put("role", "user")
            put("content", "Memory Type Hint: $memoryType\nInput Text:\n$rawText")
          })
        })
      }

      val request = Request.Builder()
        .url(OPENROUTER_ENDPOINT)
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("HTTP-Referer", "https://memoryos.app")
        .addHeader("X-Title", "MemoryOS")
        .addHeader("Content-Type", "application/json")
        .post(requestJson.toString().toRequestBody(jsonMediaType))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string()

      if (response.isSuccessful && !responseBody.isNullOrBlank()) {
        val root = JSONObject(responseBody)
        val choices = root.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
          val message = choices.getJSONObject(0).optJSONObject("message")
          val rawContent = message?.optString("content") ?: ""

          val parsed = parseExtractedDataJson(rawContent, memoryType, rawText)
          if (parsed != null) {
            return@withContext parsed
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "OpenRouter entity extraction error: ${e.message}")
    }

    // Heuristic rule-based fallback
    fallbackExtractMemory(rawText, memoryType)
  }

  // --- Helpers and Fallback Parsers ---

  private fun parseAnswerJson(raw: String, query: String, memories: List<MemoryEntity>): GroundedAnswer? {
    try {
      val jsonStr = extractJsonObjectString(raw) ?: return null
      val obj = JSONObject(jsonStr)
      val answer = obj.optString("answer", "").trim()
      val confidence = obj.optString("confidence", "HIGH").uppercase()

      val sourcesList = mutableListOf<SourceCitation>()
      val sourcesArray = obj.optJSONArray("sources")
      if (sourcesArray != null) {
        for (i in 0 until sourcesArray.length()) {
          val sObj = sourcesArray.getJSONObject(i)
          val memId = sObj.optString("memoryId", "")
          val reason = sObj.optString("reason", "Matched query context")
          val mem = memories.find { it.id == memId }
          if (mem != null) {
            sourcesList.add(
              SourceCitation(
                memoryId = mem.id,
                title = mem.title,
                reason = reason,
                date = mem.eventDate,
                category = mem.category
              )
            )
          }
        }
      }

      if (answer.isNotBlank()) {
        return GroundedAnswer(
          query = query,
          answer = answer,
          confidence = confidence,
          sources = sourcesList
        )
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to parse answer json: ${e.message}")
    }
    return null
  }

  private fun parseSemanticJson(
    raw: String,
    query: String,
    memories: List<MemoryEntity>,
    durationMs: Long
  ): SemanticSearchResult? {
    try {
      val jsonStr = extractJsonObjectString(raw) ?: return null
      val obj = JSONObject(jsonStr)
      val summary = obj.optString("semanticSummary", "Found relevant memories.")
      val matchesArray = obj.optJSONArray("matches") ?: return null

      val resultMatches = mutableListOf<SemanticMatch>()
      for (i in 0 until matchesArray.length()) {
        val mObj = matchesArray.getJSONObject(i)
        val id = mObj.optString("id")
        val score = mObj.optInt("score", 75)
        val reason = mObj.optString("reason", "Contextually relevant")
        val snippet = mObj.optString("snippet", "")

        val mem = memories.find { it.id == id }
        if (mem != null) {
          resultMatches.add(
            SemanticMatch(
              memory = mem,
              relevanceScore = score.coerceIn(1, 100),
              reason = reason,
              highlightSnippet = if (snippet.isNotBlank()) snippet else mem.summary
            )
          )
        }
      }

      return SemanticSearchResult(
        query = query,
        semanticSummary = summary,
        matches = resultMatches.sortedByDescending { it.relevanceScore },
        executionTimeMs = durationMs
      )
    } catch (e: Exception) {
      Log.w(TAG, "Failed to parse semantic json: ${e.message}")
    }
    return null
  }

  private fun parseExtractedDataJson(
    raw: String,
    fallbackType: String,
    rawText: String
  ): ExtractedMemoryData? {
    try {
      val jsonStr = extractJsonObjectString(raw) ?: return null
      val obj = JSONObject(jsonStr)

      val title = obj.optString("title").ifBlank { rawText.take(30) }
      val summary = obj.optString("summary").ifBlank { rawText.take(120) }
      val memoryType = obj.optString("memoryType").ifBlank { fallbackType }
      val category = obj.optString("category").ifBlank { "Personal" }
      val merchant = if (obj.has("merchant") && !obj.isNull("merchant")) obj.optString("merchant") else null
      val amount = if (obj.has("amount") && !obj.isNull("amount")) obj.optDouble("amount") else null
      val currency = if (obj.has("currency") && !obj.isNull("currency")) obj.optString("currency") else "$"
      val eventDate = if (obj.has("eventDate") && !obj.isNull("eventDate")) obj.optString("eventDate") else null
      val location = if (obj.has("location") && !obj.isNull("location")) obj.optString("location") else null

      val tagsList = mutableListOf<String>()
      val tagsArray = obj.optJSONArray("tags")
      if (tagsArray != null) {
        for (i in 0 until tagsArray.length()) {
          val t = tagsArray.optString(i).trim()
          if (t.isNotBlank()) tagsList.add(t.lowercase())
        }
      }

      return ExtractedMemoryData(
        title = title,
        summary = summary,
        memoryType = memoryType,
        category = category,
        merchant = merchant,
        amount = amount,
        currency = currency,
        eventDate = eventDate,
        location = location,
        tags = tagsList
      )
    } catch (e: Exception) {
      Log.w(TAG, "Failed to parse extracted json: ${e.message}")
    }
    return null
  }

  private fun extractJsonObjectString(raw: String): String? {
    val clean = raw.trim()
    val start = clean.indexOf('{')
    val end = clean.lastIndexOf('}')
    return if (start != -1 && end != -1 && end > start) {
      clean.substring(start, end + 1)
    } else {
      null
    }
  }

  private fun fallbackAnswerQuestion(query: String, memories: List<MemoryEntity>): GroundedAnswer {
    val qTerms = query.lowercase().split("\\s+".toRegex()).filter { it.length > 2 }
    val scored = memories.map { mem ->
      var score = 0
      val searchable = "${mem.title} ${mem.summary} ${mem.rawText} ${mem.merchant ?: ""} ${mem.location ?: ""} ${mem.tags}".lowercase()
      qTerms.forEach { t ->
        if (searchable.contains(t)) score += 1
      }
      Pair(mem, score)
    }.filter { it.second > 0 }.sortedByDescending { it.second }

    if (scored.isEmpty()) {
      return GroundedAnswer(
        query = query,
        answer = "I searched your memory bank for '$query' but didn't find any direct matches. Try speaking a voice note or saving a receipt!",
        confidence = "LOW",
        sources = emptyList()
      )
    }

    val top = scored.take(3)
    val answerText = "Based on your saved memories, here is what matches '$query': ${top.first().first.title} (${top.first().first.summary})."
    val citations = top.map {
      SourceCitation(
        memoryId = it.first.id,
        title = it.first.title,
        reason = "Matched query keywords",
        date = it.first.eventDate,
        category = it.first.category
      )
    }

    return GroundedAnswer(
      query = query,
      answer = answerText,
      confidence = "HIGH",
      sources = citations
    )
  }

  private fun fallbackSemanticSearch(query: String, memories: List<MemoryEntity>, durationMs: Long): SemanticSearchResult {
    val qTerms = query.lowercase().split("\\s+".toRegex()).filter { it.length > 2 }
    val matches = memories.mapNotNull { mem ->
      val searchable = "${mem.title} ${mem.summary} ${mem.rawText} ${mem.merchant ?: ""} ${mem.location ?: ""} ${mem.tags}".lowercase()
      var hits = 0
      qTerms.forEach { if (searchable.contains(it)) hits++ }
      if (hits > 0 || qTerms.isEmpty()) {
        val score = if (qTerms.isEmpty()) 70 else ((hits.toDouble() / qTerms.size.coerceAtLeast(1)) * 100).toInt().coerceIn(30, 98)
        SemanticMatch(
          memory = mem,
          relevanceScore = score,
          reason = "Content matches terms: ${qTerms.joinToString()}",
          highlightSnippet = mem.summary
        )
      } else {
        null
      }
    }.sortedByDescending { it.relevanceScore }

    return SemanticSearchResult(
      query = query,
      semanticSummary = "Found ${matches.size} memories matching '$query'",
      matches = matches,
      executionTimeMs = durationMs
    )
  }

  private fun fallbackExtractMemory(rawText: String, memoryType: String): ExtractedMemoryData {
    val clean = rawText.trim()
    val words = clean.split("\\s+".toRegex())
    val title = words.take(5).joinToString(" ")
    val isReceipt = memoryType == "RECEIPT" || clean.contains("paid", ignoreCase = true) || clean.contains("$")

    val amountRegex = "\\$([0-9]+(?:\\.[0-9]{2})?)".toRegex()
    val amountMatch = amountRegex.find(clean)
    val amount = amountMatch?.groupValues?.get(1)?.toDoubleOrNull()

    return ExtractedMemoryData(
      title = if (title.isNotBlank()) title else "Personal Memory",
      summary = clean.take(150),
      memoryType = if (isReceipt) "RECEIPT" else memoryType,
      category = if (isReceipt) "Finance" else "Personal",
      amount = amount,
      currency = if (amount != null) "USD" else null,
      tags = listOf("memory", if (isReceipt) "expense" else "note")
    )
  }
}
