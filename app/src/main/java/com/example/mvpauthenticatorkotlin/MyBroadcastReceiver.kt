package com.example.mvpauthenticatorkotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.mvpauthenticatorkotlin.service.MyService

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Check if the received broadcast has the correct action
        if (intent?.action == MyService.ACTION_MVP_RESULT) {
            val status = intent.getStringExtra(MyService.EXTRA_STATUS) ?: "No status received"
            val message = "Result from MVP App: $status"

            Log.d(FirstFragment::class.java.simpleName, message)

            // Update the UI with the final result
//            binding.textviewFirst.text = message
//            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }
}