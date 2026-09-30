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
 * High-friction security confirmation modal that ensures the user explicitly confirms
 * destructive operations such as purging all memories or clearing the local database.
 */
@Composable
fun ClearConfirmationDialog(
  title: String = "Permanent Data Deletion",
  description: String = "Are you absolutely sure you want to permanently delete your stored memories and reset your local vault?",
  confirmButtonLabel: String = "Yes, Delete Everything",
  onConfirm: () -> Unit,
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
        // Warning Icon Badge
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(TerracottaRed.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = IconlyIcons.Delete,
            contentDescription = null,
            tint = TerracottaRed,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
          ),
          color = TerracottaRed,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium.copy(
            lineHeight = 22.sp
          ),
          color = InkSecondary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bulleted Warnings
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = WarmCreamDark,
          border = BorderStroke(1.dp, WarmCreamBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "⚠️ Irreversible Actions:",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = BrandBlack
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "• All journals, receipts, and notes will be permanently erased.\n• Cached voice audio and transcription models will be cleared.\n• Recent search queries and custom tags will be reset.",
              style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
              color = InkSecondary
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Confirmation Actions
        Button(
          onClick = {
            onConfirm()
            onDismiss()
          },
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = TerracottaRed,
            contentColor = PureWhite
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          Text(
            text = confirmButtonLabel,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.dp, CardBorder),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlack),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          Text("Cancel · Keep My Memories", fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}
