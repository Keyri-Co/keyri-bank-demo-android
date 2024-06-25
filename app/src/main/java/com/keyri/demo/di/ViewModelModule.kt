package com.keyri.demo.di

import com.keyri.demo.MainActivityViewModel
import com.keyri.demo.screens.main.MainViewModel
import com.keyri.demo.screens.welcome.WelcomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelsModule = module {
    viewModelOf(::MainActivityViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::WelcomeViewModel)
}
