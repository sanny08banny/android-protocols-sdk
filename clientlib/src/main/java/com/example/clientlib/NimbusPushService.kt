package com.example.clientlib

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class NimbusPushService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, createNotification())

        val deviceId = "e5230340-2a46-4764-ab93-27d8a2d3de0a"
        NimbusWebSocket.connect(deviceId, context = applicationContext)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("Nimbus Push")
            .setContentText("Connected to message dispatcher")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIF_CHANNEL_ID = "nimbus_push_channel"
        const val NOTIF_ID = 101

        fun start(context: Context) {
            val intent = Intent(context, NimbusPushService::class.java)
            context.startForegroundService(intent)
        }

        fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "Nimbus Push",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}

