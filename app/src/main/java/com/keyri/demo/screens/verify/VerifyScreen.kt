package com.keyri.demo.screens.verify

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.keyri.demo.composables.KeyriButton
import com.keyri.demo.data.VerifyType
import com.keyri.demo.routes.Routes
import com.keyri.demo.ui.theme.primaryDisabled
import com.keyri.demo.ui.theme.textColor
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.concurrent.timer

@Composable
fun VerifyScreen(
    viewModel: VerifyViewModel = koinViewModel(),
    navController: NavHostController,
    isVerify: Boolean,
    email: String, // TODO: Set to null, if not verify but confirm - open login screen
    number: String? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var verifyTriple by remember { mutableStateOf<VerifyType?>(null) }

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
                progress = verifyTriple == VerifyType.EMAIL,
                onClick = {
                    if (verifyTriple == null) {
                        verifyTriple = VerifyType.EMAIL

                        coroutineScope.launch {
                            kotlin.concurrent.timer(period = 3L) {
                                cancel()

                                viewModel.sendEvent(email)
                                navController.navigate("${Routes.VerifiedScreen.name}?email=$email&number=$number&isVerified=$isVerify")
                            }
                        }
                    }
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
                progress = verifyTriple == VerifyType.NUMBER,
                text = "${if (isVerify) "Verify" else "Confirm"} phone number",
                onClick = {
                    if (verifyTriple == null) {
                        verifyTriple = VerifyType.NUMBER

                        coroutineScope.launch {
                            kotlin.concurrent.timer(period = 3L) {
                                cancel()

                                viewModel.sendEvent(email)
                                navController.navigate("${Routes.VerifiedScreen.name}?email=$email&number=$number&isVerified=$isVerify")
                            }
                        }
                    }
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
                progress = verifyTriple == VerifyType.EMAIL_NUMBER,
                text = "${if (isVerify) "Verify" else "Confirm"} email + phone number",
                onClick = {
                    if (verifyTriple == null) {
                        verifyTriple = VerifyType.EMAIL_NUMBER

                        coroutineScope.launch {
                            kotlin.concurrent.timer(period = 3L) {
                                cancel()

                                viewModel.sendEvent(email)
                                navController.navigate("${Routes.VerifiedScreen.name}?email=$email&number=$number&isVerified=$isVerify")
                            }
                        }
                    }
                })
        }
    }
}
