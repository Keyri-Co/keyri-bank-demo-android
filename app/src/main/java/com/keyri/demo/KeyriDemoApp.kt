package com.keyri.demo

import android.app.Application
import com.keyri.demo.di.appModule
import com.keyri.demo.di.viewModelsModule
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class KeyriDemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            modules(viewModelsModule, appModule)
        }
    }
}
