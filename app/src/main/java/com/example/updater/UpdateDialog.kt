package com.example.updater

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.IconlyIcons
import com.example.ui.components.MemoryOSEmblem
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.DustyBlueLight
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory

@Composable
fun UpdateDialog(
  updateInfo: AppUpdateInfo,
  downloadState: UpdateDownloadState,
  onStartDownload: () -> Unit,
  onInstallClick: () -> Unit,
  onRequestPermission: () -> Unit,
  onDismiss: () -> Unit,
  hasInstallPermission: Boolean
) {
  Dialog(
    onDismissRequest = {
      if (!updateInfo.forceUpdate && downloadState !is UpdateDownloadState.Downloading) {
        onDismiss()
      }
    },
    properties = DialogProperties(
      dismissOnBackPress = !updateInfo.forceUpdate,
      dismissOnClickOutside = !updateInfo.forceUpdate
    )
  ) {
    Card(
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = PureWhite),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "MemoryOS Update",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MidnightNavy
            )
            Text(
              text = "Version ${updateInfo.versionName} ready",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(DustyBlueLight)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = if (updateInfo.forceUpdate) "Required" else "New",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = DustyBlue
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Release notes card
        if (updateInfo.releaseNotes.isNotBlank()) {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmIvory),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "What's New in this Release:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MidnightNavy
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = updateInfo.releaseNotes,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = TextPrimary
              )
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }

        // Progress or Status
        when (downloadState) {
          is UpdateDownloadState.Downloading -> {
            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Downloading update...",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                  color = TextPrimary
                )
                Text(
                  text = "${downloadState.progressPercent}%",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MidnightNavy
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              LinearProgressIndicator(
                progress = { downloadState.progressPercent / 100f },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = MidnightNavy,
                trackColor = DustyBlueLight
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
          }

          is UpdateDownloadState.DownloadCompleted -> {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(EmeraldGreen),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = IconlyIcons.Check,
                  contentDescription = null,
                  tint = PureWhite,
                  modifier = Modifier.size(16.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "APK verified & ready to install",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = EmeraldGreen
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
          }

          is UpdateDownloadState.Error -> {
            Text(
              text = downloadState.message,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
          }

          else -> {}
        }

        // Install permission warning if Android 8+
        if (!hasInstallPermission && downloadState is UpdateDownloadState.DownloadCompleted) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(DustyBlueLight)
              .padding(10.dp)
          ) {
            Text(
              text = "Android requires permission to install APKs downloaded outside the store. Tap below to enable.",
              style = MaterialTheme.typography.labelSmall,
              color = MidnightNavy
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
        }

        // Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (!updateInfo.forceUpdate && downloadState !is UpdateDownloadState.Downloading) {
            OutlinedButton(
              onClick = onDismiss,
              modifier = Modifier
                .weight(1f)
                .height(46.dp),
              shape = RoundedCornerShape(16.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
              Text("Later", color = TextSecondary)
            }
          }

          Button(
            onClick = {
              when (downloadState) {
                is UpdateDownloadState.DownloadCompleted -> {
                  if (!hasInstallPermission) {
                    onRequestPermission()
                  } else {
                    onInstallClick()
                  }
                }
                is UpdateDownloadState.Downloading -> {
                  // No-op while downloading
                }
                else -> {
                  onStartDownload()
                }
              }
            },
            enabled = downloadState !is UpdateDownloadState.Downloading,
            modifier = Modifier
              .weight(1f)
              .height(46.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy, contentColor = PureWhite)
          ) {
            val label = when (downloadState) {
              is UpdateDownloadState.DownloadCompleted -> if (!hasInstallPermission) "Enable Permission" else "Install Now"
              is UpdateDownloadState.Downloading -> "Downloading..."
              else -> "Download Update"
            }
            Text(label, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
