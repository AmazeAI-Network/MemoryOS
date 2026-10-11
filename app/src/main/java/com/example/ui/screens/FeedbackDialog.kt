package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.IconlyIcons
import com.example.ui.components.MemoryOSEmblem
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory

@Composable
fun FeedbackDialog(
  screenshot: Bitmap?,
  isSubmitting: Boolean,
  onSubmit: (category: String, message: String, includeScreenshot: Boolean) -> Unit,
  onEmailFallback: (category: String, message: String) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedCategory by remember { mutableStateOf("Bug Report") }
  var userMessage by remember { mutableStateOf("") }
  var includeScreenshot by remember { mutableStateOf(screenshot != null) }

  val categories = listOf("Bug Report", "Feature Idea", "Performance", "Question")

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
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
              text = "Feedback & Bug Report",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Help us improve MemoryOS",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(androidx.compose.foundation.shape.CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .clickable { onDismiss() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = IconlyIcons.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category selection pills
        Text(
          text = "CATEGORY",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          categories.take(2).forEach { cat ->
            val isSelected = selectedCategory == cat
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .clickable { selectedCategory = cat }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = cat,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          categories.drop(2).forEach { cat ->
            val isSelected = selectedCategory == cat
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .clickable { selectedCategory = cat }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = cat,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Message input
        Text(
          text = "DESCRIBE WHAT HAPPENED",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
          color = TextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = userMessage,
          onValueChange = { userMessage = it },
          placeholder = { Text("What were you trying to do? What happened?", color = TextMuted, style = MaterialTheme.typography.bodySmall) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
          ),
          shape = RoundedCornerShape(16.dp),
          minLines = 3,
          maxLines = 6,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Screenshot attachment toggle
        if (screenshot != null) {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                  bitmap = screenshot.asImageBitmap(),
                  contentDescription = "Screen preview",
                  modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                  contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Include Screenshot",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "Helps identify visual issues",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Switch(
                checked = includeScreenshot,
                onCheckedChange = { includeScreenshot = it },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                  checkedTrackColor = MaterialTheme.colorScheme.primary,
                  uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
              )
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }

        // Hardware Specs summary badge
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Text(
            text = "Specs: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} · Android ${android.os.Build.VERSION.RELEASE}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Submit & Fallback
        Button(
          onClick = { onSubmit(selectedCategory, userMessage, includeScreenshot) },
          enabled = userMessage.isNotBlank() && !isSubmitting,
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          if (isSubmitting) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sending Report...")
          } else {
            Icon(imageVector = IconlyIcons.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send Report", fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
          onClick = { onEmailFallback(selectedCategory, userMessage) },
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
        ) {
          Text("Send via Email Client instead", color = TextSecondary, fontSize = 12.sp)
        }
      }
    }
  }
}
