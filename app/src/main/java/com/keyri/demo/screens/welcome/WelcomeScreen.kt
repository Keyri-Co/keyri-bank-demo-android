package com.keyri.demo.screens.welcome

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.keyri.demo.R
import com.keyri.demo.composables.KeyriButton
import com.keyri.demo.data.KeyriProfiles
import com.keyri.demo.routes.Routes
import com.keyri.demo.ui.theme.textColor
import com.keyri.demo.utils.getActivity
import com.keyri.demo.utils.navigateWithPopUp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel = koinViewModel(),
    navController: NavHostController,
    keyriAccounts: KeyriProfiles?,
    onShowSnackbar: (String) -> Unit,
    onAccountsRemoved: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showAccountsList by remember { mutableStateOf(false) }

    val context = LocalContext.current

    var showBiometricPrompt by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        showBiometricPrompt = true
    }

    var clickedAccount by remember { mutableStateOf<String?>(null) }

    Column {
        Text(
            modifier = Modifier
                .padding(top = 80.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = if (keyriAccounts?.profiles?.isEmpty() == true) "Welcome to\nKeyri Bank" else "Welcome back\nto Keyri Bank",
            style = MaterialTheme.typography.headlineLarge,
            color = textColor
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
        ) {
            Image(
                modifier = Modifier
                    .size(130.dp, 62.dp)
                    .align(Alignment.Center)
                    .combinedClickable(
                        onClick = {
                            // Do nothing
                        },
                        onLongClick = {
                            viewModel.removeAllAccounts {
                                onAccountsRemoved()

                                val vibrator =
                                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

                                vibrator?.cancel()

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val effect = VibrationEffect.createOneShot(
                                        100,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                    vibrator?.vibrate(effect)
                                } else {
                                    vibrator?.vibrate(100)
                                }
                            }
                        },
                    ),
                contentScale = ContentScale.Fit,
                painter = painterResource(id = R.drawable.keyri_logo),
                contentDescription = null
            )
        }

        val containerColors = if (keyriAccounts?.profiles?.isEmpty() == true) {
            MaterialTheme.colorScheme.onPrimary to MaterialTheme.colorScheme.primary.copy(alpha = 0.04F)
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.04F) to MaterialTheme.colorScheme.onPrimary
        }

        KeyriButton(Modifier, text = "Log in", containerColor = containerColors.first, onClick = {
            // TODO: If one account -> show biometric prompt to login into this account
            if (keyriAccounts.isNotEmpty()) {
                showAccountsList = true
            } else {
                navController.navigate(Routes.LoginScreen.name)
//                navController.navigate("${Routes.VerifyScreen.name}?email=$email&number=$mobile&isVerify=false")
            }
        })

        KeyriButton(Modifier.padding(top = 28.dp),
            containerColor = containerColors.second,
            text = "Sign up",
            onClick = {
                navController.navigate(Routes.SignupScreen.name)
            })
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

                    Log.e("KeyriDemo", "Biometric authentication failed")
                    onShowSnackbar("Biometric authentication failed")
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    showBiometricPrompt = false
                    viewModel.saveBiometricAuth()

                    // TODO: Add impl (enter email screen)
                    navController.navigateWithPopUp(
                        Routes.MainScreen.name,
                        Routes.WelcomeScreen.name
                    )
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showBiometricPrompt = false

                    Log.e("KeyriDemo", "Biometric authentication failed")
                    onShowSnackbar("Biometric authentication failed")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Set up")
            .setSubtitle("biometric authentication")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    if (showAccountsList) {
        ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            dragHandle = null,
            onDismissRequest = { showAccountsList = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth(),
                    text = "Choose an account\nto continue to Keyri Bank"
                )

                LazyColumn(modifier = Modifier.padding(vertical = 40.dp)) {
                    items(keyriAccounts?.profiles?.map { it.email } ?: emptyList()) {
                        Column(modifier = Modifier
                            .wrapContentHeight()
                            .clickable {
                                when (BiometricManager
                                    .from(context)
                                    .canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)) {
                                    BiometricManager.BIOMETRIC_SUCCESS -> {
                                        Log.d(
                                            "KeyriDemo",
                                            "App can authenticate using biometrics."
                                        )
                                        showBiometricPrompt = true
                                        clickedAccount = it
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
                            }) {
                            Text(
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .fillMaxWidth(),
                                text = it
                            )
                        }
                    }
                }
            }
        }
    }
}
