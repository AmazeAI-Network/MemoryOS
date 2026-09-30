package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.data.model.CollectionEntity
import com.example.ui.components.EmptyState
import com.example.ui.components.IconlyIcons
import com.example.ui.components.MemoryOSEmblem
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.DustyBlueLight
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.MemoryViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun CollectionsScreen(
  viewModel: MemoryViewModel,
  modifier: Modifier = Modifier
) {
  val collections by viewModel.collections.collectAsStateWithLifecycle()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(WarmIvory)
      .statusBarsPadding(),
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
  ) {
    item {
      Column {
        Text(
          text = "Collections",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
          ),
          color = TextPrimary
        )
        Text(
          text = "Curated smart memory clusters",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    if (collections.isEmpty()) {
      item {
        EmptyState(
          title = "No Collections Yet",
          description = "Collections group related memories, such as trips, projects, or recurring receipts. Save your first memories to create collections.",
          iconVector = IconlyIcons.Folder,
          primaryActionLabel = "Create Memory",
          primaryActionIcon = IconlyIcons.Add,
          onPrimaryAction = { viewModel.openAddSheet() },
          asCard = true
        )
      }
    } else {
      // Performance: Key by collection ID prevents full LazyColumn re-rendering
      items(collections, key = { it.id }) { collection ->
        CollectionItemCard(
          collection = collection,
          onClick = { viewModel.navigateTo(Screen.CollectionDetail(collection.id)) },
          onAskClick = {
            viewModel.setAskQuery("Tell me about my ${collection.title}")
            viewModel.navigateTo(Screen.Ask)
            viewModel.submitAskQuestion("Tell me about my ${collection.title}")
          }
        )
        Spacer(modifier = Modifier.height(14.dp))
      }
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@Composable
private fun CollectionItemCard(
  collection: CollectionEntity,
  onClick: () -> Unit,
  onAskClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DustyBlueLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = IconlyIcons.Folder,
            contentDescription = null,
            tint = DustyBlue,
            modifier = Modifier.size(24.dp)
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(WarmIvory)
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(
            text = "${collection.memoryCount} memories",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = TextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = collection.title,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.2).sp
        ),
        color = TextPrimary
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = collection.description,
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )

      Spacer(modifier = Modifier.height(16.dp))

      // "Ask this collection" Button
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(42.dp)
          .clip(RoundedCornerShape(21.dp))
          .background(DustyBlueLight)
          .clickable(onClick = onAskClick),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = IconlyIcons.Search,
            contentDescription = null,
            tint = DustyBlue,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Ask this collection",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = DustyBlue
          )
        }
      }
    }
  }
}
