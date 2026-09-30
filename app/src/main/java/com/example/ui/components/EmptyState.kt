package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Data model for contextual tips or capability items shown within an empty state.
 */
data class EmptyStateTip(
  val icon: ImageVector,
  val title: String,
  val subtitle: String,
  val onClick: (() -> Unit)? = null
)

/**
 * Reusable, brand-aligned EmptyState component.
 * Adheres strictly to MemoryOS's cream (#F7F3ED) and black (#000000) brand color palette.
 *
 * Incorporates:
 * - Minimalist vector icon inside a refined cream/black badge
 * - Descriptive instructional typography (tagline, title, actionable body)
 * - Clear primary action button with >= 48dp touch target
 * - Optional capability guide tips and privacy/ease value pills
 */
@Composable
fun EmptyState(
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  tagline: String? = null,
  iconVector: ImageVector? = IconlyIcons.Add,
  iconSize: Dp = 24.dp,
  primaryActionLabel: String? = "Capture First Memory",
  primaryActionIcon: ImageVector? = IconlyIcons.Add,
  onPrimaryAction: (() -> Unit)? = null,
  secondaryActionLabel: String? = null,
  secondaryActionIcon: ImageVector? = null,
  onSecondaryAction: (() -> Unit)? = null,
  tips: List<EmptyStateTip> = emptyList(),
  showValuePills: Boolean = false,
  containerColor: Color = WarmCreamLight,
  borderColor: Color = WarmCreamBorder,
  asCard: Boolean = true
) {
  val content = @Composable {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(if (asCard) 24.dp else 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Minimalist Icon Badge in Cream & Black
      if (iconVector != null) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(WarmCreamDark)
            .border(BorderStroke(1.dp, WarmCreamBorder), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = BrandBlack,
            modifier = Modifier.size(iconSize)
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Optional Tagline / Section Indicator
      if (!tagline.isNullOrBlank()) {
        Text(
          text = tagline.uppercase(),
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
          ),
          color = InkMuted,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
      }

      // Primary Title
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.3).sp
        ),
        color = BrandBlack,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Descriptive Instructional Body
      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium.copy(
          lineHeight = 22.sp
        ),
        color = InkSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(0.95f)
      )

      // Action Buttons (Primary & Secondary)
      if (primaryActionLabel != null || secondaryActionLabel != null) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (primaryActionLabel != null && onPrimaryAction != null) {
            val cleanPrimaryLabel = primaryActionLabel.removePrefix("+").trim()
            Button(
              onClick = onPrimaryAction,
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("empty_state_primary_button"),
              shape = RoundedCornerShape(24.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = BrandBlack,
                contentColor = BrandWhite
              ),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                if (primaryActionIcon != null) {
                  Icon(
                    imageVector = primaryActionIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                  text = cleanPrimaryLabel,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.2.sp
                  ),
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }

          if (secondaryActionLabel != null && onSecondaryAction != null) {
            val cleanSecondaryLabel = secondaryActionLabel.removePrefix("+").trim()
            OutlinedButton(
              onClick = onSecondaryAction,
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("empty_state_secondary_button"),
              shape = RoundedCornerShape(24.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
              border = BorderStroke(1.dp, WarmCreamBorder),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = BrandBlack
              )
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                if (secondaryActionIcon != null) {
                  Icon(
                    imageVector = secondaryActionIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                  text = cleanSecondaryLabel,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                  ),
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }
        }
      }

      // Optional Contextual Guidance / Capability Tips
      if (tips.isNotEmpty()) {
        Spacer(modifier = Modifier.height(20.dp))
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          tips.forEach { tip ->
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = WarmCream.copy(alpha = 0.5f)),
              border = BorderStroke(1.dp, CardBorder),
              modifier = Modifier
                .fillMaxWidth()
                .then(
                  if (tip.onClick != null) Modifier.clickable(onClick = tip.onClick)
                  else Modifier
                )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .padding(end = 12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PureWhite),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = tip.icon,
                    contentDescription = null,
                    tint = MidnightNavy,
                    modifier = Modifier.size(16.dp)
                  )
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = tip.title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = BrandBlack
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = tip.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                  )
                }
              }
            }
          }
        }
      }

      // Optional Value Pills (Security, Privacy, Ease)
      if (showValuePills) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          EmptyStateValuePill(icon = IconlyIcons.Voice, text = "Zero manual filing")
          EmptyStateValuePill(icon = IconlyIcons.Verified, text = "Auto-structured")
          EmptyStateValuePill(icon = IconlyIcons.Lock, text = "100% Private")
        }
      }
    }
  }

  if (asCard) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = containerColor),
      border = BorderStroke(1.dp, borderColor),
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
      content()
    }
  } else {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 16.dp),
      contentAlignment = Alignment.Center
    ) {
      content()
    }
  }
}

@Composable
private fun EmptyStateValuePill(icon: ImageVector, text: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = TextMuted,
      modifier = Modifier.size(13.dp)
    )
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = TextMuted
    )
  }
}
