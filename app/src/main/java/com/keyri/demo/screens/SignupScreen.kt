package com.keyri.demo.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.keyri.demo.composables.KeyriButton
import com.keyri.demo.composables.KeyriTextField
import com.keyri.demo.routes.Routes
import com.keyri.demo.ui.theme.primaryDisabled
import com.keyri.demo.ui.theme.textColor
import com.keyri.demo.ui.theme.textFieldUnfocusedColor
import com.keyri.demo.utils.isValidEmail
import com.keyri.demo.utils.isValidPhoneNumber

@Composable
fun SignupScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }

    // TODO: Fix issue -> unable to input + digit
    // TODO: +1 Should be autopopulated



    // TODO: If user close app on verified screen - force them to login with biometrics on next opening app

    Column {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp)
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "Provide your details below",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor
        )

        Column(modifier = Modifier.weight(1F), verticalArrangement = Arrangement.Center) {
            KeyriTextField(
                value = name,
                placeholder = {
                    Text(
                        text = "Name",
                        color = textFieldUnfocusedColor
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                onValueChange = { name = it }
            )

            KeyriTextField(
                modifier = Modifier.padding(top = 20.dp),
                value = email,
                placeholder = {
                    Text(
                        text = "Email",
                        color = textFieldUnfocusedColor
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                onValueChange = { email = it },
            )

            KeyriTextField(
                modifier = Modifier.padding(top = 20.dp),
                value = mobile,
                placeholder = {
                    Text(
                        text = "+1 (---) --- - ----",
                        color = textFieldUnfocusedColor
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = {
                    mobile = it
                },
            )
        }

        KeyriButton(modifier = Modifier.padding(top = 28.dp),
            enabled = name.length > 2 && email.isValidEmail() && mobile.isEmpty() or mobile.isValidPhoneNumber(),
            disabledTextColor = primaryDisabled,
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
            disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
            disabledBorderColor = primaryDisabled,
            text = "Continue",
            onClick = {
                navController.navigate("${Routes.VerifyScreen.name}?email=$email&number=$mobile&isVerify=true")
            })
    }
}
