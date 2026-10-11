package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayStreakItem
import com.example.data.model.StreakUiState
import com.example.ui.theme.*

/**
 * [Performance Architecture]
 * UserStreakDisplayComponent is an ultra-smooth, 60/120 FPS Jetpack Compose component.
 * Features responsive layout, adaptive dark mode support, and clean contrast.
 */
@Composable
fun UserStreakDisplayComponent(
  streakState: StreakUiState,
  onStreakClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val animatedProgress by animateFloatAsState(
    targetValue = streakState.milestoneProgress,
    animationSpec = tween(durationMillis = 600),
    label = "streak_progress"
  )

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onStreakClick() }
      .semantics { contentDescription = "User Streak Card: ${streakState.currentStreak} day streak" }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Top Row: Streak Counter and Status Badge (flex aligned, no text overflow)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f, fill = false),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Fiery Flame / Premium Emblem Badge
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  colors = listOf(
                    Color(0xFFFF8A65),
                    Color(0xFFFF5722),
                    MaterialTheme.colorScheme.primary
                  )
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = IconlyIcons.Premium,
              contentDescription = "Active Streak",
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }

          Column(modifier = Modifier.weight(1f, fill = false)) {
            Row(
              verticalAlignment = Alignment.Bottom,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "${streakState.currentStreak}",
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (streakState.currentStreak == 1) "Day Streak" else "Days Streak",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 3.dp)
              )
            }

            Text(
              text = if (streakState.isTodayActive) "Active today · Streak secured" else "Capture today to keep streak",
              style = MaterialTheme.typography.bodySmall,
              color = if (streakState.isTodayActive) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Longest Streak Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = IconlyIcons.Verified,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = "Best: ${streakState.longestStreak}d",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 7-Day Activity Weekly Track (Pills)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        streakState.weeklyDays.forEach { dayItem ->
          key(dayItem.dateString) {
            DayStreakPill(dayItem = dayItem)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Milestone Progress Bar
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Next milestone: ${streakState.nextMilestoneTarget} days",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(3.5.dp)),
          color = MaterialTheme.colorScheme.primary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
      }
    }
  }
}

/**
 * Individual day pill component for the 7-day track.
 */
@Composable
private fun DayStreakPill(dayItem: DayStreakItem) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Text(
      text = dayItem.dayLabel,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = if (dayItem.isToday) FontWeight.Bold else FontWeight.Medium
      ),
      color = if (dayItem.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )

    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(
          when {
            dayItem.isCompleted -> EmeraldLight
            dayItem.isToday -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
          }
        ),
      contentAlignment = Alignment.Center
    ) {
      if (dayItem.isCompleted) {
        Icon(
          imageVector = IconlyIcons.Check,
          contentDescription = "Completed",
          tint = EmeraldGreen,
          modifier = Modifier.size(16.dp)
        )
      } else if (dayItem.isToday) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
        )
      } else {
        Box(
          modifier = Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outline)
        )
      }
    }
  }
}
