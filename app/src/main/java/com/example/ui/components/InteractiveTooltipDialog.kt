package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

data class TooltipStep(
  val featureName: String,
  val icon: ImageVector,
  val title: String,
  val explanation: String,
  val tip: String
)

/**
 * Interactive Tooltips Pop-up:
 * Highlights specific buttons and features when a user explores a screen,
 * providing actionable "learn-by-doing" guidance.
 */
@Composable
fun InteractiveTooltipDialog(
  onDismiss: () -> Unit
) {
  val steps = remember {
    listOf(
      TooltipStep(
        featureName = "Voice Reflection",
        icon = IconlyIcons.Voice,
        title = "Capture Thoughts by Speaking",
        explanation = "Tap the microphone icon anywhere in the app to speak. Our local transcription engine organizes speech into structured notes.",
        tip = "💡 Tip: Speak naturally—dates, dollar amounts, and tags are extracted automatically."
      ),
      TooltipStep(
        featureName = "Semantic Memory Search",
        icon = IconlyIcons.Search,
        title = "Ask Questions Naturally",
        explanation = "Use the Search tab to ask open questions like 'Where did I stay in Tokyo?' or 'Receipt from yesterday'.",
        tip = "💡 Tip: Even without exact keywords, semantic reasoning locates relevant memories."
      ),
      TooltipStep(
        featureName = "Smart Collections",
        icon = IconlyIcons.Folder,
        title = "Organized Into Clusters",
        explanation = "Group memories into thematic collections for travel, work meetings, home projects, and warranties.",
        tip = "💡 Tip: You can query an entire collection at once using the 'Ask' action."
      )
    )
  }

  var currentStepIndex by remember { mutableStateOf(0) }
  val currentStep = steps[currentStepIndex]

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
        // Step tracker & Dismiss
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(DustyBlueLight)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "FEATURE TOUR · ${currentStepIndex + 1}/${steps.size}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = DustyBlue
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = IconlyIcons.Close,
              contentDescription = "Dismiss",
              tint = TextMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feature Highlight Icon
        Box(
          modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(MidnightNavy),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = currentStep.icon,
            contentDescription = null,
            tint = PureWhite,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = currentStep.title,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.2).sp
          ),
          color = BrandBlack,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = currentStep.explanation,
          style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
          color = InkSecondary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = WarmCreamDark,
          border = BorderStroke(1.dp, WarmCreamBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = currentStep.tip,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MidnightNavy,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            textAlign = TextAlign.Center
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (currentStepIndex > 0) {
            OutlinedButton(
              onClick = { currentStepIndex-- },
              shape = RoundedCornerShape(18.dp),
              border = BorderStroke(1.dp, CardBorder),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlack),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
            ) {
              Text("Previous")
            }
          }

          Button(
            onClick = {
              if (currentStepIndex < steps.size - 1) {
                currentStepIndex++
              } else {
                onDismiss()
              }
            },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MidnightNavy,
              contentColor = PureWhite
            ),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
          ) {
            Text(
              text = if (currentStepIndex < steps.size - 1) "Next Feature" else "Got It, Thanks!",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
