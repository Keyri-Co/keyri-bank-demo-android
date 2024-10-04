package com.keyri.androidFullExample.di

import com.keyri.androidFullExample.MainActivityViewModel
import com.keyri.androidFullExample.screens.login.LoginViewModel
import com.keyri.androidFullExample.screens.main.MainScreenViewModel
import com.keyri.androidFullExample.screens.payment.MakePaymentViewModel
import com.keyri.androidFullExample.screens.paymentresult.PaymentResultViewModel
import com.keyri.androidFullExample.screens.requestsent.RequestSentViewModel
import com.keyri.androidFullExample.screens.signup.SignupViewModel
import com.keyri.androidFullExample.screens.verified.VerifiedViewModel
import com.keyri.androidFullExample.screens.verify.VerifyViewModel
import com.keyri.androidFullExample.screens.welcome.WelcomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelsModule =
    module {
        viewModelOf(::MainScreenViewModel)
        viewModelOf(::WelcomeViewModel)
        viewModelOf(::VerifyViewModel)
        viewModelOf(::VerifiedViewModel)
        viewModelOf(::LoginViewModel)
        viewModelOf(::MakePaymentViewModel)
        viewModelOf(::SignupViewModel)
        viewModelOf(::PaymentResultViewModel)
        viewModelOf(::MainActivityViewModel)
        viewModelOf(::RequestSentViewModel)
    }
