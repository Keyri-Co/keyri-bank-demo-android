package com.keyri.androidFullExample.screens.requestsent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.keyri.androidFullExample.R
import com.keyri.androidFullExample.data.KeyriIcon
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.theme.textColor
import com.keyri.androidFullExample.theme.verifiedTextColor
import com.keyri.androidFullExample.utils.navigateWithPopUp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RequestSentScreen(
    viewModel: RequestSentViewModel = koinViewModel(),
    email: String,
    navController: NavHostController,
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = email) {
        viewModel.setCurrentAccount(email)

        scope.launch(Dispatchers.IO) {
            delay(1_500L)

            withContext(Dispatchers.Main) {
                navController.navigateWithPopUp(Routes.MainScreen.name, Routes.WelcomeScreen.name)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KeyriIcon(
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 80.dp),
            iconResId = R.drawable.ic_done,
            iconTint = verifiedTextColor,
        )

        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
                    .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "Authentication request sent",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor,
        )
    }
}
