package com.example.mvpauthenticatorkotlin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import org.json.JSONObject

object MVPVerificationService {

    private const val TAG = "MvpVerificationService" // Renamed for clarity
    private const val MVP_APP_PACKAGE = "com.bunkerchain.mvp_app"
    private const val MVP_APP_SERVICE = "com.bunkerchain.mvp_app.main.TokenProcessingService"

    // ... (startVerification and sendToken functions remain the same) ...
    fun startVerification(context: Context, imoNumber: String?, code: String?): Boolean {
        val packageManager: PackageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(MVP_APP_PACKAGE)
        }

        // Check if the app is installed
        val apps = packageManager.queryIntentActivities(intent, 0)
        if (apps.isEmpty()) {
            Log.w(TAG, "MVP app not found. Check package name and <queries> in manifest.")
            Toast.makeText(context, "MVP app not installed", Toast.LENGTH_SHORT).show()
            return false
        }

        // Prepare the launch intent with extras
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            val ri = apps[0]
            val packageName = ri.activityInfo.packageName
            val className = ri.activityInfo.name
            component = ComponentName(packageName, className)

            putExtra("appName", "demo_broadcast") // As per your Flutter code
            putExtra("deviceCode", Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID))
            putExtra("imoNumber", imoNumber ?: "")
            putExtra("packageName", context.packageName)
            putExtra("code", code ?: "")
        }

        context.startActivity(launchIntent)
        return true
    }

    fun sendToken(context: Context, token: String?) {
        val serviceIntent = Intent().apply {
            component = ComponentName(MVP_APP_PACKAGE, MVP_APP_SERVICE)
            Log.d("MVPVerificationService", "packageName: ${context.packageName}")
            putExtra("token", token ?: "")
            putExtra("packageName", context.packageName)
        }

        try {
            Log.d(TAG, "Attempting to start MVP app service...")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d(TAG, "Service intent sent successfully.")
        } catch (e: Exception) {
            // Log the full exception. This is the most important step for debugging.
            Log.e(TAG, "Failed to start MVP app service. Check if the service is exported in the target app.", e)
            Toast.makeText(context, "Could not start MVP app service.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Parses the JSON response from the MVP app and logs the result.
     *
     * @param context The application context.
     * @param response The JSON string received from the MVP app.
     */
    fun handleVerificationResult(context: Context, response: String) {
        try {
            val result = JSONObject(response)
            // Use optString to safely get the value, defaulting to "" if not found.
            val code = result.optString("code", "")

            when (code) {
                "200" -> {
                    Log.i(TAG, "✅ Verification successful")
                    // You could also show a Toast or update the UI via a broadcast
                    Toast.makeText(context, "Verification successful", Toast.LENGTH_LONG).show()
                }
                "600" -> Log.w(TAG, "❌ Invalid token format")
                "601" -> Log.w(TAG, "❌ Token validation failed")
                "602" -> Log.w(TAG, "❌ Token expired")
                else -> Log.e(TAG, "❌ Unknown verification error with code: $code")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to parse verification response JSON", e)
        }
    }

    /**
     * Creates the token payload in the expected JSON format.
     * In a real app, the inner 'token' value should be RSA encrypted.
     *
     * @return A JSON string representing the complete token structure.
     */
    fun generateToken(): String {
        // Timestamps in milliseconds
        val startTime = System.currentTimeMillis()
        val endTime = startTime + (24 * 60 * 60 * 1000) // 24 hours later

        // 1. Create the inner token data object
        val tokenData = JSONObject().apply {
            put("startDatetime", startTime)
            put("endDateTime", endTime)
            put("imo", "IMO1234567")
            put("mvpNumber", 123)
            put("licenseNumber", "LIC123")
            put("vesselName", "Test Vessel")
        }

        // 2. Create the main payload object
        val mainPayload = JSONObject().apply {
            // For now, the inner token is just a string. In a real scenario,
            // this would be the result of RSA encrypting the tokenData.
            put("token", tokenData.toString())

            // 3. Add metadata
            val metaData = JSONObject().apply {
                put("clientApp", "mvpauthenticatorkotlin") // Your app's identifier
            }
            put("metaData", metaData)
        }

        return mainPayload.toString()
    }
}
