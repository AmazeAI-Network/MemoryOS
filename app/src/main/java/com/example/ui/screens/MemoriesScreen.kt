package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.data.model.SemanticMatch
import com.example.ui.components.IconlyIcons
import com.example.ui.components.MemoryCard
import com.example.ui.components.MemoryOSEmblem
import com.example.ui.components.EmptyState
import com.example.ui.components.SkeletonMemoryCard
import com.example.ui.components.bouncingClickable
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.DustyBlueLight
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.MemoryFilter
import com.example.ui.viewmodel.MemoryViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun MemoriesScreen(
  viewModel: MemoryViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateTo(Screen.Home)
  }

  val memories by viewModel.filteredMemories.collectAsStateWithLifecycle()
  val allMemories by viewModel.allMemories.collectAsStateWithLifecycle()
  val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
  val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
  val sortDateAscending by viewModel.sortDateAscending.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val semanticState by viewModel.semanticState.collectAsStateWithLifecycle()
  val context = androidx.compose.ui.platform.LocalContext.current

  val filterOptions = listOf(
    MemoryFilter.ALL to "All",
    MemoryFilter.TRAVEL to "Travel",
    MemoryFilter.RECEIPTS to "Receipts",
    MemoryFilter.TICKETS to "Tickets",
    MemoryFilter.NOTES to "Notes",
    MemoryFilter.FAVORITES to "Favorites"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(WarmIvory)
      .statusBarsPadding(),
    contentPadding = PaddingValues(bottom = 100.dp)
  ) {
    item {
      Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Your Memories",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
              ),
              color = TextPrimary
            )
            Text(
              text = if (allMemories.isEmpty()) "Your library is ready" else "${allMemories.size} personal memories preserved",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary
            )
          }

          // Search Mode Toggle Chip (Quiet Technology)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (semanticState.isSemanticMode) MidnightNavy else PureWhite)
              .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
              .bouncingClickable(pressedScale = 0.92f) { viewModel.toggleSemanticMode() }
              .padding(horizontal = 12.dp, vertical = 7.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(if (semanticState.isSemanticMode) PureWhite else MidnightNavy)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (semanticState.isSemanticMode) "Deep Recall" else "Keyword",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (semanticState.isSemanticMode) PureWhite else TextPrimary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar with Voice & Semantic trigger
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = PureWhite,
          border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
          shadowElevation = 1.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = IconlyIcons.Search,
              contentDescription = null,
              tint = if (semanticState.isSemanticMode) DustyBlue else TextMuted,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
              value = if (semanticState.isSemanticMode) semanticState.query else searchQuery,
              onValueChange = {
                if (semanticState.isSemanticMode) {
                  viewModel.executeSemanticSearch(it)
                } else {
                  viewModel.setSearchQuery(it)
                }
              },
              placeholder = {
                Text(
                  text = if (semanticState.isSemanticMode) "Natural query (e.g. 'flight seat', 'dinner cost')..."
                  else "Search by keyword, place, date...",
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
              keyboardActions = KeyboardActions(
                onSearch = {
                  if (semanticState.isSemanticMode) {
                    viewModel.executeSemanticSearch(semanticState.query)
                  }
                }
              ),
              modifier = Modifier.weight(1f),
              singleLine = true
            )

            // Voice Snippet quick trigger
            IconButton(
              onClick = { viewModel.requestVoiceRecording(context) },
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(DustyBlueLight)
            ) {
              Icon(
                imageVector = IconlyIcons.Voice,
                contentDescription = "Record Voice Snippet",
                tint = DustyBlue,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Categories, Sorting, and Filter Pills (when not in semantic result mode)
        if (!semanticState.isSemanticMode) {
          // Categories Header & Date Sort Toggle Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "CATEGORIES",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
              ),
              color = TextMuted
            )

            // Date Sorting Toggle Button (Ascending / Descending)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(PureWhite)
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .bouncingClickable(
                  pressedScale = 0.90f,
                  onClickLabel = "Sort by date"
                ) { viewModel.toggleDateSortOrder() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (sortDateAscending) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                  contentDescription = "Toggle Date Sort",
                  tint = MidnightNavy,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = if (sortDateAscending) "Oldest first" else "Newest first",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = MidnightNavy
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Category filter pills: Filter by 'Work', 'Personal', 'Ideas', etc.
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            // Performance: Key by category string to skip recomposition when category selection changes
            items(availableCategories, key = { it }) { category ->
              val isCatSelected = selectedCategory.equals(category, ignoreCase = true)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .background(if (isCatSelected) MidnightNavy else PureWhite)
                  .border(1.dp, if (isCatSelected) MidnightNavy else CardBorder, RoundedCornerShape(16.dp))
                  .bouncingClickable(pressedScale = 0.92f) { viewModel.setCategory(category) }
                  .padding(horizontal = 14.dp, vertical = 7.dp)
              ) {
                Text(
                  text = category,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = if (isCatSelected) PureWhite else TextSecondary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Type Filter Pills
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(filterOptions) { (filter, label) ->
              val isSelected = selectedFilter == filter
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .background(if (isSelected) DustyBlue else PureWhite)
                  .border(1.dp, if (isSelected) DustyBlue else CardBorder, RoundedCornerShape(16.dp))
                  .bouncingClickable(pressedScale = 0.92f) { viewModel.setFilter(filter) }
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                  color = if (isSelected) PureWhite else TextSecondary
                )
              }
            }
          }
        }
      }
    }

    // AI Semantic Mode View
    if (semanticState.isSemanticMode) {
      if (semanticState.isSearching) {
        item {
          Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            SkeletonMemoryCard()
            SkeletonMemoryCard()
          }
        }
      } else if (semanticState.result != null) {
        val res = semanticState.result!!
        item {
          Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            // Semantic Summary Card
            Card(
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = PureWhite),
              border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = IconlyIcons.Verified,
                    contentDescription = null,
                    tint = DustyBlue,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Semantic Context Synthesis",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MidnightNavy
                  )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = res.semanticSummary,
                  style = MaterialTheme.typography.bodyMedium,
                  color = TextPrimary,
                  lineHeight = 20.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = "${res.matches.size} Semantic Matches",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
          }
        }

        // Performance: Key by matched memory ID to prevent re-instantiating card nodes on query updates
        items(res.matches, key = { it.memory.id }) { match ->
          Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
            SemanticMatchCard(
              match = match,
              onClick = { viewModel.navigateTo(Screen.MemoryDetail(match.memory.id)) }
            )
          }
        }
      }
    } else {
      // Standard List
      if (allMemories.isEmpty()) {
        item {
          EmptyState(
            title = "Your Memory Library",
            description = "Your personal memory library is ready. Capture voice snippets, thoughts, receipts, or travel plans to preserve your personal moments.",
            primaryActionLabel = "+ New Memory",
            primaryActionIcon = IconlyIcons.Add,
            onPrimaryAction = { viewModel.openAddSheet() },
            secondaryActionLabel = "Voice Snippet",
            secondaryActionIcon = IconlyIcons.Voice,
            onSecondaryAction = { viewModel.requestVoiceRecording(context) },
            showValuePills = true,
            asCard = true
          )
        }
      } else if (memories.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 20.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = IconlyIcons.Search,
                contentDescription = null,
                tint = DustyBlue,
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = if (searchQuery.isNotBlank()) "No memories found for \"$searchQuery\"" else "No memories in this filter",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "You can clear your search query or ask the AI retrieval engine to search by contextual meaning.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
              Spacer(modifier = Modifier.height(16.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                if (searchQuery.isNotBlank() || selectedFilter != MemoryFilter.ALL || selectedCategory != "All") {
                  androidx.compose.material3.OutlinedButton(
                    onClick = {
                      viewModel.setSearchQuery("")
                      viewModel.setFilter(MemoryFilter.ALL)
                      viewModel.setCategory("All")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                  ) {
                    Text("Clear Filter", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                  }
                }
                androidx.compose.material3.Button(
                  onClick = {
                    val query = searchQuery
                    viewModel.navigateTo(Screen.Ask)
                    if (query.isNotBlank()) {
                      viewModel.submitAskQuestion(query)
                    }
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(16.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy)
                ) {
                  Icon(imageVector = IconlyIcons.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Ask Memory AI", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
              }
            }
          }
        }
      } else {
        // Performance: Key by unique entity ID so DiffUtil selectively updates only changed/favorited/deleted cards
        items(memories, key = { it.id }) { memory ->
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
  }
}

@Composable
private fun SemanticMatchCard(
  match: SemanticMatch,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
    modifier = Modifier
      .fillMaxWidth()
      .bouncingClickable(
        pressedScale = 0.98f,
        onClick = onClick
      )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = match.memory.title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary,
          modifier = Modifier.weight(1f)
        )

        // Relevance Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(EmeraldLight)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "${match.relevanceScore}% Match",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = EmeraldGreen
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = match.reason,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = DustyBlue
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = match.highlightSnippet,
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        maxLines = 2
      )
    }
  }
}

