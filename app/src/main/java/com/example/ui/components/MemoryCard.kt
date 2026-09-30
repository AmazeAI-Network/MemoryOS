package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemoryEntity
import com.example.ui.theme.CardBackground
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

@Composable
fun MemoryCard(
  memory: MemoryEntity,
  onClick: () -> Unit,
  onFavoriteClick: () -> Unit,
  modifier: Modifier = Modifier,
  onPressStart: (() -> Unit)? = null
) {
  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .bouncingClickable(
        pressedScale = 0.982f,
        onPressStart = onPressStart,
        onClick = onClick
      )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header row: Category pill & Favorite bookmark
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val (badgeBg, badgeColor, icon) = getCategoryStyling(memory.memoryType)
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(badgeBg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = badgeColor,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = memory.category.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            ),
            color = badgeColor
          )
        }

        Box(
          modifier = Modifier
            .size(36.dp)
            .bouncingClickable(
              pressedScale = 0.85f,
              onClick = onFavoriteClick
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (memory.isFavorite) IconlyIcons.Heart else IconlyIcons.HeartOutline,
            contentDescription = "Favorite",
            tint = if (memory.isFavorite) TerracottaRed else TextMuted,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Title
      Text(
        text = memory.title,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.SemiBold,
          letterSpacing = (-0.2).sp
        ),
        color = TextPrimary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Summary / Content Preview
      Text(
        text = memory.summary.ifBlank { memory.rawText.take(120) },
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      // Tags preview pills
      if (memory.tags.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          memory.tags.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(3)
            .forEach { tag ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(CardBackground)
                  .padding(horizontal = 7.dp, vertical = 2.dp)
              ) {
                Text(
                  text = if (tag.startsWith("#")) tag else "#$tag",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                  ),
                  color = TextSecondary
                )
              }
            }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Bottom information row: Date & Location & Amount
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
            imageVector = IconlyIcons.Calendar,
            contentDescription = "Date",
            tint = DustyBlue,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = memory.eventDate ?: formatMemoryDate(memory.createdAt),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          if (!memory.location.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = IconlyIcons.Location,
              contentDescription = "Location",
              tint = TerracottaRed,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = memory.location,
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Amount tag pill if applicable
        if (memory.amount != null) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(MidnightNavy)
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            val formatted = if (memory.currency == "JPY" || memory.currency == "IDR") {
              "${memory.currency} ${memory.amount.toInt()}"
            } else {
              "$${String.format("%.2f", memory.amount)}"
            }
            Text(
              text = formatted,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
              ),
              color = PureWhite
            )
          }
        }
      }
    }
  }
}

private fun formatMemoryDate(timestamp: Long): String {
  val now = System.currentTimeMillis()
  val diff = now - timestamp
  val minutes = (diff / (1000 * 60)).coerceAtLeast(0)
  val hours = minutes / 60
  val days = hours / 24

  return when {
    minutes < 2 -> "Just now"
    minutes < 60 -> "${minutes}m ago"
    hours < 24 -> "${hours}h ago"
    days == 1L -> "Yesterday"
    days < 7 -> "${days}d ago"
    else -> {
      val sdf = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
      sdf.format(java.util.Date(timestamp))
    }
  }
}

private fun getCategoryStyling(memoryType: String): Triple<Color, Color, ImageVector> {
  return when (memoryType.uppercase()) {
    "RECEIPT" -> Triple(EmeraldLight, EmeraldGreen, IconlyIcons.Receipt)
    "TRAVEL", "TICKET" -> Triple(DustyBlueLight, DustyBlue, IconlyIcons.Calendar)
    "RESERVATION" -> Triple(TerracottaLight, TerracottaRed, IconlyIcons.Location)
    else -> Triple(CardBackground, TextSecondary, IconlyIcons.Document)
  }
}
