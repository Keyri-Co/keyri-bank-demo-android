package com.keyri.androidFullExample.di

import android.content.Context
import androidx.datastore.core.DataStore
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import com.keyri.androidFullExample.services.provideApiService
import com.keyri.androidFullExample.utils.keyriProfilesDataStore
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.config.KeyriDetectionsConfig
import org.koin.dsl.module

val appModule =
    module {
        single { getKeyriProfilesDataStore(get()) }
        single { getKeyri(get()) }
        single { KeyriDemoRepository(get(), get()) }
        single { provideApiService() }
    }

private fun getKeyriProfilesDataStore(context: Context): DataStore<KeyriProfiles> = context.keyriProfilesDataStore

private fun getKeyri(context: Context): Keyri =
    Keyri(
        context,
        "Ekrdi04LFJSRraLObtJpUap6fkh45fwi",
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEzteySVilYBihc6V67mN084ajGYlBOqXr6JmZ2A26Z6iW/9G8EYxPxfPRgzADrcZUHAcCuXfnv3alDvwYoGaFg==",
        "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgDSsMLClY0b0s4LdbXSujp//2kE2kkKDCVoUq0d+z0jmhRANCAAQTO17JJWKVgGKFzpXruY3TzhqMZiUE6pevomZnYDbpnqJb/0bwRjE/F89GDMAOtxlQcBwK5d+e/dqUO/BigZoW",
        KeyriDetectionsConfig(
            blockEmulatorDetection = true,
            blockRootDetection = true,
            blockDangerousAppsDetection = true,
            blockTamperDetection = true,
            blockSwizzleDetection = true,
        ),
    )
