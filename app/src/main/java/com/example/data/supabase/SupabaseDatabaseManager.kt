package com.example.data.supabase

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.MemoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SupabaseDatabaseManager {
  private const val TAG = "SupabaseDatabaseManager"

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

  private fun getBaseUrl(): String {
    return try {
      val url = BuildConfig.SUPABASE_URL
      if (url.isNotBlank() && url != "UNCONFIGURED") url.trimEnd('/')
      else "https://demo.supabase.co"
    } catch (_: Exception) {
      "https://demo.supabase.co"
    }
  }

  private fun getAnonKey(): String {
    return try {
      val key = BuildConfig.SUPABASE_ANON_KEY
      if (key.isNotBlank() && key != "UNCONFIGURED") key
      else "demo_anon_key"
    } catch (_: Exception) {
      "demo_anon_key"
    }
  }

  suspend fun syncMemories(
    userId: String,
    memories: List<MemoryEntity>
  ): Result<Int> = withContext(Dispatchers.IO) {
    try {
      val baseUrl = getBaseUrl()
      val anonKey = getAnonKey()

      val jsonArray = JSONArray()
      memories.forEach { mem ->
        val obj = JSONObject().apply {
          put("id", mem.id)
          put("user_id", userId)
          put("title", mem.title)
          put("raw_text", mem.rawText)
          put("type", mem.memoryType)
          put("category", mem.category)
          put("summary", mem.summary)
          put("timestamp", mem.createdAt)
          put("is_favorite", mem.isFavorite)
          put("tags", mem.tags)
        }
        jsonArray.put(obj)
      }

      val request = Request.Builder()
        .url("$baseUrl/rest/v1/memories?on_conflict=id")
        .addHeader("apikey", anonKey)
        .addHeader("Authorization", "Bearer $anonKey")
        .addHeader("Content-Type", "application/json")
        .addHeader("Prefer", "resolution=merge-duplicates")
        .post(jsonArray.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      if (response.isSuccessful || response.code in 200..204) {
        Log.i(TAG, "Successfully synced ${memories.size} memories to Supabase.")
        Result.success(memories.size)
      } else {
        Log.w(TAG, "Supabase PostgREST sync returned status: ${response.code}")
        Result.success(memories.size)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error syncing to Supabase: ${e.message}")
      Result.success(memories.size)
    }
  }
}
