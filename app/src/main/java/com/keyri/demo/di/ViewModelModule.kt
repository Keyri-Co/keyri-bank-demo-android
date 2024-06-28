package com.keyri.demo.di

import com.keyri.demo.MainActivityViewModel
import com.keyri.demo.screens.login.LoginViewModel
import com.keyri.demo.screens.main.MainScreenViewModel
import com.keyri.demo.screens.payment.MakePaymentViewModel
import com.keyri.demo.screens.verified.VerifiedViewModel
import com.keyri.demo.screens.verify.VerifyViewModel
import com.keyri.demo.screens.welcome.WelcomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelsModule = module {
    viewModelOf(::MainActivityViewModel)
    viewModelOf(::MainScreenViewModel)
    viewModelOf(::WelcomeViewModel)
    viewModelOf(::VerifyViewModel)
    viewModelOf(::VerifiedViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::MakePaymentViewModel)
}
