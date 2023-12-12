package com.wpi.attendancetracker

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityIntentService : IntentService("ActivityIntentService") {
    companion object {
        var lastDetectedActivityType: Int? = null
    }

    override fun onHandleIntent(intent: Intent?) {
        intent?.let {  // 使用 let 和 lambda 表达式来确保 intent 不为 null
            if (ActivityRecognitionResult.hasResult(it)) {
                Log.d("ActivityRecognition", "Received an activity recognition result.")
                val result = ActivityRecognitionResult.extractResult(it) ?: return  // 使用 elvis 运算符确保 result 不为 null

                val probableActivities = result.probableActivities.sortedByDescending { it.confidence }

                val maxConfidence = probableActivities.first().confidence
                val highestConfidenceActivities = probableActivities.filter { it.confidence == maxConfidence }
                val detectedActivity = highestConfidenceActivities.firstOrNull { it.type != DetectedActivity.UNKNOWN && it.type != DetectedActivity.ON_FOOT}
                    ?: highestConfidenceActivities.firstOrNull()

                Log.d("ActivityRecognition", "Broadcast received at: ${System.currentTimeMillis()}")

                for (activity in probableActivities) {
                    val activityName = getActivityString(activity.type)
                    val activityConfidence = activity.confidence
                    Log.d("ActivityRecognition", "Activity: $activityName, Confidence: $activityConfidence")
                }

                detectedActivity?.let {  // 使用 let 和 lambda 表达式来确保 detectedActivity 不为 null
                    // Check if the detected activity is different from the last one
                    if (lastDetectedActivityType != it.type) {
                        // Update the last detected activity type
                        lastDetectedActivityType = it.type

                        // Log the detected activity type and confidence
                        val activityType = getActivityString(it.type)
                        val confidence = it.confidence
                        Log.d("ActivityRecognition", "Detected activity: $activityType with confidence: $confidence")

                        // Send a local broadcast to notify MainActivity to update UI
                        val localIntent = Intent("ACTIVITY_RECOGNITION_UPDATE")
                        localIntent.putExtra("activity_type", activityType)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)
                        Log.d("ActivityRecognition", "Sent broadcast to update activity.")
                    } else {
                        Log.d("ActivityRecognition", "Activity detected was same as last time. No broadcast sent.")
                    }
                } ?: run {
                    Log.d("ActivityRecognition", "No activity detected with sufficient confidence.")
                }
            }
        }
    }


    // Helper function to get activity type as a string
    private fun getActivityString(detectedActivityType: Int): String {
        return when (detectedActivityType) {

            DetectedActivity.WALKING -> "walking"
            DetectedActivity.RUNNING -> "running"
            DetectedActivity.IN_VEHICLE -> "in_vehicle"
            DetectedActivity.STILL -> "still"
            DetectedActivity.ON_FOOT -> "on_foot"
            DetectedActivity.ON_BICYCLE -> "on_bicycle"
            DetectedActivity.TILTING -> "tilting"
            // Add other types if needed
            else -> "unknown"
        }
    }
}