package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

const val SUPPORT_EMAIL_ADDRESS = "bintangjanuarda0809@gmail.com"

/**
 * Direct Customer Support Dialog:
 * Allows the user to compose a support message or bug report and transmit it
 * directly to bintangjanuarda0809@gmail.com using Android's native mail intent.
 */
@Composable
fun SupportEmailDialog(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var subject by remember { mutableStateOf("Support Request: MemoryOS") }
  var message by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Question / Help") }

  val categories = listOf("Question / Help", "Feature Request", "Bug Report", "General Feedback")

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
          .verticalScroll(rememberScrollState())
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DustyBlueLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = IconlyIcons.Info,
                contentDescription = null,
                tint = MidnightNavy,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Customer Support",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MidnightNavy
              )
              Text(
                text = SUPPORT_EMAIL_ADDRESS,
                style = MaterialTheme.typography.bodySmall,
                color = DustyBlue
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = IconlyIcons.Close,
              contentDescription = "Close",
              tint = TextMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category selection
        Text(
          text = "Category",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = TextSecondary,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          categories.take(2).forEach { cat ->
            val isSelected = category == cat
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) MidnightNavy else WarmCreamDark)
                .border(
                  BorderStroke(1.dp, if (isSelected) MidnightNavy else WarmCreamBorder),
                  RoundedCornerShape(12.dp)
                )
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              TextButton(
                onClick = {
                  category = cat
                  subject = "$cat: MemoryOS"
                },
                contentPadding = PaddingValues(0.dp)
              ) {
                Text(
                  text = cat,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = if (isSelected) PureWhite else TextPrimary
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subject Input
        Text(
          text = "Subject",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = TextSecondary,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = subject,
          onValueChange = { subject = it },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MidnightNavy,
            unfocusedBorderColor = CardBorder
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Message Body Input
        Text(
          text = "Message",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = TextSecondary,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = message,
          onValueChange = { message = it },
          placeholder = {
            Text(
              text = "Describe your question, request, or issue in detail...",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )
          },
          minLines = 4,
          maxLines = 8,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MidnightNavy,
            unfocusedBorderColor = CardBorder
          )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Primary: Launch Native Email Client via Intent
        Button(
          onClick = {
            val mailBody = buildString {
              append(message)
              append("\n\n---\n")
              append("App: MemoryOS v1.0.0 Standalone\n")
              append("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n")
              append("Android: ${android.os.Build.VERSION.RELEASE}\n")
            }

            val intent = Intent(Intent.ACTION_SENDTO).apply {
              data = Uri.parse("mailto:$SUPPORT_EMAIL_ADDRESS")
              putExtra(Intent.EXTRA_SUBJECT, subject)
              putExtra(Intent.EXTRA_TEXT, mailBody)
            }

            try {
              context.startActivity(Intent.createChooser(intent, "Send Email to Customer Support"))
              onDismiss()
            } catch (_: Exception) {
              Toast.makeText(context, "No email client installed. Address copied to clipboard.", Toast.LENGTH_LONG).show()
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              clipboard.setPrimaryClip(ClipData.newPlainText("Customer Support Email", SUPPORT_EMAIL_ADDRESS))
            }
          },
          enabled = message.isNotBlank(),
          shape = RoundedCornerShape(18.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MidnightNavy,
            contentColor = PureWhite
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
        ) {
          Icon(
            imageVector = IconlyIcons.Send,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Compose & Send to Support",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary: Copy Email Address to Clipboard
        OutlinedButton(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Customer Support Email", SUPPORT_EMAIL_ADDRESS))
            Toast.makeText(context, "Support email copied: $SUPPORT_EMAIL_ADDRESS", Toast.LENGTH_SHORT).show()
          },
          shape = RoundedCornerShape(18.dp),
          border = BorderStroke(1.dp, CardBorder),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlack),
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
        ) {
          Icon(
            imageVector = IconlyIcons.Copy,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Copy Email Address", fontSize = 13.sp)
        }
      }
    }
  }
}
