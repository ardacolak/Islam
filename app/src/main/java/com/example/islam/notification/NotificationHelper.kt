package com.example.islam.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.islam.R
import com.example.islam.domain.model.Prayer

object NotificationHelper {

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            Prayer.NOTIFICATION_CHANNEL_ID,
            Prayer.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Ezan ve namaz vakti bildirimleri"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun showPrayerNotification(context: Context, prayerName: String, prayerTime: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, Prayer.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$prayerName Vakti")
            .setContentText("$prayerName namazının vakti geldi: $prayerTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(prayerName.hashCode(), notification)
    }
}
