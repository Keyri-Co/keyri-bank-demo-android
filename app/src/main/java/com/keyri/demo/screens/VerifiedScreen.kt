package com.keyri.demo.screens

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.keyri.demo.R
import com.keyri.demo.composables.KeyriButton
import com.keyri.demo.routes.Routes
import com.keyri.demo.ui.theme.textColor
import com.keyri.demo.ui.theme.verifiedTextColor
import com.keyri.demo.utils.getActivity

@Composable
fun VerifiedScreen(
    navController: NavHostController,
    isVerified: Boolean,
    email: String,
    number: String? = null
) {
    Column {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp)
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = verifiedTextColor)) {
                    append(email)
                }

                if (number != null) {
                    append(" and ")

                    withStyle(style = SpanStyle(color = verifiedTextColor)) {
                        append(number)
                    }
                }

                if (isVerified) {
                    append(" verified")
                } else {
                    append(" confirmed")
                }
            },
            style = MaterialTheme.typography.headlineSmall,
            color = textColor
        )

        Image(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterHorizontally)
                .padding(top = 40.dp),
            painter = painterResource(id = R.drawable.icon_check),
            contentDescription = null
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "Passwordless credential created",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor
        )

        Image(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterHorizontally)
                .padding(top = 40.dp),
            painter = painterResource(id = R.drawable.icon_key),
            contentDescription = null
        )

        val context = LocalContext.current

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            // TODO: If enrolled
        }

        var showBiometricPrompt by remember { mutableStateOf(false) }

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
                        // TODO: Dismiss and show error
                        Log.e("Auth error", "error")
                    }

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        // TODO: Add it to prefs
                        navController.navigate(Routes.MainScreen.name)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.e("Auth failed", "error")
                        // TODO: Show and print error
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Set up")
                .setSubtitle("biometric authentication")
                .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }

        KeyriButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            text = "Set up biometric authentication",
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
        ) {
            when (BiometricManager.from(context)
                .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    Log.d("MY_APP_TAG", "App can authenticate using biometrics.")

                    showBiometricPrompt = true
                }

                // TODO: Display errors
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    Log.e("MY_APP_TAG", "No biometric features available on this device.")

                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    // Prompts the user to create credentials that your app accepts.
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                        )
                    }

                    launcher.launch(enrollIntent)
                }
            }
        }
    }
}
