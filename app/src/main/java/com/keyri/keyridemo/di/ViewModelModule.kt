package com.keyri.keyridemo.di

import com.keyri.keyridemo.screens.login.LoginViewModel
import com.keyri.keyridemo.screens.main.MainScreenViewModel
import com.keyri.keyridemo.screens.payment.MakePaymentViewModel
import com.keyri.keyridemo.screens.verified.VerifiedViewModel
import com.keyri.keyridemo.screens.verify.VerifyViewModel
import com.keyri.keyridemo.screens.welcome.WelcomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelsModule = module {
    viewModelOf(::MainScreenViewModel)
    viewModelOf(::WelcomeViewModel)
    viewModelOf(::VerifyViewModel)
    viewModelOf(::VerifiedViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::MakePaymentViewModel)
}
