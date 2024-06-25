package com.keyri.demo.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.keyri.demo.R
import com.keyri.demo.composables.KeyriButton
import com.keyri.demo.routes.Routes
import com.keyri.demo.ui.theme.textColor

@Composable
fun WelcomeScreen(navController: NavHostController, keyriAccounts: Map<String, String> = emptyMap()) {
    Column {
        Text(
            modifier = Modifier
                .padding(top = 80.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = if (keyriAccounts.isEmpty()) "Welcome to\nKeyri Bank" else "Welcome back\nto Keyri Bank" ,
            style = MaterialTheme.typography.headlineLarge,
            color = textColor
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
        ) {
            Image(
                modifier = Modifier
                    .size(130.dp, 62.dp)
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit,
                painter = painterResource(id = R.drawable.keyri_logo),
                contentDescription = null
            )
        }

        // TODO: Add primary style based on keyriAccounts
        // TODO: Pass keyriAccounts to the login and show list dialog

        KeyriButton(Modifier,
            text = "Log in",
            containerColor = MaterialTheme.colorScheme.onPrimary,
            onClick = {
                navController.navigate(Routes.LoginScreen.name)
            })

        KeyriButton(Modifier.padding(top = 28.dp),
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04F),
            text = "Sign up",
            onClick = {
                navController.navigate(Routes.SignupScreen.name)
            })
    }
}
