package com.example.ui.components

import android.content.Context
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

object OnboardingManager {
  private const val PREF_ONBOARDING = "memory_onboarding_prefs"
  private const val KEY_COMPLETED = "has_completed_questionnaire"
  private const val KEY_USER_GOAL = "user_primary_goal"
  private const val KEY_VAULT_NAME = "user_vault_name"

  fun hasCompletedQuestionnaire(context: Context): Boolean {
    return context.getSharedPreferences(PREF_ONBOARDING, Context.MODE_PRIVATE)
      .getBoolean(KEY_COMPLETED, false)
  }

  fun setCompletedQuestionnaire(
    context: Context,
    goal: String,
    vaultName: String
  ) {
    context.getSharedPreferences(PREF_ONBOARDING, Context.MODE_PRIVATE).edit()
      .putBoolean(KEY_COMPLETED, true)
      .putString(KEY_USER_GOAL, goal)
      .putString(KEY_VAULT_NAME, vaultName)
      .apply()
  }
}

@Composable
fun OnboardingQuestionnaireDialog(
  onComplete: (goal: String, reminderHour: Int, isCloudEnabled: Boolean, vaultName: String) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var currentStep by remember { mutableStateOf(0) }
  var showParticleCelebration by remember { mutableStateOf(false) }

  // Step 1: Goals
  var selectedGoal by remember { mutableStateOf("Daily Thoughts & Journaling") }

  // Step 2: Reminder Rhythm
  var selectedRhythm by remember { mutableStateOf("Evening Reflection (8:30 PM)") }
  var selectedHour by remember { mutableStateOf(20) }

  // Step 3: Vault Security
  var isCloudEnabled by remember { mutableStateOf(true) }

  // Step 4: Persona
  var userVaultName by remember { mutableStateOf("") }

  Dialog(
    onDismissRequest = { /* Require completion or skip */ },
    properties = DialogProperties(
      dismissOnBackPress = false,
      dismissOnClickOutside = false,
      usePlatformDefaultWidth = false
    )
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = WarmIvory,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 16.dp,
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .fillMaxHeight(0.85f)
          .align(Alignment.Center)
          .padding(vertical = 16.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
        ) {
          // Top Header: Step Indicator & Skip
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DustyBlueLight)
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Text(
                text = "STEP ${currentStep + 1} OF 4",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                ),
                color = DustyBlue
              )
            }

            TextButton(
              onClick = {
                OnboardingManager.setCompletedQuestionnaire(context, "General", "Personal Vault")
                onComplete("General", 20, true, "Personal Vault")
                onDismiss()
              }
            ) {
              Text(
                text = "Skip Setup",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextMuted
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Progress Bar
          LinearProgressIndicator(
            progress = { (currentStep + 1) / 4f },
            color = MidnightNavy,
            trackColor = WarmCreamDark,
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp))
          )

          Spacer(modifier = Modifier.height(18.dp))

          // Animated Step Body
          Box(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState())
          ) {
            AnimatedContent(
              targetState = currentStep,
              transitionSpec = {
                (fadeIn() + slideInHorizontally { width -> width / 2 })
                  .togetherWith(fadeOut() + slideOutHorizontally { width -> -width / 2 })
              },
              label = "questionnaire_step"
            ) { step ->
              when (step) {
                0 -> StepOneGoals(
                  selectedGoal = selectedGoal,
                  onSelectGoal = { selectedGoal = it }
                )
                1 -> StepTwoRhythm(
                  selectedRhythm = selectedRhythm,
                  onSelectRhythm = { rhythm, hour ->
                    selectedRhythm = rhythm
                    selectedHour = hour
                  }
                )
                2 -> StepThreeSecurity(
                  isCloudEnabled = isCloudEnabled,
                  onToggleCloud = { isCloudEnabled = it }
                )
                3 -> StepFourPersona(
                  vaultName = userVaultName,
                  onVaultNameChange = { userVaultName = it }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Bottom Action Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (currentStep > 0) {
              OutlinedButton(
                onClick = { currentStep-- },
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                  .weight(1f)
                  .height(50.dp)
              ) {
                Text(
                  text = "Back",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary
                )
              }
            }

            Button(
              onClick = {
                if (currentStep < 3) {
                  currentStep++
                } else {
                  showParticleCelebration = true
                  val finalName = if (userVaultName.isBlank()) "Personal Vault" else userVaultName.trim()
                  OnboardingManager.setCompletedQuestionnaire(context, selectedGoal, finalName)
                  onComplete(selectedGoal, selectedHour, isCloudEnabled, finalName)
                }
              },
              shape = RoundedCornerShape(18.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = MidnightNavy,
                contentColor = PureWhite
              ),
              modifier = Modifier
                .weight(if (currentStep > 0) 2f else 1f)
                .height(50.dp)
            ) {
              Text(
                text = if (currentStep < 3) "Continue" else "Launch My Vault",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              )
            }
          }
        }
      }

      // Particle celebration burst on finishing questionnaire
      ParticleBurst(
        trigger = showParticleCelebration,
        onFinished = { onDismiss() }
      )
    }
  }
}

@Composable
private fun StepOneGoals(
  selectedGoal: String,
  onSelectGoal: (String) -> Unit
) {
  Column(horizontalAlignment = Alignment.Start) {
    Text(
      text = "What is your primary focus?",
      style = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp
      ),
      color = BrandBlack
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "We will optimize your quick-capture shortcuts and memory categories based on your goals.",
      style = MaterialTheme.typography.bodySmall,
      color = TextSecondary
    )

    Spacer(modifier = Modifier.height(16.dp))

    val goals = listOf(
      QuestionnaireOption("Daily Thoughts & Journaling", "Reflect on moments and track personal insights.", IconlyIcons.Document),
      QuestionnaireOption("Voice Reflections on the Go", "Speak spontaneous ideas with instant transcription.", IconlyIcons.Voice),
      QuestionnaireOption("Receipts & Expense Tracking", "Organize spending, purchases, and warranties.", IconlyIcons.Receipt),
      QuestionnaireOption("Travel, Places & Bucket Lists", "Log destinations, bookings, and hidden gems.", IconlyIcons.Location)
    )

    goals.forEach { item ->
      val isSelected = selectedGoal == item.title
      OptionCard(
        title = item.title,
        subtitle = item.subtitle,
        icon = item.icon,
        isSelected = isSelected,
        onClick = { onSelectGoal(item.title) }
      )
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun StepTwoRhythm(
  selectedRhythm: String,
  onSelectRhythm: (String, Int) -> Unit
) {
  Column(horizontalAlignment = Alignment.Start) {
    Text(
      text = "When do you like to reflect?",
      style = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp
      ),
      color = BrandBlack
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "MemoryOS delivers a quiet, respectful daily nudge to keep your journaling streak alive.",
      style = MaterialTheme.typography.bodySmall,
      color = TextSecondary
    )

    Spacer(modifier = Modifier.height(16.dp))

    val rhythms = listOf(
      RhythmOption("Morning Clarity (8:00 AM)", "Start your day with intent and focus.", 8, IconlyIcons.Calendar),
      RhythmOption("Midday Check-in (1:00 PM)", "Log afternoon encounters and quick receipts.", 13, IconlyIcons.Refresh),
      RhythmOption("Evening Reflection (8:30 PM)", "Wind down and record the highlight of your day.", 20, IconlyIcons.Heart),
      RhythmOption("Spontaneous Only (No Alarms)", "Capture strictly whenever inspiration strikes.", -1, IconlyIcons.Shield)
    )

    rhythms.forEach { item ->
      val isSelected = selectedRhythm == item.title
      OptionCard(
        title = item.title,
        subtitle = item.subtitle,
        icon = item.icon,
        isSelected = isSelected,
        onClick = { onSelectRhythm(item.title, item.hour) }
      )
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun StepThreeSecurity(
  isCloudEnabled: Boolean,
  onToggleCloud: (Boolean) -> Unit
) {
  Column(horizontalAlignment = Alignment.Start) {
    Text(
      text = "Vault Architecture",
      style = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp
      ),
      color = BrandBlack
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Choose how your memories are stored and synchronized across your private devices.",
      style = MaterialTheme.typography.bodySmall,
      color = TextSecondary
    )

    Spacer(modifier = Modifier.height(16.dp))

    OptionCard(
      title = "Cloud Vault Sync (Recommended)",
      subtitle = "Zero-knowledge end-to-end encryption with seamless multi-device backup.",
      icon = IconlyIcons.Share,
      isSelected = isCloudEnabled,
      onClick = { onToggleCloud(true) }
    )

    Spacer(modifier = Modifier.height(10.dp))

    OptionCard(
      title = "Air-Gapped Local Storage Only",
      subtitle = "Memories never leave your local phone storage. Maximum physical isolation.",
      icon = IconlyIcons.Lock,
      isSelected = !isCloudEnabled,
      onClick = { onToggleCloud(false) }
    )
  }
}

@Composable
private fun StepFourPersona(
  vaultName: String,
  onVaultNameChange: (String) -> Unit
) {
  Column(horizontalAlignment = Alignment.Start) {
    Text(
      text = "Name your private vault",
      style = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp
      ),
      color = BrandBlack
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Personalize your memory workspace. You can change this anytime in Settings.",
      style = MaterialTheme.typography.bodySmall,
      color = TextSecondary
    )

    Spacer(modifier = Modifier.height(18.dp))

    OutlinedTextField(
      value = vaultName,
      onValueChange = onVaultNameChange,
      label = { Text("Vault Name or Nickname") },
      placeholder = { Text("e.g. Alex's Memory Vault") },
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MidnightNavy,
        unfocusedBorderColor = CardBorder,
        focusedContainerColor = PureWhite,
        unfocusedContainerColor = PureWhite
      ),
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = DustyBlueLight),
      border = BorderStroke(1.dp, CardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = IconlyIcons.Verified,
          contentDescription = null,
          tint = DustyBlue,
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = "Your vault is initialized with zero-knowledge AES-256 local database protection.",
          style = MaterialTheme.typography.labelSmall,
          color = MidnightNavy
        )
      }
    }
  }
}

@Composable
private fun OptionCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
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
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(if (isSelected) PureWhite.copy(alpha = 0.15f) else DustyBlueLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isSelected) PureWhite else DustyBlue,
          modifier = Modifier.size(20.dp)
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
          color = if (isSelected) PureWhite else TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = if (isSelected) PureWhite.copy(alpha = 0.8f) else TextSecondary
        )
      }

      if (isSelected) {
        Icon(
          imageVector = IconlyIcons.Check,
          contentDescription = "Selected",
          tint = PureWhite,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

private data class QuestionnaireOption(
  val title: String,
  val subtitle: String,
  val icon: ImageVector
)

private data class RhythmOption(
  val title: String,
  val subtitle: String,
  val hour: Int,
  val icon: ImageVector
)
