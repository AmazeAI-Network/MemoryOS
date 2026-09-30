package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.IconlyIcons
import com.example.ui.components.SkeletonMemoryDetail
import com.example.ui.components.bouncingClickable
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.DustyBlueLight
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.MemoryViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun MemoryDetailScreen(
  viewModel: MemoryViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateTo(Screen.Home)
  }

  val memory by viewModel.selectedMemory.collectAsStateWithLifecycle()

  if (memory == null) {
    Column(
      modifier = modifier
        .fillMaxSize()
        .background(WarmIvory)
        .statusBarsPadding()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .bouncingClickable(pressedScale = 0.88f) { viewModel.navigateTo(Screen.Home) }
            .clip(CircleShape)
            .background(PureWhite),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = IconlyIcons.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }
      }
      SkeletonMemoryDetail()
    }
    return
  }

  val mem = memory!!

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(WarmIvory)
      .statusBarsPadding(),
    contentPadding = PaddingValues(bottom = 100.dp)
  ) {
    // Top Bar
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .bouncingClickable(pressedScale = 0.88f) { viewModel.navigateTo(Screen.Home) }
            .clip(CircleShape)
            .background(PureWhite),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = IconlyIcons.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Row {
          Box(
            modifier = Modifier
              .size(40.dp)
              .bouncingClickable(pressedScale = 0.88f) {
                viewModel.toggleFavorite(mem.id, mem.isFavorite)
              }
              .clip(CircleShape)
              .background(PureWhite),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (mem.isFavorite) IconlyIcons.Heart else IconlyIcons.HeartOutline,
              contentDescription = "Favorite",
              tint = if (mem.isFavorite) TerracottaRed else TextMuted,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier = Modifier
              .size(40.dp)
              .bouncingClickable(pressedScale = 0.88f) {
                viewModel.deleteMemory(mem.id)
              }
              .clip(CircleShape)
              .background(PureWhite),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = IconlyIcons.Delete,
              contentDescription = "Delete",
              tint = TextMuted,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    // Hero Visual Banner
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
          .height(180.dp)
          .clip(RoundedCornerShape(26.dp))
          .background(DustyBlueLight),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = when (mem.memoryType) {
              "RECEIPT" -> IconlyIcons.Receipt
              "TRAVEL", "TICKET" -> IconlyIcons.Location
              else -> IconlyIcons.Document
            },
            contentDescription = null,
            tint = DustyBlue,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(10.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(PureWhite)
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = "${mem.memoryType} · ${mem.category}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MidnightNavy
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // Title & Core Metadata
    item {
      Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
          text = mem.title,
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
          ),
          color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Info pills row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (!mem.eventDate.isNullOrBlank()) {
            InfoChip(icon = IconlyIcons.Calendar, text = mem.eventDate)
          }
          if (!mem.location.isNullOrBlank()) {
            InfoChip(icon = IconlyIcons.Location, text = mem.location)
          }
          if (mem.amount != null) {
            val formatted = if (mem.currency == "JPY" || mem.currency == "IDR") {
              "${mem.currency} ${mem.amount.toInt()}"
            } else {
              "$${String.format("%.2f", mem.amount)}"
            }
            InfoChip(icon = IconlyIcons.Receipt, text = formatted, isHighlight = true)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Summary Section
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = PureWhite),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = IconlyIcons.Verified,
                contentDescription = null,
                tint = DustyBlue,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "MEMORY SUMMARY",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                ),
                color = DustyBlue
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = mem.summary,
              style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
              color = TextPrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Extracted Entities
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = PureWhite),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = "Extracted Intelligence",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (!mem.merchant.isNullOrBlank()) {
              DetailRow(label = "Merchant / Entity", value = mem.merchant)
            }
            if (!mem.places.isNullOrBlank()) {
              DetailRow(label = "Places", value = mem.places)
            }
            if (!mem.people.isNullOrBlank()) {
              DetailRow(label = "People", value = mem.people)
            }
            if (mem.tags.isNotBlank()) {
              DetailRow(label = "Tags", value = mem.tags)
            }
            DetailRow(label = "Preserved On", value = formatDetailTimestamp(mem.createdAt))
            DetailRow(label = "Source", value = "${mem.sourceType} import")
          }
        }

        // Original Text / OCR
        if (mem.ocrText.isNotBlank() || mem.rawText.isNotBlank()) {
          Spacer(modifier = Modifier.height(16.dp))
          Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              androidx.compose.material3.Text(
                text = "Original Extracted Text",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
              )
              Spacer(modifier = Modifier.height(8.dp))
              androidx.compose.material3.Text(
                text = if (mem.ocrText.isNotBlank()) mem.ocrText else mem.rawText,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons: Ask AI & Share
        val context = LocalContext.current
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = {
              viewModel.navigateTo(Screen.Ask)
              viewModel.submitAskQuestion("What are the key details and summary for ${mem.title}?")
            },
            modifier = Modifier
              .weight(1.2f)
              .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy, contentColor = PureWhite)
          ) {
            Icon(imageVector = IconlyIcons.Search, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            androidx.compose.material3.Text(
              "Ask About This",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          OutlinedButton(
            onClick = {
              val shareText = "MemoryOS: ${mem.title}\n${mem.summary}\nDate: ${mem.eventDate ?: "N/A"}\nTags: ${mem.tags}"
              val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
              }
              val shareIntent = Intent.createChooser(sendIntent, "Share Memory")
              context.startActivity(shareIntent)
            },
            modifier = Modifier
              .weight(0.8f)
              .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
          ) {
            Icon(imageVector = IconlyIcons.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            androidx.compose.material3.Text(
              "Share",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun InfoChip(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  text: String,
  isHighlight: Boolean = false
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(if (isHighlight) MidnightNavy else PureWhite)
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isHighlight) PureWhite else TextSecondary,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
        color = if (isHighlight) PureWhite else TextSecondary
      )
    }
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
      color = TextPrimary
    )
  }
}

private fun formatDetailTimestamp(timestamp: Long): String {
  val sdf = java.text.SimpleDateFormat("MMM d, yyyy · h:mm a", java.util.Locale.getDefault())
  return sdf.format(java.util.Date(timestamp))
}
