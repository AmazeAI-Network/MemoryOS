package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemoryEntity
import com.example.ui.components.MemoryOSEmblem
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.DustyBlueLight
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AhaMomentResultDialog(
  memory: MemoryEntity,
  onDismiss: () -> Unit,
  onTryAsk: (sampleQuery: String) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val suggestedQuery = when {
    !memory.merchant.isNullOrBlank() -> "How much did I spend at ${memory.merchant}?"
    memory.amount != null -> "What was the expense of ${memory.amount} for?"
    !memory.location.isNullOrBlank() -> "What did I do in ${memory.location}?"
    !memory.eventDate.isNullOrBlank() -> "What do I have scheduled on ${memory.eventDate}?"
    else -> "What was the note about ${memory.title}?"
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PureWhite,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp)
        .padding(bottom = 36.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Preserved in MemoryOS",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = BrandBlack
          )
          Text(
            text = "Saved to your personal memory space",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(imageVector = IconlyIcons.Close, contentDescription = "Close", tint = TextMuted)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Structured Breakdown Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WarmIvory),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = memory.title,
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
                text = memory.category.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                ),
                color = DustyBlue
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = memory.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 20.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Extracted Metadata Chips
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (!memory.merchant.isNullOrBlank()) {
              AttributeBadge(label = "Merchant", value = memory.merchant)
            }
            if (memory.amount != null) {
              AttributeBadge(label = "Amount", value = "${memory.currency ?: "$"}${memory.amount}")
            }
            if (!memory.eventDate.isNullOrBlank()) {
              AttributeBadge(label = "Date", value = memory.eventDate)
            }
            if (!memory.location.isNullOrBlank()) {
              AttributeBadge(label = "Place", value = memory.location)
            }
            if (memory.tags.isNotBlank()) {
              memory.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(3).forEach { tag ->
                AttributeBadge(label = "Tag", value = "#$tag")
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // The Aha Moment Prompt
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DustyBlueLight),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = IconlyIcons.Search,
              contentDescription = null,
              tint = DustyBlue,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Try asking your memory now:",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = MidnightNavy
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "\"$suggestedQuery\"",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MidnightNavy
          )
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      // Action Buttons
      Button(
        onClick = {
          onDismiss()
          onTryAsk(suggestedQuery)
        },
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy, contentColor = PureWhite),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
      ) {
        Icon(imageVector = IconlyIcons.Search, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Try Asking This Question",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
      ) {
        Text(
          text = "Done & Keep Exploring",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
          color = TextPrimary
        )
      }
    }
  }
}

@Composable
private fun AttributeBadge(label: String, value: String) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(PureWhite)
      .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(
      text = "$label: $value",
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
      color = TextSecondary
    )
  }
}
