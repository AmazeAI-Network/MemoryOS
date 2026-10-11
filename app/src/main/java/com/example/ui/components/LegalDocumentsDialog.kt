package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

enum class LegalTab {
  PRIVACY_POLICY,
  TERMS_AND_CONDITIONS
}

/**
 * Privacy Policy and Terms & Conditions Dialog:
 * Complete, transparent legal disclosures detailing offline-first storage,
 * hardware AES-256 encryption, zero ad tracking, and user data ownership.
 */
@Composable
fun LegalDocumentsDialog(
  initialTab: LegalTab = LegalTab.PRIVACY_POLICY,
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(initialTab) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(26.dp),
      color = PureWhite,
      border = BorderStroke(1.dp, CardBorder),
      shadowElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .fillMaxHeight(0.85f)
        .padding(vertical = 20.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Legal & Privacy",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.3).sp
            ),
            color = BrandBlack
          )

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = IconlyIcons.Close,
              contentDescription = "Close",
              tint = TextMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs (Privacy Policy / Terms of Service)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WarmCreamDark)
            .padding(4.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (selectedTab == LegalTab.PRIVACY_POLICY) MidnightNavy else WarmCreamDark)
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            TextButton(
              onClick = { selectedTab = LegalTab.PRIVACY_POLICY },
              contentPadding = PaddingValues(0.dp)
            ) {
              Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (selectedTab == LegalTab.PRIVACY_POLICY) PureWhite else TextSecondary
              )
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (selectedTab == LegalTab.TERMS_AND_CONDITIONS) MidnightNavy else WarmCreamDark)
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            TextButton(
              onClick = { selectedTab = LegalTab.TERMS_AND_CONDITIONS },
              contentPadding = PaddingValues(0.dp)
            ) {
              Text(
                text = "Terms of Service",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (selectedTab == LegalTab.TERMS_AND_CONDITIONS) PureWhite else TextSecondary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable Body Content
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
        ) {
          if (selectedTab == LegalTab.PRIVACY_POLICY) {
            LegalSection(
              title = "1. Zero-Knowledge Offline-First Architecture",
              body = "MemoryOS operates on an offline-first architecture. All your memories, audio transcriptions, journal notes, receipts, and search queries are stored directly on your device in a local SQLite Room database."
            )
            LegalSection(
              title = "2. Hardware-Backed Encryption",
              body = "Sensitive attributes and tokens are secured using Android KeyStore and EncryptedSharedPreferences (AES-256-GCM). Data at rest remains inaccessible to other applications installed on your system."
            )
            LegalSection(
              title = "3. No Advertising or Tracking",
              body = "We do not sell, rent, or monetize your personal data. We do not integrate advertising SDKs, third-party analytics trackers, or social network tracking pixels."
            )
            LegalSection(
              title = "4. AI & Grounded Semantic Retrieval",
              body = "When you query your personal memory vault using semantic search or Ask AI, questions are answered strictly based on your personal memories. Your thoughts are never used to train public foundation models."
            )
            LegalSection(
              title = "5. Data Ownership & Export",
              body = "You own 100% of your data. You may export your entire memory archive in open standard formats (JSON and Markdown) or permanently erase all records via the in-app purge tool at any time."
            )
            LegalSection(
              title = "6. Contact & Support",
              body = "For any inquiries or privacy requests regarding your data, contact our support team at: bintangjanuarda0809@gmail.com"
            )
          } else {
            LegalSection(
              title = "1. Acceptance of Terms",
              body = "By downloading, installing, or using MemoryOS, you agree to be bound by these Terms and Conditions. If you disagree with any portion of these terms, please discontinue use of the application."
            )
            LegalSection(
              title = "2. Permitted Use",
              body = "MemoryOS is licensed for personal, non-commercial journaling, note-taking, expense tracking, and private reflection. You agree not to reverse engineer or distribute unauthorized copies."
            )
            LegalSection(
              title = "3. Local Data & Device Responsibility",
              body = "Because MemoryOS is an offline-first application that stores memories locally, you are responsible for maintaining your device's security and creating regular backups using our built-in JSON/Markdown export tools."
            )
            LegalSection(
              title = "4. Content & Intellectual Property",
              body = "You retain full intellectual property rights to all memories, texts, receipts, and recordings you create within MemoryOS. The MemoryOS interface, branding, and proprietary algorithms remain the exclusive property of the developer."
            )
            LegalSection(
              title = "5. Disclaimer of Warranties",
              body = "MemoryOS is provided 'as is' without warranties of any kind. While we design for maximum data durability and crash resilience, the developers are not liable for incidental data loss resulting from device hardware failure."
            )
            LegalSection(
              title = "6. Inquiries & Support",
              body = "For questions or legal notifications, contact our direct developer desk: bintangjanuarda0809@gmail.com"
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = onDismiss,
          shape = RoundedCornerShape(18.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MidnightNavy,
            contentColor = PureWhite
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          Text("I Understand & Agree", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun LegalSection(
  title: String,
  body: String
) {
  Column(modifier = Modifier.padding(bottom = 14.dp)) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
      color = BrandBlack
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = body,
      style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
      color = InkSecondary
    )
  }
}
