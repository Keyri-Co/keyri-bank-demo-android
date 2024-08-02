package com.keyri.androidFullExample.screens.paymentresult

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.keyri.androidFullExample.R
import com.keyri.androidFullExample.composables.KeyriButton
import com.keyri.androidFullExample.data.KeyriIcon
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.theme.denyTextColor
import com.keyri.androidFullExample.theme.textColor
import com.keyri.androidFullExample.theme.verifiedTextColor
import com.keyri.androidFullExample.theme.warningTextColor
import org.koin.androidx.compose.koinViewModel

@Composable
fun PaymentResult(
    viewModel: PaymentResultViewModel = koinViewModel(),
    navController: NavController,
    riskResult: String,
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.processRiskResult(riskResult)
    }

    val riskResponse = viewModel.riskResponse.collectAsState()

    Column {
        val mainText =
            if (riskResponse.value?.riskDetermination !=
                "deny"
            ) {
                "Payment processing confirmed with biometrics and passwordless credential"
            } else {
                "Payment denied"
            }

        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp)
                    .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = mainText,
            style = MaterialTheme.typography.headlineSmall,
            color = textColor,
        )

        KeyriIcon(
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 40.dp),
            iconResId = if (riskResponse.value?.riskDetermination != "deny") R.drawable.ic_done else R.drawable.ic_denial,
            iconTint = if (riskResponse.value?.riskDetermination != "deny") verifiedTextColor else warningTextColor,
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
                            append("Device id: ${riskResponse.value?.fingerprintId}\n")
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

        if (riskResponse.value?.riskDetermination != "deny") {
            KeyriButton(
                Modifier.padding(top = 28.dp),
                text = "Done",
                containerColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    navController.popBackStack(
                        route = Routes.MainScreen.name,
                        inclusive = false,
                    )
                },
            )
        } else {
            KeyriButton(
                Modifier.padding(top = 28.dp),
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
                text = "Cancel",
                onClick = {
                    navController.popBackStack(
                        route = Routes.MainScreen.name,
                        inclusive = false,
                    )
                },
            )
        }
    }
}
