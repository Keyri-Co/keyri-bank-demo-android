package com.keyri.androidFullExample.screens.login

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.keyri.androidFullExample.composables.KeyriButton
import com.keyri.androidFullExample.composables.KeyriTextField
import com.keyri.androidFullExample.theme.primaryDisabled
import com.keyri.androidFullExample.theme.textColor
import com.keyri.androidFullExample.theme.textFieldUnfocusedColor
import com.keyri.androidFullExample.utils.isValidEmail
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    navController: NavController,
    onShowSnackbar: (String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    val error = viewModel.errorMessage.collectAsState()
    val loading = viewModel.loading.collectAsState()
    val context = LocalContext.current

    if (error.value != null) {
        error.value?.let {
            viewModel.stopLoading()
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
            text = "Enter email address to log in",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor,
        )

        Column(modifier = Modifier.weight(1F), verticalArrangement = Arrangement.Center) {
            Text(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                text = "We’ll send you an email magic link. It expires 15 minutes after you request it.",
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
            )

            KeyriTextField(
                modifier = Modifier.padding(top = 10.dp),
                value = email,
                placeholder = {
                    Text(
                        text = "Email address",
                        color = textFieldUnfocusedColor,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                onValueChange = { email = it },
            )
        }

        KeyriButton(
            modifier = Modifier.padding(top = 28.dp),
            enabled = email.isValidEmail(),
            disabledTextColor = primaryDisabled,
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
            disabledContainerColor = primaryDisabled.copy(alpha = 0.1F),
            disabledBorderColor = primaryDisabled,
            text = "Confirm",
            progress = loading.value,
            onClick = {
                if (!loading.value) {
                    viewModel.emailLogin(email) {
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
                .padding(top = 28.dp)
                .align(Alignment.CenterHorizontally)
                .clickable {
                    navController.popBackStack()
                },
            textAlign = TextAlign.Center,
            text = "Cancel",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
