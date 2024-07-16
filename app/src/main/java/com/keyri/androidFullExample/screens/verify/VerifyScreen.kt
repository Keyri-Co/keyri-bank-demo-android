package com.keyri.androidFullExample.screens.verify

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.keyri.androidFullExample.R
import com.keyri.androidFullExample.composables.KeyriButton
import com.keyri.androidFullExample.composables.ListModalBottomSheet
import com.keyri.androidFullExample.data.ModalListItem
import com.keyri.androidFullExample.data.VerifyType
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.theme.primaryDisabled
import com.keyri.androidFullExample.theme.textColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.concurrent.timer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyScreen(
    viewModel: VerifyViewModel = koinViewModel(),
    navController: NavHostController,
    isVerify: Boolean,
    name: String? = null,
    email: String? = null,
    number: String? = null,
    onShowSnackbar: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var verifyType by remember { mutableStateOf<VerifyType?>(null) }
//    var showVerifyNumberChooser by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val error = viewModel.errorMessage.collectAsState()

    if (error.value != null) {
        error.value?.let {
            onShowSnackbar(it)
        }
    }


    // TODO: Questions:
    // 1. Ask Zain how magic link should looks like
    // 2. Ask about FCM payload structure
    // 3. Ask about responses (payload)
    // 4. Phone verify and confirm number
    // 5. Flow sequence


    val sendSmsPermissionState =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // TODO: Change recipient and sms body
            val smsAddress = "+380960874951"
            val smsText = "sms message 1"

            if (isGranted) {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    SmsManager.getDefault()
                }

                smsManager.sendTextMessage(smsAddress, null, smsText, null, null)
            } else {
                try {
                    val sendIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:$smsAddress")
                        putExtra("sms_body", smsText)
                    }

                    context.startActivity(sendIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "There is no SMS app installed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    Column {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp)
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "Help us ${if (isVerify) "verify" else "confirm"} your identity",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor
        )

        Column(modifier = Modifier.weight(1F), verticalArrangement = Arrangement.Bottom) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Option 1:")
                    }

                    append(" We'll send you an email magic link. It expires 15 minutes after you request it.")
                },
                style = MaterialTheme.typography.bodySmall,
                color = textColor
            )

            KeyriButton(modifier = Modifier.padding(top = 20.dp),
                disabledTextColor = primaryDisabled,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
                disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
                disabledBorderColor = primaryDisabled,
                text = "${if (isVerify) "Verify" else "Confirm"} email",
                progress = verifyType == VerifyType.EMAIL,
                onClick = {
                    email?.let {
                        viewModel.emailLogin(it) {
                            try {
                                val intent = Intent(Intent.ACTION_MAIN)

                                intent.addCategory(Intent.CATEGORY_APP_EMAIL)
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    "There is no email client installed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

//                        if (isVerify) {
//                            viewModel.cryptoRegister(it) {
//
//                            }
//                        } else {
//                            viewModel.cryptoLogin(it) {
//
//                            }
//                        }
                    }


                    // TODO: Do this after verify
//                    if (verifyType == null) {
//                        verifyType = VerifyType.EMAIL
//                        startEventTimer(
//                            coroutineScope,
//                            isVerify,
//                            viewModel,
//                            name,
//                            email,
//                            number,
//                            navController
//                        )
//                    }
                })

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 40.dp),
                textAlign = TextAlign.Center,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Option 2:")
                    }

                    append(" You'll send an auto populated message through messaging service.")
                },
                style = MaterialTheme.typography.bodySmall,
                color = textColor
            )

            // TODO: If user clicks on verify and we see installed Telegram or WhatsApp -> show simple chooser with 3 options (if only sms - no need to show option)

            // TODO: Remove only do verify of chosen option

            KeyriButton(modifier = Modifier.padding(top = 20.dp),
                enabled = number != null,
                disabledTextColor = primaryDisabled,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
                disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
                disabledBorderColor = primaryDisabled,
                progress = verifyType == VerifyType.NUMBER,
                text = "${if (isVerify) "Verify" else "Confirm"} phone number",
                onClick = {
//                    showVerifyNumberChooser = true

                    sendSmsPermissionState.launch(Manifest.permission.SEND_SMS)

                    // TODO: Need to notify user sms sending status? -> if loader - everything good
                    // TODO: If SMS wasn't sent - Show toast with error message and stop loader

                    // TODO: Do this after verify
//                    if (verifyType == null) {
//                        verifyType = VerifyType.NUMBER
//                        startEventTimer(
//                            coroutineScope,
//                            isVerify,
//                            viewModel,
//                            name,
//                            email,
//                            number,
//                            navController
//                        )
//                    }
                })

//            if (showVerifyNumberChooser) {
//                val list = mutableListOf(ModalListItem(null, "Verify with sending SMS"))
//
//                try {
//                    context.packageManager.getPackageInfo("org.telegram.messenger", 0)
//
//                    list.add(ModalListItem(R.drawable.ic_telegram, "Verify with Telegram app"))
//                } catch (_: PackageManager.NameNotFoundException) {
//                }
//
//                // TODO: Some issue with Whatsapp, can't see it
//                try {
//                    val packageManager = context.packageManager
//                    val i = Intent(Intent.ACTION_VIEW)
//
//                    val url = "https://api.whatsapp.com/send?phone=dummy&text=hey"
//                    i.setPackage("com.whatsapp")
//                    i.setData(Uri.parse(url))
//
//                    if (i.resolveActivity(packageManager) != null) {
//                        ModalListItem(R.drawable.ic_whatsapp, "Verify with Whatsapp app")
//                    }
//
//                    context.packageManager.getPackageInfo("com.whatsapp", 0)
//
//                    ModalListItem(R.drawable.ic_whatsapp, "Verify with Whatsapp app")
//                } catch (_: PackageManager.NameNotFoundException) {
//                }
//
//                ListModalBottomSheet(
//                    sheetState = sheetState,
//                    "Choose how to verify your number?",
//                    list,
//                    {
//                        // TODO: Finalize
//
//                    }) {
//
//                }
//            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 40.dp),
                textAlign = TextAlign.Center,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Option 3:")
                    }

                    append(" You'll send an auto populated message and then receive an email magic link.")
                },
                style = MaterialTheme.typography.bodySmall,
                color = textColor
            )

            KeyriButton(modifier = Modifier.padding(top = 20.dp),
                enabled = number != null,
                disabledTextColor = primaryDisabled,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
                disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
                disabledBorderColor = primaryDisabled,
                progress = verifyType == VerifyType.EMAIL_NUMBER,
                text = "${if (isVerify) "Verify" else "Confirm"} email + phone number",
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_MAIN)

                        intent.addCategory(Intent.CATEGORY_APP_EMAIL)
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            "There is no email client installed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    sendSmsPermissionState.launch(Manifest.permission.SEND_SMS)

                    // TODO: Do this after verify
//                    if (verifyType == null) {
//                        verifyType = VerifyType.EMAIL_NUMBER
//                        startEventTimer(
//                            coroutineScope,
//                            isVerify,
//                            viewModel,
//                            name,
//                            email,
//                            number,
//                            navController
//                        )
//                    }
                })
        }
    }
}

private fun startEventTimer(
    coroutineScope: CoroutineScope,
    isVerify: Boolean,
    viewModel: VerifyViewModel,
    name: String?,
    email: String?,
    number: String?,
    navController: NavController
) {
    coroutineScope.launch(Dispatchers.IO) {
        timer(initialDelay = 2_000L, period = 1_000L) {
            if (isVerify) {
                cancel()
                viewModel.sendEvent(name, email, number) {
                    navController.navigate("${Routes.VerifiedScreen.name}?email=$email&number=$number&isVerified=true")
                }
            } else {
                cancel()
                coroutineScope.launch(Dispatchers.Main) {
                    navController.navigate(Routes.LoginScreen.name)
                }
            }
        }
    }
}
