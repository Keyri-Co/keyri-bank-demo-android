package com.keyri.androidFullExample.screens.payment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.keyri.androidFullExample.composables.KeyriButton
import com.keyri.androidFullExample.composables.KeyriTextField
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.theme.primaryDisabled
import com.keyri.androidFullExample.theme.textColor
import com.keyri.androidFullExample.theme.textFieldUnfocusedColor
import org.koin.androidx.compose.koinViewModel

@Composable
fun MakePayment(
    viewModel: MakePaymentViewModel = koinViewModel(),
    navController: NavHostController,
) {
    var amount by remember { mutableFloatStateOf(0.0F) }
    var recipientInfo by remember { mutableStateOf("") }

    // TODO: Fix here (actual data)

    Column {
        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp)
                    .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "Make payment",
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
                text = "Enter dollar amount",
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
            )

            KeyriTextField(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                value = amount.takeIf { it != 0F }?.toString() ?: "",
                placeholder = {
                    Text(
                        text = "$0.00",
                        color = textFieldUnfocusedColor,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                onValueChange = { amount = it.toFloatOrNull() ?: 0F },
            )

            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                text = "Enter recipient information",
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
            )

            KeyriTextField(
                modifier = Modifier.padding(top = 5.dp),
                value = recipientInfo,
                placeholder = {
                    Text(
                        text = "Name or id",
                        color = textFieldUnfocusedColor,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                onValueChange = { recipientInfo = it },
            )
        }

        KeyriButton(
            modifier = Modifier.padding(top = 28.dp),
            enabled = amount > 0F && recipientInfo.isNotEmpty(),
            disabledTextColor = primaryDisabled,
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
            disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
            disabledBorderColor = primaryDisabled,
            text = "Confirm",
            onClick = {
                viewModel.performMakePaymentEvent(recipientInfo, amount) { success ->
                    navController.navigate("${Routes.PaymentResultScreen.name}?success=$success")
                }
            },
        )

        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp)
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        navController.popBackStack()
                    },
            textAlign = TextAlign.Center,
            text = "Cancel",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
        )
    }
}
