package com.keyri.androidFullExample

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.screens.PaymentResult
import com.keyri.androidFullExample.screens.login.LoginScreen
import com.keyri.androidFullExample.screens.main.MainScreen
import com.keyri.androidFullExample.screens.payment.MakePayment
import com.keyri.androidFullExample.screens.signup.SignupScreen
import com.keyri.androidFullExample.screens.verified.VerifiedScreen
import com.keyri.androidFullExample.screens.verify.VerifyScreen
import com.keyri.androidFullExample.screens.welcome.WelcomeScreen
import com.keyri.androidFullExample.theme.KeyriDemoTheme
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()

        setContent {
            KeyriDemoTheme {
                val scope = rememberCoroutineScope()
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.onPrimary)
                            .imePadding(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                ) { innerPadding ->
                    Box(
                        modifier =
                            Modifier
                                .padding(innerPadding)
                                .padding(50.dp),
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Routes.WelcomeScreen.name,
                        ) {
                            composable(Routes.WelcomeScreen.name) {
                                WelcomeScreen(navController = navController)
                            }

                            composable(Routes.SignupScreen.name) {
                                SignupScreen(navController = navController)
                            }

                            composable(Routes.LoginScreen.name) {
                                LoginScreen(navController = navController)
                            }

                            composable(
                                "${Routes.VerifiedScreen.name}?email={email}&number={number}&isVerified={isVerified}",
                                arguments =
                                    listOf(
                                        navArgument("email") {
                                            type = NavType.StringType
                                            nullable = false
                                        },
                                        navArgument("number") {
                                            type = NavType.StringType
                                            nullable = true
                                        },
                                        navArgument("isVerified") {
                                            type = NavType.BoolType
                                        },
                                    ),
                                deepLinks =
                                    listOf(
                                        navDeepLink {
                                            uriPattern =
                                                "https://android-full-example.keyri.com?email={email}&isVerified={isVerified}"
                                        },
                                        navDeepLink {
                                            uriPattern =
                                                "https://android-full-example.keyri.com?email={email}&number={number}&isVerified={isVerified}"
                                        },
                                    ),
                            ) { backStackEntry ->
                                val email =
                                    backStackEntry.arguments?.getString("email")
                                        ?: throw IllegalStateException("Email shouldn't be null")
                                val number =
                                    backStackEntry.arguments
                                        ?.getString("number")
                                        ?.takeIf { it.isNotEmpty() }
                                val isVerified =
                                    backStackEntry.arguments?.getBoolean("isVerified") ?: true

                                VerifiedScreen(
                                    navController = navController,
                                    isVerified = isVerified,
                                    email = email,
                                    number = number,
                                    onShowSnackbar = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = it,
                                                withDismissAction = true,
                                                duration = SnackbarDuration.Long,
                                            )
                                        }
                                    },
                                )
                            }

                            composable(
                                "${Routes.VerifyScreen.name}?email={email}&number={number}&isVerify={isVerify}",
                                arguments =
                                    listOf(
                                        navArgument("email") {
                                            type = NavType.StringType
                                            nullable = true
                                        },
                                        navArgument("number") {
                                            type = NavType.StringType
                                            nullable = true
                                        },
                                        navArgument("isVerify") {
                                            type = NavType.BoolType
                                        },
                                    ),
                            ) { backStackEntry ->
                                val email = backStackEntry.arguments?.getString("email")
                                val number =
                                    backStackEntry.arguments
                                        ?.getString("number")
                                        ?.takeIf { it.isNotEmpty() }
                                val isVerify =
                                    backStackEntry.arguments?.getBoolean("isVerify") ?: true

                                VerifyScreen(
                                    navController = navController,
                                    isVerify = isVerify,
                                    email = email,
                                    number = number,
                                    onShowSnackbar = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = it,
                                                withDismissAction = true,
                                                duration = SnackbarDuration.Long,
                                            )
                                        }
                                    },
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
                                arguments =
                                    listOf(
                                        navArgument("success") {
                                            type = NavType.BoolType
                                        },
                                    ),
                            ) { backStackEntry ->
                                val success =
                                    backStackEntry.arguments?.getBoolean("success") ?: false

                                PaymentResult(
                                    navController = navController,
                                    success,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
