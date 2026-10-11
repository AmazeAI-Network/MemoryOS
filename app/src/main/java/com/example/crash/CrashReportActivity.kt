package com.example.crash

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.ui.components.IconlyIcons
import com.example.ui.components.MemoryOSEmblem
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class CrashReportActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val crashJson = intent.getStringExtra(EXTRA_CRASH_JSON) ?: "{}"
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val details = try {
      moshi.adapter(CrashDetails::class.java).fromJson(crashJson)
    } catch (_: Exception) {
      null
    }

    setContent {
      MyApplicationTheme {
        Scaffold(
          containerColor = WarmIvory
        ) { innerPadding ->
          CrashReportScreen(
            details = details,
            crashJson = crashJson,
            onRestartClick = { restartApplication() },
            onCopyClick = { copyToClipboard(crashJson) },
            onSendEmailClick = { sendEmailReport(crashJson, details) },
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }

  private fun restartApplication() {
    val intent = Intent(this, MainActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
    startActivity(intent)
    finish()
  }

  private fun copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("MemoryOS Crash Report", text))
    Toast.makeText(this, "Crash report copied to clipboard", Toast.LENGTH_SHORT).show()
  }

  private fun sendEmailReport(json: String, details: CrashDetails?) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
      data = Uri.parse("mailto:")
      putExtra(Intent.EXTRA_EMAIL, arrayOf("bintangjanuarda0809@gmail.com"))
      putExtra(Intent.EXTRA_SUBJECT, "MemoryOS Crash Log: ${details?.errorType ?: "Fatal Error"}")
      putExtra(Intent.EXTRA_TEXT, "Hello Team,\n\nThe app encountered an unexpected error:\n\n$json")
    }
    try {
      startActivity(Intent.createChooser(intent, "Send Crash Report"))
    } catch (_: Exception) {
      Toast.makeText(this, "No email client installed", Toast.LENGTH_SHORT).show()
    }
  }

  companion object {
    const val EXTRA_CRASH_JSON = "extra_crash_json"
    const val EXTRA_CRASH_FILE_PATH = "extra_crash_file_path"
  }
}

@Composable
fun CrashReportScreen(
  details: CrashDetails?,
  crashJson: String,
  onRestartClick: () -> Unit,
  onCopyClick: () -> Unit,
  onSendEmailClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showDetails by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(20.dp))

    // Icon & Emblem
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(PureWhite)
        .border(1.dp, CardBorder, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = IconlyIcons.Info,
        contentDescription = null,
        tint = MidnightNavy,
        modifier = Modifier.size(28.dp)
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    Text(
      text = "We're Really Sorry",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = MidnightNavy
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "MemoryOS encountered an unexpected error and closed gracefully. Your memories in local storage remain safe and intact.",
      style = MaterialTheme.typography.bodyMedium,
      color = TextSecondary,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Diagnostic Card
    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = PureWhite),
      border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "DIAGNOSTIC SUMMARY",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
            color = TextMuted
          )
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(TerracottaRed.copy(alpha = 0.12f))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = details?.errorType ?: "Crash Intercepted",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = TerracottaRed
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = details?.errorMessage ?: "An unexpected exception occurred",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Device: ${details?.deviceManufacturer} ${details?.deviceModel} · Android ${details?.androidVersion}",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
        Text(
          text = "App: v${details?.appVersion} · RAM: ${details?.availableMemoryMb} MB free",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Toggle Details
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { showDetails = !showDetails }
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (showDetails) "Hide Error Trace ▲" else "View Technical Details ▼",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = DustyBlue
          )
        }

        AnimatedVisibility(visible = showDetails) {
          Column(modifier = Modifier.padding(top = 10.dp)) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(WarmIvory)
                .padding(12.dp)
            ) {
              Text(
                text = details?.stackTrace ?: crashJson,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  lineHeight = 15.sp
                ),
                color = TextPrimary
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Primary Action: Restart App
    Button(
      onClick = onRestartClick,
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy, contentColor = PureWhite)
    ) {
      Icon(imageVector = IconlyIcons.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Restart MemoryOS", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Secondary Actions
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedButton(
        onClick = onCopyClick,
        modifier = Modifier
          .weight(1f)
          .height(46.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
      ) {
        Icon(imageVector = IconlyIcons.Copy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Copy Log", color = TextPrimary)
      }

      OutlinedButton(
        onClick = onSendEmailClick,
        modifier = Modifier
          .weight(1f)
          .height(46.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
      ) {
        Icon(imageVector = IconlyIcons.Send, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Send Report", color = TextPrimary)
      }
    }
  }
}
