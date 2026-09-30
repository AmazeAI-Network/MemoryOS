package com.example.data.supabase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseUser(
  @Json(name = "id") val id: String,
  @Json(name = "email") val email: String?,
  @Json(name = "full_name") val fullName: String? = null,
  @Json(name = "created_at") val createdAt: String? = null,
  val accessToken: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
  @Json(name = "access_token") val accessToken: String?,
  @Json(name = "token_type") val tokenType: String?,
  @Json(name = "expires_in") val expiresIn: Long?,
  @Json(name = "refresh_token") val refreshToken: String?,
  @Json(name = "user") val user: SupabaseUserPayload?
)

@JsonClass(generateAdapter = true)
data class SupabaseUserPayload(
  @Json(name = "id") val id: String,
  @Json(name = "email") val email: String?,
  @Json(name = "created_at") val createdAt: String?,
  @Json(name = "user_metadata") val userMetadata: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseMemoryRecord(
  @Json(name = "id") val id: String,
  @Json(name = "user_id") val userId: String,
  @Json(name = "raw_text") val rawText: String,
  @Json(name = "type") val type: String,
  @Json(name = "category") val category: String,
  @Json(name = "summary") val summary: String?,
  @Json(name = "tags") val tags: List<String>?,
  @Json(name = "timestamp") val timestamp: Long,
  @Json(name = "is_favorite") val isFavorite: Boolean
)
