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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.DustyBlueLight
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemorySheet(
  viewModel: com.example.ui.viewmodel.MemoryViewModel? = null,
  onDismiss: () -> Unit,
  onSave: (rawText: String, memoryType: String, category: String, tags: String) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val context = androidx.compose.ui.platform.LocalContext.current
  val existingDraft = remember { viewModel?.getDraft() }
  var selectedType by remember { mutableStateOf(existingDraft?.second ?: "NOTE") }
  var selectedCategory by remember { mutableStateOf("Personal") }
  var tagsText by remember { mutableStateOf("") }
  var inputText by remember { mutableStateOf(existingDraft?.first ?: "") }

  val memoryTypes = listOf(
    "NOTE" to "Quick Note",
    "RECEIPT" to "Receipt",
    "TICKET" to "Ticket",
    "RESERVATION" to "Booking",
    "TRAVEL" to "Travel"
  )

  val categories = listOf("Personal", "Work", "Ideas", "Travel")
  val sampleTags = listOf("Meeting", "Project", "Important", "Reading")

  ModalBottomSheet(
    onDismissRequest = {
      if (inputText.isNotBlank()) {
        viewModel?.saveDraft(inputText, selectedType)
      }
      onDismiss()
    },
    sheetState = sheetState,
    containerColor = PureWhite,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .padding(bottom = 32.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
          Text(
            text = "Add Memory",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.3).sp
            ),
            color = TextPrimary
          )

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (inputText.isNotBlank()) {
            Text(
              text = "Draft saved",
              style = MaterialTheme.typography.labelSmall,
              color = DustyBlue,
              modifier = Modifier.padding(end = 8.dp)
            )
          }
          IconButton(onClick = {
            if (inputText.isNotBlank()) {
              viewModel?.saveDraft(inputText, selectedType)
            }
            onDismiss()
          }) {
            Icon(imageVector = IconlyIcons.Close, contentDescription = "Close", tint = TextMuted)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Choose Memory Type",
        style = MaterialTheme.typography.labelMedium,
        color = TextSecondary
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Type Selector Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        memoryTypes.forEach { (typeKey, label) ->
          val isSelected = selectedType == typeKey
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) MidnightNavy else WarmIvory)
              .clickable { selectedType = typeKey }
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              color = if (isSelected) PureWhite else TextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "Paste or type any information, ticket confirmation, or note:",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
      )

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        placeholder = {
          Text(
            text = when (selectedType) {
              "RECEIPT" -> "e.g. Dinner at Sushi Sho, Tokyo. Total ¥12,400 with tax on 15 Oct."
              "TICKET" -> "e.g. Flight SQ-958 Singapore to Tokyo Narita, Gate B4, Seat 22K."
              "RESERVATION" -> "e.g. Booked Park Hyatt Tokyo for 3 nights checking in Nov 2."
              else -> "e.g. Met David at Blue Bottle Coffee. Recommended reading 'Thinking Fast & Slow'."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = WarmIvory,
          unfocusedContainerColor = WarmIvory,
          focusedBorderColor = DustyBlue,
          unfocusedBorderColor = CardBorder
        )
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Camera Capture Action Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(DustyBlueLight)
          .clickable {
            onDismiss()
            viewModel?.requestCameraCapture(context)
          }
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = IconlyIcons.Camera,
          contentDescription = "Capture Photo",
          tint = DustyBlue,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Capture Photo with Camera",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = DustyBlue
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Auto-Extraction badge note
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(DustyBlueLight)
          .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = IconlyIcons.Verified,
          contentDescription = null,
          tint = DustyBlue,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Intelligent Memory Engine — Automatically extracts merchant, dates, places, amounts, and tags.",
          style = MaterialTheme.typography.labelSmall,
          color = MidnightNavy
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Category",
        style = MaterialTheme.typography.labelMedium,
        color = TextSecondary
      )
      Spacer(modifier = Modifier.height(8.dp))

      // Category Chips ('Personal', 'Work', 'Ideas')
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categories.forEach { cat ->
          val isSelected = selectedCategory == cat
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) MidnightNavy else WarmIvory)
              .clickable { selectedCategory = cat }
              .padding(horizontal = 14.dp, vertical = 7.dp)
          ) {
            Text(
              text = cat,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              color = if (isSelected) PureWhite else TextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Tags (e.g. Meeting, Project, Travel)",
        style = MaterialTheme.typography.labelMedium,
        color = TextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))

      OutlinedTextField(
        value = tagsText,
        onValueChange = { tagsText = it },
        placeholder = {
          Text(text = "Add comma-separated tags...", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = WarmIvory,
          unfocusedContainerColor = WarmIvory,
          focusedBorderColor = DustyBlue,
          unfocusedBorderColor = CardBorder
        )
      )

      Spacer(modifier = Modifier.height(18.dp))

      Button(
        onClick = {
          if (inputText.isNotBlank()) {
            onSave(inputText, selectedType, selectedCategory, tagsText)
          }
        },
        enabled = inputText.isNotBlank(),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MidnightNavy,
          contentColor = PureWhite
        )
      ) {
        Icon(imageVector = IconlyIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Save Memory & Understand",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
      }
    }
  }
}
