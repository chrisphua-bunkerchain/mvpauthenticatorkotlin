package com.example.mvpauthenticatorkotlin.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * This receiver acts as the secure entry point for the MVP app's result.
 * Its only job is to receive the result and broadcast it internally to the app's UI.
 */
class MvpResultReceiver : BroadcastReceiver() {

    companion object {
        // This is the action your FirstFragment will listen for.
        const val ACTION_MVP_RESULT = "com.example.mvpauthenticatorkotlin.ACTION_MVP_RESULT"
        const val EXTRA_STATUS = "status"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getStringExtra("status")
        Log.d(
            "MvpResultReceiver",
            "Received result from MVP app. Broadcasting to UI. Status: $status"
        )

        // Create a *new* intent to send to our UI (FirstFragment)
        val uiIntent = Intent(ACTION_MVP_RESULT).apply {
            putExtra(EXTRA_STATUS, status ?: "No result data")
        }

        // Send the broadcast directly to the UI.
        // No service, no foreground notification, no crash.
        context.sendBroadcast(uiIntent)
    }
}
