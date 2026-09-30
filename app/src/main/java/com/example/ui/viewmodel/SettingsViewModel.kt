package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MemoryDatabase
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LanguageOption(
  val code: String,
  val displayName: String,
  val nativeName: String,
  val region: String
)

data class SettingsUiState(
  val selectedLanguage: String = "English (US)",
  val themeMode: String = "Light (Brand Cream)",
  val autoOcrEnabled: Boolean = true,
  val audioHighQuality: Boolean = true,
  val hapticFeedbackEnabled: Boolean = true,
  val biometricLockEnabled: Boolean = false,
  val offlineVaultMode: Boolean = false,
  val totalMemoriesCount: Int = 0,
  val totalCollectionsCount: Int = 0,
  val estimatedStorageKb: Long = 0,
  val actionFeedback: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

  private val database = MemoryDatabase.getDatabase(application)
  private val memoryDao = database.memoryDao()
  private val collectionDao = database.collectionDao()
  private val userProfileDao = database.userProfileDao()

  val supportedLanguages = listOf(
    LanguageOption("en-US", "English (US)", "English", "North America"),
    LanguageOption("es-ES", "Spanish", "Español", "Europe & Americas"),
    LanguageOption("fr-FR", "French", "Français", "Europe & Africa"),
    LanguageOption("de-DE", "German", "Deutsch", "Europe"),
    LanguageOption("ja-JP", "Japanese", "日本語", "Asia"),
    LanguageOption("zh-CN", "Chinese (Simplified)", "中文 (简体)", "Asia"),
    LanguageOption("zh-TW", "Chinese (Traditional)", "中文 (繁體)", "Asia"),
    LanguageOption("ar-SA", "Arabic", "العربية", "Middle East"),
    LanguageOption("hi-IN", "Hindi", "हिन्दी", "South Asia"),
    LanguageOption("pt-BR", "Portuguese (Brazil)", "Português", "South America"),
    LanguageOption("it-IT", "Italian", "Italiano", "Europe"),
    LanguageOption("ko-KR", "Korean", "한국어", "East Asia"),
    LanguageOption("ru-RU", "Russian", "Русский", "Eurasia"),
    LanguageOption("id-ID", "Indonesian", "Bahasa Indonesia", "Southeast Asia"),
    LanguageOption("nl-NL", "Dutch", "Nederlands", "Europe"),
    LanguageOption("tr-TR", "Turkish", "Türkçe", "Eurasia"),
    LanguageOption("vi-VN", "Vietnamese", "Tiếng Việt", "Southeast Asia")
  )

  val themeOptions = listOf(
    "Light (Brand Cream)",
    "Dark (Obsidian)",
    "System Default"
  )

  private val _uiState = MutableStateFlow(SettingsUiState())
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  val userProfile: StateFlow<UserProfileEntity?> = userProfileDao.getUserProfile()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  init {
    refreshStats()
  }

  fun refreshStats() {
    viewModelScope.launch {
      val memories = memoryDao.getAllMemoriesSnapshot()
      val collCount = collectionDao.countCollections()
      val count = memories.size
      // Approximate storage calculation
      val textBytes = memories.sumOf { it.rawText.length + it.ocrText.length + it.summary.length + it.title.length }
      val totalKb = (textBytes / 1024L) + (count * 2L)

      _uiState.value = _uiState.value.copy(
        totalMemoriesCount = count,
        totalCollectionsCount = collCount,
        estimatedStorageKb = totalKb
      )
    }
  }

  fun selectLanguage(languageName: String) {
    _uiState.value = _uiState.value.copy(
      selectedLanguage = languageName,
      actionFeedback = "Language set to $languageName"
    )
  }

  fun selectThemeMode(mode: String) {
    _uiState.value = _uiState.value.copy(
      themeMode = mode,
      actionFeedback = "Theme updated: $mode"
    )
  }

  fun toggleAutoOcr() {
    val current = _uiState.value.autoOcrEnabled
    _uiState.value = _uiState.value.copy(
      autoOcrEnabled = !current,
      actionFeedback = if (!current) "Auto OCR enabled" else "Auto OCR paused"
    )
  }

  fun toggleAudioQuality() {
    val current = _uiState.value.audioHighQuality
    _uiState.value = _uiState.value.copy(
      audioHighQuality = !current,
      actionFeedback = if (!current) "Lossless audio quality enabled" else "Standard audio quality"
    )
  }

  fun toggleHapticFeedback() {
    val current = _uiState.value.hapticFeedbackEnabled
    _uiState.value = _uiState.value.copy(
      hapticFeedbackEnabled = !current
    )
  }

  fun toggleBiometricLock() {
    val current = _uiState.value.biometricLockEnabled
    _uiState.value = _uiState.value.copy(
      biometricLockEnabled = !current,
      actionFeedback = if (!current) "Biometric vault lock armed" else "Biometric lock disarmed"
    )
  }

  fun toggleOfflineVault() {
    val current = _uiState.value.offlineVaultMode
    _uiState.value = _uiState.value.copy(
      offlineVaultMode = !current,
      actionFeedback = if (!current) "Offline-only mode engaged" else "Hybrid cloud sync ready"
    )
  }

  fun clearAllMemories(onComplete: () -> Unit) {
    viewModelScope.launch {
      memoryDao.deleteAllMemories()
      refreshStats()
      _uiState.value = _uiState.value.copy(
        actionFeedback = "All memories cleared from local vault"
      )
      onComplete()
    }
  }

  fun dismissFeedback() {
    _uiState.value = _uiState.value.copy(actionFeedback = null)
  }
}
