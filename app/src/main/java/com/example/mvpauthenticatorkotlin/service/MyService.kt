package com.example.mvpauthenticatorkotlin.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mvpauthenticatorkotlin.R

class MyService : Service() {

    val TAG: String = this::class.java.simpleName

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "mvp_result_channel"

        // Define a public action for the broadcast so the UI (Activity/Fragment) can listen for it
        const val ACTION_MVP_RESULT = "com.example.mvpauthenticatorkotlin.MVP_RESULT"

        const val EXTRA_STATUS = "status"
    }

    /**
     * Called by the system when the service is first created.
     * This is where you should do one-time setup and start the foreground process.
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created.")
        // Immediately promote the service to a foreground service.
        // This is required within 5 seconds of the service starting on modern Android
        // to avoid the app being terminated.
        startForegroundWithNotification()
    }

    /**
     * Called by the system every time a client starts the service.
     * This method is triggered by calls to both `startService()` and `startForegroundService()`.
     * This is where the service does its main work.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand received.")
        intent?.let {
            val status = it.getStringExtra(EXTRA_STATUS)
            Log.d(TAG, "Received verification result from MVP app: $status")

            // 1. Broadcast the result to any listening UI components (like MainActivity).
            sendResultBroadcast(status)

            // 2. Update the notification to show the final result.
            updateNotification("Result received: ${status ?: "No status"}")
        }

        // START_NOT_STICKY tells the system not to recreate the service if it's killed.
        // This is appropriate for services that handle a single, one-off task.
        return START_NOT_STICKY
    }

    /**
     * This service is designed to be "started" not "bound". Therefore, onBind should return null.
     * The communication back to the UI is handled via Broadcasts, not by binding to the service.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Sends the received status via a local broadcast, which the MainActivity can listen for.
     */
    fun sendResultBroadcast(status: String?) {
        val broadcastIntent = Intent(ACTION_MVP_RESULT).apply {
            putExtra(EXTRA_STATUS, status ?: "No result data")
        }
        sendBroadcast(broadcastIntent)
        Log.d(TAG, "Result broadcast sent.")
    }

    /**
     * Creates the notification and promotes the service to the foreground.
     */
    @SuppressLint("ForegroundServiceType")
    private fun startForegroundWithNotification() {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Authenticator App")
            .setContentText("Waiting for MVP app result...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        // The foregroundServiceType must also be declared in the AndroidManifest.xml for this service.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
    }

    /**
     * Updates the persistent notification with the final result and then stops the service.
     */
    private fun updateNotification(contentText: String) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("MVP Result")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
        stopSelf()
    }

    /**
     * Creates the Notification Channel required for Android 8.0 (Oreo) and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "MVP App Results",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
