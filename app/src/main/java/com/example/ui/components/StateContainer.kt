package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory

/**
 * Enterprise standardized UI State interface for Jetpack Compose screens.
 * Enforces dynamic handling of 4 canonical UI states:
 * Loading (Shimmer), Empty (Illustration), Error (Retry affordance), and Success.
 */
sealed interface ScreenState<out T> {
  data object Loading : ScreenState<Nothing>

  data class Empty(
    val title: String = "No Items Found",
    val description: String = "Preserve your first memory or adjust your filters.",
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
  ) : ScreenState<Nothing>

  data class Error(
    val message: String = "Unable to load data at this time.",
    val technicalDetails: String? = null,
    val onRetry: (() -> Unit)? = null
  ) : ScreenState<Nothing>

  data class Success<T>(val data: T) : ScreenState<T>
}

/**
 * Standardized Screen Container Composable.
 * Seamlessly transitions between Loading, Empty, Error, and Success states
 * using hardware-accelerated animations and consistent Material 3 styling.
 */
@Composable
fun <T> StandardStateContainer(
  state: ScreenState<T>,
  modifier: Modifier = Modifier,
  loadingContent: (@Composable () -> Unit)? = null,
  content: @Composable (data: T) -> Unit
) {
  AnimatedContent(
    targetState = state,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    label = "StandardStateContainerTransition",
    modifier = modifier.fillMaxSize()
  ) { targetState ->
    when (targetState) {
      is ScreenState.Loading -> {
        if (loadingContent != null) {
          loadingContent()
        } else {
          StandardLoadingState()
        }
      }

      is ScreenState.Empty -> {
        StandardEmptyState(
          title = targetState.title,
          description = targetState.description,
          actionLabel = targetState.actionLabel,
          onAction = targetState.onAction
        )
      }

      is ScreenState.Error -> {
        StandardErrorState(
          message = targetState.message,
          technicalDetails = targetState.technicalDetails,
          onRetry = targetState.onRetry
        )
      }

      is ScreenState.Success -> {
        content(targetState.data)
      }
    }
  }
}

@Composable
fun StandardLoadingState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    SkeletonMemoryCard()
    SkeletonMemoryCard()
    SkeletonMemoryCard()
  }
}

@Composable
fun StandardEmptyState(
  title: String,
  description: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = PureWhite),
      border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(WarmIvory)
            .border(1.dp, CardBorder, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = IconlyIcons.FolderOpen,
            contentDescription = null,
            tint = DustyBlue,
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          textAlign = TextAlign.Center,
          lineHeight = 22.sp
        )

        if (actionLabel != null && onAction != null) {
          Spacer(modifier = Modifier.height(24.dp))
          Button(
            onClick = onAction,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy, contentColor = PureWhite),
            modifier = Modifier.height(48.dp)
          ) {
            Text(actionLabel, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}

@Composable
fun StandardErrorState(
  message: String,
  technicalDetails: String? = null,
  onRetry: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = PureWhite),
      border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(TerracottaRed.copy(alpha = 0.10f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = IconlyIcons.Info,
            contentDescription = "Error",
            tint = TerracottaRed,
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = "Something went wrong",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = message,
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          textAlign = TextAlign.Center
        )

        if (technicalDetails != null) {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = technicalDetails,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = TextMuted,
            textAlign = TextAlign.Center
          )
        }

        if (onRetry != null) {
          Spacer(modifier = Modifier.height(24.dp))
          Button(
            onClick = onRetry,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy, contentColor = PureWhite),
            modifier = Modifier.height(48.dp)
          ) {
            Icon(imageVector = IconlyIcons.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Try Again", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
