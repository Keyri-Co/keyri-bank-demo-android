package com.keyri.androidFullExample.screens.welcome

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
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
    onShowSnackbar: (String) -> Unit,
) {
    val context = LocalContext.current

    val error = viewModel.errorMessage.collectAsState()
    val blockBiometricPrompt = viewModel.blockBiometricPrompt.collectAsState()

    if (error.value != null) {
        error.value?.let {
            onShowSnackbar(it)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val keyriAccounts =
        viewModel.dataStore.data.collectAsState(initial = KeyriProfiles(null, emptyList()))

    val filteredAccounts =
        keyriAccounts.value.profiles.filter { it.verifyState?.isVerificationDone() == true }

    var showAccountsList by remember { mutableStateOf(false) }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    var clickedAccount by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = filteredAccounts) {
        if (filteredAccounts.any { it.email == keyriAccounts.value.currentProfile && it.biometricsSet } && !blockBiometricPrompt.value) {
            showBiometricPrompt = true
        }
    }

    Column {
        Text(
            modifier =
                Modifier
                    .padding(top = 80.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = if (filteredAccounts.isEmpty()) "Welcome to\nKeyri Bank" else "Welcome back\nto Keyri Bank",
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
                            onDoubleClick = {
                                viewModel.getDeviceInfoJson {
                                    Toast
                                        .makeText(context, "Copied!", Toast.LENGTH_SHORT)
                                        .show()

                                    val clipboard: ClipboardManager? =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                                    val clip = ClipData.newPlainText("Device id values", it)
                                    clipboard?.setPrimaryClip(clip)
                                }
                            },
                            onLongClick = {
                                viewModel.removeAllAccounts {
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
                                        @Suppress("Deprecation")
                                        val vibrator =
                                            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

                                        vibrator?.cancel()
                                        @Suppress("Deprecation")
                                        vibrator?.vibrate(100)
                                    }
                                }
                            },
                        ),
                contentScale = ContentScale.Fit,
                painter = painterResource(id = R.drawable.ic_keyri_icon_full),
                contentDescription = null,
            )
        }

        val containerColors =
            if (filteredAccounts.isEmpty()) {
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
                if (filteredAccounts.size == 1) {
                    if (filteredAccounts.first().associationKey != null) {
                        showBiometricPrompt = true
                    } else {
                        navController.navigate("${Routes.LoginScreen.name}?email=${filteredAccounts.first().email}")
                    }
                } else if (filteredAccounts.size > 1) {
                    showAccountsList = true
                } else {
                    navController.navigate("${Routes.LoginScreen.name}?email=null")
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

    if (showBiometricPrompt) {
        val currentAccount =
            clickedAccount ?: keyriAccounts.value.currentProfile
                ?: filteredAccounts.firstOrNull { it.biometricsSet }?.email
                ?: filteredAccounts.firstOrNull()?.email

        if (filteredAccounts.firstOrNull { it.email == currentAccount }?.associationKey != null) {
            BiometricAuth(
                context,
                "Use Biometric to login as",
                currentAccount ?: filteredAccounts.firstOrNull()?.email,
                {},
                { showBiometricPrompt = false },
            ) {
                viewModel.cryptoLogin(requireNotNull(currentAccount)) {
                    showBiometricPrompt = false

                    navController.navigateWithPopUp(
                        Routes.MainScreen.name,
                        Routes.WelcomeScreen.name,
                    )
                }
            }
        }
    }

    if (showAccountsList) {
        ListModalBottomSheet(
            sheetState = sheetState,
            title = "Choose an account\nto continue to Keyri Bank",
            filteredAccounts.map {
                ModalListItem(
                    iconRes = R.drawable.ic_keyri_logo,
                    text = it.email,
                )
            },
            onListItemClicked = {
                clickedAccount = it.text

                if (filteredAccounts.firstOrNull { a -> a.email == clickedAccount }?.associationKey != null) {
                    showBiometricPrompt = true
                    showAccountsList = false
                } else {
                    navController.navigate("${Routes.LoginScreen.name}?email=$clickedAccount")
                }

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
