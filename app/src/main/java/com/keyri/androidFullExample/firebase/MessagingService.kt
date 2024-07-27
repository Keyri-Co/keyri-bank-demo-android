package com.keyri.androidFullExample.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("Keyri Demo", "New FCM token available: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val customToken = message.data["customToken"]

        // TODO: Save custom token
        // TODO: Refactor keyri profile to show verified phone or email state
        // TODO: Update data store
        saveToken(customToken)
    }
}
