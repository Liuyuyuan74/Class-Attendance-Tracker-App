package com.wpi.attendancetracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    // ...
    override fun onReceive(context: Context?, intent: Intent?) {
        if ((intent == null)||(context == null)) return
        Log.d("Location", "Geofencing Event!")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e("Location", "No geofencing event")
        } else if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e("Location", errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent!!.geofenceTransition
        // Test that the reported transition was of interest.
        // Send a local broadcast to notify MainActivity to update UI
        val localIntent = Intent("GEOFENCE_UPDATE")
        localIntent.putExtra("transition", geofenceTransition)
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
    }
}