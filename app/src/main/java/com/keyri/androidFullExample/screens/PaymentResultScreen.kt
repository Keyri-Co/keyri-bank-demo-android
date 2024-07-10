package com.keyri.androidFullExample.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.keyri.androidFullExample.R
import com.keyri.androidFullExample.composables.KeyriButton
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.theme.textColor
import com.keyri.androidFullExample.theme.warningTextColor

@Composable
fun PaymentResult(navController: NavController, success: Boolean) {
    Column {
        val riskSignals by remember { mutableStateOf(listOf("No Signals")) }

        val mainText =
            if (success) "Payment processing confirmed with biometrics and passwordless credential" else "Payment denied"

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp)
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = mainText,
            style = MaterialTheme.typography.headlineSmall,
            color = textColor
        )

        Image(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterHorizontally)
                .padding(top = 40.dp),
            painter = painterResource(id = if (success) R.drawable.icon_check else R.drawable.icon_deny),
            contentDescription = null
        )

        Column(modifier = Modifier.weight(1F), verticalArrangement = Arrangement.Center) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Summary risk determination:\n")
                    }

                    withStyle(style = SpanStyle(color = warningTextColor)) {
                        append("Warn")
                    }
                },
                style = MaterialTheme.typography.headlineSmall,
                color = textColor
            )

            if (riskSignals.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 30.dp),
                    textAlign = TextAlign.Center,
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Fraud risk signals:\n")
                        }

                        riskSignals.forEachIndexed { index, s ->
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                append(s)

                                if (riskSignals.lastIndex != index) {
                                    append(",")
                                }
                            }
                        }
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = textColor
                )
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 20.dp),
                textAlign = TextAlign.Center,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Device info:\n")
                    }

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                        append("Device id: f8bd9...6d3b\n")
                    }

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                        append("Location: New York City, NY, US")
                    }
                },
                style = MaterialTheme.typography.headlineSmall,
                color = textColor
            )
        }

        if (success) {
            KeyriButton(
                Modifier.padding(top = 28.dp),
                text = "Done",
                containerColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    navController.popBackStack(route = Routes.MainScreen.name, inclusive = false)
                })
        } else {
            KeyriButton(Modifier.padding(top = 28.dp),
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
                text = "Cancel",
                onClick = {
                    navController.popBackStack(route = Routes.MainScreen.name, inclusive = false)
                })
        }
    }
}
