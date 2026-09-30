package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WarmIvory

/**
 * Shimmer animation modifier for skeleton screens.
 * Replaces static loading spinners with fluid, hardware-accelerated animated placeholders.
 */
fun Modifier.shimmerLoading(
  visible: Boolean = true,
  shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
): Modifier = composed {
  if (!visible) return@composed this

  val transition = rememberInfiniteTransition(label = "ShimmerTransition")
  val translateAnim by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ShimmerTranslation"
  )

  val shimmerColors = listOf(
    Color(0xFFE8E2D8).copy(alpha = 0.6f),
    Color(0xFFF7F3ED),
    Color(0xFFE8E2D8).copy(alpha = 0.6f)
  )

  val brush = Brush.linearGradient(
    colors = shimmerColors,
    start = Offset(x = translateAnim - 300f, y = translateAnim - 300f),
    end = Offset(x = translateAnim, y = translateAnim)
  )

  this
    .clip(shape)
    .background(brush)
}

/**
 * Skeleton placeholder for Memory cards during search or filtering operations.
 */
@Composable
fun SkeletonMemoryCard(
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(width = 80.dp, height = 24.dp)
            .shimmerLoading(shape = RoundedCornerShape(12.dp))
        )
        Box(
          modifier = Modifier
            .size(24.dp)
            .shimmerLoading(shape = CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth(0.85f)
          .height(20.dp)
          .shimmerLoading()
      )

      Spacer(modifier = Modifier.height(8.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth(0.65f)
          .height(14.dp)
          .shimmerLoading()
      )

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Box(
          modifier = Modifier
            .size(width = 110.dp, height = 16.dp)
            .shimmerLoading()
        )
        Box(
          modifier = Modifier
            .size(width = 60.dp, height = 20.dp)
            .shimmerLoading(shape = RoundedCornerShape(10.dp))
        )
      }
    }
  }
}

/**
 * Skeleton placeholder for Ask Memory grounded responses.
 */
@Composable
fun SkeletonAskAnswer(
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(width = 130.dp, height = 24.dp)
            .shimmerLoading(shape = RoundedCornerShape(12.dp))
        )
        Box(
          modifier = Modifier
            .size(width = 70.dp, height = 20.dp)
            .shimmerLoading(shape = RoundedCornerShape(8.dp))
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(18.dp)
          .shimmerLoading()
      )
      Spacer(modifier = Modifier.height(8.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .height(18.dp)
          .shimmerLoading()
      )
      Spacer(modifier = Modifier.height(8.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth(0.75f)
          .height(18.dp)
          .shimmerLoading()
      )

      Spacer(modifier = Modifier.height(20.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
          modifier = Modifier
            .size(width = 120.dp, height = 48.dp)
            .shimmerLoading(shape = RoundedCornerShape(14.dp))
        )
        Box(
          modifier = Modifier
            .size(width = 120.dp, height = 48.dp)
            .shimmerLoading(shape = RoundedCornerShape(14.dp))
        )
      }
    }
  }
}

/**
 * Skeleton placeholder for Memory Detail view to prevent blank screens or flickers.
 */
@Composable
fun SkeletonMemoryDetail(
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 16.dp)
  ) {
    Box(
      modifier = Modifier
        .size(width = 90.dp, height = 28.dp)
        .shimmerLoading(shape = RoundedCornerShape(14.dp))
    )
    Spacer(modifier = Modifier.height(16.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth(0.9f)
        .height(32.dp)
        .shimmerLoading()
    )
    Spacer(modifier = Modifier.height(12.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth(0.5f)
        .height(16.dp)
        .shimmerLoading()
    )
    Spacer(modifier = Modifier.height(24.dp))
    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = PureWhite),
      border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .shimmerLoading()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(16.dp)
            .shimmerLoading()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(16.dp)
            .shimmerLoading()
        )
      }
    }
  }
}
