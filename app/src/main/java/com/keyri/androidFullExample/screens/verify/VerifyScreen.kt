package com.keyri.androidFullExample.screens.verify

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.keyri.androidFullExample.composables.KeyriButton
import com.keyri.androidFullExample.data.VerifyType
import com.keyri.androidFullExample.theme.primaryDisabled
import com.keyri.androidFullExample.theme.textColor
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnspecifiedRegisterReceiverFlag")
@Composable
fun VerifyScreen(
    viewModel: VerifyViewModel = koinViewModel(),
    isVerify: Boolean,
    name: String? = null,
    email: String? = null,
    number: String? = null,
    onShowSnackbar: (String) -> Unit,
) {
    var verifyType by remember { mutableStateOf<VerifyType?>(null) }
    var smsAddress by remember { mutableStateOf<String?>(null) }
    var smsText by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val error = viewModel.errorMessage.collectAsState()

    if (error.value != null) {
        error.value?.let {
            onShowSnackbar(it)
        }
    }

    @Suppress("Deprecation")
    val sendSmsPermissionState =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val smsManager =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.getSystemService(SmsManager::class.java)
                    } else {
                        SmsManager.getDefault()
                    }

                val intentAction = "SMS-sent"
                val sentIntent = Intent(intentAction)

                val sentPI =
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        sentIntent,
                        PendingIntent.FLAG_IMMUTABLE,
                    )

                context.registerReceiver(
                    object : BroadcastReceiver() {
                        override fun onReceive(
                            context: Context?,
                            intent: Intent?,
                        ) {
                            val message =
                                when (resultCode) {
                                    Activity.RESULT_OK -> "SMS sent"
                                    SmsManager.RESULT_ERROR_NO_SERVICE -> "No service"
                                    SmsManager.RESULT_ERROR_NULL_PDU -> "Null PDU"
                                    SmsManager.RESULT_ERROR_RADIO_OFF -> "Radio off"
                                    else -> "Unable to send SMS"
                                }

                            onShowSnackbar(message)
                        }
                    },
                    IntentFilter(intentAction),
                )

                smsManager.sendTextMessage(smsAddress, null, smsText, sentPI, null)
                onShowSnackbar("SMS was sent")
            } else {
                try {
                    val sendIntent =
                        Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:$smsAddress")
                            putExtra("sms_body", smsText)
                        }

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
                text = "${if (isVerify) "Verify" else "Confirm"} email",
                progress = verifyType == VerifyType.EMAIL,
                onClick = {
                    if (verifyType == null) {
                        verifyType = VerifyType.EMAIL

                        viewModel.emailLogin(
                            isVerify,
                            requireNotNull(name),
                            requireNotNull(email),
                        ) {
                            try {
                                val intent = Intent(Intent.ACTION_MAIN)

                                intent.addCategory(Intent.CATEGORY_APP_EMAIL)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast
                                    .makeText(
                                        context,
                                        "There is no email client app installed.",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
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
                enabled = number != null,
                disabledTextColor = primaryDisabled,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
                disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
                disabledBorderColor = primaryDisabled,
                progress = verifyType == VerifyType.NUMBER,
                text = "${if (isVerify) "Verify" else "Confirm"} phone number",
                onClick = {
                    if (verifyType == null) {
                        verifyType = VerifyType.NUMBER

                        viewModel.smsLogin(
                            isVerify,
                            requireNotNull(name),
                            requireNotNull(email),
                            requireNotNull(number),
                        ) {
                            smsAddress = it.smsUrl.sendTo
                            smsText = it.smsUrl.confirmationMessage

                            sendSmsPermissionState.launch(Manifest.permission.SEND_SMS)
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
                enabled = number != null,
                disabledTextColor = primaryDisabled,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
                disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
                disabledBorderColor = primaryDisabled,
                progress = verifyType == VerifyType.EMAIL_NUMBER,
                text = "${if (isVerify) "Verify" else "Confirm"} email + phone number",
                onClick = {
                    if (verifyType == null) {
                        verifyType = VerifyType.EMAIL_NUMBER

                        viewModel.userRegister(
                            isVerify,
                            requireNotNull(name),
                            requireNotNull(email),
                            requireNotNull(number),
                        ) {
                            smsAddress = it.smsUrl.sendTo
                            smsText = it.smsUrl.confirmationMessage

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

                            sendSmsPermissionState.launch(Manifest.permission.SEND_SMS)
                        }
                    }
                },
            )
        }
    }
}
