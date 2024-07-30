package com.keyri.androidFullExample.screens.verified

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.keyri.androidFullExample.R
import com.keyri.androidFullExample.composables.BiometricAuth
import com.keyri.androidFullExample.composables.KeyriButton
import com.keyri.androidFullExample.data.KeyriIcon
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.theme.textColor
import com.keyri.androidFullExample.theme.verifiedTextColor
import com.keyri.androidFullExample.utils.navigateWithPopUp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.concurrent.timer

@Composable
fun VerifiedScreen(
    viewModel: VerifiedViewModel = koinViewModel(),
    navController: NavHostController,
    customToken: String,
    onShowSnackbar: (String) -> Unit,
) {
    SideEffect {
        viewModel.saveBiometricAuth(customToken)
    }

    val scope = rememberCoroutineScope()
    val error = viewModel.errorMessage.collectAsState()
    val keyriProfiles = viewModel.dataStore.data.collectAsState(KeyriProfiles(null, emptyList()))
    val currentProfile =
        keyriProfiles.value.profiles.firstOrNull { it.email == keyriProfiles.value.currentProfile }
    val loading = viewModel.loading.collectAsState()
    val passwordlessCredentialCreated = remember { mutableStateOf(false) }
    val navigateToMain = remember { mutableStateOf(false) }

    if (error.value != null) {
        error.value?.let {
            onShowSnackbar(it)
        }
    }

    if (navigateToMain.value) {
        navController.navigateWithPopUp(
            "${Routes.MainScreen.name}?email={${currentProfile?.email}}",
            Routes.WelcomeScreen.name,
        )
    }

    if (loading.value) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        Column {
            val context = LocalContext.current
            var showBiometricPrompt by remember { mutableStateOf(false) }

            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp)
                        .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                text =
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = verifiedTextColor)) {
                            append(currentProfile?.email)
                        }

                        if (currentProfile?.phone != null) {
                            append(" and ")

                            withStyle(style = SpanStyle(color = verifiedTextColor)) {
                                append(currentProfile.phone)
                            }
                        }

                        if (currentProfile?.isVerify == true) {
                            append(" verified")
                        } else {
                            append(" confirmed")
                        }
                    },
                style = MaterialTheme.typography.headlineSmall,
                color = textColor,
            )

            KeyriIcon(
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 40.dp),
                iconResId = R.drawable.ic_done,
                iconTint = verifiedTextColor,
            )

            if (passwordlessCredentialCreated.value) {
                Text(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp)
                            .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text = "Passwordless credential created",
                    style = MaterialTheme.typography.headlineSmall,
                    color = textColor,
                )

                KeyriIcon(
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 40.dp),
                    iconResId = R.drawable.ic_key,
                    iconTint = verifiedTextColor,
                    iconSizeFraction = 0.5F,
                )
            }

            if (showBiometricPrompt) {
                BiometricAuth(context, "Set up Biometric authentication", null, {
                    onShowSnackbar(it)
                }, { showBiometricPrompt = false }) {
                    showBiometricPrompt = false
                    passwordlessCredentialCreated.value = true

                    scope.launch(Dispatchers.IO) {
                        timer(initialDelay = 0L, period = 2_000L) {
                            navigateToMain.value = true
                        }
                    }
                }
            }

            KeyriButton(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                text = "Set up biometric authentication",
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
            ) {
                showBiometricPrompt = true
            }
        }
    }
}
