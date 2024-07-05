package com.keyri.keyridemo.screens.verify

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.keyri.keyridemo.composables.KeyriButton
import com.keyri.keyridemo.data.VerifyType
import com.keyri.keyridemo.routes.Routes
import com.keyri.keyridemo.ui.theme.primaryDisabled
import com.keyri.keyridemo.ui.theme.textColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.concurrent.timer


@Composable
fun VerifyScreen(
    viewModel: VerifyViewModel = koinViewModel(),
    navController: NavHostController,
    isVerify: Boolean,
    name: String? = null,
    email: String? = null,
    number: String? = null
) {
    // TODO: Add loaders here

    val coroutineScope = rememberCoroutineScope()
    var verifyType by remember { mutableStateOf<VerifyType?>(null) }
    val context = LocalContext.current

    val sendSmsPermissionState =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // TODO: Change recipient and sms body
            val smsAddress = "+15555555555"
            val smsText = "sms message"

            if (isGranted) {
                // TODO: Process with result intent?
                context.getSystemService(SmsManager::class.java)
                    .sendTextMessage(smsAddress, null, smsText, null, null)
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

            KeyriButton(modifier = Modifier.padding(top = 20.dp),
                enabled = number != null,
                disabledTextColor = primaryDisabled,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
                disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
                disabledBorderColor = primaryDisabled,
                progress = verifyType == VerifyType.NUMBER,
                text = "${if (isVerify) "Verify" else "Confirm"} phone number",
                onClick = {
                    sendSmsPermissionState.launch(Manifest.permission.SEND_SMS)

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
