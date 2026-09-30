package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.analytics.AppAnalytics
import com.example.network.NetworkConnectivityObserver
import com.example.notification.NotificationHelper
import com.example.sync.SyncQueueDialog
import com.example.sync.SyncStatusBanner
import com.example.ui.components.AddMemorySheet
import com.example.ui.components.AhaMomentResultDialog
import com.example.ui.components.AmbientFloatingOrbs
import com.example.ui.components.AuthDialog
import com.example.ui.components.ClearConfirmationDialog
import com.example.ui.components.FloatingBottomBar
import com.example.ui.components.HelpFeedbackSheet
import com.example.ui.components.InteractiveTooltipDialog
import com.example.ui.components.LegalDocumentsDialog
import com.example.ui.components.NpsFeedbackDialog
import com.example.ui.components.OfflineBanner
import com.example.ui.components.OnboardingQuestionnaireDialog
import com.example.ui.components.PrePermissionPromptDialog
import com.example.ui.components.ProPaywallDialog
import com.example.ui.components.StreakMilestoneDialog
import com.example.ui.components.SupportEmailDialog
import com.example.ui.components.VoiceMemoryDialog
import com.example.ui.components.WelcomeWalkthroughDialog
import com.example.ui.components.blurBackground
import com.example.ui.navigation.FastPageTransition
import com.example.ui.screens.AskScreen
import com.example.ui.screens.CollectionDetailScreen
import com.example.ui.screens.CollectionsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoriesScreen
import com.example.ui.screens.MemoryDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.MemoryViewModel
import com.example.ui.viewmodel.Screen
import com.example.updater.AppUpdateInfo
import com.example.updater.AppUpdater
import com.example.updater.UpdateDialog
import com.example.updater.UpdateDownloadState
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
  private var shouldOpenVoiceOnLaunch = false

  override fun onCreate(savedInstanceState: Bundle?) {
    // 1. Android 12+ Backward-Compatible Splash Screen API
    val splashScreen = installSplashScreen()

    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    shouldOpenVoiceOnLaunch = intent?.getBooleanExtra(NotificationHelper.EXTRA_OPEN_VOICE_CAPTURE, false) == true

    setContent {
      MyApplicationTheme {
        MemoryApp(initialOpenVoice = shouldOpenVoiceOnLaunch)
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    if (intent.getBooleanExtra(NotificationHelper.EXTRA_OPEN_VOICE_CAPTURE, false)) {
      shouldOpenVoiceOnLaunch = true
    }
  }
}

@Composable
fun MemoryApp(
  initialOpenVoice: Boolean = false,
  viewModel: MemoryViewModel = viewModel()
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
  val showAddSheet by viewModel.showAddSheet.collectAsStateWithLifecycle()
  val showVoiceDialog by viewModel.showVoiceDialog.collectAsStateWithLifecycle()
  val showPaywallSheet by viewModel.showPaywallSheet.collectAsStateWithLifecycle()
  val showAhaDialog by viewModel.showAhaDialog.collectAsStateWithLifecycle()
  val ahaMemory by viewModel.ahaMemory.collectAsStateWithLifecycle()
  val showHelpSheet by viewModel.showHelpSheet.collectAsStateWithLifecycle()
  val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
  val recentlyDeleted by viewModel.recentlyDeletedMemory.collectAsStateWithLifecycle()

  val showWalkthrough by viewModel.showWalkthrough.collectAsStateWithLifecycle()
  val showTooltips by viewModel.showTooltips.collectAsStateWithLifecycle()
  val showStreakMilestone by viewModel.showStreakMilestone.collectAsStateWithLifecycle()
  val showNpsDialog by viewModel.showNpsDialog.collectAsStateWithLifecycle()
  val showPrePermissionDialog by viewModel.showPrePermissionDialog.collectAsStateWithLifecycle()
  val pendingPermissionType by viewModel.pendingPermissionType.collectAsStateWithLifecycle()
  val showSupportEmailDialog by viewModel.showSupportEmailDialog.collectAsStateWithLifecycle()
  val showLegalDialog by viewModel.showLegalDialog.collectAsStateWithLifecycle()
  val legalDialogTab by viewModel.legalDialogTab.collectAsStateWithLifecycle()
  val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()

  // Supabase Auth and Onboarding States
  val showAuthDialog by viewModel.showAuthDialog.collectAsStateWithLifecycle()
  val showCameraDialog by viewModel.showCameraDialog.collectAsStateWithLifecycle()
  val authDialogMode by viewModel.authDialogMode.collectAsStateWithLifecycle()
  val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
  val authError by viewModel.authError.collectAsStateWithLifecycle()
  val showOnboardingQuestionnaire by viewModel.showOnboardingQuestionnaire.collectAsStateWithLifecycle()

  var showSplashScreen by remember { mutableStateOf(true) }

  LaunchedEffect(Unit) {
    kotlinx.coroutines.delay(1200)
    showSplashScreen = false
    if (AppAnalytics.isFirstLaunch(context)) {
      viewModel.openWalkthrough()
    }
  }

  // --- Pillar 3: Global Network State Listener & Sync Queue Manager ---
  val networkObserver = remember { NetworkConnectivityObserver(context) }
  DisposableEffect(networkObserver) {
    onDispose {
      networkObserver.unregister()
    }
  }
  val isConnected by networkObserver.isConnected.collectAsStateWithLifecycle()
  val syncState by viewModel.syncState.collectAsStateWithLifecycle()
  val queuedOps by viewModel.queuedOperations.collectAsStateWithLifecycle()
  var showSyncQueueDialog by remember { mutableStateOf(false) }

  LaunchedEffect(isConnected) {
    viewModel.updateNetworkOnlineStatus(isConnected)
  }

  // --- Pillar 1: In-App Auto-Updater ---
  val appUpdater = remember { AppUpdater(context) }
  var updateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
  var downloadState by remember { mutableStateOf<UpdateDownloadState>(UpdateDownloadState.Idle) }
  var showUpdateDialog by remember { mutableStateOf(false) }

  // Check for updates on startup (non-intrusively in background)
  LaunchedEffect(Unit) {
    val result = appUpdater.checkForUpdate()
    result.getOrNull()?.let { info ->
      updateInfo = info
      showUpdateDialog = true
    }
  }

  val isAnyModalActive = showAddSheet || showVoiceDialog || showPaywallSheet || showAhaDialog ||
    showHelpSheet || showUpdateDialog || showSyncQueueDialog || showWalkthrough ||
    showTooltips || showStreakMilestone || showNpsDialog || showPrePermissionDialog ||
    showSupportEmailDialog || showLegalDialog || showOnboardingQuestionnaire || showAuthDialog || showCameraDialog

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(snackbarMessage) {
    snackbarMessage?.let { msg ->
      val result = snackbarHostState.showSnackbar(
        message = msg,
        actionLabel = if (recentlyDeleted != null) "Undo" else null,
        duration = SnackbarDuration.Short
      )
      if (result == SnackbarResult.ActionPerformed) {
        viewModel.undoDelete()
      }
      viewModel.dismissSnackbar()
    }
  }

  LaunchedEffect(Unit) {
    if (initialOpenVoice) {
      viewModel.openVoiceDialog()
    }
  }

  // Handle hardware back button
  BackHandler(
    enabled = currentScreen is Screen.MemoryDetail || currentScreen is Screen.CollectionDetail || currentScreen is Screen.Settings
  ) {
    if (currentScreen is Screen.MemoryDetail) {
      viewModel.navigateTo(Screen.Home)
    } else if (currentScreen is Screen.CollectionDetail) {
      viewModel.navigateTo(Screen.Collections)
    } else if (currentScreen is Screen.Settings) {
      viewModel.navigateTo(Screen.Profile)
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(WarmIvory)
  ) {
    val deviceProfile = remember { com.example.performance.DevicePerformanceManager.getDeviceProfile(context) }

    // Ambient floating subtle orbs for motion aesthetics (disabled on low-end devices to save GPU fill rate)
    if (!deviceProfile.isLowEndTier) {
      AmbientFloatingOrbs(modifier = Modifier.fillMaxSize())
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .blurBackground(isAnyModalActive, 16.dp, canBlur = deviceProfile.canRenderHardwareBlur)
    ) {
      // Fast, hardware-accelerated Screen Content Transition (< 300ms, 60-120 FPS)
      Box(modifier = Modifier.weight(1f)) {
        FastPageTransition(
          targetScreen = currentScreen
        ) { screen ->
          when (screen) {
            Screen.Home -> HomeScreen(viewModel = viewModel)
            Screen.Ask -> AskScreen(viewModel = viewModel)
            Screen.Memories -> MemoriesScreen(viewModel = viewModel)
            Screen.Collections -> CollectionsScreen(viewModel = viewModel)
            Screen.Profile -> ProfileScreen(viewModel = viewModel)
            Screen.Settings -> SettingsScreen(onNavigateBack = { viewModel.navigateTo(Screen.Profile) })
            is Screen.MemoryDetail -> MemoryDetailScreen(viewModel = viewModel)
            is Screen.CollectionDetail -> CollectionDetailScreen(viewModel = viewModel)
          }
        }
      }
    }

    // Floating Bottom Bar (visible on top-level tabs)
    val showBottomBar = currentScreen !is Screen.MemoryDetail && currentScreen !is Screen.CollectionDetail && currentScreen !is Screen.Settings
    if (showBottomBar) {
      FloatingBottomBar(
        currentScreen = currentScreen,
        onNavigate = { viewModel.navigateTo(it) },
        onAddClick = { viewModel.openAddSheet() },
        modifier = Modifier.align(Alignment.BottomCenter)
      )
    }

    // Snackbar Host for Undo notifications
    SnackbarHost(
      hostState = snackbarHostState,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = if (showBottomBar) 90.dp else 16.dp)
        .padding(horizontal = 16.dp)
    )

    // Add Memory Sheet / Modal
    if (showAddSheet) {
      AddMemorySheet(
        viewModel = viewModel,
        onDismiss = { viewModel.closeAddSheet() },
        onSave = { rawText, type, category, tags ->
          viewModel.saveNewMemory(rawText, type, category, tags)
        }
      )
    }

    // Voice Memory Recording Dialog
    if (showVoiceDialog) {
      VoiceMemoryDialog(
        onDismiss = { viewModel.closeVoiceDialog() },
        onSaveSnippet = { transcribedText ->
          viewModel.saveVoiceSnippet(transcribedText)
        }
      )
    }

    // Pro Paywall Dialog (Coming soon preview)
    if (showPaywallSheet) {
      ProPaywallDialog(
        onDismiss = { viewModel.closePaywall() }
      )
    }

    // Aha Moment Result celebration dialog
    if (showAhaDialog && ahaMemory != null) {
      AhaMomentResultDialog(
        memory = ahaMemory!!,
        onDismiss = { viewModel.closeAhaDialog() },
        onTryAsk = { query ->
          viewModel.navigateTo(Screen.Ask)
          viewModel.submitAskQuestion(query)
        }
      )
    }

    // Contextual Help & FAQ Sheet
    if (showHelpSheet) {
      HelpFeedbackSheet(
        onDismiss = { viewModel.closeHelpSheet() }
      )
    }

    // In-App Update Dialog
    if (showUpdateDialog && updateInfo != null) {
      UpdateDialog(
        updateInfo = updateInfo!!,
        downloadState = downloadState,
        onStartDownload = {
          coroutineScope.launch {
            appUpdater.downloadApk(updateInfo!!).collect { state ->
              downloadState = state
            }
          }
        },
        onInstallClick = {
          if (downloadState is UpdateDownloadState.DownloadCompleted) {
            val file = File((downloadState as UpdateDownloadState.DownloadCompleted).apkPath)
            appUpdater.triggerInstall(file)
          }
        },
        onRequestPermission = { appUpdater.openInstallPermissionSettings() },
        onDismiss = { showUpdateDialog = false },
        hasInstallPermission = appUpdater.canRequestPackageInstalls()
      )
    }

    // Offline Sync Queue Dialog
    if (showSyncQueueDialog) {
      SyncQueueDialog(
        isOnline = isConnected,
        syncState = syncState,
        queuedOperations = queuedOps,
        onTriggerSync = { viewModel.triggerManualSync() },
        onDismiss = { showSyncQueueDialog = false }
      )
    }

    // Welcome Walkthroughs Pop-up (swipeable cards explaining core value proposition)
    if (showWalkthrough) {
      WelcomeWalkthroughDialog(
        onDismiss = {
          viewModel.closeWalkthrough()
          AppAnalytics.markFirstLaunchComplete(context)
        }
      )
    }

    // Interactive Tooltips Pop-up (feature walkthrough & learning by doing)
    if (showTooltips) {
      InteractiveTooltipDialog(
        onDismiss = { viewModel.closeTooltips() }
      )
    }

    // Streak Milestones Pop-up (gamified streak celebrations)
    if (showStreakMilestone) {
      StreakMilestoneDialog(
        streakDays = currentStreak,
        onDismiss = { viewModel.closeStreakMilestone() }
      )
    }

    // Net Promoter Score (NPS) Pop-up (1-to-10 scale rating, triggered after 7 days)
    if (showNpsDialog) {
      NpsFeedbackDialog(
        onDismiss = { viewModel.closeNpsDialog() },
        onSubmitRating = { score, notes ->
          viewModel.submitNpsRating(score, notes)
          viewModel.closeNpsDialog()
        }
      )
    }

    // Pre-Permission "Soft Prompts" Pop-up
    if (showPrePermissionDialog) {
      val micLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
      ) { isGranted ->
        if (isGranted) {
          viewModel.openVoiceDialog()
        }
      }

      val notifLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
      ) { isGranted ->
        viewModel.setDailyRemindersEnabled(isGranted)
      }

      val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
      ) { isGranted ->
        if (isGranted) {
          viewModel.openCameraDialog()
        } else {
          android.widget.Toast.makeText(context, "Camera permission is required to capture photos", android.widget.Toast.LENGTH_SHORT).show()
        }
      }

      PrePermissionPromptDialog(
        type = pendingPermissionType,
        onAllow = {
          viewModel.closePrePermissionDialog()
          when (pendingPermissionType) {
            com.example.ui.components.PermissionType.MICROPHONE -> {
              micLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
            com.example.ui.components.PermissionType.NOTIFICATIONS -> {
              if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
              } else {
                viewModel.setDailyRemindersEnabled(true)
              }
            }
            com.example.ui.components.PermissionType.CAMERA -> {
              cameraLauncher.launch(android.Manifest.permission.CAMERA)
            }
          }
        },
        onDismiss = { viewModel.closePrePermissionDialog() }
      )
    }

    // Support Email Dialog (compose and send directly to bintangjanuarda0809@gmail.com)
    if (showSupportEmailDialog) {
      SupportEmailDialog(
        onDismiss = { viewModel.closeSupportEmailDialog() }
      )
    }

    // Legal Documents Dialog (Privacy Policy and Terms & Conditions)
    if (showLegalDialog) {
      LegalDocumentsDialog(
        initialTab = legalDialogTab,
        onDismiss = { viewModel.closeLegalDialog() }
      )
    }

    // First-Time User Onboarding Questionnaire
    if (showOnboardingQuestionnaire) {
      OnboardingQuestionnaireDialog(
        onComplete = { goal, hour, isCloud, vaultName ->
          viewModel.completeQuestionnaire(goal, hour, isCloud, vaultName)
        },
        onDismiss = { viewModel.closeQuestionnaire() }
      )
    }

    // Supabase Auth Dialog (Sign In & Sign Up)
    if (showAuthDialog) {
      AuthDialog(
        initialMode = authDialogMode,
        isLoading = isAuthLoading,
        errorMessage = authError,
        onDismiss = { viewModel.closeAuthDialog() },
        onSignIn = { email, pass -> viewModel.signInSupabase(email, pass) },
        onSignUp = { email, pass, name -> viewModel.signUpSupabase(email, pass, name) }
      )
    }

    // CameraX Photo Capture Dialog
    if (showCameraDialog) {
      com.example.ui.components.CameraCaptureDialog(
        onDismiss = { viewModel.closeCameraDialog() },
        onImageCaptured = { file -> viewModel.onPhotoCaptured(file) }
      )
    }

    // Master Splash Screen (displays the complete logo matching the splash design)
    androidx.compose.animation.AnimatedVisibility(
      visible = showSplashScreen,
      enter = androidx.compose.animation.fadeIn(),
      exit = androidx.compose.animation.fadeOut(
        animationSpec = androidx.compose.animation.core.tween(400)
      )
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(WarmIvory),
        contentAlignment = Alignment.Center
      ) {
        androidx.compose.foundation.Image(
          painter = androidx.compose.ui.res.painterResource(id = R.drawable.memoryos_complete_logo),
          contentDescription = "MemoryOS Logo",
          modifier = Modifier
            .width(260.dp)
            .padding(24.dp),
          contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
      }
    }
  }
}
