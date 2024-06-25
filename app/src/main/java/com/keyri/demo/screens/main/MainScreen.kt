package com.keyri.demo.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavController
import com.keyri.demo.ui.theme.textColor
import com.keyri.demo.ui.theme.verifiedTextColor
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(viewModel : MainViewModel = koinViewModel(), navController: NavController) {
    var email by remember { mutableStateOf("saif@keyri.com") }

    Column {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = buildAnnotatedString {
                append("Authenticated as ")

                withStyle(style = SpanStyle(color = verifiedTextColor)) {
                    append(email)
                }
            },
            style = MaterialTheme.typography.headlineSmall,
            color = textColor
        )


    }
}
