package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MemoryDatabase
import com.example.data.model.CollectionEntity
import com.example.data.model.GroundedAnswer
import com.example.data.model.MemoryEntity
import com.example.data.model.RecentSearchEntity
import com.example.ui.components.PermissionType
import com.example.ui.components.LegalTab
import com.example.data.repository.MemoryRepository
import com.example.sync.QueuedSyncOperation
import com.example.sync.SyncOperationType
import com.example.sync.SyncQueueManager
import com.example.sync.SyncState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.supabase.SupabaseAuthManager
import com.example.data.supabase.SupabaseDatabaseManager
import com.example.data.supabase.SupabaseUser
import com.example.ui.components.AuthMode
import com.example.ui.components.OnboardingManager

sealed interface Screen {
  data object Home : Screen
  data object Ask : Screen
  data object Memories : Screen
  data object Collections : Screen
  data object Profile : Screen
  data object Settings : Screen
  data class MemoryDetail(val memoryId: String) : Screen
  data class CollectionDetail(val collectionId: String) : Screen
}

enum class MemoryFilter {
  ALL, TRAVEL, RECEIPTS, TICKETS, NOTES, FAVORITES
}

@Immutable
data class AskUiState(
  val query: String = "",
  val isLoading: Boolean = false,
  val stepMessage: String = "",
  val answer: GroundedAnswer? = null,
  val searchHistory: List<String> = listOf(
    "What did I spend on dining this week?",
    "Find my hotel or flight reservations",
    "What was the meeting note about?",
    "Search my receipts and purchases"
  )
)

@Immutable
data class SemanticSearchUiState(
  val query: String = "",
  val isSearching: Boolean = false,
  val result: com.example.data.model.SemanticSearchResult? = null,
  val isSemanticMode: Boolean = false
)

class MemoryViewModel(application: Application) : AndroidViewModel(application) {

  private val database = MemoryDatabase.getDatabase(application)
  val searchRepository = com.example.data.repository.SearchRepository(
    searchDao = database.searchDao(),
    memoryDao = database.memoryDao()
  )
  private val repository = MemoryRepository(
    memoryDao = database.memoryDao(),
    collectionDao = database.collectionDao(),
    searchDao = database.searchDao(),
    userProfileDao = database.userProfileDao()
  )

  val userProfile: StateFlow<com.example.data.model.UserProfileEntity?> = repository.userProfile
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // [Performance Architecture] Streak Repository backed by Room database
  val streakRepository = com.example.data.repository.StreakRepository(
    userStreakDao = database.userStreakDao()
  )
  val streakState: StateFlow<com.example.data.model.StreakUiState> = streakRepository.streakUiState
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.model.StreakUiState())

  val npsDao = database.npsDao()

  // Offline-First Sync Queue
  val syncQueueManager = SyncQueueManager.getInstance(application)
  val syncState: StateFlow<SyncState> = syncQueueManager.syncState
  val queuedOperations: StateFlow<List<QueuedSyncOperation>> = syncQueueManager.queuedOperations

  fun updateNetworkOnlineStatus(isOnline: Boolean) {
    syncQueueManager.onNetworkStateChanged(isOnline)
  }

  fun triggerManualSync() {
    syncQueueManager.triggerSync()
  }

  // Navigation state
  private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
  val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

  // Selected memory for detail view
  private val _selectedMemory = MutableStateFlow<MemoryEntity?>(null)
  val selectedMemory: StateFlow<MemoryEntity?> = _selectedMemory.asStateFlow()

  // Selected collection for detail view
  private val _selectedCollection = MutableStateFlow<CollectionEntity?>(null)
  val selectedCollection: StateFlow<CollectionEntity?> = _selectedCollection.asStateFlow()

  // Filter & Search
  private val _selectedFilter = MutableStateFlow(MemoryFilter.ALL)
  val selectedFilter: StateFlow<MemoryFilter> = _selectedFilter.asStateFlow()

  // Tagging & Categorization filters ('Work', 'Personal', 'Ideas', etc.)
  private val _selectedCategory = MutableStateFlow("All")
  val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

  private val _selectedTag = MutableStateFlow<String?>(null)
  val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

  // Sorting memories by date (ascending vs descending)
  private val _sortDateAscending = MutableStateFlow(false)
  val sortDateAscending: StateFlow<Boolean> = _sortDateAscending.asStateFlow()

  fun toggleDateSortOrder() {
    _sortDateAscending.value = !_sortDateAscending.value
  }

  fun setDateSortAscending(ascending: Boolean) {
    _sortDateAscending.value = ascending
  }

  val availableCategories: StateFlow<List<String>> = searchRepository.getCategories()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All", "Work", "Personal", "Ideas", "Travel"))

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  // Ask Memory UI state
  private val _askState = MutableStateFlow(AskUiState())
  val askState: StateFlow<AskUiState> = _askState.asStateFlow()

  // Semantic Search UI state
  private val _semanticState = MutableStateFlow(SemanticSearchUiState())
  val semanticState: StateFlow<SemanticSearchUiState> = _semanticState.asStateFlow()

  // Add Memory Modal state
  private val _showAddSheet = MutableStateFlow(false)
  val showAddSheet: StateFlow<Boolean> = _showAddSheet.asStateFlow()

  // Voice snippet recording dialog
  private val _showVoiceDialog = MutableStateFlow(false)
  val showVoiceDialog: StateFlow<Boolean> = _showVoiceDialog.asStateFlow()

  // Pro Subscription Paywall dialog
  private val _showPaywallSheet = MutableStateFlow(false)
  val showPaywallSheet: StateFlow<Boolean> = _showPaywallSheet.asStateFlow()

  private val _isProUser = MutableStateFlow(false)
  val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

  // Daily Memory Engagement Notification Reminders
  private val _dailyRemindersEnabled = MutableStateFlow(
    com.example.notification.NotificationHelper.isReminderEnabled(application)
  )
  val dailyRemindersEnabled: StateFlow<Boolean> = _dailyRemindersEnabled.asStateFlow()

  private val _reminderTime = MutableStateFlow(
    com.example.notification.NotificationHelper.getReminderTime(application)
  )
  val reminderTime: StateFlow<Pair<Int, Int>> = _reminderTime.asStateFlow()

  // Cloud Backup status (clean native naming)
  private val _cloudSyncConnected = MutableStateFlow(true)
  val cloudSyncConnected: StateFlow<Boolean> = _cloudSyncConnected.asStateFlow()

  // Aha Moment Result celebration
  private val _showAhaDialog = MutableStateFlow(false)
  val showAhaDialog: StateFlow<Boolean> = _showAhaDialog.asStateFlow()

  private val _ahaMemory = MutableStateFlow<MemoryEntity?>(null)
  val ahaMemory: StateFlow<MemoryEntity?> = _ahaMemory.asStateFlow()

  // Contextual Help Sheet
  private val _showHelpSheet = MutableStateFlow(false)
  val showHelpSheet: StateFlow<Boolean> = _showHelpSheet.asStateFlow()

  // Undo delete & Snackbar
  private val _recentlyDeletedMemory = MutableStateFlow<MemoryEntity?>(null)
  val recentlyDeletedMemory: StateFlow<MemoryEntity?> = _recentlyDeletedMemory.asStateFlow()

  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

  // Habit Streak
  private val _currentStreak = MutableStateFlow(
    com.example.analytics.AppAnalytics.getCurrentStreak(application)
  )
  val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

  // Room Local Storage: Recent Search Queries
  val recentSearches: StateFlow<List<RecentSearchEntity>> = database.recentSearchDao()
    .getAllRecentSearches()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Engagement & Gamification Pop-up states
  private val _showWalkthrough = MutableStateFlow(false)
  val showWalkthrough: StateFlow<Boolean> = _showWalkthrough.asStateFlow()

  private val _showTooltips = MutableStateFlow(false)
  val showTooltips: StateFlow<Boolean> = _showTooltips.asStateFlow()

  private val _showStreakMilestone = MutableStateFlow(false)
  val showStreakMilestone: StateFlow<Boolean> = _showStreakMilestone.asStateFlow()

  private val _showNpsDialog = MutableStateFlow(false)
  val showNpsDialog: StateFlow<Boolean> = _showNpsDialog.asStateFlow()

  private val _showPrePermissionDialog = MutableStateFlow(false)
  val showPrePermissionDialog: StateFlow<Boolean> = _showPrePermissionDialog.asStateFlow()

  private val _pendingPermissionType = MutableStateFlow(PermissionType.MICROPHONE)
  val pendingPermissionType: StateFlow<PermissionType> = _pendingPermissionType.asStateFlow()

  private val _showSupportEmailDialog = MutableStateFlow(false)
  val showSupportEmailDialog: StateFlow<Boolean> = _showSupportEmailDialog.asStateFlow()

  private val _showLegalDialog = MutableStateFlow(false)
  val showLegalDialog: StateFlow<Boolean> = _showLegalDialog.asStateFlow()

  private val _legalDialogTab = MutableStateFlow(LegalTab.PRIVACY_POLICY)
  val legalDialogTab: StateFlow<LegalTab> = _legalDialogTab.asStateFlow()

  private val _showClearAllConfirmation = MutableStateFlow(false)
  val showClearAllConfirmation: StateFlow<Boolean> = _showClearAllConfirmation.asStateFlow()

  private val _showClearDatabaseConfirmation = MutableStateFlow(false)
  val showClearDatabaseConfirmation: StateFlow<Boolean> = _showClearDatabaseConfirmation.asStateFlow()

  // Supabase Account & Database States
  val currentUser: StateFlow<SupabaseUser?> = SupabaseAuthManager.currentUser
  val isAuthLoading: StateFlow<Boolean> = SupabaseAuthManager.isLoading
  val authError: StateFlow<String?> = SupabaseAuthManager.authError

  private val _showAuthDialog = MutableStateFlow(false)
  val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

  // Camera & Photo Capture States
  private val _showCameraDialog = MutableStateFlow(false)
  val showCameraDialog: StateFlow<Boolean> = _showCameraDialog.asStateFlow()

  private val _lastCapturedPhoto = MutableStateFlow<java.io.File?>(null)
  val lastCapturedPhoto: StateFlow<java.io.File?> = _lastCapturedPhoto.asStateFlow()

  private val _authDialogMode = MutableStateFlow(AuthMode.SIGN_IN)
  val authDialogMode: StateFlow<AuthMode> = _authDialogMode.asStateFlow()

  // First-Time User Onboarding Questionnaire
  private val _showOnboardingQuestionnaire = MutableStateFlow(
    !OnboardingManager.hasCompletedQuestionnaire(application)
  )
  val showOnboardingQuestionnaire: StateFlow<Boolean> = _showOnboardingQuestionnaire.asStateFlow()

  // High-performance in-memory cache for 0ms page transitions
  private val memoryCache = java.util.concurrent.ConcurrentHashMap<String, MemoryEntity>()

  init {
    SupabaseAuthManager.initialize(application)

    viewModelScope.launch {
      repository.userProfile.collect { profile ->
        if (profile == null || !profile.hasCompletedOnboarding) {
          _showOnboardingQuestionnaire.value = true
        } else {
          _showOnboardingQuestionnaire.value = false
        }
      }
    }

    // [Performance: Dispatchers.IO] Record daily active usage for streak calculation
    viewModelScope.launch(Dispatchers.IO) {
      streakRepository.recordDailyActiveUsage()
      // Check 7-day install milestone for NPS trigger
      if (com.example.analytics.AppAnalytics.shouldTriggerNps(application)) {
        kotlinx.coroutines.delay(1800)
        _showNpsDialog.value = true
      }
    }

    viewModelScope.launch {
      _cloudSyncConnected.value = repository.checkCloudSync()
    }
    viewModelScope.launch {
      com.example.billing.BillingManager.isProActive.collect { active ->
        _isProUser.value = active
      }
    }
    // Prefetch and synchronize in-memory cache with Room database
    viewModelScope.launch {
      repository.allMemories.collect { list ->
        list.forEach { mem -> memoryCache[mem.id] = mem }
      }
    }
  }

  // Reactive memory streams
  val allMemories: StateFlow<List<MemoryEntity>> = repository.allMemories
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val collections: StateFlow<List<CollectionEntity>> = repository.allCollections
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  data class FilterCriteria(
    val category: String,
    val tag: String?,
    val filter: MemoryFilter,
    val query: String,
    val sortAscending: Boolean
  )

  private val filterCriteria: Flow<FilterCriteria> = combine(
    _selectedCategory,
    _selectedTag,
    _selectedFilter,
    _searchQuery,
    _sortDateAscending
  ) { category, tag, filter, query, sortAscending ->
    FilterCriteria(category, tag, filter, query, sortAscending)
  }

  val filteredMemories: StateFlow<List<MemoryEntity>> = combine(
    allMemories,
    filterCriteria
  ) { memories, criteria ->
    var list = memories

    // 1. Category filtering ('Work', 'Personal', 'Ideas', etc.)
    if (!criteria.category.equals("All", ignoreCase = true) && criteria.category.isNotBlank()) {
      list = list.filter {
        it.category.equals(criteria.category, ignoreCase = true) ||
        it.tags.contains(criteria.category, ignoreCase = true)
      }
    }

    // 2. Tag filtering
    if (!criteria.tag.isNullOrBlank()) {
      list = list.filter {
        it.tags.contains(criteria.tag, ignoreCase = true)
      }
    }

    // 3. Type Filter
    list = when (criteria.filter) {
      MemoryFilter.ALL -> list
      MemoryFilter.TRAVEL -> list.filter { it.memoryType == "TRAVEL" || it.category.equals("Travel", true) }
      MemoryFilter.RECEIPTS -> list.filter { it.memoryType == "RECEIPT" || it.category.equals("Dining", true) || it.category.equals("Shopping", true) }
      MemoryFilter.TICKETS -> list.filter { it.memoryType == "TICKET" || it.memoryType == "RESERVATION" }
      MemoryFilter.NOTES -> list.filter { it.memoryType == "NOTE" || it.memoryType == "DOCUMENT" }
      MemoryFilter.FAVORITES -> list.filter { it.isFavorite }
    }

    // 4. Instant search query
    if (criteria.query.isNotBlank()) {
      val q = criteria.query.trim().lowercase()
      list = list.filter {
        it.title.lowercase().contains(q) ||
        it.summary.lowercase().contains(q) ||
        it.rawText.lowercase().contains(q) ||
        it.ocrText.lowercase().contains(q) ||
        it.tags.lowercase().contains(q) ||
        it.category.lowercase().contains(q) ||
        (it.merchant?.lowercase()?.contains(q) ?: false) ||
        (it.location?.lowercase()?.contains(q) ?: false)
      }
    }

    // 5. Date sorting (Ascending vs Descending)
    if (criteria.sortAscending) {
      list.sortedBy { it.createdAt }
    } else {
      list.sortedByDescending { it.createdAt }
    }
  }
    // Performance: Offload CPU-intensive filtering, lowercasing & sorting from UI thread to worker pool
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun setCategory(category: String) {
    _selectedCategory.value = category
  }

  fun setTag(tag: String?) {
    _selectedTag.value = tag
  }

  /**
   * Pre-fetches a memory entity into cache ahead of screen navigation.
   * Invoked on card touch-down / hover for instantaneous visual transitions.
   */
  fun prefetchMemory(memoryId: String) {
    val cached = memoryCache[memoryId] ?: allMemories.value.firstOrNull { it.id == memoryId }
    if (cached != null) {
      _selectedMemory.value = cached
    } else {
      viewModelScope.launch {
        val fetched = repository.getMemoryById(memoryId)
        if (fetched != null) {
          memoryCache[memoryId] = fetched
          if (_currentScreen.value is Screen.MemoryDetail && (_currentScreen.value as Screen.MemoryDetail).memoryId == memoryId) {
            _selectedMemory.value = fetched
          }
        }
      }
    }
  }

  fun prefetchCollection(collectionId: String) {
    val cols = collections.value
    _selectedCollection.value = cols.firstOrNull { it.id == collectionId }
  }

  fun navigateTo(screen: Screen) {
    if (screen is Screen.MemoryDetail) {
      // Instant cache retrieval: 0ms wait before page transition starts
      val cached = memoryCache[screen.memoryId] ?: allMemories.value.firstOrNull { it.id == screen.memoryId }
      if (cached != null) {
        _selectedMemory.value = cached
      }
      viewModelScope.launch {
        val fetched = repository.getMemoryById(screen.memoryId)
        if (fetched != null) {
          memoryCache[screen.memoryId] = fetched
          _selectedMemory.value = fetched
        }
      }
    } else if (screen is Screen.CollectionDetail) {
      val cols = collections.value
      _selectedCollection.value = cols.firstOrNull { it.id == screen.collectionId }
    }
    _currentScreen.value = screen
  }

  fun setFilter(filter: MemoryFilter) {
    _selectedFilter.value = filter
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setAskQuery(query: String) {
    _askState.value = _askState.value.copy(query = query)
  }

  fun openAddSheet() {
    _showAddSheet.value = true
  }

  fun closeAddSheet() {
    _showAddSheet.value = false
  }

  /**
   * Optimistic favorite toggle:
   * Instantly updates UI state in < 16ms, then reconciles asynchronously with SQLite.
   */
  fun toggleFavorite(memoryId: String, currentFavorite: Boolean) {
    val targetFav = !currentFavorite
    if (_selectedMemory.value?.id == memoryId) {
      _selectedMemory.value = _selectedMemory.value?.copy(isFavorite = targetFav)
    }
    memoryCache[memoryId]?.let { mem ->
      memoryCache[memoryId] = mem.copy(isFavorite = targetFav)
    }

    viewModelScope.launch {
      try {
        repository.toggleFavorite(memoryId, currentFavorite)
        syncQueueManager.recordDataOperation(
          SyncOperationType.TOGGLE_FAVORITE,
          memoryId,
          if (targetFav) "Favorited memory" else "Unfavorited memory"
        )
      } catch (_: Exception) {
        // Rollback state on error
        if (_selectedMemory.value?.id == memoryId) {
          _selectedMemory.value = _selectedMemory.value?.copy(isFavorite = currentFavorite)
        }
        memoryCache[memoryId]?.let { mem ->
          memoryCache[memoryId] = mem.copy(isFavorite = currentFavorite)
        }
      }
    }
  }

  /**
   * Optimistic delete:
   * Instantly removes from view and cache, presents undo snackbar, commits to Room in background.
   */
  fun deleteMemory(memoryId: String) {
    val existing = memoryCache[memoryId] ?: allMemories.value.firstOrNull { it.id == memoryId }
    if (existing != null) {
      _recentlyDeletedMemory.value = existing
      memoryCache.remove(memoryId)
      _snackbarMessage.value = "Memory \"${existing.title}\" deleted"
    }
    if (_currentScreen.value is Screen.MemoryDetail) {
      _currentScreen.value = Screen.Memories
    }
    viewModelScope.launch {
      repository.deleteMemory(memoryId)
      syncQueueManager.recordDataOperation(
        SyncOperationType.DELETE_MEMORY,
        memoryId,
        "Deleted: ${existing?.title ?: "Memory"}"
      )
    }
  }

  fun undoDelete() {
    val toRestore = _recentlyDeletedMemory.value ?: return
    viewModelScope.launch {
      repository.insertMemory(toRestore)
      _recentlyDeletedMemory.value = null
      _snackbarMessage.value = "Restored \"${toRestore.title}\""
    }
  }

  fun dismissSnackbar() {
    _snackbarMessage.value = null
  }

  fun openHelpSheet() {
    _showHelpSheet.value = true
  }

  fun closeHelpSheet() {
    _showHelpSheet.value = false
  }

  fun closeAhaDialog() {
    _showAhaDialog.value = false
    _ahaMemory.value = null
  }

  fun saveDraft(text: String, type: String) {
    com.example.analytics.AppAnalytics.saveDraft(getApplication(), text, type)
  }

  fun getDraft(): Pair<String, String>? {
    return com.example.analytics.AppAnalytics.getDraft(getApplication())
  }

  fun clearDraft() {
    com.example.analytics.AppAnalytics.clearDraft(getApplication())
  }

  fun submitAskQuestion(questionOverride: String? = null) {
    val q = questionOverride ?: _askState.value.query
    if (q.isBlank()) return

    com.example.analytics.AppAnalytics.recordQueryAsked(getApplication())
    saveRecentSearch(q)

    viewModelScope.launch {
      _askState.value = _askState.value.copy(
        query = q,
        isLoading = true,
        stepMessage = "Searching your memories...",
        answer = null
      )

      val answer = repository.askMemory(q)

      _askState.value = _askState.value.copy(
        isLoading = false,
        stepMessage = "Grounded memory retrieved",
        answer = answer
      )
    }
  }

  fun openVoiceDialog() {
    _showVoiceDialog.value = true
  }

  fun closeVoiceDialog() {
    _showVoiceDialog.value = false
  }

  fun openPaywall() {
    _showPaywallSheet.value = true
  }

  fun closePaywall() {
    _showPaywallSheet.value = false
  }

  fun setDailyRemindersEnabled(enabled: Boolean) {
    _dailyRemindersEnabled.value = enabled
    com.example.notification.NotificationHelper.setReminderEnabled(getApplication(), enabled)
  }

  fun setReminderTime(hour: Int, minute: Int) {
    _reminderTime.value = Pair(hour, minute)
    com.example.notification.NotificationHelper.setReminderTime(getApplication(), hour, minute)
  }

  fun triggerTestNotification() {
    com.example.notification.NotificationHelper.triggerTestNotification(getApplication())
  }

  fun saveVoiceSnippet(text: String) {
    if (text.isBlank()) return
    viewModelScope.launch {
      val newMem = repository.createMemoryWithAI(rawText = text, memoryType = "NOTE", sourceType = "VOICE")
      _showVoiceDialog.value = false
      com.example.analytics.AppAnalytics.recordMemoryCreated(getApplication())
      _currentStreak.value = com.example.analytics.AppAnalytics.getCurrentStreak(getApplication())
      syncQueueManager.recordDataOperation(
        SyncOperationType.CREATE_MEMORY,
        newMem.id,
        "Voice note: ${newMem.title}"
      )
      _ahaMemory.value = newMem
      _showAhaDialog.value = true
    }
  }

  fun toggleSemanticMode() {
    _semanticState.value = _semanticState.value.copy(
      isSemanticMode = !_semanticState.value.isSemanticMode
    )
  }

  fun executeSemanticSearch(query: String) {
    val q = query.trim()
    if (q.isBlank()) return
    viewModelScope.launch {
      _semanticState.value = _semanticState.value.copy(
        query = q,
        isSearching = true,
        result = null
      )
      val result = repository.semanticSearch(q)
      _semanticState.value = _semanticState.value.copy(
        isSearching = false,
        result = result
      )
    }
  }

  fun clearSemanticSearch() {
    _semanticState.value = _semanticState.value.copy(
      query = "",
      isSearching = false,
      result = null
    )
  }

  fun clearAllData() {
    viewModelScope.launch {
      repository.clearAllData()
      database.recentSearchDao().clearAllRecentSearches()
      _selectedMemory.value = null
      _selectedCollection.value = null
      _askState.value = _askState.value.copy(answer = null)
      _semanticState.value = SemanticSearchUiState()
      _ahaMemory.value = null
      _showAhaDialog.value = false
    }
  }

  // Room Local Storage: Recent Searches
  fun saveRecentSearch(query: String) {
    val clean = query.trim()
    if (clean.isBlank()) return
    viewModelScope.launch {
      database.recentSearchDao().insertSearch(RecentSearchEntity(clean))
    }
  }

  fun deleteRecentSearch(query: String) {
    viewModelScope.launch {
      database.recentSearchDao().deleteSearch(query)
    }
  }

  fun clearAllRecentSearches() {
    viewModelScope.launch {
      database.recentSearchDao().clearAllRecentSearches()
    }
  }

  // Gamification & Engagement Dialog Controls
  fun openWalkthrough() {
    _showWalkthrough.value = true
  }

  fun closeWalkthrough() {
    _showWalkthrough.value = false
  }

  fun openTooltips() {
    _showTooltips.value = true
  }

  fun closeTooltips() {
    _showTooltips.value = false
  }

  fun openStreakMilestone() {
    _showStreakMilestone.value = true
  }

  fun closeStreakMilestone() {
    _showStreakMilestone.value = false
  }

  fun openNpsDialog() {
    _showNpsDialog.value = true
  }

  fun closeNpsDialog() {
    _showNpsDialog.value = false
    com.example.analytics.AppAnalytics.markNpsDismissed(getApplication())
  }

  fun submitNpsRating(score: Int, notes: String) {
    viewModelScope.launch(Dispatchers.IO) {
      npsDao.insertFeedback(
        com.example.data.model.NpsFeedbackEntity(
          rating = score,
          feedbackText = notes,
          submittedAt = System.currentTimeMillis()
        )
      )
      com.example.analytics.AppAnalytics.recordNpsSubmitted(getApplication(), score, notes)
      _snackbarMessage.value = "Thank you for your rating of $score/10!"
    }
  }

  fun openCameraDialog() {
    _showCameraDialog.value = true
  }

  fun closeCameraDialog() {
    _showCameraDialog.value = false
  }

  fun onPhotoCaptured(file: java.io.File) {
    _lastCapturedPhoto.value = file
    _showCameraDialog.value = false
    val timeFormatted = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.US).format(java.util.Date())
    saveNewMemory(
      rawText = "Captured photo memory on $timeFormatted (${file.name})",
      memoryType = "PHOTO",
      category = "Personal"
    )
    _snackbarMessage.value = "Photo memory saved to intelligence database!"
  }

  fun openPrePermissionDialog(type: PermissionType) {
    _pendingPermissionType.value = type
    _showPrePermissionDialog.value = true
  }

  fun closePrePermissionDialog() {
    _showPrePermissionDialog.value = false
  }

  fun openSupportEmailDialog() {
    _showSupportEmailDialog.value = true
  }

  fun closeSupportEmailDialog() {
    _showSupportEmailDialog.value = false
  }

  fun openLegalDialog(tab: LegalTab = LegalTab.PRIVACY_POLICY) {
    _legalDialogTab.value = tab
    _showLegalDialog.value = true
  }

  fun closeLegalDialog() {
    _showLegalDialog.value = false
  }

  fun openClearAllConfirmation() {
    _showClearAllConfirmation.value = true
  }

  fun closeClearAllConfirmation() {
    _showClearAllConfirmation.value = false
  }

  fun openClearDatabaseConfirmation() {
    _showClearDatabaseConfirmation.value = true
  }

  fun closeClearDatabaseConfirmation() {
    _showClearDatabaseConfirmation.value = false
  }

  fun requestVoiceRecording(context: android.content.Context) {
    val hasMic = androidx.core.content.ContextCompat.checkSelfPermission(
      context,
      android.Manifest.permission.RECORD_AUDIO
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    if (hasMic) {
      openVoiceDialog()
    } else {
      openPrePermissionDialog(PermissionType.MICROPHONE)
    }
  }

  fun requestCameraCapture(context: android.content.Context) {
    val hasCamera = androidx.core.content.ContextCompat.checkSelfPermission(
      context,
      android.Manifest.permission.CAMERA
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    if (hasCamera) {
      openCameraDialog()
    } else {
      openPrePermissionDialog(PermissionType.CAMERA)
    }
  }

  fun saveNewMemory(
    rawText: String,
    memoryType: String,
    category: String = "Personal",
    tags: String = ""
  ) {
    viewModelScope.launch {
      val newMem = repository.createMemoryWithAI(
        rawText = rawText,
        memoryType = memoryType,
        category = category,
        userTags = tags
      )
      clearDraft()
      _showAddSheet.value = false
      com.example.analytics.AppAnalytics.recordMemoryCreated(getApplication())
      _currentStreak.value = com.example.analytics.AppAnalytics.getCurrentStreak(getApplication())
      streakRepository.recordDailyActiveUsage(isMemoryCreated = true)
      syncQueueManager.recordDataOperation(
        SyncOperationType.CREATE_MEMORY,
        newMem.id,
        "Created: ${newMem.title}"
      )
      _ahaMemory.value = newMem
      _showAhaDialog.value = true
    }
  }

  fun openAuthDialog(mode: AuthMode = AuthMode.SIGN_IN) {
    _authDialogMode.value = mode
    _showAuthDialog.value = true
  }

  fun closeAuthDialog() {
    _showAuthDialog.value = false
  }

  fun signInSupabase(email: String, pass: String) {
    viewModelScope.launch {
      val res = SupabaseAuthManager.signIn(getApplication(), email, pass)
      if (res.isSuccess) {
        _showAuthDialog.value = false
        _snackbarMessage.value = "Welcome back, ${res.getOrNull()?.fullName ?: email}!"
        syncToSupabase()
      }
    }
  }

  fun signUpSupabase(email: String, pass: String, fullName: String) {
    viewModelScope.launch {
      val res = SupabaseAuthManager.signUp(getApplication(), email, pass, fullName)
      if (res.isSuccess) {
        _showAuthDialog.value = false
        _snackbarMessage.value = "Account created successfully! Syncing vault..."
        syncToSupabase()
      }
    }
  }

  fun signOutSupabase() {
    SupabaseAuthManager.signOut(getApplication())
    _snackbarMessage.value = "Signed out from Cloud Vault."
  }

  fun openQuestionnaire() {
    _showOnboardingQuestionnaire.value = true
  }

  fun closeQuestionnaire() {
    _showOnboardingQuestionnaire.value = false
  }

  fun completeQuestionnaire(goal: String, reminderHour: Int, isCloudEnabled: Boolean, vaultName: String) {
    _showOnboardingQuestionnaire.value = false
    viewModelScope.launch {
      val profile = com.example.data.model.UserProfileEntity(
        userId = "local_user",
        primaryGoal = goal,
        privacySensitivity = if (isCloudEnabled) "ENCRYPTED_CLOUD" else "LOCAL_FIRST",
        primaryExpectation = vaultName,
        hasCompletedOnboarding = true,
        lastPersonalizedAt = System.currentTimeMillis()
      )
      repository.saveUserProfile(profile)
    }
    if (reminderHour >= 0) {
      setDailyRemindersEnabled(true)
      setReminderTime(reminderHour, 0)
    }
    _snackbarMessage.value = "Welcome to your personalized $vaultName!"
  }

  fun syncToSupabase() {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      val list = repository.allMemories.first()
      SupabaseDatabaseManager.syncMemories(user.id, list)
    }
  }
}
