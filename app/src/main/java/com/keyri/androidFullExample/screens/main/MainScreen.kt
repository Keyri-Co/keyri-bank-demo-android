package com.keyri.androidFullExample.screens.main

import android.annotation.SuppressLint
import android.app.Activity
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.keyri.androidFullExample.R
import com.keyri.androidFullExample.composables.KeyriAlertDialog
import com.keyri.androidFullExample.composables.KeyriButton
import com.keyri.androidFullExample.data.KeyriCredentials
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.theme.denyTextColor
import com.keyri.androidFullExample.theme.textColor
import com.keyri.androidFullExample.theme.verifiedTextColor
import com.keyri.androidFullExample.theme.warningTextColor
import com.keyrico.scanner.easyKeyriAuth
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@SuppressLint("HardwareIds")
@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = koinViewModel(),
    navController: NavController,
    onShowSnackbar: (String) -> Unit,
) {
    val currentProfile = viewModel.currentProfile.collectAsState()
    val openAlertDialog = remember { mutableStateOf(false) }
    val loading = viewModel.loading.collectAsState()
    val error = viewModel.errorMessage.collectAsState()
    val riskResponse = viewModel.riskResponse.collectAsState()

    val keyriCredentials = koinInject<KeyriCredentials>()

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                navController.navigate("${Routes.RequestSentScreen.name}?email=${currentProfile.value}")
            } else {
                onShowSnackbar("Failed to authenticate")
            }
        }

    val context = LocalContext.current

    if (error.value != null) {
        error.value?.let {
            onShowSnackbar(it)
        }
    }

    if (loading.value) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        Column {
            OutlinedIconButton(
                modifier =
                    Modifier
                        .align(Alignment.End)
                        .size(48.dp),
                colors =
                    IconButtonDefaults.outlinedIconButtonColors().copy(
                        contentColor = Color(0xFF8C8C8C),
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                shape = RoundedCornerShape(20),
                border =
                    IconButtonDefaults
                        .outlinedIconButtonBorder(true)
                        .copy(brush = SolidColor(Color(0xFF8C8C8C).copy(alpha = 0.4f))),
                onClick = {
                    currentProfile.value?.let { email ->
                        viewModel.generatePayload(email) {
                            easyKeyriAuth(
                                context,
                                launcher,
                                keyriCredentials.appKey,
                                keyriCredentials.publicApiKey,
                                keyriCredentials.serviceEncryptionKey,
                                it,
                                email,
                            )
                        }
                    }
                },
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize(0.4f),
                    painter = painterResource(R.drawable.ic_photo),
                    contentDescription = null,
                )
            }

            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 40.dp),
                textAlign = TextAlign.Center,
                text =
                    buildAnnotatedString {
                        append("Authenticated as\n")

                        withStyle(style = SpanStyle(color = verifiedTextColor)) {
                            append(currentProfile.value)
                        }
                    },
                style = MaterialTheme.typography.headlineSmall,
                color = textColor,
            )

            Column(modifier = Modifier.weight(1F), verticalArrangement = Arrangement.Center) {
                Text(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text =
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Summary risk determination:\n")
                            }

                            val riskDetermination = riskResponse.value?.riskDetermination

                            val color =
                                when (riskDetermination) {
                                    "allow" -> verifiedTextColor
                                    "warn" -> warningTextColor
                                    else -> denyTextColor
                                }

                            withStyle(style = SpanStyle(color = color)) {
                                append(riskDetermination)
                            }
                        },
                    style = MaterialTheme.typography.headlineSmall,
                    color = textColor,
                )

                if (riskResponse.value?.signals?.isNotEmpty() == true) {
                    Text(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 30.dp),
                        textAlign = TextAlign.Center,
                        text =
                            buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Fraud risk signals:\n")
                                }

                                riskResponse.value?.signals?.forEachIndexed { index, s ->
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                        append(s)

                                        if (riskResponse.value?.signals?.lastIndex != index) {
                                            append(", ")
                                        }
                                    }
                                }
                            },
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor,
                    )
                }

                Text(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 20.dp),
                    textAlign = TextAlign.Center,
                    text =
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Device info:\n")
                            }

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                append(
                                    "Device id: ${
                                        Settings.Secure.getString(
                                            context.contentResolver,
                                            Settings.Secure.ANDROID_ID,
                                        )
                                    }\n",
                                )
                            }

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                val location = riskResponse.value?.location

                                append(
                                    "Location: ${location?.city} ${location?.regionCode} ${location?.countryCode} ${location?.continentCode}",
                                )
                            }
                        },
                    style = MaterialTheme.typography.headlineSmall,
                    color = textColor,
                )
            }

            if (openAlertDialog.value) {
                KeyriAlertDialog(
                    onDismissRequest = { openAlertDialog.value = false },
                    onConfirmation = {
                        openAlertDialog.value = false

                        viewModel.logout {
                            navController.navigate(Routes.WelcomeScreen.name) {
                                popUpTo(Routes.MainScreen.name) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    dialogTitle = "Log out?",
                    dialogText =
                        buildAnnotatedString {
                            append("Are you sure you want to log out from ")

                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append(currentProfile.value)
                            }

                            append("?")
                        },
                )
            }

            KeyriButton(
                text = "Make payment",
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
                onClick = {
                    navController.navigate(Routes.MakePaymentScreen.name)
                },
            )

            KeyriButton(
                Modifier.padding(top = 28.dp),
                containerColor = MaterialTheme.colorScheme.onPrimary,
                text = "Log out",
                onClick = {
                    openAlertDialog.value = true
                },
            )
        }
    }
}
