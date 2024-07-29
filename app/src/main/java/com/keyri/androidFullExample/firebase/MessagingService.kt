package com.keyri.androidFullExample.firebase

import android.util.Log
import androidx.datastore.core.DataStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.data.VerifyingState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MessagingService : FirebaseMessagingService() {
    private val dataStore: DataStore<KeyriProfiles> by inject()

    private val throwableScope =
        CoroutineExceptionHandler { _, throwable ->
            Log.e("Keyri Demo", "Error: " + throwable.message.toString())
        }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("Keyri Demo", "New FCM token available: $token")
    }

    // TODO: Email and SMS verification flow -> first verify SMS, after receiving custom token, then trigger email flow (if not hard to implement)

    @OptIn(DelicateCoroutinesApi::class)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val customToken = message.data["customToken"]

        GlobalScope.launch(Dispatchers.IO + throwableScope) {
            dataStore.updateData { profiles ->
                val mappedProfiles =
                    profiles.profiles.map {
                        if (it.email == profiles.currentProfile) {
                            it.copy(
                                phoneVerifyState = VerifyingState.VERIFIED,
                                customToken = customToken
                            )
                        } else {
                            it
                        }
                    }

                profiles.copy(profiles = mappedProfiles)
            }
        }
    }
}
