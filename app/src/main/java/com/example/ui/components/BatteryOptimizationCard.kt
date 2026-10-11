package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmCreamBorder
import com.example.ui.theme.WarmCreamDark
import com.example.ui.theme.WarmCreamLight
import com.example.util.BatteryOptimizationHelper

/**
 * Material 3 warning card displayed when the app is restricted by Android battery optimizations.
 * Warns the user that background WorkManager tasks (cache cleaning, log pruning, offline sync)
 * may be deferred or killed by system Doze mode, and provides seamless one-tap whitelisting.
 */
@Composable
fun BatteryOptimizationWarningCard(
  modifier: Modifier = Modifier,
  onWhitelisted: () -> Unit = {}
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var isWhitelisted by remember {
    mutableStateOf(BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
  }
  var isDismissed by remember {
    mutableStateOf(BatteryOptimizationHelper.isWarningDismissed(context))
  }
  var showGuideDialog by remember { mutableStateOf(false) }

  // Re-check battery status whenever app resumes from System Settings
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        val updated = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
        isWhitelisted = updated
        if (updated) {
          onWhitelisted()
        }
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  val isVisible = !isWhitelisted && !isDismissed

  AnimatedVisibility(
    visible = isVisible,
    enter = expandVertically() + fadeIn(),
    exit = shrinkVertically() + fadeOut(),
    modifier = modifier
  ) {
    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = AmberLight),
      border = BorderStroke(1.dp, AmberOrange.copy(alpha = 0.35f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 6.dp)
        .testTag("battery_optimization_warning_card")
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AmberOrange.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = IconlyIcons.BatteryAlert,
                contentDescription = null,
                tint = AmberOrange,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Background Tasks Restricted",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary
              )
              Text(
                text = "Battery Optimization Active",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = AmberOrange,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          IconButton(
            onClick = {
              BatteryOptimizationHelper.setWarningDismissed(context, true)
              isDismissed = true
            },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = IconlyIcons.Close,
              contentDescription = "Dismiss warning",
              tint = InkMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Android may defer or cancel background WorkManager tasks, log purges, and offline vault synchronization when battery optimizations are active.",
          style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
          color = InkSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Button(
            onClick = {
              BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = BrandBlack,
              contentColor = PureWhite
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .height(40.dp)
              .testTag("battery_whitelist_button")
          ) {
            Icon(
              imageVector = IconlyIcons.Shield,
              contentDescription = null,
              modifier = Modifier.size(14.dp),
              tint = PureWhite
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Whitelist App",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
          }

          OutlinedButton(
            onClick = { showGuideDialog = true },
            border = BorderStroke(1.dp, WarmCreamBorder),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = InkPrimary),
            modifier = Modifier
              .height(40.dp)
              .testTag("battery_guide_button")
          ) {
            Text(
              text = "Why Whitelist?",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
            )
          }
        }
      }
    }
  }

  if (showGuideDialog) {
    BatteryOptimizationDialog(
      isWhitelisted = isWhitelisted,
      onOpenSettings = {
        BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
      },
      onDismiss = { showGuideDialog = false }
    )
  }
}

/**
 * Educational dialog outlining why unrestricted battery access is needed
 * for WorkManager maintenance and offline sync reliability, with manufacturer-tailored tips.
 */
@Composable
fun BatteryOptimizationDialog(
  isWhitelisted: Boolean,
  onOpenSettings: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val oemGuidance = remember { BatteryOptimizationHelper.getManufacturerGuidance() }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(26.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(22.dp)) {
          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(if (isWhitelisted) EmeraldLight else AmberLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isWhitelisted) IconlyIcons.Check else IconlyIcons.BatteryAlert,
                  contentDescription = null,
                  tint = if (isWhitelisted) EmeraldGreen else AmberOrange,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Battery Optimization",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = BrandBlack
                )
                Text(
                  text = if (isWhitelisted) "Status: Unrestricted (Optimal)" else "Status: Throttled by Android",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isWhitelisted) EmeraldGreen else AmberOrange,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            IconButton(onClick = onDismiss) {
              Icon(
                imageVector = IconlyIcons.Close,
                contentDescription = "Close",
                tint = InkMuted,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "To guarantee the health of your personal memory archive, MemoryOS runs scheduled maintenance in the background:",
            style = MaterialTheme.typography.bodySmall,
            color = InkSecondary,
            lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Feature List
          BatteryFeatureRow(
            title = "WorkManager Cache Sanitization",
            subtitle = "Cleans old crash logs, orphaned temporary files, and expired downloads every 24 hours to prevent storage bloat."
          )
          Spacer(modifier = Modifier.height(10.dp))
          BatteryFeatureRow(
            title = "Zero-Loss Offline Synchronization",
            subtitle = "Ensures mutations created while offline are queued and committed reliably even when the app is closed."
          )
          Spacer(modifier = Modifier.height(10.dp))
          BatteryFeatureRow(
            title = "Zero Battery Impact",
            subtitle = "Maintenance passes finish in under 3 seconds and require battery not low constraints."
          )

          Spacer(modifier = Modifier.height(16.dp))

          // OEM Guidance Box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(WarmCreamDark)
              .padding(14.dp)
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = IconlyIcons.Info,
                  contentDescription = null,
                  tint = InkPrimary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Manufacturer Instructions",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = InkPrimary
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = oemGuidance,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = InkSecondary
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            if (!isWhitelisted) {
              Button(
                onClick = {
                  onOpenSettings()
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = BrandBlack,
                  contentColor = PureWhite
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(46.dp)
                  .testTag("dialog_open_battery_settings")
              ) {
                Text(
                  text = "Open Battery Settings",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              }
            }

            OutlinedButton(
              onClick = onDismiss,
              shape = RoundedCornerShape(16.dp),
              border = BorderStroke(1.dp, CardBorder),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = InkPrimary),
              modifier = Modifier
                .then(if (isWhitelisted) Modifier.fillMaxWidth() else Modifier.weight(0.7f))
                .height(46.dp)
            ) {
              Text(
                text = if (isWhitelisted) "Got It" else "Dismiss",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BatteryFeatureRow(
  title: String,
  subtitle: String
) {
  Row(verticalAlignment = Alignment.Top) {
    Box(
      modifier = Modifier
        .padding(top = 2.dp)
        .size(16.dp)
        .clip(CircleShape)
        .background(EmeraldGreen),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = IconlyIcons.Check,
        contentDescription = null,
        tint = PureWhite,
        modifier = Modifier.size(10.dp)
      )
    }
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = InkPrimary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
        color = InkSecondary
      )
    }
  }
}
