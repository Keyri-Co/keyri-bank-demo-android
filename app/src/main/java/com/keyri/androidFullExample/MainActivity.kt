package com.keyri.androidFullExample

import android.content.Intent
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Consumer
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.keyri.androidFullExample.screens.requestsent.RequestSentScreen
import com.keyri.androidFullExample.screens.signup.SignupScreen
import com.keyri.androidFullExample.screens.verified.VerifiedScreen
import com.keyri.androidFullExample.screens.verify.VerifyScreen
import com.keyri.androidFullExample.screens.welcome.WelcomeScreen
import com.keyri.androidFullExample.theme.KeyriDemoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                val profiles = viewModel.dataStore.data.collectAsState(null)
                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(Unit) {
                    val listener =
                        Consumer<Intent> {
                            val link = it.data

                            if (openScreen.value != null && link != null) {
                                scope.launch(Dispatchers.IO) {
                                    val route = viewModel.getScreenByLink(link)

                                    withContext(Dispatchers.Main) {
                                        navController.navigate(route)
                                    }
                                }
                            }
                        }

                    addOnNewIntentListener(listener)
                    onDispose { removeOnNewIntentListener(listener) }
                }

                LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
                    scope.launch(Dispatchers.IO) {
                        viewModel.restoreAccounts()

                        if (openScreen.value == null) {
                            viewModel.getInitialScreen(intent?.data)
                        }
                    }
                }

                LifecycleStartEffect(key1 = profiles, lifecycleOwner = lifecycleOwner) {
                    val job =
                        scope.launch(Dispatchers.IO) {
                            if (profiles.value != null) {
                                viewModel.checkPhoneVerifyState()
                            }
                        }

                    onStopOrDispose { job.cancel() }
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
                                composable(
                                    "${Routes.WelcomeScreen.name}?sessionId={sessionId}",
                                    arguments =
                                        listOf(
                                            navArgument("sessionId") {
                                                type = NavType.StringType
                                                nullable = true
                                            },
                                        ),
                                ) { backStackEntry ->
                                    WelcomeScreen(
                                        navController = navController,
                                        sessionId = backStackEntry.arguments?.getString("sessionId"),
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

                                composable(Routes.SignupScreen.name) {
                                    SignupScreen(navController = navController)
                                }

                                composable(
                                    "${Routes.LoginScreen.name}?email={email}",
                                    arguments =
                                        listOf(
                                            navArgument("email") {
                                                type = NavType.StringType
                                                nullable = true
                                            },
                                        ),
                                ) { backStackEntry ->
                                    val email = backStackEntry.arguments?.getString("email")

                                    LoginScreen(
                                        navController = navController,
                                        recoveredEmail = email,
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
                                        backStackEntry.arguments?.getBoolean("isVerify")
                                            ?: throw IllegalArgumentException("isVerify shouldn't be null")

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

                                composable(
                                    "${Routes.RequestSentScreen.name}?email={email}",
                                    arguments =
                                        listOf(
                                            navArgument("email") {
                                                type = NavType.StringType
                                            },
                                        ),
                                ) { backStackEntry ->
                                    val email =
                                        backStackEntry.arguments?.getString("email")
                                            ?: throw IllegalArgumentException("email shouldn't be null")

                                    RequestSentScreen(email = email, navController = navController)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
