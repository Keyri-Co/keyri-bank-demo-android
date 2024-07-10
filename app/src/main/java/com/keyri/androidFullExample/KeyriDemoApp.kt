package com.keyri.androidFullExample

import android.app.Application
import com.keyri.androidFullExample.di.appModule
import com.keyri.androidFullExample.di.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class KeyriDemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@KeyriDemoApp)
            modules(viewModelsModule, appModule)
        }
    }
}
