package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ClearConfirmationDialog
import com.example.ui.components.IconlyIcons
import com.example.ui.components.MemoryOSEmblem
import com.example.ui.components.MemoryOSCompleteLogo
import com.example.ui.theme.MemoryScriptFontFamily
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.DustyBlueLight
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TerracottaLight
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.MemoryViewModel

@Composable
fun ProfileScreen(
  viewModel: MemoryViewModel,
  modifier: Modifier = Modifier
) {
  val memories by viewModel.allMemories.collectAsStateWithLifecycle()
  val collections by viewModel.collections.collectAsStateWithLifecycle()
  val isCloudConnected by viewModel.cloudSyncConnected.collectAsStateWithLifecycle()
  val isPro by viewModel.isProUser.collectAsStateWithLifecycle()
  val dailyRemindersEnabled by viewModel.dailyRemindersEnabled.collectAsStateWithLifecycle()
  val reminderTime by viewModel.reminderTime.collectAsStateWithLifecycle()
  val showClearAllConfirmation by viewModel.showClearAllConfirmation.collectAsStateWithLifecycle()
  val showClearDatabaseConfirmation by viewModel.showClearDatabaseConfirmation.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val streakState by viewModel.streakState.collectAsStateWithLifecycle()
  val context = LocalContext.current

  val notifPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
    contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    viewModel.setDailyRemindersEnabled(isGranted)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(WarmIvory)
      .statusBarsPadding(),
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
  ) {
    // Top Profile Header with "Me" Brand Motif
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (currentUser != null) EmeraldLight else DustyBlueLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = IconlyIcons.Profile,
            contentDescription = "Profile Avatar",
            tint = if (currentUser != null) EmeraldGreen else MidnightNavy,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = currentUser?.fullName ?: "Personal Account",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.2).sp
            ),
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Your Personal Memory Space",
            fontFamily = MemoryScriptFontFamily,
            fontSize = 18.sp,
            color = BrandBlack
          )
          Text(
            text = currentUser?.email ?: "Guest Mode · Local Vault",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        androidx.compose.material3.IconButton(
          onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.Settings) },
          modifier = Modifier.size(48.dp)
        ) {
          Icon(
            imageVector = IconlyIcons.Settings,
            contentDescription = "Open Settings",
            tint = BrandBlack,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // Supabase Account Action Section below Personal Account header
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          if (currentUser == null) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(DustyBlueLight)
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "CLOUD DATABASE",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                  ),
                  color = DustyBlue
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "Connect Cloud Account",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "Sign in or sign up to back up your encrypted vault, sync memories across devices, and secure your database.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = { viewModel.openAuthDialog(com.example.ui.components.AuthMode.SIGN_IN) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MidnightNavy,
                  contentColor = PureWhite
                ),
                modifier = Modifier
                  .weight(1f)
                  .height(46.dp)
              ) {
                Text(
                  text = "Sign In",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
              }

              OutlinedButton(
                onClick = { viewModel.openAuthDialog(com.example.ui.components.AuthMode.SIGN_UP) },
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                  .weight(1f)
                  .height(46.dp)
              ) {
                Text(
                  text = "Sign Up",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary
                )
              }
            }
          } else {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(EmeraldGreen)
                )
                Text(
                  text = "Connected to Cloud Vault",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary
                )
              }

              TextButton(onClick = { viewModel.signOutSupabase() }) {
                Text(
                  text = "Sign Out",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = TerracottaRed
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "User ID: ${currentUser?.id?.take(12)}... | Real-time backup active",
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
              onClick = { viewModel.syncToSupabase() },
              shape = RoundedCornerShape(14.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
              modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = IconlyIcons.Refresh,
                  contentDescription = null,
                  tint = MidnightNavy,
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = "Sync Local Vault to Cloud Now",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = MidnightNavy
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // Intelligence Stats Card
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          StatItem(number = "${memories.size}", label = "Memories")
          StatItem(number = "${collections.size}", label = "Collections")
          StatItem(
            number = "${memories.count { it.isFavorite }}",
            label = "Favorites"
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // [Performance Architecture: Keyed Composable] User Streak Tracker Card
    item(key = "profile_user_streak_tracker") {
      com.example.ui.components.UserStreakDisplayComponent(
        streakState = streakState,
        onStreakClick = { viewModel.openStreakMilestone() }
      )

      Spacer(modifier = Modifier.height(20.dp))
    }

    // Pro Subscription Card (Direct distribution, Pro plan coming soon)
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isPro) MidnightNavy else PureWhite
        ),
        border = androidx.compose.foundation.BorderStroke(
          width = if (isPro) 0.dp else 1.dp,
          color = CardBorder
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(22.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isPro) DustyBlue else MidnightNavy),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = IconlyIcons.Premium,
                  contentDescription = null,
                  tint = PureWhite,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "MemoryOS Pro",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isPro) PureWhite else TextPrimary
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Upgrade to unlock unlimited voice memory recordings, deep semantic search, automatic entity extraction, and priority cloud sync.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 20.sp
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = { viewModel.openPaywall() },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MidnightNavy,
              contentColor = PureWhite
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = IconlyIcons.Premium,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Upgrade to MemoryOS Pro",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // Daily Memory Engagement Reminders (Local Notifications)
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(DustyBlueLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = IconlyIcons.Notification,
                  contentDescription = null,
                  tint = DustyBlue,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Daily Memory Reminders",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary
                )
              }
            }

            Switch(
              checked = dailyRemindersEnabled,
              onCheckedChange = { isChecked ->
                if (isChecked && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                  notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                  viewModel.setDailyRemindersEnabled(isChecked)
                }
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = MidnightNavy,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DustyBlueLight
              )
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Receive a gentle prompt to capture your highlights, reflections, or voice thoughts and maintain your daily memory streak.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )

          if (dailyRemindersEnabled) {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Reminder Schedule",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val isEvening = reminderTime.first == 20 && reminderTime.second == 0
              val isMorning = reminderTime.first == 9 && reminderTime.second == 0
              val isAfternoon = reminderTime.first == 14 && reminderTime.second == 0

              FilterChip(
                selected = isEvening,
                onClick = { viewModel.setReminderTime(20, 0) },
                label = { Text("8:00 PM") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MidnightNavy,
                  selectedLabelColor = PureWhite
                )
              )

              FilterChip(
                selected = isMorning,
                onClick = { viewModel.setReminderTime(9, 0) },
                label = { Text("9:00 AM") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MidnightNavy,
                  selectedLabelColor = PureWhite
                )
              )

              FilterChip(
                selected = isAfternoon,
                onClick = { viewModel.setReminderTime(14, 0) },
                label = { Text("2:00 PM") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MidnightNavy,
                  selectedLabelColor = PureWhite
                )
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            androidx.compose.material3.OutlinedButton(
              onClick = { viewModel.triggerTestNotification() },
              shape = RoundedCornerShape(14.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, DustyBlue),
              colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = DustyBlue
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = IconlyIcons.Notification,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Send Test Notification Now",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // Privacy & Data Isolation
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = IconlyIcons.Lock,
              contentDescription = null,
              tint = DustyBlue,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Privacy & Isolation",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Your memories never train public models. All queries are grounded strictly in your personal Room database with AES encryption.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // Data Export & Ownership Card
    item {
      val context = LocalContext.current
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = IconlyIcons.Document,
              contentDescription = null,
              tint = DustyBlue,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Data Ownership & Export",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "You own 100% of your data. Export your entire memory vault at any time in open formats.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            androidx.compose.material3.OutlinedButton(
              onClick = {
                val json = com.example.util.DataExportHelper.formatMemoriesAsJson(memories)
                com.example.util.DataExportHelper.shareExport(context, json, "MemoryOS Export (JSON)")
              },
              shape = RoundedCornerShape(14.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
              modifier = Modifier.weight(1f)
            ) {
              Text("Export JSON", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
            }

            androidx.compose.material3.OutlinedButton(
              onClick = {
                val md = com.example.util.DataExportHelper.formatMemoriesAsMarkdown(memories)
                com.example.util.DataExportHelper.shareExport(context, md, "MemoryOS Export (Markdown)")
              },
              shape = RoundedCornerShape(14.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
              modifier = Modifier.weight(1f)
            ) {
              Text("Export Notes", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // Help & FAQ Card
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = IconlyIcons.Info,
                contentDescription = null,
                tint = DustyBlue,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Help, FAQ & Support",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Browse frequently asked questions, learn how natural language retrieval works, or get help.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { viewModel.openSupportEmailDialog() },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy, contentColor = PureWhite),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(imageVector = IconlyIcons.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contact Customer Support")
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedButton(
            onClick = { viewModel.openHelpSheet() },
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlack),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(imageVector = IconlyIcons.Info, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse Help Center & FAQ")
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Quick Action links for Experience, Tooltips, Privacy & Terms
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { viewModel.openWalkthrough() },
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Tour", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
              onClick = { viewModel.openTooltips() },
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Tips", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
              onClick = { viewModel.openNpsDialog() },
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Rate", style = MaterialTheme.typography.labelSmall)
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { viewModel.openLegalDialog(com.example.ui.components.LegalTab.PRIVACY_POLICY) },
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Privacy Policy", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
              onClick = { viewModel.openLegalDialog(com.example.ui.components.LegalTab.TERMS_AND_CONDITIONS) },
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Terms of Service", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // Direct Distribution & Data Cleanliness
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "MemoryOS Direct Edition",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(DustyBlueLight)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "v1.0.0 Standalone",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DustyBlue
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Independent standalone distribution with zero third-party store dependencies. Direct offline vault and local AI intelligence.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = IconlyIcons.Info,
              contentDescription = null,
              tint = DustyBlue,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Customer Support: bintangjanuarda0809@gmail.com",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
              ),
              color = MidnightNavy
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          androidx.compose.material3.OutlinedButton(
            onClick = { viewModel.openClearAllConfirmation() },
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaRed),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
              contentColor = TerracottaRed
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = IconlyIcons.Delete,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Clear All Memories & Cache",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          androidx.compose.material3.OutlinedButton(
            onClick = { viewModel.openClearDatabaseConfirmation() },
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaRed.copy(alpha = 0.7f)),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
              contentColor = TerracottaRed
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = IconlyIcons.Delete,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Clear Local Memory Database",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }
        }
      }
    }

    // MemoryOS Brand Signature Footer
    item {
      Spacer(modifier = Modifier.height(32.dp))
      MemoryOSCompleteLogo(
        iconSize = 48.dp,
        textColor = BrandBlack,
        tagline = "Your Personal Memory Space • v2.4"
      )
      Spacer(modifier = Modifier.height(100.dp))
    }
  }

  // Clear All Memories & Cache Confirmation Dialog
  if (showClearAllConfirmation) {
    ClearConfirmationDialog(
      title = "Clear All Memories & Cache?",
      description = "Are you absolutely sure you want to delete all memories, local voice transcripts, and cached media? This action cannot be reversed.",
      confirmButtonLabel = "Yes, Clear Everything",
      onConfirm = {
        viewModel.clearAllData()
        viewModel.closeClearAllConfirmation()
        android.widget.Toast.makeText(context, "All memories and cache cleared.", android.widget.Toast.LENGTH_SHORT).show()
      },
      onDismiss = { viewModel.closeClearAllConfirmation() }
    )
  }

  // Clear Local Memory Database Confirmation Dialog
  if (showClearDatabaseConfirmation) {
    ClearConfirmationDialog(
      title = "Clear Local Memory Database?",
      description = "Are you sure you want to clear your local database tables and stored queries? All Room SQLite entries will be permanently removed.",
      confirmButtonLabel = "Yes, Clear Database",
      onConfirm = {
        viewModel.clearAllData()
        viewModel.closeClearDatabaseConfirmation()
        android.widget.Toast.makeText(context, "Local memory database cleared.", android.widget.Toast.LENGTH_SHORT).show()
      },
      onDismiss = { viewModel.closeClearDatabaseConfirmation() }
    )
  }
}

@Composable
private fun StatItem(number: String, label: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = number,
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = TextPrimary
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = TextSecondary
    )
  }
}

@Composable
private fun ServiceStatusRow(
  title: String,
  description: String,
  isConnected: Boolean,
  icon: androidx.compose.ui.graphics.vector.ImageVector
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(if (isConnected) EmeraldLight else TerracottaLight),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isConnected) EmeraldGreen else TerracottaRed,
        modifier = Modifier.size(20.dp)
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = TextPrimary
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
      )
    }

    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .background(if (isConnected) EmeraldLight else TerracottaLight)
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Text(
        text = if (isConnected) "ACTIVE" else "OFFLINE",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = if (isConnected) EmeraldGreen else TerracottaRed
      )
    }
  }
}
