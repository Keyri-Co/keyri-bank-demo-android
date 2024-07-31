package com.keyri.androidFullExample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.screens.login.LoginScreen
import com.keyri.androidFullExample.screens.main.MainScreen
import com.keyri.androidFullExample.screens.payment.MakePayment
import com.keyri.androidFullExample.screens.paymentresult.PaymentResult
import com.keyri.androidFullExample.screens.signup.SignupScreen
import com.keyri.androidFullExample.screens.verified.VerifiedScreen
import com.keyri.androidFullExample.screens.verify.VerifyScreen
import com.keyri.androidFullExample.screens.welcome.WelcomeScreen
import com.keyri.androidFullExample.theme.KeyriDemoTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

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
                val viewModel: MainActivityViewModel = koinViewModel()
                val openScreen = viewModel.openScreen.collectAsState()

                LifecycleStartEffect(Unit) {
                    viewModel.checkStartScreen(intent?.data)

                    onStopOrDispose {}
                }

                if (openScreen.value == null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else {
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
                                startDestination = openScreen.value ?: Routes.WelcomeScreen.name,
                            ) {
                                composable(Routes.WelcomeScreen.name) {
                                    WelcomeScreen(navController = navController)
                                }

                                composable(Routes.SignupScreen.name) {
                                    SignupScreen(navController = navController)
                                }

                                composable(Routes.LoginScreen.name) {
                                    LoginScreen(
                                        navController = navController,
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

                                composable(Routes.VerifiedScreen.name) {
                                    VerifiedScreen(
                                        navController = navController,
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
                                    "${Routes.VerifyScreen.name}?name={name}&email={email}&number={number}&isVerify={isVerify}",
                                    arguments =
                                        listOf(
                                            navArgument("name") {
                                                type = NavType.StringType
                                                nullable = true
                                            },
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
                                    val name = backStackEntry.arguments?.getString("name")
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
                                        name = name,
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
                                    MainScreen(
                                        navController = navController,
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

                                composable(Routes.MakePaymentScreen.name) {
                                    MakePayment(
                                        navController = navController,
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
                                    "${Routes.PaymentResultScreen.name}?riskResult={riskResult}",
                                    arguments =
                                        listOf(
                                            navArgument("riskResult") {
                                                type = NavType.StringType
                                            },
                                        ),
                                ) { backStackEntry ->
                                    val riskResult =
                                        backStackEntry.arguments?.getString("riskResult")
                                            ?: throw IllegalArgumentException("riskResult shouldn't be null")

                                    PaymentResult(
                                        navController = navController,
                                        riskResult = riskResult,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
