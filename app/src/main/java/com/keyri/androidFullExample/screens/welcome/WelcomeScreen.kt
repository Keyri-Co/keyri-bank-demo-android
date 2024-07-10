package com.keyri.androidFullExample.screens.welcome

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
import com.keyri.androidFullExample.R
import com.keyri.androidFullExample.composables.BiometricAuth
import com.keyri.androidFullExample.composables.KeyriButton
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.theme.primaryDisabled
import com.keyri.androidFullExample.theme.textColor
import com.keyri.androidFullExample.utils.navigateWithPopUp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel = koinViewModel(),
    navController: NavHostController,
) {
    SideEffect {
        viewModel.checkKeyriAccounts()
    }

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val keyriAccounts = viewModel.keyriAccounts.collectAsState()
    var showAccountsList by remember { mutableStateOf(false) }
    var needAuth by remember { mutableStateOf(false) }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    var clickedAccount by remember { mutableStateOf<String?>(null) }
    var blockBiometricPrompt by remember { mutableStateOf(false) }

    if (!blockBiometricPrompt && keyriAccounts.value.currentProfile != null && !needAuth) {
        BiometricAuth(
            LocalContext.current,
            "Use Biometric to login as",
            keyriAccounts.value.currentProfile,
            {},
            { needAuth = true }) {
            navController.navigateWithPopUp(Routes.MainScreen.name, Routes.WelcomeScreen.name)
            blockBiometricPrompt = true
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
                    if (needAuth) {
                        showBiometricPrompt = true
                    } else if (keyriAccounts.value.profiles.size == 1) {
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
            BiometricAuth(context, promptInfo.first, promptInfo.second, {},
                { showBiometricPrompt = false }) {
                viewModel.setCurrentProfile(
                    clickedAccount ?: keyriAccounts.value.profiles.firstOrNull()?.email
                )

                navController.navigateWithPopUp(Routes.MainScreen.name, Routes.WelcomeScreen.name)
                showBiometricPrompt = false
                blockBiometricPrompt = true
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
