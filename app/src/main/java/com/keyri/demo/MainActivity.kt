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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.keyri.demo.routes.Routes
import com.keyri.demo.screens.SignupScreen
import com.keyri.demo.screens.VerifiedScreen
import com.keyri.demo.screens.VerifyScreen
import com.keyri.demo.screens.main.MainScreen
import com.keyri.demo.screens.welcome.WelcomeScreen
import com.keyri.demo.ui.theme.KeyriDemoTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : FragmentActivity() {

    private val viewModel by viewModel<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.checkKeyriAccounts()

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.keyriAccounts.value != null
            }
        }

        setContent {
            KeyriDemoTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onPrimary)
                        .imePadding()
                ) { innerPadding ->
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
                                WelcomeScreen(
                                    navController,
                                    viewModel.keyriAccounts.value ?: emptyMap()
                                )
                            }

                            composable(Routes.LoginScreen.name) {
                                VerifyScreen(
                                    navController,
                                    isVerify = false,
                                    email = "kulagin.andrew38@gmail.com" // TODO: Fix this placeholder
                                )
                            }

                            composable(Routes.SignupScreen.name) {
                                SignupScreen(navController)
                            }

                            composable("${Routes.VerifiedScreen.name}?email={email}&number={number}&isVerified={isVerified}",
                                arguments = listOf(
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
                                    }
                                )) { backStackEntry ->
                                val email = backStackEntry.arguments?.getString("email")
                                    ?: throw IllegalStateException("Email shouldn't be null")
                                val number = backStackEntry.arguments?.getString("number")
                                    ?.takeIf { it.isNotEmpty() }
                                val isVerified =
                                    backStackEntry.arguments?.getBoolean("isVerified") ?: true

                                VerifiedScreen(
                                    navController = navController,
                                    isVerified = isVerified,
                                    email = email,
                                    number = number
                                )
                            }

                            composable("${Routes.VerifyScreen.name}?email={email}&number={number}",
                                arguments = listOf(
                                    navArgument("email") {
                                        type = NavType.StringType
                                        nullable = false
                                    },
                                    navArgument("number") {
                                        type = NavType.StringType
                                        nullable = true
                                    }
                                )) { backStackEntry ->
                                val email = backStackEntry.arguments?.getString("email")
                                    ?: throw IllegalStateException("Email shouldn't be null")
                                val number = backStackEntry.arguments?.getString("number")
                                    ?.takeIf { it.isNotEmpty() }

                                VerifyScreen(
                                    navController = navController,
                                    isVerify = true,
                                    email = email,
                                    number = number
                                )
                            }

                            composable(Routes.SignupScreen.name) {
                                MainScreen(navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}
