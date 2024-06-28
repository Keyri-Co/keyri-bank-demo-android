package com.keyri.demo.composables

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.keyri.demo.utils.getActivity

@Composable
fun BiometricAuth(
    context: Context,
    biometricPromptTitle: String,
    biometricPromptSubtitle: String? = null,
    onShowSnackbar: (String) -> Unit,
    onBiometricAuthenticationCancelled: () -> Unit,
    onBiometricAuthenticationSucceeded: () -> Unit
) {
    var showBiometricPrompt by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        showBiometricPrompt = true
    }

    if (showBiometricPrompt) {
        val fragmentActivity = context.getActivity()
            ?: throw IllegalArgumentException("Should be FragmentActivity")
        val executor = ContextCompat.getMainExecutor(fragmentActivity)
        val biometricPrompt = BiometricPrompt(
            fragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    showBiometricPrompt = false

                    Log.e("KeyriDemo", "Biometric authentication cancelled")
                    onShowSnackbar("Biometric authentication cancelled")
                    onBiometricAuthenticationCancelled()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    showBiometricPrompt = false
                    onBiometricAuthenticationSucceeded()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showBiometricPrompt = false

                    Log.e("KeyriDemo", "Biometric authentication failed")
                    onShowSnackbar("Biometric authentication failed")
                    onBiometricAuthenticationCancelled()
                }
            }
        )

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(biometricPromptTitle)
            .setSubtitle(biometricPromptSubtitle)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfoBuilder)
    }

    LaunchedEffect(biometricPromptTitle) {
        when (BiometricManager
            .from(context)
            .canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(
                    "KeyriDemo",
                    "App can authenticate using biometrics."
                )
                showBiometricPrompt = true
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent =
                        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL
                            )
                        }

                    showBiometricPrompt = false

                    launcher.launch(enrollIntent)
                }
            }

            else -> {
                val message = "Biometric features are currently unavailable"

                Log.e("KeyriDemo", message)
                onShowSnackbar(message)
            }
        }
    }
}