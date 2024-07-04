package com.keyri.keyridemo.screens

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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.keyri.keyridemo.composables.KeyriButton
import com.keyri.keyridemo.composables.KeyriTextField
import com.keyri.keyridemo.routes.Routes
import com.keyri.keyridemo.ui.theme.primaryDisabled
import com.keyri.keyridemo.ui.theme.textColor
import com.keyri.keyridemo.ui.theme.textFieldUnfocusedColor
import com.keyri.keyridemo.utils.isValidEmail
import com.keyri.keyridemo.utils.isValidPhoneNumber

@Composable
fun SignupScreen(navController: NavHostController) {
    val prefix = "+1"
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf(TextFieldValue(text = "")) }
    var isMobileTextFieldFocused by remember { mutableStateOf(false) }

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
                modifier = Modifier
                    .padding(top = 20.dp)
                    .onFocusChanged {
                        isMobileTextFieldFocused = it.isFocused

                        val newText = if (isMobileTextFieldFocused && mobile.text.isEmpty()) {
                            prefix
                        } else if (!isMobileTextFieldFocused && (mobile.text == prefix || mobile.text == "")) {
                            ""
                        } else {
                            mobile.text
                        }

                        mobile = mobile.copy(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                    },
                value = if (mobile.text.isEmpty()) mobile.copy(text = "") else if (mobile.text.startsWith(
                        prefix
                    )
                ) mobile else mobile.copy(text = prefix + mobile.text),
                placeholder = {
                    Text(
                        text = "+1 (---) --- - ----",
                        color = textFieldUnfocusedColor
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                onValueChange = {
                    mobile = if (!it.text.startsWith(prefix)) {
                        TextFieldValue("")
                    } else if (it.text.length > 11) {
                        mobile
                    } else {
                        it
                    }
                },
            )
        }

        KeyriButton(modifier = Modifier.padding(top = 28.dp),
            enabled = name.length > 2 && email.isValidEmail() && mobile.text.isEmpty() or mobile.text.isValidPhoneNumber(),
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
