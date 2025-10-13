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

    companion object {
        const val TAG = "MyService"
        const val NOTIFICATION_CHANNEL_ID = "mvp_result_channel"
        // Define a public action for the broadcast so the Fragment can listen for it
        const val ACTION_MVP_RESULT = "com.example.mvpauthenticatorkotlin.MVP_RESULT"
        const val EXTRA_STATUS = "status"
    }

    // ... (onCreate, startForegroundWithNotification, etc. remain the same) ...

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand received.")
        intent?.let {
            val status = it.getStringExtra(EXTRA_STATUS)
            Log.d(TAG, "Received verification result from MVP app: $status")

            // *** BROADCAST THE RESULT ***
            sendResultBroadcast(status)

            updateNotification("Result received: ${status ?: "No status"}")
        }

        return START_NOT_STICKY
    }

    private fun sendResultBroadcast(status: String?) {
        val broadcastIntent = Intent(ACTION_MVP_RESULT).apply {
            putExtra(EXTRA_STATUS, status ?: "No result data")
        }
        sendBroadcast(broadcastIntent)
        Log.d(TAG, "Result broadcast sent.")
    }

    // --- The rest of the file (onBind, onDestroy, etc.) is the same ---
    // (I'm omitting the rest of the file for brevity, no other changes are needed there)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created.")
        startForegroundWithNotification()
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundWithNotification() {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Authenticator App")
            .setContentText("Waiting for MVP app result...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
    }

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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "Service Destroyed.")
        super.onDestroy()
    }
}
