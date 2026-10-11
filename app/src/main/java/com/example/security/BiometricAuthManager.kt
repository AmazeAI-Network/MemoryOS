package com.example.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricStatus {
  READY,
  NOT_ENROLLED,
  NO_HARDWARE,
  HW_UNAVAILABLE,
  SECURITY_UPDATE_REQUIRED,
  UNSUPPORTED
}

/**
 * Enterprise Biometric Authentication Manager.
 * Orchestrates biometric prompt (fingerprint / face) and device credential fallback
 * to gate access to the MemoryOS encrypted personal vault.
 */
object BiometricAuthManager {

  private const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG or
      BiometricManager.Authenticators.DEVICE_CREDENTIAL

  fun checkBiometricStatus(context: Context): BiometricStatus {
    return try {
      val biometricManager = BiometricManager.from(context)
      when (biometricManager.canAuthenticate(AUTHENTICATORS)) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.READY
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HW_UNAVAILABLE
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.SECURITY_UPDATE_REQUIRED
        else -> BiometricStatus.UNSUPPORTED
      }
    } catch (_: Throwable) {
      BiometricStatus.UNSUPPORTED
    }
  }

  fun isBiometricLockEnabled(context: Context): Boolean {
    return EncryptedPreferencesManager.getInstance(context).isBiometricLockEnabled()
  }

  fun setBiometricLockEnabled(context: Context, enabled: Boolean) {
    EncryptedPreferencesManager.getInstance(context).setBiometricLockEnabled(enabled)
  }

  fun authenticate(
    activity: FragmentActivity,
    title: String = "Unlock Memory Vault",
    subtitle: String = "Authenticate to access your secured memories and tokens",
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    val executor = ContextCompat.getMainExecutor(activity)

    val callback = object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        onSuccess()
      }

      override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
            errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
            errorCode != BiometricPrompt.ERROR_CANCELED
        ) {
          onError(errString.toString())
        }
      }

      override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        // Do not fail immediately on first misread, allow system retry
      }
    }

    try {
      val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setAllowedAuthenticators(AUTHENTICATORS)
        .build()

      val biometricPrompt = BiometricPrompt(activity, executor, callback)
      biometricPrompt.authenticate(promptInfo)
    } catch (e: Exception) {
      onError(e.message ?: "Authentication failed")
    }
  }
}
