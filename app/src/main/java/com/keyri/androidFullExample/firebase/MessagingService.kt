package com.keyri.androidFullExample.firebase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
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
        val intent = Intent(this, MainActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("customToken", customToken)

        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            } else {
                @Suppress("deprecation") NotificationCompat.Builder(this)
            }

        notificationBuilder.setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setContentTitle("Keyri demo")
            .setContentText("Tap to login")
            .setSound(defaultSoundUri)

        val notificationId = System.currentTimeMillis().hashCode()

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "ChannelReservation"
    }
}
