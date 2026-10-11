package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.DustyBlueLight
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpFeedbackSheet(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val faqs = listOf(
    "How does natural language search work?" to
      "MemoryOS uses intelligent semantic retrieval. Instead of just searching exact keywords, it understands context, synonyms, and dates so queries like 'dinner in Tokyo' or 'hotel booking' match immediately.",
    "Where is my memory data stored?" to
      "100% of your memories are stored in an encrypted local database (Room) on your phone. You have full ownership, and you can export or delete your data at any time.",
    "Can I record voice memories offline?" to
      "Yes! Voice recording uses on-device speech transcription. Even without an internet connection, your words are captured and saved securely.",
    "How do I set up daily memory reminders?" to
      "Open Profile -> Daily Memory Check-in. Choose your preferred time (like 8:00 PM evening reflection) to receive a gentle nudge."
  )

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PureWhite,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp)
        .padding(bottom = 36.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(DustyBlueLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = IconlyIcons.Info,
              contentDescription = null,
              tint = DustyBlue,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = "Help & Information",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(imageVector = IconlyIcons.Close, contentDescription = "Close", tint = TextMuted)
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "Frequently Asked Questions",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      faqs.forEach { (question, answer) ->
        FaqItem(question = question, answer = answer)
        Spacer(modifier = Modifier.height(10.dp))
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Direct Contact / Feedback Box
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WarmIvory),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Need further support?",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Have feedback or a feature request? Let us know directly.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              clipboard.setPrimaryClip(ClipData.newPlainText("Customer Support Email", "bintangjanuarda0809@gmail.com"))
              Toast.makeText(context, "Support email copied: bintangjanuarda0809@gmail.com", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy, contentColor = PureWhite),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(imageVector = IconlyIcons.Copy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contact Support (bintangjanuarda0809@gmail.com)", fontSize = 12.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun FaqItem(question: String, answer: String) {
  var isExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = WarmIvory),
    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { isExpanded = !isExpanded }
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = question,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = TextPrimary,
          modifier = Modifier.weight(1f)
        )
        Icon(
          imageVector = IconlyIcons.ChevronRight,
          contentDescription = null,
          tint = TextMuted,
          modifier = Modifier.size(18.dp)
        )
      }

      AnimatedVisibility(visible = isExpanded) {
        Column {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = answer,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            lineHeight = 18.sp
          )
        }
      }
    }
  }
}
