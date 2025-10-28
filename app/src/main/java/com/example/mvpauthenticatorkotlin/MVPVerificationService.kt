package com.example.mvpauthenticatorkotlin

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.example.mvpauthenticatorkotlin.service.MvpResultReceiver

object MVPVerificationService {

    val TAG: String = this::class.java.simpleName

    private const val MVP_APP_PACKAGE = "com.bunkerchain.mvp_app"

    private const val MVP_APP_SPLASH = "com.bunkerchain.mvp_app.main.SplashActivity"

    private const val MVP_APP_SERVICE = "com.bunkerchain.mvp_app.main.TokenProcessingService"

    fun checkMvpAppInstalled(context: Context): Boolean {
        val packageManager: PackageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(MVP_APP_PACKAGE)
        }

        // Check if the app is installed
        val apps = packageManager.queryIntentActivities(intent, 0)
        if (apps.isEmpty()) {
            Log.w(TAG, "MVP app not found. Check package name and <queries> in manifest.")
            Toast.makeText(context, "MVP app not installed.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    fun authenticate(context: Context, token: String) {
        val myPackageName = context.packageName;
        val intent = Intent()
        intent.putExtra("token", token)
        intent.putExtra("packageName", myPackageName)

        val component = ComponentName(
            MVP_APP_PACKAGE,
            MVP_APP_SERVICE
        )
        intent.setComponent(component)

        try {
            Log.d(TAG, "Attempting to start MVP app service...")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d(TAG, "Service intent sent successfully.")
        } catch (e: Exception) {
            // Log the full exception. This is the most important step for debugging.
            Log.e(TAG, "Failed to start MVP app service.", e)
            Toast.makeText(context, "Could not start MVP app service.", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("HardwareIds")
    fun authenticate(context: Context, imoNumber: String, code: String) {
        val packageName = context.packageName;
        val deviceCode = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
//        val intent = Intent(Intent.ACTION_MAIN)
//        intent.setPackage(MVP_APP_PACKAGE)
//        intent.addCategory(Intent.CATEGORY_LAUNCHER)
//        intent.putExtra("appName", "demo_broadcast")
//        intent.putExtra("deviceCode", deviceCode)
//        intent.putExtra("imoNumber", imoNumber)
//        intent.putExtra("code", code)
//        intent.putExtra("packageName", packageName)
//        intent.putExtra("action", MvpResultReceiver.ACTION_MVP_RESULT)

        val uri = Uri.Builder()
            .scheme("marinevesselpass")
            .authority("verify")
            .appendQueryParameter("appName", "demo_broadcast")
            .appendQueryParameter("deviceCode", deviceCode)
            .appendQueryParameter("imoNumber", imoNumber)
            .appendQueryParameter("code", code)
            .appendQueryParameter("packageName", packageName)
            .appendQueryParameter("scheme", "mvpauthenticatorkotlin")
            .appendQueryParameter("host", "callback")
            .appendQueryParameter("action", MvpResultReceiver.ACTION_MVP_RESULT)
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .addCategory(Intent.CATEGORY_DEFAULT)

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val component = ComponentName(
            MVP_APP_PACKAGE,
            MVP_APP_SPLASH
        )
        intent.setComponent(component)

        try {
            Log.d(TAG, "Attempting to start MVP app service...")
            context.startActivity(intent)
            Log.d(TAG, "Service intent sent successfully.")
        } catch (e: Exception) {
            // Log the full exception. This is the most important step for debugging.
            Log.e(TAG, "Failed to start MVP app service.", e)
            Toast.makeText(context, "Could not start MVP app service.", Toast.LENGTH_SHORT).show()
        }
    }
}
