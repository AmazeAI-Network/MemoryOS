package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.BrandWhite
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.WarmCreamDark

/**
 * Enterprise Biometric Lock Screen.
 * Guards user memory vault until fingerprint, face, or device PIN authentication succeeds.
 */
@Composable
fun BiometricLockScreen(
  onUnlock: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxSize()
      .testTag("biometric_lock_screen"),
    color = MaterialTheme.colorScheme.background
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(28.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Biometric Emblem
        Box(
          modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(WarmCreamDark)
            .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = IconlyIcons.Shield,
            contentDescription = "Vault Security Lock",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(44.dp)
          )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
          text = "MemoryOS Vault Secured",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
          ),
          color = MaterialTheme.colorScheme.onBackground,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Biometric authentication is required to access your encrypted memories and tokens.",
          style = MaterialTheme.typography.bodyMedium,
          color = InkSecondary,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
          onClick = onUnlock,
          shape = RoundedCornerShape(24.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(52.dp)
            .testTag("unlock_vault_button")
        ) {
          Icon(
            imageVector = IconlyIcons.Lock,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.size(10.dp))
          Text(
            text = "Unlock with Biometrics / PIN",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}
