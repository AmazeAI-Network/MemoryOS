package com.example.ui.screens

import android.app.Activity
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.BuildConfig
import com.example.feedback.FeedbackManager
import com.example.feedback.FeedbackPayload
import com.example.sync.SyncQueueDialog
import com.example.sync.SyncQueueManager
import com.example.sync.SyncState
import com.example.updater.AppUpdateInfo
import com.example.updater.AppUpdater
import com.example.updater.UpdateDialog
import com.example.updater.UpdateDownloadState
import com.example.worker.MaintenanceScheduler
import com.example.worker.MaintenanceStats
import com.example.ui.components.BatteryOptimizationDialog
import com.example.ui.components.IconlyIcons
import com.example.ui.components.MemoryOSEmblem
import com.example.ui.theme.*
import com.example.ui.viewmodel.LanguageOption
import com.example.ui.viewmodel.SettingsViewModel
import com.example.util.BatteryOptimizationHelper
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: SettingsViewModel = viewModel()
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  var showLanguageDialog by remember { mutableStateOf(false) }
  var showThemeDialog by remember { mutableStateOf(false) }
  var showClearConfirmDialog by remember { mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()
  val feedbackManager = remember { FeedbackManager(context) }
  val appUpdater = remember { AppUpdater(context) }
  val syncQueueManager = remember { SyncQueueManager.getInstance(context) }
  val queuedOps by syncQueueManager.queuedOperations.collectAsStateWithLifecycle()
  val syncState by syncQueueManager.syncState.collectAsStateWithLifecycle()
  var showSyncQueueDialog by remember { mutableStateOf(false) }

  var maintenanceStats by remember { mutableStateOf(MaintenanceScheduler.getLastMaintenanceStats(context)) }
  var isRunningCleanup by remember { mutableStateOf(false) }

  var showFeedbackDialog by remember { mutableStateOf(false) }
  var screenshotBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var isSubmittingFeedback by remember { mutableStateOf(false) }

  var manualUpdateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
  var manualDownloadState by remember { mutableStateOf<UpdateDownloadState>(UpdateDownloadState.Idle) }
  var showManualUpdateDialog by remember { mutableStateOf(false) }

  BackHandler {
    onNavigateBack()
  }

  LaunchedEffect(uiState.actionFeedback) {
    uiState.actionFeedback?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      viewModel.dismissFeedback()
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(WarmCream),
    containerColor = WarmCream,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Preferences & Settings",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
              ),
              color = BrandBlack
            )
            Text(
              text = "MemoryOS Personal Space",
              style = MaterialTheme.typography.labelSmall,
              color = InkMuted
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("settings_back_button")
          ) {
            Icon(
              imageVector = IconlyIcons.ArrowBack,
              contentDescription = "Navigate back",
              tint = BrandBlack
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = WarmCream
        )
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Regional & Language Preferences (World Languages)
      item {
        SettingsSectionHeader(title = "Language & Region")
        SettingsCard {
          SettingsClickableRow(
            icon = IconlyIcons.Search,
            title = "Display & Transcription Language",
            subtitle = uiState.selectedLanguage,
            badge = "17 Languages",
            onClick = { showLanguageDialog = true },
            testTag = "settings_language_selector"
          )
        }
      }

      // 2. Appearance & Atmosphere (Monochromatic + Cream)
      item {
        SettingsSectionHeader(title = "Appearance")
        SettingsCard {
          SettingsClickableRow(
            icon = IconlyIcons.Premium,
            title = "Color Theme",
            subtitle = uiState.themeMode,
            badge = "Bespoke Palette",
            onClick = { showThemeDialog = true },
            testTag = "settings_theme_selector"
          )
        }
      }

      // 3. AI Capture & Intelligence Pipeline
      item {
        SettingsSectionHeader(title = "Capture & Intelligence")
        SettingsCard {
          SettingsSwitchRow(
            icon = IconlyIcons.Document,
            title = "Instant OCR Extraction",
            subtitle = "Extract text, merchants, and totals automatically",
            checked = uiState.autoOcrEnabled,
            onCheckedChange = { viewModel.toggleAutoOcr() }
          )
          HorizontalDivider(color = WarmCreamBorder, thickness = 0.5.dp)
          SettingsSwitchRow(
            icon = IconlyIcons.Voice,
            title = "High-Fidelity Audio",
            subtitle = "Lossless speech capture for enhanced voice search",
            checked = uiState.audioHighQuality,
            onCheckedChange = { viewModel.toggleAudioQuality() }
          )
          HorizontalDivider(color = WarmCreamBorder, thickness = 0.5.dp)
          SettingsSwitchRow(
            icon = IconlyIcons.Heart,
            title = "Tactile Haptic Feedback",
            subtitle = "Vibrate on capture confirmations and bookmarks",
            checked = uiState.hapticFeedbackEnabled,
            onCheckedChange = { viewModel.toggleHapticFeedback() }
          )
        }
      }

      // 4. Security & Privacy Vault
      item {
        SettingsSectionHeader(title = "Privacy & Vault Isolation")
        SettingsCard {
          SettingsSwitchRow(
            icon = IconlyIcons.Lock,
            title = "Local-Only Offline Vault",
            subtitle = "Never ping external servers; keep memories on-device",
            checked = uiState.offlineVaultMode,
            onCheckedChange = { viewModel.toggleOfflineVault() }
          )
          HorizontalDivider(color = WarmCreamBorder, thickness = 0.5.dp)
          SettingsSwitchRow(
            icon = IconlyIcons.Shield,
            title = "Biometric Lock",
            subtitle = "Require fingerprint or passcode on app launch",
            checked = uiState.biometricLockEnabled,
            onCheckedChange = { viewModel.toggleBiometricLock() }
          )
        }
      }

      // 5. Local Database Management (Room)
      item {
        SettingsSectionHeader(title = "Local Storage (Room Database)")
        SettingsCard {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Local Storage Status",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = BrandBlack
                )
                Text(
                  text = "${uiState.totalMemoriesCount} memories • ${uiState.totalCollectionsCount} collections",
                  style = MaterialTheme.typography.bodySmall,
                  color = InkSecondary
                )
              }
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = WarmCreamDark,
                border = BorderStroke(1.dp, WarmCreamBorder)
              ) {
                Text(
                  text = "~${uiState.estimatedStorageKb} KB",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = BrandBlack,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
              onClick = { showClearConfirmDialog = true },
              shape = RoundedCornerShape(20.dp),
              border = BorderStroke(1.dp, TerracottaRed.copy(alpha = 0.5f)),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TerracottaRed
              ),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("settings_clear_storage_button")
            ) {
              Icon(
                imageVector = IconlyIcons.Delete,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = TerracottaRed
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Clear Local Memory Database", fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }

      // 6. Independent Distribution & Diagnostics
      item {
        SettingsSectionHeader(title = "Independent Distribution & Maintenance")
        SettingsCard {
          SettingsClickableRow(
            icon = IconlyIcons.Refresh,
            title = "Check for Updates",
            subtitle = "Manual in-app updater (off-store direct release)",
            badge = "v${BuildConfig.VERSION_NAME}",
            onClick = {
              Toast.makeText(context, "Checking for latest release...", Toast.LENGTH_SHORT).show()
              coroutineScope.launch {
                val res = appUpdater.checkForUpdate()
                val info = res.getOrNull()
                if (info != null) {
                  manualUpdateInfo = info
                  showManualUpdateDialog = true
                } else {
                  Toast.makeText(context, "MemoryOS is up to date (v${BuildConfig.VERSION_NAME})", Toast.LENGTH_SHORT).show()
                }
              }
            },
            testTag = "settings_check_updates"
          )

          HorizontalDivider(color = WarmCreamBorder, thickness = 0.5.dp)

          SettingsClickableRow(
            icon = IconlyIcons.Document,
            title = "Report Bug & Send Feedback",
            subtitle = "Includes real-time screen capture & hardware specs",
            badge = "Decentralized",
            onClick = {
              coroutineScope.launch {
                val activity = context as? Activity
                if (activity != null) {
                  screenshotBitmap = feedbackManager.captureActivityScreenshot(activity)
                }
                showFeedbackDialog = true
              }
            },
            testTag = "settings_bug_report"
          )

          HorizontalDivider(color = WarmCreamBorder, thickness = 0.5.dp)

          SettingsClickableRow(
            icon = IconlyIcons.Shield,
            title = "Hardware Keystore Encryption",
            subtitle = "Hardware-backed AES-256 GCM encrypted storage",
            badge = "Active",
            onClick = {
              Toast.makeText(context, "Tokens and credentials are encrypted using hardware-backed Android KeyStore", Toast.LENGTH_LONG).show()
            },
            testTag = "settings_security_info"
          )

          HorizontalDivider(color = WarmCreamBorder, thickness = 0.5.dp)

          // Offline Sync Queue & Cloud Reconciliation Status
          SettingsClickableRow(
            icon = IconlyIcons.Folder,
            title = "Offline Sync Vault & Queue",
            subtitle = if (queuedOps.isEmpty()) "All local changes synced with cloud" else "${queuedOps.size} data operations waiting to sync",
            badge = if (queuedOps.isEmpty()) "Synced" else "${queuedOps.size} Queued",
            onClick = { showSyncQueueDialog = true },
            testTag = "settings_sync_queue"
          )

          HorizontalDivider(color = WarmCreamBorder, thickness = 0.5.dp)

          // WorkManager Periodic Log & Temporary Cache Cleanup Task
          SettingsClickableRow(
            icon = IconlyIcons.Refresh,
            title = "Cache & Log Cleanup (WorkManager)",
            subtitle = if (isRunningCleanup) "Running storage cleanup task..." else "Last run: ${maintenanceStats.formattedLastRun} • Reclaimed: ${maintenanceStats.formattedBytesReclaimed}",
            badge = if (isRunningCleanup) "Cleaning..." else "24h Active",
            onClick = {
              if (!isRunningCleanup) {
                isRunningCleanup = true
                Toast.makeText(context, "Running WorkManager storage & cache cleanup...", Toast.LENGTH_SHORT).show()
                coroutineScope.launch {
                  MaintenanceScheduler.triggerImmediateMaintenance(context).collect { info ->
                    if (info?.state?.isFinished == true) {
                      isRunningCleanup = false
                      val updated = MaintenanceScheduler.getLastMaintenanceStats(context)
                      maintenanceStats = updated
                      Toast.makeText(
                        context,
                        "Storage cleaned: Reclaimed ${updated.formattedBytesReclaimed} (${updated.lastFilesRemoved} files)",
                        Toast.LENGTH_LONG
                      ).show()
                    }
                  }
                }
              }
            },
            testTag = "settings_run_maintenance"
          )
        }
      }

      // 7. Brand Footer
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "MemoryOS",
            fontFamily = MemoryScriptFontFamily,
            fontSize = 20.sp,
            color = BrandBlack
          )
          Text(
            text = "Personal Memory Intelligence • Local Room DB v2",
            style = MaterialTheme.typography.labelSmall,
            color = InkMuted
          )
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Language Selection Dialog (World Languages)
  if (showLanguageDialog) {
    AlertDialog(
      onDismissRequest = { showLanguageDialog = false },
      containerColor = PureWhite,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = IconlyIcons.Search,
            contentDescription = null,
            tint = BrandBlack,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Select Language",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = BrandBlack
          )
        }
      },
      text = {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(viewModel.supportedLanguages) { lang ->
            val isSelected = lang.displayName == uiState.selectedLanguage
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) WarmCreamDark else PureWhite,
              border = BorderStroke(1.dp, if (isSelected) BrandBlack else Color.Transparent),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.selectLanguage(lang.displayName)
                  showLanguageDialog = false
                }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = lang.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = BrandBlack
                  )
                  Text(
                    text = "${lang.nativeName} • ${lang.region}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                  )
                }
                if (isSelected) {
                  Icon(
                    imageVector = IconlyIcons.Verified,
                    contentDescription = "Selected",
                    tint = BrandBlack,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showLanguageDialog = false }) {
          Text("Cancel", color = BrandBlack, fontWeight = FontWeight.Bold)
        }
      }
    )
  }

  // Theme Selection Dialog
  if (showThemeDialog) {
    AlertDialog(
      onDismissRequest = { showThemeDialog = false },
      containerColor = PureWhite,
      title = {
        Text(
          text = "Select Appearance",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = BrandBlack
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          viewModel.themeOptions.forEach { option ->
            val isSelected = option == uiState.themeMode
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) WarmCreamDark else PureWhite,
              border = BorderStroke(1.dp, if (isSelected) BrandBlack else WarmCreamBorder),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.selectThemeMode(option)
                  showThemeDialog = false
                }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = option,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = BrandBlack
                )
                if (isSelected) {
                  Icon(
                    imageVector = IconlyIcons.Verified,
                    contentDescription = "Selected",
                    tint = BrandBlack,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showThemeDialog = false }) {
          Text("Close", color = BrandBlack, fontWeight = FontWeight.Bold)
        }
      }
    )
  }

  // Clear Storage Confirmation Dialog
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      containerColor = PureWhite,
      title = {
        Text(
          text = "Purge Local Database?",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = TerracottaRed
        )
      },
      text = {
        Text(
          text = "This will permanently remove all stored local memories and reset Room tables. This action cannot be reversed.",
          style = MaterialTheme.typography.bodyMedium,
          color = InkSecondary
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.clearAllMemories {
              showClearConfirmDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = TerracottaRed),
          shape = RoundedCornerShape(20.dp)
        ) {
          Text("Yes, Purge Vault", color = PureWhite, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel", color = BrandBlack)
        }
      }
    )
  }

  // Feedback & Bug Reporting Dialog
  if (showFeedbackDialog) {
    com.example.ui.screens.FeedbackDialog(
      screenshot = screenshotBitmap,
      isSubmitting = isSubmittingFeedback,
      onSubmit = { category, message, includeScreenshot ->
        isSubmittingFeedback = true
        coroutineScope.launch {
          val screenshotBase64 = if (includeScreenshot && screenshotBitmap != null) {
            feedbackManager.bitmapToBase64(screenshotBitmap!!)
          } else null

          val payload = FeedbackPayload.create(
            context = context,
            category = category,
            message = message,
            screenshotBase64 = screenshotBase64
          )

          val result = feedbackManager.sendReport(payload)
          isSubmittingFeedback = false
          if (result.isSuccess) {
            Toast.makeText(context, "Feedback transmitted successfully. Thank you!", Toast.LENGTH_LONG).show()
            showFeedbackDialog = false
          } else {
            Toast.makeText(context, "Webhook transmission failed. You can use email instead.", Toast.LENGTH_SHORT).show()
          }
        }
      },
      onEmailFallback = { category, message ->
        val file = if (screenshotBitmap != null) feedbackManager.saveScreenshotToFile(screenshotBitmap!!) else null
        val payload = FeedbackPayload.create(context, category, message, null)
        val activity = context as? Activity
        if (activity != null) {
          feedbackManager.shareFeedbackViaEmail(activity, payload, file)
        }
        showFeedbackDialog = false
      },
      onDismiss = { showFeedbackDialog = false }
    )
  }

  // Manual Update Dialog
  if (showManualUpdateDialog && manualUpdateInfo != null) {
    UpdateDialog(
      updateInfo = manualUpdateInfo!!,
      downloadState = manualDownloadState,
      onStartDownload = {
        coroutineScope.launch {
          appUpdater.downloadApk(manualUpdateInfo!!).collect { state ->
            manualDownloadState = state
          }
        }
      },
      onInstallClick = {
        if (manualDownloadState is UpdateDownloadState.DownloadCompleted) {
          val file = File((manualDownloadState as UpdateDownloadState.DownloadCompleted).apkPath)
          appUpdater.triggerInstall(file)
        }
      },
      onRequestPermission = { appUpdater.openInstallPermissionSettings() },
      onDismiss = { showManualUpdateDialog = false },
      hasInstallPermission = appUpdater.canRequestPackageInstalls()
    )
  }

  // Offline Sync Queue Dialog
  if (showSyncQueueDialog) {
    SyncQueueDialog(
      isOnline = syncState != SyncState.OFFLINE_IDLE && syncState != SyncState.OFFLINE_QUEUED,
      syncState = syncState,
      queuedOperations = queuedOps,
      onTriggerSync = { syncQueueManager.triggerSync() },
      onDismiss = { showSyncQueueDialog = false }
    )
  }
}

@Composable
private fun SettingsSectionHeader(title: String) {
  Text(
    text = title.uppercase(),
    style = MaterialTheme.typography.labelSmall.copy(
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.1.sp
    ),
    color = InkMuted,
    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
  )
}

@Composable
private fun SettingsCard(
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    border = BorderStroke(1.dp, WarmCreamBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(content = content)
  }
}

@Composable
private fun SettingsClickableRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  badge: String? = null,
  onClick: () -> Unit,
  testTag: String? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(CircleShape)
        .background(WarmCreamDark),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = BrandBlack,
        modifier = Modifier.size(18.dp)
      )
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = BrandBlack
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = InkSecondary
      )
    }

    if (badge != null) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = WarmCreamDark,
        modifier = Modifier.padding(horizontal = 6.dp)
      ) {
        Text(
          text = badge,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
          color = BrandBlack,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
      }
    }

    Icon(
      imageVector = IconlyIcons.ArrowForward,
      contentDescription = null,
      tint = InkMuted,
      modifier = Modifier.size(16.dp)
    )
  }
}

@Composable
private fun SettingsSwitchRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(CircleShape)
        .background(WarmCreamDark),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = BrandBlack,
        modifier = Modifier.size(18.dp)
      )
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = BrandBlack
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = InkSecondary
      )
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = PureWhite,
        checkedTrackColor = BrandBlack,
        uncheckedThumbColor = InkMuted,
        uncheckedTrackColor = WarmCreamDark
      )
    )
  }
}
