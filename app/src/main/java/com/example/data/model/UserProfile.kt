package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User Profile entity stored locally in Room.
 * Powers the adaptive personalization system based on onboarding questionnaire and survey inputs.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
  @PrimaryKey
  val userId: String = "local_user",
  val personaTag: String = "MINIMALIST_ARCHIVIST", // CREATIVE_PRO, BUSY_EXECUTIVE, RESEARCHER_STUDENT, MINIMALIST_ARCHIVIST
  val experienceLevel: String = "BEGINNER",        // BEGINNER, INTERMEDIATE, POWER_USER
  val needIntensity: String = "CASUAL",            // CASUAL, MODERATE, INTENSIVE
  val primaryCaptureMethod: String = "VOICE",      // VOICE, CAMERA_OCR, QUICK_NOTE, IMPORT
  val primaryGoal: String = "INSTANT_RECALL",      // INSTANT_RECALL, EXPENSE_TRACKING, JOURNALING, TRAVEL_LOGS
  val organizationStyle: String = "AUTO_SEMANTIC", // AUTO_SEMANTIC, MANUAL_FOLDERS, CHRONOLOGICAL
  val privacySensitivity: String = "LOCAL_FIRST",  // LOCAL_FIRST, ENCRYPTED_CLOUD
  val dailyCaptureTarget: Int = 1,                 // Daily memory goal
  val acquisitionChannel: String = "ORGANIC",      // APP_STORE, WORD_OF_MOUTH, TECH_MEDIA, SOCIAL
  val primaryExpectation: String = "",             // Free-text or selected core expectation
  val hasCompletedOnboarding: Boolean = true,
  val createdAt: Long = System.currentTimeMillis(),
  val lastPersonalizedAt: Long = System.currentTimeMillis()
)

/**
 * Adaptive UI Configuration derived from User Profile.
 */
data class AdaptiveUiConfig(
  val recommendedFabAction: String, // VOICE, CAMERA, TEXT
  val defaultHomeFilter: String,    // ALL, VOICE, RECEIPTS, TRAVEL
  val showAdvancedOcrTools: Boolean,
  val showFinancialWidgets: Boolean,
  val defaultEmptyStateMessage: String,
  val suggestedPromptTemplates: List<String>
)
