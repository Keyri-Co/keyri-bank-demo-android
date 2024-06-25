package com.keyri.demo.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.keyri.demo.utils.dataStore
import com.keyrico.keyrisdk.Keyri
import org.koin.dsl.module

val appModule = module {
    single { getDatastore(get()) }
    single { Keyri(get(), "ADD APP KEY") } // TODO: Add app key
}

private fun getDatastore(context: Context): DataStore<Preferences> {
    return context.dataStore
}
