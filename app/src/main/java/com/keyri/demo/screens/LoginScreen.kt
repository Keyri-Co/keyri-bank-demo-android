package com.keyri.demo.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.keyri.demo.composables.KeyriButton
import com.keyri.demo.ui.theme.textColor

@Composable
fun LoginScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }

    Column {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "Help us confirm your identity",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor
        )

//        Column(modifier = Modifier.weight(1F)) {
//            OutlinedTextField(value = name, onValueChange = { name = it })
//            OutlinedTextField(value = name, onValueChange = { name = it })
//            OutlinedTextField(value = name, onValueChange = { name = it })
//        }
//
//        KeyriButton(
//            Modifier.padding(top = 28.dp),
//            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1F),
//            text = "Continue",
//            onClick = {
//                // TODO: Add impl
////                navController.navigate(Routes.SignupScreen.name)
//            })
    }
}