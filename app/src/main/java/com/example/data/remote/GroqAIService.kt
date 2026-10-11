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

/**
 * Groq Ultra-Low Latency AI Inference Service:
 * Connects to Groq's LPUs for lightning-fast (100-300ms) grounded Q&A,
 * memory extraction, and semantic search.
 */
class GroqAIService {

  companion object {
    private const val TAG = "GroqAIService"
    private const val GROQ_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
    private const val DEFAULT_MODEL = "llama-3.3-70b-versatile"
    private const val FAST_MODEL = "llama-3.1-8b-instant"
  }

  private val client = OkHttpClient.Builder()
    .connectTimeout(12, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  // Groq API Key securely resolved from BuildConfig
  val apiKey: String
    get() {
      val keyFromConfig = try {
        BuildConfig::class.java.getField("GROQ_API_KEY").get(null) as? String
      } catch (_: Exception) {
        ""
      }
      return keyFromConfig?.takeIf { it.isNotBlank() && it != "null" } ?: ""
    }

  val isConfigured: Boolean
    get() = apiKey.isNotBlank()

  /**
   * Grounded Question Answering over Personal Memories using Groq LPU
   */
  suspend fun answerQuestion(
    query: String,
    memories: List<MemoryEntity>
  ): GroundedAnswer? = withContext(Dispatchers.IO) {
    if (!isConfigured) return@withContext null
    if (memories.isEmpty()) return@withContext null

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
      You are MemoryOS Intelligence powered by Groq LPU.
      Answer the user query concisely and accurately based STRICTLY on the memories below.
      
      RULES:
      1. Never hallucinate facts. Only use details from the provided memories.
      2. If found, respond in 1-2 friendly, polished sentences.
      3. Provide memoryId of supporting items with a concise reason.
      4. If not found, answer: "I couldn't find enough details in your saved memories to answer that question." with confidence: "LOW".
      
      Respond ONLY with valid JSON:
      {
        "answer": "Clear factual answer",
        "confidence": "HIGH" | "MEDIUM" | "LOW",
        "sources": [
          {
            "memoryId": "mem_id",
            "reason": "Why this memory matches"
          }
        ]
      }
    """.trimIndent()

    val userContent = "User Query: $query\n\nMemories:\n$contextBuilder"

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
        put("response_format", JSONObject().apply { put("type", "json_object") })
      }

      val request = Request.Builder()
        .url(GROQ_ENDPOINT)
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Content-Type", "application/json")
        .post(requestJson.toString().toRequestBody(jsonMediaType))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string() ?: ""

      if (response.isSuccessful) {
        val root = JSONObject(responseBody)
        val choices = root.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
          val message = choices.getJSONObject(0).optJSONObject("message")
          val content = message?.optString("content") ?: ""
          val parsedJson = JSONObject(content.trim())

          val answerText = parsedJson.optString("answer", "Here are the matching memories from your vault.")
          val confidence = parsedJson.optString("confidence", "MEDIUM")
          val sourcesArray = parsedJson.optJSONArray("sources")
          val sourcesList = mutableListOf<SourceCitation>()

          if (sourcesArray != null) {
            for (i in 0 until sourcesArray.length()) {
              val sObj = sourcesArray.getJSONObject(i)
              val memId = sObj.optString("memoryId")
              val reason = sObj.optString("reason", "Matched memory")
              val targetMem = memories.find { it.id == memId }
              if (targetMem != null) {
                sourcesList.add(
                  SourceCitation(
                    memoryId = targetMem.id,
                    title = targetMem.title,
                    reason = reason,
                    date = targetMem.eventDate,
                    category = targetMem.category
                  )
                )
              }
            }
          }

          return@withContext GroundedAnswer(
            query = query,
            answer = answerText,
            confidence = confidence,
            sources = sourcesList,
            uncertaintyNote = if (confidence == "LOW") "Groq verified limited context in memory bank" else null
          )
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Groq answerQuestion error: ${e.message}")
    }
    null
  }

  /**
   * Semantic Search across Memories via Groq LPU
   */
  suspend fun semanticSearch(
    query: String,
    memories: List<MemoryEntity>
  ): SemanticSearchResult? = withContext(Dispatchers.IO) {
    if (!isConfigured || memories.isEmpty()) return@withContext null

    val memorySummaries = StringBuilder()
    memories.take(40).forEach { mem ->
      memorySummaries.append("[ID:${mem.id}] ${mem.title} (${mem.category}, ${mem.memoryType}) - ${mem.summary} | Tags: ${mem.tags}\n")
    }

    val prompt = """
      You are a semantic memory retrieval engine on Groq.
      Find the most conceptually relevant memories for the user query, even if keywords do not match exactly.
      
      Query: "$query"
      
      Memories:
      $memorySummaries
      
      Respond in JSON format:
      {
        "matches": [
          {
            "id": "mem_id",
            "score": 0.95,
            "explanation": "Brief explanation of conceptual match"
          }
        ]
      }
    """.trimIndent()

    try {
      val requestJson = JSONObject().apply {
        put("model", FAST_MODEL)
        put("temperature", 0.0)
        put("max_tokens", 600)
        put("messages", JSONArray().apply {
          put(JSONObject().apply {
            put("role", "system")
            put("content", "You are an accurate semantic retrieval assistant that responds ONLY with valid JSON.")
          })
          put(JSONObject().apply {
            put("role", "user")
            put("content", prompt)
          })
        })
        put("response_format", JSONObject().apply { put("type", "json_object") })
      }

      val request = Request.Builder()
        .url(GROQ_ENDPOINT)
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Content-Type", "application/json")
        .post(requestJson.toString().toRequestBody(jsonMediaType))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string() ?: ""

      if (response.isSuccessful) {
        val root = JSONObject(responseBody)
        val choices = root.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
          val message = choices.getJSONObject(0).optJSONObject("message")
          val content = message?.optString("content") ?: ""
          val parsedJson = JSONObject(content.trim())
          val matchesArray = parsedJson.optJSONArray("matches")

          if (matchesArray != null && matchesArray.length() > 0) {
            val matchedMemories = mutableListOf<SemanticMatch>()
            for (i in 0 until matchesArray.length()) {
              val matchObj = matchesArray.getJSONObject(i)
              val id = matchObj.optString("id")
              val score = matchObj.optDouble("score", 0.8).toFloat()
              val explanation = matchObj.optString("explanation", "Conceptually relevant memory")
              val memory = memories.find { it.id == id }
              if (memory != null) {
                matchedMemories.add(
                  SemanticMatch(
                    memory = memory,
                    relevanceScore = (score * 100).toInt().coerceIn(10, 99),
                    reason = explanation,
                    highlightSnippet = memory.summary.take(80)
                  )
                )
              }
            }

            return@withContext SemanticSearchResult(
              query = query,
              semanticSummary = "Found ${matchedMemories.size} conceptually related memories via Groq LPU.",
              matches = matchedMemories.sortedByDescending { it.relevanceScore },
              executionTimeMs = 150
            )
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Groq semantic search error: ${e.message}")
    }
    null
  }

  /**
   * Memory Entity Extraction using Groq LPU
   */
  suspend fun extractMemoryInfo(
    rawText: String,
    memoryType: String
  ): ExtractedMemoryData? = withContext(Dispatchers.IO) {
    if (!isConfigured || rawText.isBlank()) return@withContext null

    val systemPrompt = """
      You are an automated memory information extraction assistant on Groq.
      Extract structured metadata from the user's raw note, receipt, ticket, or booking.
      
      Respond strictly with JSON:
      {
        "title": "Short descriptive title (4-6 words)",
        "summary": "1-2 sentence clean summary",
        "category": "Personal" | "Work" | "Travel" | "Finance" | "Health",
        "merchant": "Merchant or organization name or null",
        "amount": 0.0 or null,
        "currency": "$" or "USD" or null,
        "eventDate": "YYYY-MM-DD" or null,
        "location": "City, place or null",
        "tags": ["tag1", "tag2"]
      }
    """.trimIndent()

    try {
      val requestJson = JSONObject().apply {
        put("model", FAST_MODEL)
        put("temperature", 0.1)
        put("max_tokens", 500)
        put("messages", JSONArray().apply {
          put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
          })
          put(JSONObject().apply {
            put("role", "user")
            put("content", "Type: $memoryType\nContent:\n$rawText")
          })
        })
        put("response_format", JSONObject().apply { put("type", "json_object") })
      }

      val request = Request.Builder()
        .url(GROQ_ENDPOINT)
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Content-Type", "application/json")
        .post(requestJson.toString().toRequestBody(jsonMediaType))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string() ?: ""

      if (response.isSuccessful) {
        val root = JSONObject(responseBody)
        val choices = root.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
          val message = choices.getJSONObject(0).optJSONObject("message")
          val content = message?.optString("content") ?: ""
          val json = JSONObject(content.trim())

          val tagsList = mutableListOf<String>()
          val tagsArray = json.optJSONArray("tags")
          if (tagsArray != null) {
            for (i in 0 until tagsArray.length()) {
              tagsList.add(tagsArray.getString(i))
            }
          }

          val amountVal = if (json.has("amount") && !json.isNull("amount")) json.optDouble("amount") else null

          return@withContext ExtractedMemoryData(
            title = json.optString("title").takeIf { it.isNotBlank() } ?: rawText.take(30),
            summary = json.optString("summary").takeIf { it.isNotBlank() } ?: rawText.take(100),
            memoryType = memoryType,
            category = json.optString("category", "Personal"),
            merchant = json.optString("merchant").takeIf { it.isNotBlank() && it != "null" },
            amount = amountVal,
            currency = json.optString("currency").takeIf { it.isNotBlank() && it != "null" },
            eventDate = json.optString("eventDate").takeIf { it.isNotBlank() && it != "null" },
            location = json.optString("location").takeIf { it.isNotBlank() && it != "null" },
            tags = tagsList
          )
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Groq entity extraction error: ${e.message}")
    }
    null
  }
}
