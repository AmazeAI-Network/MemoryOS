package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MemoryEntity
import com.example.ui.components.EmptyState
import com.example.ui.components.IconlyIcons
import com.example.ui.components.MemoryCard
import com.example.ui.components.MemoryOSEmblem
import com.example.ui.components.SkeletonMemoryCard
import com.example.ui.components.bouncingClickable
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.DustyBlueLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TerracottaLight
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmCreamBorder
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.MemoryViewModel
import com.example.ui.viewmodel.Screen

/**
 * LazyColumn-based Memory Stream Screen.
 *
 * Observes Room database state and provides:
 * 1. Category filtering: 'All', 'Work', 'Personal', 'Ideas', 'Travel'
 * 2. Tag filtering: '#Meeting', '#Notes', '#Receipts', etc.
 * 3. Full-text search across Room database
 * 4. Rich memory stream displaying formatted dates, categories, tags, and content previews
 */
@Composable
fun MemoryStreamScreen(
  viewModel: MemoryViewModel,
  modifier: Modifier = Modifier
) {
  val memories by viewModel.filteredMemories.collectAsStateWithLifecycle()
  val allMemories by viewModel.allMemories.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
  val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
  val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

  // Extract top distinct tags dynamically from existing Room memories
  val popularTags = androidx.compose.runtime.remember(allMemories) {
    allMemories.flatMap { mem ->
      mem.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }.distinct().take(6)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(WarmIvory)
      .statusBarsPadding(),
    contentPadding = PaddingValues(bottom = 100.dp)
  ) {
    // 1. Header & Title Section
    item {
      Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Memory Stream",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
              ),
              color = TextPrimary
            )
            Text(
              text = if (allMemories.isEmpty()) "Your stream is ready" else "${allMemories.size} memories recorded",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary
            )
          }

          // Quick New Memory Trigger
          Box(
            modifier = Modifier
              .size(38.dp)
              .bouncingClickable(pressedScale = 0.90f) { viewModel.openAddSheet() }
              .clip(CircleShape)
              .background(BrandBlack),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = IconlyIcons.Add,
              contentDescription = "New Memory",
              tint = PureWhite,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Full-Text Search Input Box
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = PureWhite,
          border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
          shadowElevation = 1.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = IconlyIcons.Search,
              contentDescription = "Search",
              tint = DustyBlue,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { viewModel.setSearchQuery(it) },
              placeholder = {
                Text(
                  text = "Search Work, Personal, Ideas, notes...",
                  color = TextMuted,
                  style = MaterialTheme.typography.bodyMedium
                )
              },
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
              ),
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
              modifier = Modifier.weight(1f),
              singleLine = true
            )

            if (searchQuery.isNotBlank()) {
              IconButton(
                onClick = { viewModel.setSearchQuery("") },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = IconlyIcons.Close,
                  contentDescription = "Clear search",
                  tint = TextMuted,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Category Filter Chips ('All', 'Work', 'Personal', 'Ideas')
        Text(
          text = "Categories",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(availableCategories) { cat ->
            val isSelected = selectedCategory.equals(cat, ignoreCase = true)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) MidnightNavy else PureWhite)
                .then(
                  if (!isSelected) Modifier.border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                  else Modifier
                )
                .bouncingClickable(pressedScale = 0.92f) {
                  viewModel.setCategory(cat)
                }
                .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
              Text(
                text = cat,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) PureWhite else TextPrimary
              )
            }
          }
        }

        // 4. Tag Filter Row (if tags exist)
        if (popularTags.isNotEmpty()) {
          Spacer(modifier = Modifier.height(10.dp))
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(popularTags) { tag ->
              val isTagSelected = selectedTag?.equals(tag, ignoreCase = true) == true
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isTagSelected) WarmCream else PureWhite)
                  .border(
                    width = 1.dp,
                    color = if (isTagSelected) WarmCreamBorder else CardBorder,
                    shape = RoundedCornerShape(12.dp)
                  )
                  .bouncingClickable(pressedScale = 0.92f) {
                    if (isTagSelected) {
                      viewModel.setTag(null)
                    } else {
                      viewModel.setTag(tag)
                    }
                  }
                  .padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Text(
                  text = if (tag.startsWith("#")) tag else "#$tag",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isTagSelected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (isTagSelected) BrandBlack else TextSecondary
                )
              }
            }
          }
        }
      }
    }

    // 5. Memory Stream Content
    if (memories.isEmpty()) {
      item {
        val hasActiveFilter = !selectedCategory.equals("All", ignoreCase = true) || !selectedTag.isNullOrBlank() || searchQuery.isNotBlank()
        EmptyState(
          title = if (hasActiveFilter) "No matching memories" else "Your Memory Library",
          description = if (hasActiveFilter) {
            "No entries matched '$selectedCategory'${selectedTag?.let { " with tag #$it" } ?: ""}${if (searchQuery.isNotBlank()) " for '$searchQuery'" else ""}. Try selecting 'All' or clearing filters."
          } else {
            "Your personal archive is ready. Record thoughts, ideas, work meetings, receipts, or travel plans to see them stream here."
          },
          primaryActionLabel = if (hasActiveFilter) "Reset Filters" else "+ New Memory",
          primaryActionIcon = if (hasActiveFilter) IconlyIcons.Refresh else IconlyIcons.Add,
          onPrimaryAction = {
            if (hasActiveFilter) {
              viewModel.setCategory("All")
              viewModel.setTag(null)
              viewModel.setSearchQuery("")
            } else {
              viewModel.openAddSheet()
            }
          }
        )
      }
    } else {
      items(
        items = memories,
        key = { it.id }
      ) { memory ->
        Box(
          modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 6.dp)
        ) {
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
}
