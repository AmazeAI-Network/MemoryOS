package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.IconlyIcons
import com.example.ui.components.MemoryCard
import com.example.ui.components.bouncingClickable
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.MemoryViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun CollectionDetailScreen(
  viewModel: MemoryViewModel,
  modifier: Modifier = Modifier
) {
  val collection by viewModel.selectedCollection.collectAsStateWithLifecycle()
  val allMemories by viewModel.allMemories.collectAsStateWithLifecycle()

  val matchingMemories = allMemories.filter { it.collectionId == collection?.id }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(WarmIvory)
      .statusBarsPadding(),
    contentPadding = PaddingValues(bottom = 100.dp)
  ) {
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .bouncingClickable(pressedScale = 0.88f) { viewModel.navigateTo(Screen.Collections) }
            .clip(CircleShape)
            .background(PureWhite),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = IconlyIcons.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }
      }

      Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
          text = collection?.title ?: "Collection",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
          ),
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = collection?.description ?: "",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(18.dp))
      }
    }

    // Performance: Key items by entity ID to prevent recomposing entire collection view on bookmark toggle
    items(matchingMemories, key = { it.id }) { memory ->
      Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
        MemoryCard(
          memory = memory,
          onClick = { viewModel.navigateTo(Screen.MemoryDetail(memory.id)) },
          onFavoriteClick = { viewModel.toggleFavorite(memory.id, memory.isFavorite) },
          onPressStart = { viewModel.prefetchMemory(memory.id) }
        )
      }
    }
  }
}
