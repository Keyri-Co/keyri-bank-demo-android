package com.keyri.androidFullExample.screens.verify

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.keyri.androidFullExample.composables.KeyriButton
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.data.VerifyingState
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.services.entities.responses.SmsLoginResponse
import com.keyri.androidFullExample.theme.primaryDisabled
import com.keyri.androidFullExample.theme.textColor
import org.koin.androidx.compose.koinViewModel

@Composable
fun VerifyScreen(
    viewModel: VerifyViewModel = koinViewModel(),
    navController: NavController,
    isVerify: Boolean,
    name: String? = null,
    email: String? = null,
    number: String? = null,
    onShowSnackbar: (String) -> Unit,
) {
    val context = LocalContext.current
    val keyriProfiles = viewModel.dataStore.data.collectAsState(KeyriProfiles(null, listOf()))
    val profile =
        keyriProfiles.value.profiles.firstOrNull { it.email == keyriProfiles.value.currentProfile }
    val error = viewModel.errorMessage.collectAsState()
    val verifyState = remember { mutableStateOf<VerifyingState?>(null) }

    if (error.value != null) {
        error.value?.let {
            onShowSnackbar(it)
        }
    }

    SideEffect {
        if (profile?.verifyState?.isVerificationDone() == true) {
            // TODO: Remove logs
            Log.e("VERIFY STATE DONE", "ok, $profile")

            navController.navigate(Routes.VerifiedScreen.name) {
                popUpTo(Routes.VerifyScreen.name) {
                    inclusive = true
                }
            }
        }
    }

    if (profile?.verifyState != null && !profile.verifyState.isVerificationDone()) {
        // TODO: Remove logs
        Log.e("BACK HANDLER", "ok, $profile")

        BackHandler(true) {
            viewModel.cancelVerify {
                navController.navigate(Routes.WelcomeScreen.name) {
                    popUpTo(Routes.VerifyScreen.name) {
                        inclusive = true
                    }
                }
            }
        }
    }

    Column {
        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp)
                    .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "Help us ${if (isVerify) "verify" else "confirm"} your identity",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor,
        )

        Column(modifier = Modifier.weight(1F), verticalArrangement = Arrangement.Bottom) {
            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                text =
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Option 1:")
                        }

                        append(" We'll send you an email magic link. It expires 15 minutes after you request it.")
                    },
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
            )

            KeyriButton(
                modifier = Modifier.padding(top = 20.dp),
                disabledTextColor = primaryDisabled,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
                disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
                disabledBorderColor = primaryDisabled,
                enabled = profile?.verifyState == null || profile.verifyState is VerifyingState.Email,
                text =
                    if ((profile?.verifyState is VerifyingState.Email && profile.verifyState.isVerified) ||
                        (profile?.verifyState is VerifyingState.EmailPhone && profile.verifyState.emailVerified)
                    ) {
                        "Email verified"
                    } else {
                        "${if (isVerify) "Verify" else "Confirm"} email"
                    },
                progress = verifyState.value is VerifyingState.Email || profile?.verifyState is VerifyingState.Email,
                onClick = {
                    if (profile?.verifyState == null) {
                        verifyState.value = VerifyingState.Email(isVerified = false)

                        if (isVerify) {
                            viewModel.emailLogin(
                                true,
                                requireNotNull(name),
                                requireNotNull(email),
                                number,
                            ) {
                                openEmailApp(context)
                            }
                        } else {
                            navController.navigate(Routes.LoginScreen.name)
                        }
                    }
                },
            )

            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 40.dp),
                textAlign = TextAlign.Center,
                text =
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Option 2:")
                        }

                        append(" You'll send an auto populated message through messaging service.")
                    },
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
            )

            KeyriButton(
                modifier = Modifier.padding(top = 20.dp),
                disabledTextColor = primaryDisabled,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
                disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
                disabledBorderColor = primaryDisabled,
                progress = verifyState.value is VerifyingState.Phone || (profile?.verifyState is VerifyingState.Phone),
                enabled =
                    (number != null || profile?.phone != null) &&
                        (profile?.verifyState == null || profile.verifyState is VerifyingState.Phone),
                text =
                    if ((profile?.verifyState is VerifyingState.Phone && profile.verifyState.isVerified) ||
                        (profile?.verifyState is VerifyingState.EmailPhone && profile.verifyState.phoneVerified)
                    ) {
                        "Phone verified"
                    } else {
                        "${if (isVerify) "Verify" else "Confirm"} phone number"
                    },
                onClick = {
                    if (profile?.verifyState == null) {
                        verifyState.value = VerifyingState.Phone(isVerified = false)

                        viewModel.smsLogin(
                            isVerify,
                            requireNotNull(name),
                            requireNotNull(email),
                            requireNotNull(number),
                        ) { response ->
                            openSmsApp(response, context)
                        }
                    }
                },
            )

            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 40.dp),
                textAlign = TextAlign.Center,
                text =
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Option 3:")
                        }

                        append(" You'll send an auto populated message and then receive an email magic link.")
                    },
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
            )

            KeyriButton(
                modifier = Modifier.padding(top = 20.dp),
                disabledTextColor = primaryDisabled,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
                disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
                disabledBorderColor = primaryDisabled,
                progress = verifyState.value is VerifyingState.EmailPhone || profile?.verifyState is VerifyingState.EmailPhone,
                enabled =
                    (number != null || profile?.phone != null) &&
                        (profile?.verifyState == null || profile.verifyState is VerifyingState.EmailPhone),
                text = "${if (isVerify) "Verify" else "Confirm"} email + phone number",
                onClick = {
                    if (profile?.verifyState == null) {
                        verifyState.value =
                            VerifyingState.EmailPhone(emailVerified = false, phoneVerified = false)

                        viewModel.smsAndEmailLogin(
                            isVerify,
                            requireNotNull(name),
                            requireNotNull(email),
                            requireNotNull(number),
                        ) { response ->
                            openEmailApp(context)
                            openSmsApp(response, context)
                        }
                    }
                },
            )
        }
    }
}

private fun openEmailApp(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_MAIN)

        intent.addCategory(Intent.CATEGORY_APP_EMAIL)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast
            .makeText(
                context,
                "There is no email client installed.",
                Toast.LENGTH_SHORT,
            ).show()
    }
}

private fun openSmsApp(
    response: SmsLoginResponse,
    context: Context,
) {
    try {
        val sendIntent =
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${response.smsUrl.sendTo}")
                putExtra("sms_body", response.smsUrl.confirmationMessage)
            }

        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(sendIntent)
    } catch (e: ActivityNotFoundException) {
        Toast
            .makeText(
                context,
                "There is no SMS app installed.",
                Toast.LENGTH_SHORT,
            ).show()
    }
}
