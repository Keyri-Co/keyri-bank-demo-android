package com.keyri.demo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.keyri.demo.composables.BiometricAuth
import com.keyri.demo.routes.Routes
import com.keyri.demo.screens.PaymentResult
import com.keyri.demo.screens.SignupScreen
import com.keyri.demo.screens.login.LoginScreen
import com.keyri.demo.screens.main.MainScreen
import com.keyri.demo.screens.payment.MakePayment
import com.keyri.demo.screens.verified.VerifiedScreen
import com.keyri.demo.screens.verify.VerifyScreen
import com.keyri.demo.screens.welcome.WelcomeScreen
import com.keyri.demo.ui.theme.KeyriDemoTheme
import com.keyri.demo.utils.getActivity
import com.keyri.demo.utils.navigateWithPopUp
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : FragmentActivity() {

    private val viewModel by viewModel<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()

        setContent {
            KeyriDemoTheme {
                val scope = rememberCoroutineScope()
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                val keyriAccounts = viewModel.keyriAccounts.collectAsState()

                Scaffold(modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onPrimary)
                    .imePadding(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(50.dp)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Routes.WelcomeScreen.name
                        ) {
                            composable(Routes.WelcomeScreen.name) {
                                WelcomeScreen(navController = navController,
                                    keyriAccounts = keyriAccounts.value,
                                    onShowSnackbar = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = it,
                                                withDismissAction = true,
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }) {
                                    viewModel.checkKeyriAccounts()
                                }
                            }

                            composable(Routes.SignupScreen.name) {
                                SignupScreen(navController)
                            }

                            composable(Routes.LoginScreen.name) {
                                LoginScreen(navController = navController)
                            }

                            composable(
                                "${Routes.VerifiedScreen.name}?email={email}&number={number}&isVerified={isVerified}",
                                arguments = listOf(navArgument("email") {
                                    type = NavType.StringType
                                    nullable = false
                                }, navArgument("number") {
                                    type = NavType.StringType
                                    nullable = true
                                }, navArgument("isVerified") {
                                    type = NavType.BoolType
                                })
                            ) { backStackEntry ->
                                val email = backStackEntry.arguments?.getString("email")
                                    ?: throw IllegalStateException("Email shouldn't be null")
                                val number = backStackEntry.arguments?.getString("number")
                                    ?.takeIf { it.isNotEmpty() }
                                val isVerified =
                                    backStackEntry.arguments?.getBoolean("isVerified") ?: true

                                VerifiedScreen(navController = navController,
                                    isVerified = isVerified,
                                    email = email,
                                    number = number,
                                    onShowSnackbar = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = it,
                                                withDismissAction = true,
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    })
                            }

                            composable(
                                "${Routes.VerifyScreen.name}?email={email}&number={number}&isVerify={isVerify}",
                                arguments = listOf(navArgument("email") {
                                    type = NavType.StringType
                                    nullable = true
                                }, navArgument("number") {
                                    type = NavType.StringType
                                    nullable = true
                                }, navArgument("isVerify") {
                                    type = NavType.BoolType
                                })
                            ) { backStackEntry ->
                                val email = backStackEntry.arguments?.getString("email")
                                val number = backStackEntry.arguments?.getString("number")
                                    ?.takeIf { it.isNotEmpty() }
                                val isVerify =
                                    backStackEntry.arguments?.getBoolean("isVerify") ?: true

                                VerifyScreen(
                                    navController = navController,
                                    isVerify = isVerify,
                                    email = email,
                                    number = number
                                )
                            }

                            composable(Routes.MainScreen.name) {
                                MainScreen(navController = navController)
                            }

                            composable(Routes.MakePaymentScreen.name) {
                                MakePayment(navController = navController)
                            }

                            composable(
                                "${Routes.PaymentResultScreen.name}?success={success}",
                                arguments = listOf(navArgument("success") {
                                    type = NavType.BoolType
                                })
                            ) { backStackEntry ->
                                val success =
                                    backStackEntry.arguments?.getBoolean("success") ?: false

                                PaymentResult(
                                    navController = navController, success
                                )
                            }
                        }
                    }
                }

                if (keyriAccounts.value.currentProfile != null) {
                    BiometricAuth(
                        LocalContext.current,
                        "Use Biometric to login as",
                        keyriAccounts.value.currentProfile,
                        {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = it,
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Long
                                )
                            }
                        },
                        { getActivity()?.finish() }) {
                        navController.navigateWithPopUp(
                            Routes.MainScreen.name, Routes.WelcomeScreen.name
                        )
                    }
                }
            }
        }
    }
}
