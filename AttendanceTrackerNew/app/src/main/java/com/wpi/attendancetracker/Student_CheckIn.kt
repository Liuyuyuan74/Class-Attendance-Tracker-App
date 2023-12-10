package com.wpi.attendancetracker

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.ActivityRecognitionClient
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

        studentId = intent.getStringExtra("STUDENT_ID_KEY") ?: "default_student_id"
        classId = intent.getStringExtra("CLASS_ID_KEY") ?: "default_class_id"
        val className = intent.getStringExtra("CLASS_NAME_KEY")
        if (intent.data != null) {
            classId = intent.data!!.host.toString() // "twitter.com"
            val params = intent.data!!.pathSegments
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


    private fun checkLocation() : Boolean
    {
        if (classInfo == null) return false
        if (classInfo!!.openSelectLocation) {
            //setupLocation()
            // if it isn't setup, setup the geofence
            // return if we're in the geofence
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


    override fun onResume() {
        super.onResume()
        startTrackingActivity()  // Add this line
        LocalBroadcastManager.getInstance(this).registerReceiver(activityUpdateReceiver, IntentFilter("ACTIVITY_RECOGNITION_UPDATE"))
    }


    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(activityUpdateReceiver)
    }

}