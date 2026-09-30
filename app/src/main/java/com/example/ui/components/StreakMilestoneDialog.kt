package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

/**
 * Streak Milestones Pop-up:
 * Gamified celebration modal that recognizes user consistency and habit building
 * (e.g., 5-day streak!), delivering an encouraging dopamine hit.
 */
@Composable
fun StreakMilestoneDialog(
  streakDays: Int = 5,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = WarmIvory,
      border = BorderStroke(1.dp, CardBorder),
      shadowElevation = 10.dp,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Milestone Flame & Crown Badge
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(MidnightNavy),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = IconlyIcons.Premium,
            contentDescription = null,
            tint = PureWhite,
            modifier = Modifier.size(38.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DustyBlueLight)
            .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
          Text(
            text = "MILESTONE UNLOCKED · $streakDays-DAY STREAK",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            ),
            color = DustyBlue
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "$streakDays Days of Memories!",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
          ),
          color = BrandBlack,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Consistency is a superpower. You have preserved your thoughts, ideas, and receipts $streakDays days in a row. Your personal archive is growing stronger each day.",
          style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
          color = InkSecondary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Gamified Reward Card
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = PureWhite,
          border = BorderStroke(1.dp, CardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(WarmCreamDark),
              contentAlignment = Alignment.Center
            ) {
              Text("🏆", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Memory Habit Pioneer",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MidnightNavy
              )
              Text(
                text = "+500 Habit Points · Vault sync priority",
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = onDismiss,
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MidnightNavy,
            contentColor = PureWhite
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
        ) {
          Text(
            text = "Celebrate & Keep Preserving",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}
