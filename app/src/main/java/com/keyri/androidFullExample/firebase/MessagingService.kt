package com.keyri.androidFullExample.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("Keyri Demo", "New token available: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val customToken = message.data["customToken"]

        // TODO: Add impl send on sms-login step, update data and verify user
        Log.e("Token", customToken.toString())
    }
}
