package com.keyri.demo.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.keyri.demo.composables.KeyriButton
import com.keyri.demo.composables.KeyriTextField
import com.keyri.demo.routes.Routes
import com.keyri.demo.ui.theme.primaryDisabled
import com.keyri.demo.ui.theme.textColor
import com.keyri.demo.ui.theme.textFieldUnfocusedColor
import com.keyri.demo.utils.NanpVisualTransformation
import com.keyri.demo.utils.isValidEmail
import com.keyri.demo.utils.isValidPhoneNumber
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

@Composable
fun SignupScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    // TODO: Validate only US (hardcode +1) - just make sure it's 10 digit number
    var mobile by remember { mutableStateOf("") }

    // TODO: Signup -> send event
    // TODO: Pass send event result to Zain's API (add name, email, mobile)
    // TODO: Same for login

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
                visualTransformation = NanpVisualTransformation(),
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
                navController.navigate("${Routes.VerifyScreen.name}?email=$email&number=$mobile")
            })
    }
}
