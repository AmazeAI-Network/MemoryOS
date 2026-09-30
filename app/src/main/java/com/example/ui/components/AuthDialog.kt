package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

enum class AuthMode {
  SIGN_IN,
  SIGN_UP
}

@Composable
fun AuthDialog(
  initialMode: AuthMode = AuthMode.SIGN_IN,
  isLoading: Boolean = false,
  errorMessage: String? = null,
  onDismiss: () -> Unit,
  onSignIn: (String, String) -> Unit,
  onSignUp: (String, String, String) -> Unit
) {
  var mode by remember { mutableStateOf(initialMode) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var fullName by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val focusManager = LocalFocusManager.current

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = WarmIvory,
      border = BorderStroke(1.dp, CardBorder),
      shadowElevation = 12.dp,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 20.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top row with Close button
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
              text = "PRIVATE CLOUD VAULT",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              ),
              color = DustyBlue
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(34.dp)
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

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = if (mode == AuthMode.SIGN_IN) "Welcome Back" else "Create Your Account",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
          ),
          color = BrandBlack,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = if (mode == AuthMode.SIGN_IN)
            "Sign in to synchronize your memories and reflections with your private Cloud Vault."
          else
            "Join MemoryOS to secure your memories across devices with zero-knowledge encryption.",
          style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
          color = TextSecondary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Mode Switcher Tabs
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = WarmCreamDark,
          border = BorderStroke(1.dp, CardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(4.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (mode == AuthMode.SIGN_IN) MidnightNavy else Color.Transparent)
                .clickable { mode = AuthMode.SIGN_IN }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Sign In",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (mode == AuthMode.SIGN_IN) PureWhite else TextSecondary
                )
              )
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (mode == AuthMode.SIGN_UP) MidnightNavy else Color.Transparent)
                .clickable { mode = AuthMode.SIGN_UP }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Sign Up",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (mode == AuthMode.SIGN_UP) PureWhite else TextSecondary
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Error message banner
        if (errorMessage != null) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFFFEBEE),
            border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 16.dp)
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = IconlyIcons.Info,
                contentDescription = null,
                tint = Color(0xFFC62828),
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC62828),
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Form Fields
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (mode == AuthMode.SIGN_UP) {
            OutlinedTextField(
              value = fullName,
              onValueChange = { fullName = it },
              label = { Text("Full Name") },
              placeholder = { Text("e.g. Alex Hunter") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
              ),
              shape = RoundedCornerShape(16.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MidnightNavy,
                unfocusedBorderColor = CardBorder,
                focusedContainerColor = PureWhite,
                unfocusedContainerColor = PureWhite
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            placeholder = { Text("alex@example.com") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Email,
              imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MidnightNavy,
              unfocusedBorderColor = CardBorder,
              focusedContainerColor = PureWhite,
              unfocusedContainerColor = PureWhite
            ),
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("••••••••") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            trailingIcon = {
              TextButton(onClick = { passwordVisible = !passwordVisible }) {
                Text(
                  text = if (passwordVisible) "Hide" else "Show",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = DustyBlue
                )
              }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MidnightNavy,
              unfocusedBorderColor = CardBorder,
              focusedContainerColor = PureWhite,
              unfocusedContainerColor = PureWhite
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Button
        Button(
          onClick = {
            if (email.isBlank() || password.isBlank()) {
              Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
              return@Button
            }
            if (mode == AuthMode.SIGN_UP && fullName.isBlank()) {
              Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
              return@Button
            }

            if (mode == AuthMode.SIGN_IN) {
              onSignIn(email.trim(), password)
            } else {
              onSignUp(email.trim(), password, fullName.trim())
            }
          },
          enabled = !isLoading,
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MidnightNavy,
            contentColor = PureWhite
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              color = PureWhite,
              modifier = Modifier.size(22.dp),
              strokeWidth = 2.5.dp
            )
          } else {
            Text(
              text = if (mode == AuthMode.SIGN_IN) "Sign In to Vault" else "Create Account & Sync",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = IconlyIcons.Lock,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(13.dp)
          )
          Text(
            text = "End-to-End Encrypted Cloud Storage",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
          )
        }
      }
    }
  }
}
