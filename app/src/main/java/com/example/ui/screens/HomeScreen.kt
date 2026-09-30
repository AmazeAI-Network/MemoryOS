package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CollectionEntity
import com.example.data.model.MemoryEntity
import com.example.sync.SyncQueueDialog
import com.example.sync.SyncState
import com.example.sync.SyncStatusChip
import com.example.ui.components.IconlyIcons
import com.example.ui.components.MemoryCard
import com.example.ui.components.MemoryOSEmblem
import com.example.ui.components.MemoryOSBrandHeader
import com.example.ui.components.EmptyState
import com.example.ui.components.EmptyStateTip
import com.example.ui.components.UserStreakDisplayComponent
import com.example.ui.components.bouncingClickable
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.BrandWhite
import com.example.ui.theme.MemoryScriptFontFamily
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmCreamLight
import com.example.ui.theme.WarmCreamBorder
import com.example.ui.theme.WarmCreamBorderSubtle
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkMuted
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.DustyBlueLight
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.MemoryViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(
  viewModel: MemoryViewModel,
  modifier: Modifier = Modifier
) {
  val memories by viewModel.allMemories.collectAsStateWithLifecycle()
  val collections by viewModel.collections.collectAsStateWithLifecycle()
  val isPro by viewModel.isProUser.collectAsStateWithLifecycle()
  val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
  val streakState by viewModel.streakState.collectAsStateWithLifecycle()
  val syncState by viewModel.syncState.collectAsStateWithLifecycle()
  val queuedOps by viewModel.queuedOperations.collectAsStateWithLifecycle()
  var showSyncDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
  val isOnline = syncState != SyncState.OFFLINE_IDLE && syncState != SyncState.OFFLINE_QUEUED
  val context = androidx.compose.ui.platform.LocalContext.current

  val greeting = androidx.compose.runtime.remember {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    when (hour) {
      in 4..11 -> "Good morning,"
      in 12..17 -> "Good afternoon,"
      else -> "Good evening,"
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(WarmIvory)
        .statusBarsPadding(),
      contentPadding = PaddingValues(bottom = 100.dp)
    ) {
    // Top Bar & Title
    item {
      Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          MemoryOSBrandHeader(
            logoSize = 34.dp,
            textColor = BrandBlack,
            showTagline = true
          )

          // Streak, Pro / Upgrade, Help & Profile Avatar
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Streak Badge
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DustyBlueLight)
                .bouncingClickable(pressedScale = 0.92f) { viewModel.openStreakMilestone() }
                .padding(horizontal = 8.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = IconlyIcons.Premium,
                contentDescription = null,
                tint = DustyBlue,
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = "${streak}d",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                ),
                color = InkPrimary,
                maxLines = 1,
                softWrap = false
              )
            }

            // Pro / Upgrade Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isPro) MidnightNavy else DustyBlueLight)
                .bouncingClickable(pressedScale = 0.92f) { viewModel.openPaywall() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = IconlyIcons.Premium,
                  contentDescription = "Pro",
                  tint = if (isPro) PureWhite else BrandBlack,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isPro) "PRO" else "UPGRADE",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  ),
                  color = if (isPro) PureWhite else BrandBlack,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }

            // Quick Help Button
            Box(
              modifier = Modifier
                .size(36.dp)
                .bouncingClickable(pressedScale = 0.90f) { viewModel.openHelpSheet() }
                .clip(CircleShape)
                .background(PureWhite),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = IconlyIcons.Info,
                contentDescription = "Help & FAQ",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
              )
            }

            // Profile Button
            Box(
              modifier = Modifier
                .size(36.dp)
                .bouncingClickable(pressedScale = 0.90f) { viewModel.navigateTo(Screen.Profile) }
                .clip(CircleShape)
                .background(PureWhite),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = IconlyIcons.Profile,
                contentDescription = "Profile",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Contextual Greeting & Quiet Thought
        Column {
          Text(
            text = greeting,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "What do you want to remember?",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.3).sp
            ),
            color = TextPrimary
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar (tapping navigates to Ask Memory or Voice)
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = PureWhite,
          border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
          shadowElevation = 1.dp,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier
                .weight(1f)
                .bouncingClickable(pressedScale = 0.98f) { viewModel.navigateTo(Screen.Ask) },
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = IconlyIcons.Search,
                contentDescription = "Search",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "Ask your memory anything...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
              )
            }

            // Voice Snippet quick trigger inside search bar
            Box(
              modifier = Modifier
                .size(34.dp)
                .bouncingClickable(pressedScale = 0.88f) { viewModel.requestVoiceRecording(context) }
                .clip(CircleShape)
                .background(DustyBlueLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = IconlyIcons.Voice,
                contentDescription = "Record Voice Snippet",
                tint = DustyBlue,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick action chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          QuickActionButton(
            label = "Note",
            icon = IconlyIcons.Add,
            isPrimary = true,
            onClick = { viewModel.openAddSheet() },
            modifier = Modifier.weight(1f)
          )
          QuickActionButton(
            label = "Voice",
            icon = IconlyIcons.Voice,
            isPrimary = false,
            highlightColor = TerracottaRed,
            onClick = { viewModel.requestVoiceRecording(context) },
            modifier = Modifier.weight(1f)
          )
          QuickActionButton(
            label = "Camera",
            icon = IconlyIcons.Camera,
            isPrimary = false,
            highlightColor = DustyBlue,
            onClick = { viewModel.requestCameraCapture(context) },
            modifier = Modifier.weight(1f)
          )
          QuickActionButton(
            label = "Ask AI",
            icon = IconlyIcons.Search,
            isPrimary = false,
            onClick = { viewModel.navigateTo(Screen.Ask) },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // [Performance Architecture: Keyed Composable] User Streak Tracker Component backed by Room Database
    item(key = "home_user_streak_tracker") {
      Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
        UserStreakDisplayComponent(
          streakState = streakState,
          onStreakClick = { viewModel.openStreakMilestone() }
        )
      }
    }

    if (memories.isEmpty()) {
      // Reusable Empty State component with brand-aligned typography and clear next actions
      item {
        EmptyState(
          title = "Your Personal Memory Space",
          description = "Preserve your thoughts, moments, receipts, and travel details. Speak or write to begin building your personal archive.",
          primaryActionLabel = "New Memory",
          primaryActionIcon = IconlyIcons.Add,
          onPrimaryAction = { viewModel.openAddSheet() },
          secondaryActionLabel = "Voice Snippet",
          secondaryActionIcon = IconlyIcons.Voice,
          onSecondaryAction = { viewModel.requestVoiceRecording(context) },
          tips = listOf(
            EmptyStateTip(
              icon = IconlyIcons.Voice,
              title = "Voice Reflections",
              subtitle = "Speak your mind; live transcription organizes your memories",
              onClick = { viewModel.requestVoiceRecording(context) }
            ),
            EmptyStateTip(
              icon = IconlyIcons.Receipt,
              title = "Receipts & Expenses",
              subtitle = "Keep track of merchants, dates, and spending effortlessly",
              onClick = { viewModel.openAddSheet() }
            ),
            EmptyStateTip(
              icon = IconlyIcons.Location,
              title = "Travel & Bookings",
              subtitle = "Store flights, hotels, and gate numbers for quick recall",
              onClick = { viewModel.openAddSheet() }
            ),
            EmptyStateTip(
              icon = IconlyIcons.Document,
              title = "Notes & Daily Moments",
              subtitle = "Important thoughts, bookmarks, and private reminders",
              onClick = { viewModel.openAddSheet() }
            )
          ),
          showValuePills = true
        )
      }
    } else {
      // Milestone card for 1 to 3 memories (Activation Encouragement)
      if (memories.size in 1..3) {
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 6.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "🎯 Memory Vault Activated",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "You have ${memories.size} memory saved. See how natural questions retrieve it instantly.",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              androidx.compose.material3.OutlinedButton(
                onClick = { viewModel.navigateTo(Screen.Ask) },
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MidnightNavy),
                border = androidx.compose.foundation.BorderStroke(1.dp, MidnightNavy),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text("Try Search", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }
            }
          }
        }
      }

      // Nostalgia & Reflection Moment: "A moment to remember"
      val reflectionMemory = memories.firstOrNull { it.isFavorite } ?: memories.firstOrNull()
      if (reflectionMemory != null) {
        item {
          Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = WarmCreamLight),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmCreamBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 6.dp)
              .clickable { viewModel.navigateTo(Screen.MemoryDetail(reflectionMemory.id)) }
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "A moment to remember",
                    fontFamily = MemoryScriptFontFamily,
                    fontSize = 22.sp,
                    color = BrandBlack
                  )
                }
                Text(
                  text = reflectionMemory.eventDate ?: reflectionMemory.category,
                  style = MaterialTheme.typography.labelSmall,
                  color = InkMuted
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = reflectionMemory.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = BrandBlack
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = reflectionMemory.summary.ifBlank { reflectionMemory.rawText },
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }

      // Daily Retention Utility Card ("Apa yang sebaiknya saya lakukan berikutnya?")
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = DustyBlueLight),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Daily Memory Habit",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MidnightNavy
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Capture 1 quick moment from today to preserve context.",
                style = MaterialTheme.typography.bodySmall,
                color = SlateNavy
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            androidx.compose.material3.Button(
              onClick = { viewModel.requestVoiceRecording(context) },
              shape = RoundedCornerShape(16.dp),
              colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MidnightNavy,
                contentColor = PureWhite
              ),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Icon(imageVector = IconlyIcons.Voice, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Capture", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
          }
        }
      }
      // Featured Memory Moments Section (Reference Image carousel)
      item {
        Column(modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Personal Memories",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
              ),
              color = TextPrimary
            )
            Text(
              text = "View all",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = DustyBlue,
              modifier = Modifier.clickable { viewModel.navigateTo(Screen.Memories) }
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            // Performance: Keying items avoids full list recomposition and ensures smooth 60-120 FPS horizontal scroll
            items(memories.take(4), key = { it.id }) { memory ->
              PopularMemoryHeroCard(
                memory = memory,
                onClick = { viewModel.navigateTo(Screen.MemoryDetail(memory.id)) }
              )
            }
          }
        }
      }

      // Recent Collections Section
      if (collections.isNotEmpty()) {
        item {
          Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Recent Collections",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = (-0.2).sp
                ),
                color = TextPrimary
              )
              Text(
                text = "View all",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DustyBlue,
                modifier = Modifier.clickable { viewModel.navigateTo(Screen.Collections) }
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            collections.take(2).forEach { collection ->
              CollectionBannerCard(
                collection = collection,
                onClick = { viewModel.navigateTo(Screen.CollectionDetail(collection.id)) }
              )
              Spacer(modifier = Modifier.height(12.dp))
            }
          }
        }
      }

      // Recent Memories List
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recently Added",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.2).sp
            ),
            color = TextPrimary
          )
        }
      }

      // Performance: Keying items by entity ID allows Compose DiffUtil to recompose only updated items
      items(memories.take(5), key = { it.id }) { memory ->
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
          MemoryCard(
            memory = memory,
            onClick = { viewModel.navigateTo(Screen.MemoryDetail(memory.id)) },
            onFavoriteClick = { viewModel.toggleFavorite(memory.id, memory.isFavorite) },
            onPressStart = { viewModel.prefetchMemory(memory.id) }
          )
        }
      }
    }
  }
}

  if (showSyncDialog) {
    SyncQueueDialog(
      isOnline = isOnline,
      syncState = syncState,
      queuedOperations = queuedOps,
      onTriggerSync = { viewModel.triggerManualSync() },
      onDismiss = { showSyncDialog = false }
    )
  }
}

@Composable
private fun QuickActionButton(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isPrimary: Boolean,
  highlightColor: Color? = null,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .height(44.dp)
      .bouncingClickable(
        pressedScale = 0.93f,
        onClick = onClick
      )
      .clip(RoundedCornerShape(22.dp))
      .background(if (isPrimary) MidnightNavy else PureWhite)
      .padding(horizontal = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = when {
          isPrimary -> PureWhite
          highlightColor != null -> highlightColor
          else -> MidnightNavy
        },
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = if (isPrimary) PureWhite else TextPrimary
      )
    }
  }
}

@Composable
private fun PopularMemoryHeroCard(
  memory: MemoryEntity,
  onClick: () -> Unit,
  onPressStart: (() -> Unit)? = null
) {
  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .width(220.dp)
      .height(210.dp)
      .bouncingClickable(
        pressedScale = 0.96f,
        onPressStart = onPressStart,
        onClick = onClick
      )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Visual accent header container
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(95.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(DustyBlueLight),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = IconlyIcons.Location,
            contentDescription = null,
            tint = DustyBlue,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = memory.category.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = DustyBlue
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = memory.title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.2).sp
        ),
        color = TextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f, fill = false)
        ) {
          Icon(
            imageVector = IconlyIcons.Location,
            contentDescription = null,
            tint = TerracottaRed,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = memory.location ?: memory.places ?: "Saved record",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        if (memory.amount != null) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(MidnightNavy)
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            val amt = if (memory.currency == "JPY" || memory.currency == "IDR") {
              "${memory.currency} ${memory.amount.toInt()}"
            } else {
              "$${String.format("%.2f", memory.amount)}"
            }
            Text(
              text = amt,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = PureWhite
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CollectionBannerCard(
  collection: CollectionEntity,
  onClick: () -> Unit,
  onPressStart: (() -> Unit)? = null
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .bouncingClickable(
        pressedScale = 0.98f,
        onPressStart = onPressStart,
        onClick = onClick
      )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(DustyBlueLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = IconlyIcons.Folder,
          contentDescription = null,
          tint = DustyBlue,
          modifier = Modifier.size(26.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = collection.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.15).sp
          ),
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = collection.description,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Icon(
        imageVector = IconlyIcons.ChevronRight,
        contentDescription = null,
        tint = TextMuted,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
private fun CapabilityGuideRow(
  emoji: String,
  title: String,
  subtitle: String
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = WarmIvory),
    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = emoji,
        fontSize = 20.sp,
        modifier = Modifier.padding(end = 12.dp)
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
      }
    }
  }
}

@Composable
private fun ValuePill(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  text: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(WarmIvory)
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = DustyBlue,
      modifier = Modifier.size(13.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
      color = TextSecondary,
      fontSize = 11.sp
    )
  }
}
