package com.keyri.demo.screens.verified

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.keyri.demo.R
import com.keyri.demo.composables.BiometricAuth
import com.keyri.demo.composables.KeyriButton
import com.keyri.demo.routes.Routes
import com.keyri.demo.ui.theme.textColor
import com.keyri.demo.ui.theme.verifiedTextColor
import com.keyri.demo.utils.navigateWithPopUp
import org.koin.androidx.compose.koinViewModel

@Composable
fun VerifiedScreen(
    viewModel: VerifiedViewModel = koinViewModel(),
    navController: NavHostController,
    isVerified: Boolean,
    email: String,
    number: String? = null,
    onShowSnackbar: (String) -> Unit
) {
    Column {
        val context = LocalContext.current
        var showBiometricPrompt by remember { mutableStateOf(false) }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp)
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = verifiedTextColor)) {
                    append(email)
                }

                if (number != null) {
                    append(" and ")

                    withStyle(style = SpanStyle(color = verifiedTextColor)) {
                        append(number)
                    }
                }

                if (isVerified) {
                    append(" verified")
                } else {
                    append(" confirmed")
                }
            },
            style = MaterialTheme.typography.headlineSmall,
            color = textColor
        )

        Image(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterHorizontally)
                .padding(top = 40.dp),
            painter = painterResource(id = R.drawable.icon_check),
            contentDescription = null
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "Passwordless credential created",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor
        )

        Image(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterHorizontally)
                .padding(top = 40.dp),
            painter = painterResource(id = R.drawable.icon_key),
            contentDescription = null
        )

        if (showBiometricPrompt) {
            BiometricAuth(context, "Set up Biometric authentication", null, {
                onShowSnackbar(it)
            }, { showBiometricPrompt = false }) {
                showBiometricPrompt = false

                viewModel.saveBiometricAuth(email) {
                    navController.navigateWithPopUp(
                        "${Routes.MainScreen.name}?email={$email}",
                        Routes.WelcomeScreen.name
                    )
                }
            }
        }

        KeyriButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            text = "Set up biometric authentication",
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
        ) {
            showBiometricPrompt = true
        }
    }
}
