package com.keyri.androidFullExample.screens.signup

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
import com.keyri.androidFullExample.composables.KeyriButton
import com.keyri.androidFullExample.composables.KeyriTextField
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.theme.primaryDisabled
import com.keyri.androidFullExample.theme.textColor
import com.keyri.androidFullExample.theme.textFieldUnfocusedColor
import com.keyri.androidFullExample.utils.PHONE_PREFIX
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignupScreen(
    viewModel: SignupViewModel = koinViewModel(),
    navController: NavHostController,
) {
    val inputState = viewModel.signupState.collectAsState()
    var isMobileTextFieldFocused by remember { mutableStateOf(false) }

    Column {
        Text(
            modifier = Modifier
                .padding(top = 80.dp)
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "Provide your details below",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor,
        )

        Column(modifier = Modifier.fillMaxWidth().weight(1F), verticalArrangement = Arrangement.Center) {
            KeyriTextField(
                modifier = Modifier.fillMaxWidth(),
                value = inputState.value.name,
                placeholder = {
                    Text(text = "Name", color = textFieldUnfocusedColor)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                onValueChange = { viewModel.updateName(it) },
            )

            KeyriTextField(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                value = inputState.value.email,
                placeholder = {
                    Text(
                        text = "Email",
                        color = textFieldUnfocusedColor,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                onValueChange = { viewModel.updateEmail(it) },
            )

            KeyriTextField(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .onFocusChanged {
                        isMobileTextFieldFocused = it.isFocused

                        val newText =
                            if (isMobileTextFieldFocused &&
                                inputState.value.mobile.text
                                    .isEmpty()
                            ) {
                                PHONE_PREFIX
                            } else if (!isMobileTextFieldFocused &&
                                (inputState.value.mobile.text == PHONE_PREFIX || inputState.value.mobile.text == "")
                            ) {
                                ""
                            } else {
                                inputState.value.mobile.text
                            }

                        viewModel.updateMobile(
                            inputState.value.mobile.copy(
                                text = newText,
                                selection = TextRange(newText.length),
                            ),
                        )
                    },
                value =
                if (inputState.value.mobile.text
                        .isEmpty()
                ) {
                    inputState.value.mobile.copy(
                        text = "",
                    )
                } else if (inputState.value.mobile.text.startsWith(
                        PHONE_PREFIX,
                    )
                ) {
                    inputState.value.mobile
                } else {
                    inputState.value.mobile.copy(text = PHONE_PREFIX + inputState.value.mobile.text)
                },
                placeholder = {
                    Text(
                        text = "+1 (---) --- - ----",
                        color = textFieldUnfocusedColor,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                onValueChange = {
                    val newMobile =
                        if (!it.text.startsWith(PHONE_PREFIX)) {
                            TextFieldValue("")
                        } else if (it.text.length > 12) {
                            inputState.value.mobile
                        } else {
                            it
                        }

                    viewModel.updateMobile(newMobile)
                },
            )
        }

        KeyriButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            enabled = viewModel.validateInputs(),
            disabledTextColor = primaryDisabled,
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
            disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
            disabledBorderColor = primaryDisabled,
            text = "Continue",
            onClick = {
                navController.navigate(
                    "${Routes.VerifyScreen.name}?name=${inputState.value.name}&email=${inputState.value.email}&number=${
                        inputState.value.mobile.text.takeIf { it.isNotEmpty() }
                    }&isVerify=true",
                )
            },
        )
    }
}
