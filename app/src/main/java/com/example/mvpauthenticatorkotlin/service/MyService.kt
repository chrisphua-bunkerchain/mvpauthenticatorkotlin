package com.example.mvpauthenticatorkotlin.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class MyService : Service() {

    val TAG: String = this::class.java.simpleName

    companion object {
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
        startForeground()
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

            // Broadcast the result to any listening UI components (like MainActivity).
            sendResultBroadcast(status)
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

    @SuppressLint("ForegroundServiceType")
    private fun startForeground() {

    }
}
