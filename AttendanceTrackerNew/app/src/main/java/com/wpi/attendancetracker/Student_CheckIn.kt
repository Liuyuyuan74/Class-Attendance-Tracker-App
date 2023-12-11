package com.wpi.attendancetracker

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.wpi.attendancetracker.databinding.ActivityStudentCheckInBinding
import java.util.Date


class Student_CheckIn : AppCompatActivity() {

    private lateinit var binding: ActivityStudentCheckInBinding
    private lateinit var databaseUtil : DatabaseUtil
    private lateinit var classId : String
    private lateinit var studentId : String
    private val ACTIVITY_RECOGNITION_REQUEST_CODE = 1

    private var autoCheckin : Boolean = false
    private var classInfo : DatabaseUtil.ClassInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentCheckInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activityRecognitionClient = ActivityRecognitionClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)

        studentId = intent.getStringExtra("STUDENT_ID_KEY") ?: "default_student_id"
        classId = intent.getStringExtra("CLASS_ID_KEY") ?: "default_class_id"
        if (intent.data != null) {
            classId = intent.data!!.host.toString() // "twitter.com"
            studentId = getEmail() // "status"
            autoCheckin = true
        }

        databaseUtil = DatabaseUtil()
        databaseUtil.getClass(classId) {
            classInfo = it
            if (it?.className != null)
                binding.courseName.text = it.className
            enableCheckIn()
        }

        binding.CheckInButton.setOnClickListener {
            doCheckIn()
        }

        databaseUtil.isStudentEnrolled(studentId, classId) { b ->
            binding.enrolled.isChecked = b
        }

        binding.enrolled.setOnCheckedChangeListener { compoundButton, b ->
            databaseUtil.setStudentEnrolled(studentId, classId, b) {
                Toast.makeText(this, "Enrollment Updated", Toast.LENGTH_SHORT).show()
            }
        }

        binding.returnButton.setOnClickListener {
            finish() // Finish this activity and return to the previous one in the stack
        }

        enableCheckIn()
    }


    private var inGeofence : Boolean = false
    private fun checkLocation() : Boolean
    {
        if (classInfo == null) return false
        if (classInfo!!.openSelectLocation) {
            startGeofence()
            return inGeofence
        }
        return true
    }

    private fun checkActivity() : Boolean
    {
        if (classInfo == null) return false
        if (classInfo!!.openTracking) {
            // if it isn't setup, setup the activity tracking
            // return if we're doing still for minute
            startTrackingActivity()
            binding.classActivityCheck.isChecked = isStill
            binding.classActivityCheck.isEnabled = true

            return isStill
        }
        binding.classActivityCheck.isChecked = false
        binding.classActivityCheck.isEnabled = false
        return true
    }

    private fun checkTime() : Boolean
    {
        val duration = 90 * 60 * 1000 // 90 minutes * 60 sec/min * 1000 ms / sec
        if (classInfo == null) return false
        val now = Date().time

        // loop over all class times
        val classStart = classInfo!!.time.time
        val classEnd = classStart + duration

        val timeok = (now in classStart..classEnd)
        binding.classTimeCheck.isChecked = timeok

        return timeok
    }

    private fun enableCheckIn() {
        val locationOk = checkLocation()
        val activityOk = checkActivity()
        val timeOk = checkTime()
        val enabled = (locationOk && activityOk && timeOk)

        binding.classLocationCheck.isChecked = locationOk

        binding.CheckInButton.isEnabled = enabled

        if (autoCheckin && enabled)
            doCheckIn()
    }

    private fun doCheckIn() {
        val checkInTime = Date() // Current time
        databaseUtil.addCheckIn(studentId, classId, checkInTime)
            .addOnSuccessListener {
                Toast.makeText(this, "Check-in successful", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Check-in failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun getEmail() : String {
        val sharedPref = getSharedPreferences("attendance", Context.MODE_PRIVATE)
        return sharedPref.getString("email", "").toString()
    }

////// ACTIVITY STUFF
    private lateinit var activityRecognitionClient : ActivityRecognitionClient
    private var isStill : Boolean = false

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            ACTIVITY_RECOGNITION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTrackingActivity()
                } else {
                    // Handle the permission denial.
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private val activityUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("ActivityRecognition", "Received broadcast in MainActivity.")
            val type = intent?.getStringExtra("activity_type")
            if (!type.isNullOrEmpty()) {
                isStill = type == "still"
                enableCheckIn()
            }
        }
    }
    private fun startTrackingActivity() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("ActivityRecognition", "Requesting activity recognition permission.")
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), ACTIVITY_RECOGNITION_REQUEST_CODE)
            return
        }

        Log.d("ActivityRecognition", "Activity recognition permission already granted.")

        val intent = Intent(this, ActivityIntentService::class.java)
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        activityRecognitionClient.requestActivityUpdates(2000, pendingIntent)
        Log.d("ActivityRecognition", "Requested activity updates.")
    }

    //// all the location stuff goes here
    /*
     * code to handle location permission
     */
    private var locationPermissionGranted = false
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted ->
        if (isGranted) {
            locationPermissionGranted = true
            startGeofence()
        }
    }

    private fun getLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    /*
     * get the location provider and geocoder during onCreate
     */
    private lateinit var geofencingClient: GeofencingClient
    private val GEOFENCE_RADIUS_IN_METERS : Float = 1500.0F
    private val GEOFENCE_KEY = "GEOFENCE_KEY"
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    private fun startGeofence() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            getLocationPermission()
            return
        }

        if (classInfo == null || classInfo!!.lat == null || classInfo!!.lon == null)
            return

        Log.d("Location", "Registering Geofence")

        val lat = classInfo!!.lat!!
        val lon = classInfo!!.lon!!

        Log.d("Location", "Registering Geofence $lat $lon")

        var geofenceList : ArrayList<Geofence> = ArrayList()
        geofenceList.add(
            Geofence.Builder()
            .setRequestId(GEOFENCE_KEY)
            .setCircularRegion(lat,lon,GEOFENCE_RADIUS_IN_METERS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(60*60*1000)
            .build())

        val geofenceRequest = GeofencingRequest.Builder().
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).
            addGeofences(geofenceList).
            build()

        geofencingClient?.addGeofences(geofenceRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.d("Location", "Added geofence succesfully ")
            }
            addOnFailureListener {
                Log.d("Location", "Added geofence error: $it ")
                it.printStackTrace()
            }
        }
    }


    private val geofenceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("ActivityRecognition", "Received broadcast in MainActivity.")
            val type : Int? = intent?.getIntExtra("transition", 0)
            if (type == Geofence.GEOFENCE_TRANSITION_ENTER || type == Geofence.GEOFENCE_TRANSITION_DWELL)
                inGeofence = true
            else if (type == Geofence.GEOFENCE_TRANSITION_EXIT)
                inGeofence = false
            enableCheckIn()
        }
    }

    override fun onResume() {
        super.onResume()
        startTrackingActivity()  // Add this line
        startGeofence()
        LocalBroadcastManager.getInstance(this).registerReceiver(activityUpdateReceiver, IntentFilter("ACTIVITY_RECOGNITION_UPDATE"))
        LocalBroadcastManager.getInstance(this).registerReceiver(geofenceReceiver, IntentFilter("GEOFENCE_UPDATE"))
    }


    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(activityUpdateReceiver)
        LocalBroadcastManager.getInstance(this).registerReceiver(geofenceReceiver, IntentFilter("GEOFENCE_UPDATE"))
    }

}