package com.keyri.androidFullExample.firebase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.keyri.androidFullExample.MainActivity
import com.keyri.androidFullExample.R

class MessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("Keyri Demo", "New token available: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val customToken = message.data["customToken"]

        showLocalNotification(customToken)
    }

    private fun showLocalNotification(customToken: String?) {
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            "https://android-full-example.keyri.com/login&customToken=$customToken".toUri(),
            this,
            MainActivity::class.java
        )

        val pendingIntent =
            PendingIntent.getActivity(this, 0, deepLinkIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            } else {
                @Suppress("deprecation")
                NotificationCompat.Builder(this)
            }

        notificationBuilder
            .setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setContentTitle("Keyri demo")
            .setContentText("Tap to login")
            .setSound(defaultSoundUri)

        notificationManager.notify(
            System.currentTimeMillis().hashCode(),
            notificationBuilder.build()
        )
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "ChannelReservation"
    }
}
