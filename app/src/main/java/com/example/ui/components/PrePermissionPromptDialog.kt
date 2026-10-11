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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

enum class PermissionType(
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val reasonTitle: String,
  val reasonBody: String,
  val privacyNote: String
) {
  MICROPHONE(
    title = "Allow Microphone Access",
    subtitle = "For Voice Reflections & Instant Audio Notes",
    icon = IconlyIcons.Voice,
    reasonTitle = "Why MemoryOS needs this:",
    reasonBody = "Allows you to speak spontaneous thoughts, journals, and receipts. Our on-device speech-to-text converts your words into organized memories in real time.",
    privacyNote = "Raw audio is processed locally and never uploaded to public servers without encryption."
  ),
  NOTIFICATIONS(
    title = "Allow Memory Reminders",
    subtitle = "Gentle Daily Prompts for Memory Building",
    icon = IconlyIcons.Notification,
    reasonTitle = "Why MemoryOS needs this:",
    reasonBody = "Sends quiet, respectful reminders at your preferred time to capture memories, maintain your streak, and review past journals.",
    privacyNote = "Notifications are scheduled entirely on-device via WorkManager and AlarmManager."
  ),
  CAMERA(
    title = "Allow Camera Access",
    subtitle = "Capture Receipts, Tickets & Travel Artifacts",
    icon = IconlyIcons.Camera,
    reasonTitle = "Why MemoryOS needs this:",
    reasonBody = "Enables taking photos of receipts, documents, and places to attach visual evidence directly into your local memory cards.",
    privacyNote = "Photos are saved securely in your private application storage sandbox."
  )
}

/**
 * Pre-Permission "Soft Prompts" Pop-up:
 * Explains the precise value and privacy guarantee of requested permissions
 * before presenting Android's native system permission request dialog.
 */
@Composable
fun PrePermissionPromptDialog(
  type: PermissionType = PermissionType.MICROPHONE,
  onAllow: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(26.dp),
      color = PureWhite,
      border = BorderStroke(1.dp, CardBorder),
      shadowElevation = 8.dp,
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
        // Icon
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(DustyBlueLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = type.icon,
            contentDescription = null,
            tint = MidnightNavy,
            modifier = Modifier.size(30.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = type.title,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
          ),
          color = BrandBlack,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = type.subtitle,
          style = MaterialTheme.typography.labelMedium,
          color = TextSecondary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Explanation Box
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = WarmCreamDark,
          border = BorderStroke(1.dp, WarmCreamBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = type.reasonTitle,
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = BrandBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = type.reasonBody,
              style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
              color = InkSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = IconlyIcons.Lock,
                contentDescription = null,
                tint = DustyBlue,
                modifier = Modifier.size(13.dp)
              )
              Text(
                text = type.privacyNote,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.sp
                ),
                color = DustyBlue,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = {
            onDismiss()
            onAllow()
          },
          shape = RoundedCornerShape(18.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MidnightNavy,
            contentColor = PureWhite
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
        ) {
          Text(
            text = "Continue & Allow Access",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(18.dp),
          border = BorderStroke(1.dp, CardBorder),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlack),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          Text("Not Now", fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}
