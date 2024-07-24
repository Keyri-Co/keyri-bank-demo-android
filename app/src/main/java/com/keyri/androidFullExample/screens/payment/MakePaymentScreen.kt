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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.keyri.androidFullExample.composables.BiometricAuth
import com.keyri.androidFullExample.composables.KeyriButton
import com.keyri.androidFullExample.composables.KeyriTextField
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.theme.primaryDisabled
import com.keyri.androidFullExample.theme.textColor
import com.keyri.androidFullExample.theme.textFieldUnfocusedColor
import com.keyri.androidFullExample.utils.navigateWithPopUp
import org.koin.androidx.compose.koinViewModel

@Composable
fun MakePayment(
    viewModel: MakePaymentViewModel = koinViewModel(),
    navController: NavHostController,
    onShowSnackbar: (String) -> Unit,
) {
    var amount by remember { mutableFloatStateOf(0.0F) }
    var recipientInfo by remember { mutableStateOf("") }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    val loading = viewModel.loading.collectAsState()
    val error = viewModel.errorMessage.collectAsState()
    val riskResult = viewModel.riskResult.collectAsState()
    val context = LocalContext.current

    if (error.value != null) {
        error.value?.let {
            onShowSnackbar(it)
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

        if (showBiometricPrompt) {
            BiometricAuth(context, "Set up Biometric authentication", null, {
                onShowSnackbar(it)
            }, { showBiometricPrompt = false }) {
                showBiometricPrompt = false

                navController.navigateWithPopUp(
                    "${Routes.PaymentResultScreen.name}?riskResult=${riskResult.value}",
                    Routes.MakePaymentScreen.name
                )
            }
        }

        KeyriButton(
            modifier = Modifier.padding(top = 28.dp),
            enabled = amount > 0F && recipientInfo.isNotEmpty(),
            disabledTextColor = primaryDisabled,
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
            disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
            disabledBorderColor = primaryDisabled,
            text = "Confirm",
            progress = loading.value,
            onClick = {
                viewModel.performMakePaymentEvent(recipientInfo, amount) {
                    showBiometricPrompt = true
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
