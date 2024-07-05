package com.keyri.keyridemo.screens.welcome

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavHostController
import com.keyri.keyridemo.R
import com.keyri.keyridemo.composables.BiometricAuth
import com.keyri.keyridemo.composables.KeyriButton
import com.keyri.keyridemo.routes.Routes
import com.keyri.keyridemo.ui.theme.primaryDisabled
import com.keyri.keyridemo.ui.theme.textColor
import com.keyri.keyridemo.utils.getActivity
import com.keyri.keyridemo.utils.navigateWithPopUp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel = koinViewModel(),
    navController: NavHostController,
    onShowSnackbar: (String) -> Unit,
) {
    SideEffect {
        viewModel.checkKeyriAccounts()
    }

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val keyriAccounts = viewModel.keyriAccounts.collectAsState()
    var showAccountsList by remember { mutableStateOf(false) }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    var clickedAccount by remember { mutableStateOf<String?>(null) }

    if (keyriAccounts.value.currentProfile != null) {
        BiometricAuth(
            LocalContext.current,
            "Use Biometric to login as",
            keyriAccounts.value.currentProfile,
            onShowSnackbar,
            { context.getActivity()?.finish() }) {
            navController.navigateWithPopUp(
                Routes.MainScreen.name, Routes.WelcomeScreen.name
            )
        }
    } else {
        Column {
            Text(
                modifier = Modifier
                    .padding(top = 80.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                text = if (keyriAccounts.value.profiles.isEmpty()) "Welcome to\nKeyri Bank" else "Welcome back\nto Keyri Bank",
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
                            onClick = {},
                            onLongClick = {
                                viewModel.removeAllAccounts {
                                    viewModel.checkKeyriAccounts()

                                    @Suppress("Deprecation")
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val effect = VibrationEffect.createOneShot(
                                            100,
                                            VibrationEffect.DEFAULT_AMPLITUDE
                                        )

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            val vibratorManager =
                                                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                                            val vibrator = vibratorManager?.defaultVibrator

                                            vibrator?.cancel()
                                            vibrator?.vibrate(effect)
                                        }
                                    } else {
                                        val vibrator =
                                            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

                                        vibrator?.cancel()
                                        vibrator?.vibrate(100)
                                    }
                                }
                            },
                        ),
                    contentScale = ContentScale.Fit,
                    painter = painterResource(id = R.drawable.ic_keyri_icon_full),
                    contentDescription = null
                )
            }

            val containerColors = if (keyriAccounts.value.profiles.isEmpty()) {
                MaterialTheme.colorScheme.onPrimary to MaterialTheme.colorScheme.primary.copy(alpha = 0.04F)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.04F) to MaterialTheme.colorScheme.onPrimary
            }

            KeyriButton(
                Modifier,
                text = "Log in",
                containerColor = containerColors.first,
                onClick = {
                    if (keyriAccounts.value.profiles.size == 1) {
                        showBiometricPrompt = true
                    } else if (keyriAccounts.value.profiles.size > 1) {
                        showAccountsList = true
                    } else {
                        navController.navigate("${Routes.VerifyScreen.name}?email=null&number=null&isVerify=false")
                    }
                })

            KeyriButton(Modifier.padding(top = 28.dp),
                containerColor = containerColors.second,
                text = "Sign up",
                onClick = {
                    navController.navigate(Routes.SignupScreen.name)
                })
        }

        val promptInfo = if (keyriAccounts.value.profiles.size == 1) {
            "Use Biometric to login as" to keyriAccounts.value.profiles.firstOrNull()?.email
        } else {
            "Use Biometric to login" to null
        }

        if (showBiometricPrompt) {
            BiometricAuth(context, promptInfo.first, promptInfo.second, {
                onShowSnackbar(it)
            }, { showBiometricPrompt = false }) {
                viewModel.setCurrentProfile(
                    clickedAccount ?: keyriAccounts.value.profiles.firstOrNull()?.email
                )

                navController.navigateWithPopUp(
                    Routes.MainScreen.name,
                    Routes.WelcomeScreen.name
                )
                showBiometricPrompt = false
            }
        }

        if (showAccountsList) {
            ModalBottomSheet(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                dragHandle = null,
                onDismissRequest = { showAccountsList = false },
                sheetState = sheetState
            ) {
                Column {
                    Text(
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        text = "Choose an account\nto continue to Keyri Bank"
                    )

                    // TODO: Update all images to vectors (from https://keyri.slack.com/archives/D0760PRVCE5/p1720013532362389: ic_keyri_logo, ic_check)

                    // TODO: On biometric cancellation on main screen with one logged in user -> show welcome back screen
                    // TODO: If user tap on Login in this case -> show biometric prompt

                    // TODO: Second biometric prompt appear after selecting account from list

                    // TODO: If user have existing account (but not logged in) and it's new device (signal from fraud device) -> open verification screen


                    LazyColumn(modifier = Modifier.padding(vertical = 20.dp)) {
                        items(keyriAccounts.value.profiles) {
                            Row(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        showBiometricPrompt = true
                                        clickedAccount = it.email
                                    }
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_keyri_logo),
                                    contentDescription = null
                                )

                                Text(
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 16.dp),
                                    textAlign = TextAlign.Center,
                                    text = it.email
                                )
                            }

                            if (keyriAccounts.value.profiles.indexOf(it) != keyriAccounts.value.profiles.lastIndex) {
                                HorizontalDivider(color = primaryDisabled, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}
