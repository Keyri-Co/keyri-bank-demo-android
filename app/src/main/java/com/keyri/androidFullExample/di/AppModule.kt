package com.keyri.androidFullExample.di

import android.content.Context
import androidx.datastore.core.DataStore
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import com.keyri.androidFullExample.services.provideApiService
import com.keyri.androidFullExample.services.provideRiskApiService
import com.keyri.androidFullExample.services.provideTestApiService
import com.keyri.androidFullExample.utils.keyriProfilesDataStore
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.config.KeyriDetectionsConfig
import org.koin.dsl.module

val appModule =
    module {
        single { getKeyriProfilesDataStore(get()) }
        single { getKeyri(get()) }
        single { KeyriDemoRepository(get(), get(), get()) }
        single { provideApiService() }
        single { provideRiskApiService() }
        single { provideTestApiService() }
    }

private fun getKeyriProfilesDataStore(context: Context): DataStore<KeyriProfiles> = context.keyriProfilesDataStore

private fun getKeyri(context: Context): Keyri =
    Keyri(
        context,
        "Ekrdi04LFJSRraLObtJpUap6fkh45fwi",
        "QjBbOrlRALdlebpAuhjtNIJJzgL4vkIF",
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEzteySVilYBihc6V67mN084ajGYlBOqXr6JmZ2A26Z6iW/9G8EYxPxfPRgzADrcZUHAcCuXfnv3alDvwYoGaFg==",
        KeyriDetectionsConfig(
            blockEmulatorDetection = true,
            blockRootDetection = true,
            blockDangerousAppsDetection = true,
            blockTamperDetection = true,
            blockSwizzleDetection = true,
        ),
    )
