package com.example.mvpauthenticatorkotlin.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * This receiver acts as the secure entry point for the MVP app's result.
 * Its only job is to receive the result and broadcast it internally to the app's UI.
 */
class ExternalReceiver : BroadcastReceiver() {
    companion object {
        val TAG: String = this::class.java.simpleName
        const val ACTION_MVP_RESULT = "com.example.mvpauthenticatorkotlin.ACTION_MVP_RESULT"
        const val RESULT = "result"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val result = intent.getStringExtra("result")
        Log.d(TAG, "Received result from MVP app. Broadcasting to UI. Result: $result")

        // Create a new intent to send to UI (FirstFragment)
        val uiIntent = Intent(ACTION_MVP_RESULT).apply {
            putExtra(RESULT, result ?: "No result data")
        }

        // Send the broadcast directly to the UI.
        context.sendBroadcast(uiIntent)
    }
}
