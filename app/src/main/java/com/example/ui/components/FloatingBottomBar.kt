package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.Screen

@Composable
fun FloatingBottomBar(
  currentScreen: Screen,
  onNavigate: (Screen) -> Unit,
  onAddClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 24.dp, vertical = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(36.dp),
      color = PureWhite,
      shadowElevation = 8.dp,
      border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .height(64.dp)
    ) {
      Row(
        modifier = Modifier
          .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // 1. Home Tab
        val isHome = currentScreen is Screen.Home
        NavItem(
          icon = IconlyIcons.Home,
          label = "Home",
          isActive = isHome,
          onClick = { onNavigate(Screen.Home) }
        )

        // 2. Ask Memory / Search Tab
        val isAsk = currentScreen is Screen.Ask
        NavItem(
          icon = IconlyIcons.Search,
          label = "Ask AI",
          isActive = isAsk,
          onClick = { onNavigate(Screen.Ask) }
        )

        // 3. Central Add Memory Action
        Box(
          modifier = Modifier
            .size(48.dp)
            .bouncingClickable(
              pressedScale = 0.90f,
              onClickLabel = "Add Memory",
              onClick = onAddClick
            )
            .clip(CircleShape)
            .background(BrandBlack),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = IconlyIcons.Add,
            contentDescription = "Add Memory",
            tint = PureWhite,
            modifier = Modifier.size(24.dp)
          )
        }

        // 4. Memories / Timeline Tab
        val isMemories = currentScreen is Screen.Memories
        NavItem(
          icon = IconlyIcons.Document,
          label = "Memories",
          isActive = isMemories,
          onClick = { onNavigate(Screen.Memories) }
        )

        // 5. Collections / Profile Tab
        val isProfile = currentScreen is Screen.Profile || currentScreen is Screen.Collections
        NavItem(
          icon = IconlyIcons.Profile,
          label = "Profile",
          isActive = isProfile,
          onClick = { onNavigate(Screen.Profile) }
        )
      }
    }
  }
}

@Composable
private fun NavItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  isActive: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(48.dp)
      .bouncingClickable(
        pressedScale = 0.88f,
        onClickLabel = label,
        onClick = onClick
      )
      .clip(CircleShape)
      .background(if (isActive) BrandBlack else Color.Transparent),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (isActive) PureWhite else TextMuted,
      modifier = Modifier.size(22.dp)
    )
  }
}
