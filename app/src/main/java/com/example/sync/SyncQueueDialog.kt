package com.example.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TerracottaLight
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncQueueDialog(
  isOnline: Boolean,
  syncState: SyncState,
  queuedOperations: List<QueuedSyncOperation>,
  onTriggerSync: () -> Unit,
  onDismiss: () -> Unit
) {
  val timeFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = PureWhite),
      border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 24.dp)
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
              text = "Sync & Operations Queue",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MidnightNavy
            )
            Text(
              text = "Vault & Storage Queue",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }

          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(WarmIvory)
              .clickable { onDismiss() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = IconlyIcons.Close,
              contentDescription = "Close",
              tint = TextPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Network Status Badge Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isOnline) EmeraldLight.copy(alpha = 0.5f) else TerracottaLight.copy(alpha = 0.5f)
          ),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isOnline) EmeraldGreen.copy(alpha = 0.3f) else TerracottaRed.copy(alpha = 0.3f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(DustyBlue)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Local Storage & Queue",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MidnightNavy
              )
              Text(
                text = "All memories and edits are saved safely in SQLite local database.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 17.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Queued Operations List Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "QUEUED OPERATIONS",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
            color = TextMuted
          )
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(DustyBlueLight)
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "${queuedOperations.size} pending",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = DustyBlue
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (queuedOperations.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(WarmIvory)
              .padding(20.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = IconlyIcons.Check,
                contentDescription = null,
                tint = EmeraldGreen,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Queue is empty",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
              )
              Text(
                text = "All local records match cloud state.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 240.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(queuedOperations) { op ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(WarmIvory)
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  modifier = Modifier.weight(1f),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  val icon = when (op.type) {
                    "CREATE_MEMORY" -> IconlyIcons.Add
                    "DELETE_MEMORY" -> IconlyIcons.Delete
                    "TOGGLE_FAVORITE" -> IconlyIcons.Heart
                    else -> IconlyIcons.Edit
                  }
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(PureWhite),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = icon,
                      contentDescription = null,
                      tint = DustyBlue,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = op.titleOrSummary,
                      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                      color = TextPrimary,
                      maxLines = 1
                    )
                    Text(
                      text = "Action: ${op.type} · ${timeFormat.format(Date(op.timestamp))}",
                      style = MaterialTheme.typography.labelSmall,
                      color = TextMuted
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(46.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
          ) {
            Text("Close", color = TextSecondary)
          }

          Button(
            onClick = onTriggerSync,
            enabled = isOnline && queuedOperations.isNotEmpty() && syncState != SyncState.SYNCING,
            modifier = Modifier
              .weight(1f)
              .height(46.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy, contentColor = PureWhite)
          ) {
            if (syncState == SyncState.SYNCING) {
              CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Syncing...", fontSize = 13.sp)
            } else {
              Icon(imageVector = IconlyIcons.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isOnline) "Sync Now" else "Offline", fontSize = 13.sp)
            }
          }
        }
      }
    }
  }
}
