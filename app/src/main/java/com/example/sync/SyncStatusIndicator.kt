package com.example.sync

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Online/Offline status banner completely removed across page & frontend/UI per user request.
 */
@Composable
fun SyncStatusBanner(
  isOnline: Boolean,
  syncState: SyncState,
  queuedCount: Int,
  onClickQueue: () -> Unit,
  onRetryConnection: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Status banner completely removed per user request
}

/**
 * Online/Offline status chip completely removed across page & frontend/UI per user request.
 */
@Composable
fun SyncStatusChip(
  isOnline: Boolean,
  queuedCount: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Status chip completely removed per user request
}
