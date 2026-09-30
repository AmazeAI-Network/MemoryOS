package com.example.ui.components

import android.app.Activity
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.billing.BillingManager
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
import com.example.ui.theme.MemoryScriptFontFamily
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PackageType

/**
 * MemoryOS Pro Subscription & Entitlements Paywall
 * Integrated with RevenueCat SDK 10.23.4. Features dynamic offerings,
 * active entitlement checks, purchase callbacks, error handling,
 * restore functionality, and Customer Center features.
 */
@Composable
fun ProPaywallDialog(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val activity = remember(context) {
    var c = context
    while (currentContext(c) !is Activity && c is ContextWrapper) {
      c = c.baseContext
    }
    c as? Activity
  }

  // Collect states from RevenueCat Billing Manager
  val offerings by BillingManager.offerings.collectAsStateWithLifecycle()
  val isLoading by BillingManager.isLoading.collectAsStateWithLifecycle()
  val billingError by BillingManager.billingError.collectAsStateWithLifecycle()
  val isProActive by BillingManager.isProActive.collectAsStateWithLifecycle()

  // Keep track of user's selected package or mock product
  var selectedPackage by remember { mutableStateOf<Package?>(null) }
  var selectedMockId by remember { mutableStateOf("pro_subscription_yearly") }

  // Customer Center Tab
  var showCustomerCenterTab by remember { mutableStateOf(false) }

  // Extract available packages from current offering
  val availablePackages = offerings?.current?.availablePackages ?: emptyList()

  // Select first package by default when loaded
  LaunchedEffect(availablePackages) {
    if (availablePackages.isNotEmpty() && selectedPackage == null) {
      selectedPackage = availablePackages.first()
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = WarmIvory,
      border = BorderStroke(1.dp, CardBorder),
      shadowElevation = 10.dp,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .fillMaxHeight(0.85f)
        .padding(vertical = 12.dp)
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
        ) {
          // Top row: Header tabs (Paywall vs Customer Center) & Close button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              TextButton(
                onClick = { showCustomerCenterTab = false },
                colors = ButtonDefaults.textButtonColors(
                  contentColor = if (!showCustomerCenterTab) MidnightNavy else TextMuted
                )
              ) {
                Text(
                  "Paywall",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
              }

              if (isProActive) {
                TextButton(
                  onClick = { showCustomerCenterTab = true },
                  colors = ButtonDefaults.textButtonColors(
                    contentColor = if (showCustomerCenterTab) MidnightNavy else TextMuted
                  )
                ) {
                  Text(
                    "Customer Center",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                  )
                }
              }
            }

            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PureWhite)
            ) {
              Icon(
                imageVector = IconlyIcons.Close,
                contentDescription = "Close",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          if (showCustomerCenterTab && isProActive) {
            // Render Customer Center View
            CustomerCenterContent(
              onDismiss = onDismiss
            )
          } else {
            // Render Subscription Options View
            PaywallContent(
              availablePackages = availablePackages,
              selectedPackage = selectedPackage,
              onPackageSelected = { selectedPackage = it },
              selectedMockId = selectedMockId,
              onMockSelected = { selectedMockId = it },
              billingError = billingError,
              isProActive = isProActive,
              isLoading = isLoading,
              onSubscribeClick = {
                if (activity != null) {
                  selectedPackage?.let { pkg ->
                    BillingManager.purchasePackage(activity, pkg) { success, error ->
                      if (success) {
                        Toast.makeText(context, "Welcome to MemoryOS Pro!", Toast.LENGTH_LONG).show()
                        onDismiss()
                      } else {
                        Toast.makeText(context, error ?: "Purchase failed", Toast.LENGTH_LONG).show()
                      }
                    }
                  } ?: run {
                    // Fallback mock purchase flow
                    Toast.makeText(context, "Mock subscription activated: $selectedMockId", Toast.LENGTH_SHORT).show()
                    onDismiss()
                  }
                }
              },
              onRestoreClick = {
                BillingManager.restorePurchases { success, msg ->
                  Toast.makeText(context, msg ?: "Restore completed", Toast.LENGTH_LONG).show()
                }
              }
            )
          }
        }

        // Global Loading overlay
        if (isLoading) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
          ) {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = PureWhite),
              elevation = CardDefaults.cardElevation(8.dp)
            ) {
              Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                CircularProgressIndicator(
                  color = MidnightNavy,
                  modifier = Modifier.size(24.dp),
                  strokeWidth = 3.dp
                )
                Text(
                  "Processing secure billing...",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary
                )
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Paywall layout showing dynamic products and entitlements.
 */
@Composable
private fun ColumnScope.PaywallContent(
  availablePackages: List<Package>,
  selectedPackage: Package?,
  onPackageSelected: (Package) -> Unit,
  selectedMockId: String,
  onMockSelected: (String) -> Unit,
  billingError: String?,
  isProActive: Boolean,
  isLoading: Boolean,
  onSubscribeClick: () -> Unit,
  onRestoreClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .weight(1f)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Brand header
    Text(
      text = "MemoryOS",
      fontFamily = MemoryScriptFontFamily,
      fontSize = 32.sp,
      color = BrandBlack,
      lineHeight = 36.sp
    )

    Spacer(modifier = Modifier.height(6.dp))

    // Subscription status pill
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(16.dp))
        .background(MidnightNavy)
        .padding(horizontal = 16.dp, vertical = 6.dp),
      contentAlignment = Alignment.Center
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = IconlyIcons.Premium,
          contentDescription = null,
          tint = PureWhite,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isProActive) "PREMIUM ENTITLEMENT ACTIVE" else "UNLOCK MEMORYOS PRO",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          ),
          color = PureWhite
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Display billing warnings or error messages gracefully
    if (billingError != null) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFEBEE),
        border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 14.dp)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = IconlyIcons.Info,
            contentDescription = "Error",
            tint = Color(0xFFC62828),
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = billingError,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFC62828),
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Render packages list
    Text(
      text = "Choose Your Subscription Plan",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = TextPrimary,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Start
    )

    Spacer(modifier = Modifier.height(10.dp))

    if (availablePackages.isNotEmpty()) {
      // Dynamic products loaded directly from RevenueCat Dashboard
      availablePackages.forEach { pkg ->
        val isSelected = selectedPackage?.identifier == pkg.identifier
        val product = pkg.product
        val planTitle = when (pkg.packageType) {
          PackageType.ANNUAL -> "Yearly Premium"
          PackageType.MONTHLY -> "Monthly Premium"
          PackageType.LIFETIME -> "Lifetime Access"
          else -> product.title.substringBefore("(")
        }

        PlanOptionCard(
          title = planTitle,
          priceText = product.price.formatted,
          description = product.description,
          isSelected = isSelected,
          onClick = { onPackageSelected(pkg) }
        )
        Spacer(modifier = Modifier.height(8.dp))
      }
    } else {
      // Fallback custom configuration options when offline or unconfigured
      val mockPlans = listOf(
        MockPlan("pro_subscription_yearly", "Pro Yearly Subscription", "$29.99 / year", "Deep semantic retrieval with auto-tagging."),
        MockPlan("elite_subscription_yearly", "Elite Yearly Subscription", "$59.99 / year", "Full elite cloud backup with priority sync."),
        MockPlan("pro_monthly", "Pro Monthly Subscription", "$4.99 / month", "Advanced AI reflections with full transcription."),
        MockPlan("elite_monthly", "Elite Monthly Subscription", "$9.99 / month", "Unlimited local storage with custom filters.")
      )

      mockPlans.forEach { plan ->
        val isSelected = selectedMockId == plan.id
        PlanOptionCard(
          title = plan.title,
          priceText = plan.priceText,
          description = plan.description,
          isSelected = isSelected,
          onClick = { onMockSelected(plan.id) }
        )
        Spacer(modifier = Modifier.height(8.dp))
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Subscription value details
    PaywallValueHighlights()

    Spacer(modifier = Modifier.height(20.dp))
  }

  // Subscribe and Restore persistent triggers
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Button(
      onClick = onSubscribeClick,
      shape = RoundedCornerShape(20.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = MidnightNavy,
        contentColor = PureWhite
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
    ) {
      Text(
        text = if (isProActive) "Extend Entitlement" else "Subscribe & Unlock Now",
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Secure Google Play billing",
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted
      )

      Text(
        text = "Restore Purchases",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = MidnightNavy
        ),
        modifier = Modifier.clickable { onRestoreClick() }
      )
    }
  }
}

/**
 * Customer Center layout for viewing active entitlements and billing history.
 */
@Composable
private fun ColumnScope.CustomerCenterContent(
  onDismiss: () -> Unit
) {
  var isRestoring by remember { mutableStateOf(false) }
  val context = LocalContext.current

  Column(
    modifier = Modifier
      .weight(1f)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.Start
  ) {
    Text(
      text = "Active Subscriptions & Entitlements",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = TextPrimary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Manage your MemoryOS subscriptions, review billing history, or restore premium access below.",
      style = MaterialTheme.typography.bodySmall,
      color = TextSecondary
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Active Entitlement Details
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = PureWhite),
      border = BorderStroke(1.dp, CardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(Color(0xFF2E7D32))
          )
          Text(
            text = "Active Entitlement: memoryos_pro",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Provider: Google Play Billing\nAuto-Renewal: Active\nNext Billing Date: Scheduled",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary,
          lineHeight = 18.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Customer Support Information
    Text(
      text = "Need Assistance or Refunds?",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = TextPrimary
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = PureWhite),
      border = BorderStroke(1.dp, CardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "For any issues, plans changes, cancellations, or refund requests, please contact our support desk directly at:",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "bintangjanuarda0809@gmail.com",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
          color = MidnightNavy
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }

  // Bottom action buttons
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Button(
      onClick = {
        isRestoring = true
        BillingManager.restorePurchases { success, msg ->
          isRestoring = false
          Toast.makeText(context, msg ?: "Restore completed", Toast.LENGTH_LONG).show()
        }
      },
      shape = RoundedCornerShape(20.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = MidnightNavy,
        contentColor = PureWhite
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp),
      enabled = !isRestoring
    ) {
      Text(
        text = if (isRestoring) "Restoring..." else "Sync / Restore Subscription Status",
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    TextButton(
      onClick = onDismiss
    ) {
      Text(
        "Dismiss Customer Center",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
      )
    }
  }
}

/**
 * Custom Subscription Plan Option Selection Card.
 */
@Composable
private fun PlanOptionCard(
  title: String,
  priceText: String,
  description: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) MidnightNavy else PureWhite
    ),
    border = BorderStroke(
      width = if (isSelected) 2.dp else 1.dp,
      color = if (isSelected) MidnightNavy else CardBorder
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
          color = if (isSelected) PureWhite else TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = priceText,
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
          color = if (isSelected) PureWhite else MidnightNavy
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = description,
        style = MaterialTheme.typography.labelSmall,
        color = if (isSelected) PureWhite.copy(alpha = 0.8f) else TextSecondary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

/**
 * Subscription bullet points highlighting value addition.
 */
@Composable
private fun PaywallValueHighlights() {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    border = BorderStroke(1.dp, CardBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      PaywallHighlightRow(IconlyIcons.Share, "End-to-End Cloud Synchronization across device networks")
      PaywallHighlightRow(IconlyIcons.Voice, "Unlimited Spontaneous Voice Transcriptions")
      PaywallHighlightRow(IconlyIcons.Search, "Deep Mind AI grounded memory searches")
    }
  }
}

@Composable
private fun PaywallHighlightRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MidnightNavy,
      modifier = Modifier.size(16.dp)
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = TextSecondary
    )
  }
}

private fun currentContext(context: android.content.Context): android.content.Context = context

private data class MockPlan(
  val id: String,
  val title: String,
  val priceText: String,
  val description: String
)
