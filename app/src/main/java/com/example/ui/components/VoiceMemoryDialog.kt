package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DustyBlue
import com.example.ui.theme.DustyBlueLight
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmIvory
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceMemoryDialog(
  onDismiss: () -> Unit,
  onSaveSnippet: (transcribedText: String) -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var hasPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
      ) == PackageManager.PERMISSION_GRANTED
    )
  }

  var isListening by remember { mutableStateOf(false) }
  var spokenText by remember { mutableStateOf("") }
  var rmsLevel by remember { mutableFloatStateOf(0f) }
  var statusMessage by remember { mutableStateOf("Tap the microphone to speak") }

  // Speech Recognizer instance
  var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

  // Fallback system Speech Intent launcher
  val speechIntentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
    if (!matches.isNullOrEmpty()) {
      spokenText = matches[0]
      statusMessage = "Speech captured!"
      isListening = false
    }
  }

  // Permission Launcher
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasPermission = isGranted
    if (isGranted) {
      startSpeechRecognition(
        context = context,
        onListeningChange = { isListening = it },
        onTextChange = { spokenText = it },
        onRmsChange = { rmsLevel = it },
        onStatusChange = { statusMessage = it },
        onRecognizerReady = { speechRecognizer = it },
        onFallback = {
          launchSystemSpeechIntent(speechIntentLauncher)
        }
      )
    } else {
      statusMessage = "Microphone permission required for voice notes."
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      try {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
      } catch (_: Exception) {}
    }
  }

  // Auto-start when opened if permission already granted
  LaunchedEffect(hasPermission) {
    if (hasPermission && !isListening && spokenText.isEmpty()) {
      startSpeechRecognition(
        context = context,
        onListeningChange = { isListening = it },
        onTextChange = { spokenText = it },
        onRmsChange = { rmsLevel = it },
        onStatusChange = { statusMessage = it },
        onRecognizerReady = { speechRecognizer = it },
        onFallback = {
          launchSystemSpeechIntent(speechIntentLauncher)
        }
      )
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PureWhite,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .padding(bottom = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
          Text(
            text = "Voice Memory Snippet",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.3).sp
            ),
            color = TextPrimary
          )

        IconButton(onClick = onDismiss) {
          Icon(imageVector = IconlyIcons.Close, contentDescription = "Close", tint = TextMuted)
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Animated Microphone Waveform Container
      Box(
        modifier = Modifier
          .size(110.dp),
        contentAlignment = Alignment.Center
      ) {
        if (isListening) {
          PulseRings(rmsLevel = rmsLevel)
        }

        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(if (isListening) TerracottaRed else MidnightNavy)
            .clickable {
              if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
              } else if (isListening) {
                try {
                  speechRecognizer?.stopListening()
                } catch (_: Exception) {}
                isListening = false
                statusMessage = "Recording finished."
              } else {
                startSpeechRecognition(
                  context = context,
                  onListeningChange = { isListening = it },
                  onTextChange = { spokenText = it },
                  onRmsChange = { rmsLevel = it },
                  onStatusChange = { statusMessage = it },
                  onRecognizerReady = { speechRecognizer = it },
                  onFallback = {
                    launchSystemSpeechIntent(speechIntentLauncher)
                  }
                )
              }
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
            contentDescription = if (isListening) "Stop Recording" else "Start Recording",
            tint = PureWhite,
            modifier = Modifier.size(36.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Status Indicator
      Text(
        text = statusMessage,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = if (isListening) TerracottaRed else TextSecondary
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Transcription Output Box
      OutlinedTextField(
        value = spokenText,
        onValueChange = { spokenText = it },
        placeholder = {
          Text(
            text = if (isListening) "Listening... speak now about what you want to remember..."
            else "Your speech will appear here. Tap microphone to record snippet.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(120.dp),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = WarmIvory,
          unfocusedContainerColor = WarmIvory,
          focusedBorderColor = DustyBlue,
          unfocusedBorderColor = CardBorder
        )
      )

      Spacer(modifier = Modifier.height(14.dp))

      // AI auto-understanding badge note
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(DustyBlueLight)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = IconlyIcons.Verified,
          contentDescription = null,
          tint = DustyBlue,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "MemoryOS will automatically extract entities, dates, places, and tags.",
          style = MaterialTheme.typography.labelSmall,
          color = MidnightNavy
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Bottom Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        if (!hasPermission) {
          Button(
            onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy)
          ) {
            Text("Grant Microphone Permission", fontWeight = FontWeight.Bold)
          }
        } else {
          Button(
            onClick = {
              if (spokenText.isNotBlank()) {
                onSaveSnippet(spokenText)
              }
            },
            enabled = spokenText.isNotBlank(),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MidnightNavy,
              contentColor = PureWhite
            )
          ) {
            Icon(imageVector = IconlyIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Save Memory Snippet",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PulseRings(rmsLevel: Float) {
  val transition = rememberInfiniteTransition(label = "Pulse")
  val scale by transition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.35f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "PulseScale"
  )

  Box(
    modifier = Modifier
      .size(100.dp)
      .scale(scale)
      .clip(CircleShape)
      .background(TerracottaRed.copy(alpha = 0.2f))
  )
}

private fun launchSystemSpeechIntent(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
  val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your quick memory snippet...")
  }
  try {
    launcher.launch(intent)
  } catch (_: Exception) {}
}

private fun startSpeechRecognition(
  context: Context,
  onListeningChange: (Boolean) -> Unit,
  onTextChange: (String) -> Unit,
  onRmsChange: (Float) -> Unit,
  onStatusChange: (String) -> Unit,
  onRecognizerReady: (SpeechRecognizer) -> Unit,
  onFallback: () -> Unit
) {
  if (!SpeechRecognizer.isRecognitionAvailable(context)) {
    onFallback()
    return
  }

  try {
    val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    onRecognizerReady(recognizer)

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    recognizer.setRecognitionListener(object : RecognitionListener {
      override fun onReadyForSpeech(params: Bundle?) {
        onListeningChange(true)
        onStatusChange("Listening... speak now")
      }

      override fun onBeginningOfSpeech() {
        onStatusChange("Recording voice...")
      }

      override fun onRmsChanged(rmsdB: Float) {
        onRmsChange(rmsdB)
      }

      override fun onBufferReceived(buffer: ByteArray?) {}

      override fun onEndOfSpeech() {
        onListeningChange(false)
        onStatusChange("Processing speech...")
      }

      override fun onError(error: Int) {
        onListeningChange(false)
        onStatusChange("Tap microphone to retry or type")
      }

      override fun onResults(results: Bundle?) {
        onListeningChange(false)
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
          onTextChange(matches[0])
          onStatusChange("Memory captured!")
        }
      }

      override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
          onTextChange(matches[0])
        }
      }

      override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    recognizer.startListening(intent)
  } catch (e: Exception) {
    onFallback()
  }
}
