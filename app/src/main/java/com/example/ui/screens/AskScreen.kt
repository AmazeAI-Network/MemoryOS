package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SourceCitation
import com.example.ui.components.IconlyIcons
import com.example.ui.components.MemoryOSEmblem
import com.example.ui.components.SkeletonAskAnswer
import com.example.ui.components.bouncingClickable
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.DustyBlueLight
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.MemoryViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun AskScreen(
  viewModel: MemoryViewModel,
  modifier: Modifier = Modifier
) {
  val askState by viewModel.askState.collectAsStateWithLifecycle()
  val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
  val context = androidx.compose.ui.platform.LocalContext.current

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(WarmIvory)
      .statusBarsPadding(),
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
  ) {
    // Header
    item {
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Recall & Search",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
          ),
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Ask about any past event, expense, or note.",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Input Search Box with Send Action
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = PureWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = IconlyIcons.Search,
            contentDescription = null,
            tint = DustyBlue,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          OutlinedTextField(
            value = askState.query,
            onValueChange = { viewModel.setAskQuery(it) },
            placeholder = {
              Text(
                text = "Ask anything...",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted
              )
            },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
              unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
              focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
              unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
            ),
            modifier = Modifier.weight(1f),
            singleLine = true
          )

          // Voice Search Button
          Box(
            modifier = Modifier
              .size(36.dp)
              .bouncingClickable(pressedScale = 0.88f) { viewModel.requestVoiceRecording(context) }
              .clip(CircleShape)
              .background(DustyBlueLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = IconlyIcons.Voice,
              contentDescription = "Speak query",
              tint = DustyBlue,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          Box(
            modifier = Modifier
              .size(40.dp)
              .bouncingClickable(
                enabled = askState.query.isNotBlank() && !askState.isLoading,
                pressedScale = 0.88f
              ) { viewModel.submitAskQuestion() }
              .clip(CircleShape)
              .background(if (askState.query.isNotBlank()) MidnightNavy else WarmIvory),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = IconlyIcons.Send,
              contentDescription = "Send",
              tint = if (askState.query.isNotBlank()) PureWhite else TextMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Room Local Storage: Recent Search Queries
      if (recentSearches.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Searches",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MidnightNavy
          )
          TextButton(
            onClick = { viewModel.clearAllRecentSearches() },
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
          ) {
            Text(
              text = "Clear all",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              color = TextMuted
            )
          }
        }
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(recentSearches, key = { it.query }) { item ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(PureWhite)
                .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(16.dp))
                .padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = item.query,
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                  color = TextPrimary,
                  modifier = Modifier.bouncingClickable(pressedScale = 0.94f) {
                    viewModel.submitAskQuestion(item.query)
                  }
                )
                IconButton(
                  onClick = { viewModel.deleteRecentSearch(item.query) },
                  modifier = Modifier.size(20.dp)
                ) {
                  Icon(
                    imageVector = IconlyIcons.Close,
                    contentDescription = "Remove query",
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Try Asking Chips
      Text(
        text = "Try asking",
        style = MaterialTheme.typography.labelMedium,
        color = TextSecondary
      )
      Spacer(modifier = Modifier.height(8.dp))

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(askState.searchHistory) { suggestion ->
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(PureWhite)
              .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(16.dp))
              .bouncingClickable(pressedScale = 0.94f) {
                viewModel.submitAskQuestion(suggestion)
              }
              .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Text(
              text = suggestion,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = TextPrimary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }

    // Helpful Quick Query Suggestions (when idle)
    if (askState.answer == null && !askState.isLoading) {
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = PureWhite),
          border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = "Popular Questions",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            AskQuickPromptItem(
              icon = IconlyIcons.Document,
              title = "Spending & Receipts",
              query = "What did I spend money on recently?",
              onClick = { viewModel.submitAskQuestion("What did I spend money on recently?") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            AskQuickPromptItem(
              icon = IconlyIcons.Location,
              title = "Travel & Flights",
              query = "What upcoming flights or hotel reservations do I have?",
              onClick = { viewModel.submitAskQuestion("What upcoming flights or hotel reservations do I have?") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            AskQuickPromptItem(
              icon = IconlyIcons.Voice,
              title = "Recent Conversations",
              query = "What did I discuss in recent meetings or voice notes?",
              onClick = { viewModel.submitAskQuestion("What did I discuss in recent meetings or voice notes?") }
            )
          }
        }
        Spacer(modifier = Modifier.height(20.dp))
      }
    }

    // High-performance Skeleton Answer Experience during retrieval
    if (askState.isLoading) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(18.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = DustyBlue,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = askState.stepMessage,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = TextPrimary
                )
                Text(
                  text = "Reasoning and cross-referencing over your memories...",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary
                )
              }
            }
          }

          // Shimmer Skeleton placeholder to prevent UI freezing
          SkeletonAskAnswer()
        }
      }
    }

    // Grounded Answer Experience
    askState.answer?.let { ans ->
      item {
        AnimatedVisibility(visible = true, enter = fadeIn()) {
          Column {
            Card(
              shape = RoundedCornerShape(24.dp),
              colors = CardDefaults.cardColors(containerColor = PureWhite),
              elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(20.dp)) {
                // Header badge
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "GROUNDED ANSWER",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                      ),
                      color = EmeraldGreen
                    )
                  }

                  if (ans.confidence.isNotBlank()) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                      Text(
                        text = "${ans.confidence} CONFIDENCE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldGreen
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Answer Text (Direct, factual, clean)
                Text(
                  text = ans.answer,
                  style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 26.sp
                  ),
                  color = TextPrimary
                )

                if (ans.sources.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(14.dp))
                  Text(
                    text = "Based on ${ans.sources.size} memories",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = TextSecondary
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Evidence / Supporting Source Cards
            if (ans.sources.isNotEmpty()) {
              Text(
                text = "Supporting Evidence",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
              )
              Spacer(modifier = Modifier.height(8.dp))
            }
          }
        }
      }

      items(ans.sources) { source ->
        SourceEvidenceCard(
          source = source,
          onClick = { viewModel.navigateTo(Screen.MemoryDetail(source.memoryId)) }
        )
        Spacer(modifier = Modifier.height(10.dp))
      }
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@Composable
private fun SourceEvidenceCard(
  source: SourceCitation,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(DustyBlueLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = IconlyIcons.Document,
          contentDescription = null,
          tint = DustyBlue,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = source.title,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = source.reason,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
      }

      Icon(
        imageVector = IconlyIcons.ChevronRight,
        contentDescription = null,
        tint = TextMuted,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
private fun AskQuickPromptItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  query: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .bouncingClickable(
        pressedScale = 0.96f,
        onClick = onClick
      )
      .clip(RoundedCornerShape(12.dp))
      .background(WarmIvory)
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = DustyBlue,
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = TextPrimary
      )
      Text(
        text = query,
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
      )
    }
    Icon(
      imageVector = IconlyIcons.ChevronRight,
      contentDescription = null,
      tint = TextMuted,
      modifier = Modifier.size(16.dp)
    )
  }
}
