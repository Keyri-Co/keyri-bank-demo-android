package com.keyri.demo.di

import com.keyri.demo.MainActivityViewModel
import com.keyri.demo.screens.verified.VerifiedViewModel
import com.keyri.demo.screens.verify.VerifyViewModel
import com.keyri.demo.screens.welcome.WelcomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelsModule = module {
    viewModelOf(::MainActivityViewModel)
    viewModelOf(::WelcomeViewModel)
    viewModelOf(::VerifyViewModel)
    viewModelOf(::VerifiedViewModel)
}
