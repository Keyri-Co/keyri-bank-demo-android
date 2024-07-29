package com.keyri.androidFullExample.screens.welcome

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.keyri.androidFullExample.composables.ListModalBottomSheet
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.data.ModalListItem
import com.keyri.androidFullExample.data.VerifyingState
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.theme.textColor
import com.keyri.androidFullExample.utils.navigateWithPopUp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel = koinViewModel(),
    navController: NavHostController,
) {
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val keyriAccounts =
        viewModel.dataStore.data.collectAsState(initial = KeyriProfiles(null, emptyList()))
    var showAccountsList by remember { mutableStateOf(false) }
    var needAuth by remember { mutableStateOf(false) }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    var clickedAccount by remember { mutableStateOf<String?>(null) }
    var blockBiometricPrompt by remember { mutableStateOf(false) }

    val currentProfile = keyriAccounts.value.currentProfile
    val profile = keyriAccounts.value.profiles.firstOrNull { it.email == currentProfile }

    // TODO: Simplify here

    if (
        currentProfile != null &&
        profile?.emailVerifyState == VerifyingState.VERIFYING ||
        profile?.phoneVerifyState == VerifyingState.VERIFYING
    ) {
        navController.navigateWithPopUp(
            "${Routes.VerifyScreen.name}?name=${profile?.name}?email=${profile?.email}&number=${profile?.phone}&isVerify=${profile?.isVerify}",
            Routes.WelcomeScreen.name,
        )
    } else if (!blockBiometricPrompt &&
        (
                currentProfile != null &&
                        profile?.emailVerifyState == VerifyingState.VERIFIED &&
                        profile.phoneVerifyState == VerifyingState.VERIFIED
                ) &&
        !needAuth
    ) {
        BiometricAuth(
            LocalContext.current,
            "Use Biometric to login as",
            currentProfile,
            {},
            { needAuth = true },
        ) {
            navController.navigateWithPopUp(Routes.MainScreen.name, Routes.WelcomeScreen.name)
            blockBiometricPrompt = true
        }
    } else {
        Column {
            Text(
                modifier =
                Modifier
                    .padding(top = 80.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                text = if (keyriAccounts.value.profiles.isEmpty()) "Welcome to\nKeyri Bank" else "Welcome back\nto Keyri Bank",
                style = MaterialTheme.typography.headlineLarge,
                color = textColor,
            )

            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1F),
            ) {
                Image(
                    modifier =
                    Modifier
                        .size(130.dp, 62.dp)
                        .align(Alignment.Center)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                viewModel.removeAllAccounts {
                                    @Suppress("Deprecation")
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val effect =
                                            VibrationEffect.createOneShot(
                                                100,
                                                VibrationEffect.DEFAULT_AMPLITUDE,
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
                    painter = painterResource(id = R.drawable.ic_tabby_charcoal),
                    contentDescription = null,
                )
            }

            val containerColors =
                if (keyriAccounts.value.profiles.isEmpty()) {
                    MaterialTheme.colorScheme.onPrimary to
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.04F,
                            )
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
                        navController.navigate("${Routes.VerifyScreen.name}?name=null?email=null&number=null&isVerify=false")
                    }
                },
            )

            KeyriButton(
                Modifier.padding(top = 28.dp),
                containerColor = containerColors.second,
                text = "Sign up",
                onClick = {
                    navController.navigate(Routes.SignupScreen.name)
                },
            )
        }

        val promptInfo =
            if (keyriAccounts.value.profiles.size == 1) {
                "Use Biometric to login as" to
                        keyriAccounts.value.profiles
                            .firstOrNull()
                            ?.email
            } else {
                "Use Biometric to login" to null
            }

        if (showBiometricPrompt) {
            BiometricAuth(
                context,
                promptInfo.first,
                promptInfo.second,
                {},
                { showBiometricPrompt = false },
            ) {
                val currentAccount =
                    clickedAccount ?: keyriAccounts.value.profiles
                        .firstOrNull()
                        ?.email

                // TODO: Check is logged in?

                viewModel.cryptoLogin(requireNotNull(currentAccount)) {
                    showBiometricPrompt = false
                    blockBiometricPrompt = true

                    viewModel.setCurrentProfile(currentAccount)

                    navController.navigateWithPopUp(
                        Routes.MainScreen.name,
                        Routes.WelcomeScreen.name,
                    )
                }
            }
        }

        if (showAccountsList) {
            ListModalBottomSheet(
                sheetState = sheetState,
                title = "Choose an account\nto continue to Keyri Bank",
                keyriAccounts.value.profiles.map {
                    ModalListItem(
                        iconRes = R.drawable.ic_tabby_charcoal,
                        text = it.email,
                    )
                },
                onListItemClicked = {
                    showBiometricPrompt = true
                    showAccountsList = false
                    clickedAccount = it.text

                    coroutineScope.launch(Dispatchers.IO) {
                        sheetState.hide()
                    }
                },
                onDismissRequest = {
                    showAccountsList = false

                    coroutineScope.launch(Dispatchers.IO) {
                        sheetState.hide()
                    }
                },
            )
        }
    }
}
