package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

private data class WalkthroughCard(
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val badge: String,
  val highlight: String,
  val highlightIcon: ImageVector
)

/**
 * Welcome Walkthroughs Pop-up:
 * Swipeable/steppable cards that explain the core value proposition of MemoryOS
 * immediately after registration or on first launch.
 */
@Composable
fun WelcomeWalkthroughDialog(
  onDismiss: () -> Unit
) {
  var currentStep by remember { mutableStateOf(0) }

  val cards = remember {
    listOf(
      WalkthroughCard(
        title = "Private Memory Vault",
        subtitle = "Capture thoughts, receipts, journals, and moments stored strictly in your local AES-256 encrypted database.",
        icon = IconlyIcons.Shield,
        badge = "ZERO-KNOWLEDGE",
        highlight = "No public models or ads ever touch your memories.",
        highlightIcon = IconlyIcons.Lock
      ),
      WalkthroughCard(
        title = "Thought-to-Voice Notes",
        subtitle = "Speak your spontaneous thoughts freely. On-device transcription extracts dates, places, and key entities instantly.",
        icon = IconlyIcons.Voice,
        badge = "NATIVE SPEECH",
        highlight = "Real-time local voice processing with instant summaries.",
        highlightIcon = IconlyIcons.Mic
      ),
      WalkthroughCard(
        title = "Natural Concept Recall",
        subtitle = "Ask your memory anything like 'Where did I leave my passport?' or 'What did I spend on dining this week?'.",
        icon = IconlyIcons.Search,
        badge = "SEMANTIC SEARCH",
        highlight = "Semantic query intelligence finds memories by context, not just keywords.",
        highlightIcon = IconlyIcons.Verified
      )
    )
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = WarmIvory,
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
        // Top Bar: Step Indicator & Skip
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(cards.size) { index ->
              Box(
                modifier = Modifier
                  .height(6.dp)
                  .width(if (index == currentStep) 24.dp else 8.dp)
                  .clip(RoundedCornerShape(3.dp))
                  .background(if (index == currentStep) MidnightNavy else DustyBlueLight)
              )
            }
          }

          TextButton(onClick = onDismiss) {
            Text(
              text = "Skip",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = InkMuted
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Animated Card Content
        AnimatedContent(
          targetState = currentStep,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "walkthrough_animation"
        ) { step ->
          val card = cards[step]
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
          ) {
            // Icon Badge
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PureWhite)
                .border(BorderStroke(1.dp, CardBorder), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = card.icon,
                contentDescription = null,
                tint = MidnightNavy,
                modifier = Modifier.size(32.dp)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Value Tag
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DustyBlueLight)
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = card.badge,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                ),
                color = DustyBlue
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = card.title,
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
              ),
              color = BrandBlack,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = card.subtitle,
              style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp
              ),
              color = InkSecondary,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
              shape = RoundedCornerShape(14.dp),
              color = PureWhite,
              border = BorderStroke(1.dp, CardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = card.highlightIcon,
                  contentDescription = null,
                  tint = MidnightNavy,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = card.highlight,
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                  color = MidnightNavy,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Button(
          onClick = {
            if (currentStep < cards.size - 1) {
              currentStep++
            } else {
              onDismiss()
            }
          },
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
            text = if (currentStep < cards.size - 1) "Next Step" else "Get Started With MemoryOS",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}
