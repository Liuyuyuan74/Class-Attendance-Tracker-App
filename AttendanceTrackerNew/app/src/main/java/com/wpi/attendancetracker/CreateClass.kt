package com.wpi.attendancetracker

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import  com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class CreateClass : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    var className = ""
    var classID = 0
    lateinit var classTime: Date
    var isOpenSelectLocation = false
    var isOpenTracking = false
    var isOpenUsingQr = false
    var isOpenOtherTechnique = false
    var location: LatLng? = null
    lateinit var ed_class_name: EditText
    lateinit var ed_class_id: EditText
    lateinit var tv_time: TextView
    lateinit var ll_time: LinearLayoutCompat
    lateinit var sw_open_location: Switch
    lateinit var ll_select_location: LinearLayoutCompat
    lateinit var rl_location: RelativeLayout
    lateinit var sw_activity_tracking: Switch
    lateinit var sw_using_qr: Switch
    lateinit var sw_other_technique: Switch
    lateinit var btn_set_class: Button
    var sp = SimpleDateFormat("yyyy-MM-dd HH:mm")
    var address: String? = null
    var mDatabaseUtil = DatabaseUtil()
    val TAG = "CreateClass"
    val apiKey = "CreateClass"
    private val startAutocomplete = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result: ActivityResult ->

            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)

                    // Write a method to read the address components from the Place
                    // and populate the form with the address components
                    Log.d(TAG, "Place: " + place.addressComponents)
                    fillInAddress(place)
                }
            } else if (result.resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Log.i(TAG, "User canceled autocomplete")
            }
        } as ActivityResultCallback<ActivityResult>)


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val classId = intent.getStringExtra("CLASS_ID_KEY") ?: "default_class_id"

        setContentView(R.layout.activity_create_class)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        ed_class_name = findViewById(R.id.ed_class_name)
        ed_class_id = findViewById(R.id.ed_class_id)
        tv_time = findViewById(R.id.tv_time)
        sw_open_location = findViewById(R.id.sw_open_location)
        ll_select_location = findViewById(R.id.ll_select_location)
        rl_location = findViewById(R.id.rl_location)
        sw_activity_tracking = findViewById(R.id.sw_activity_tracking)
        sw_using_qr = findViewById(R.id.sw_using_qr)
        sw_other_technique = findViewById(R.id.sw_other_technique)
        ll_time = findViewById(R.id.ll_time)
        btn_set_class = findViewById(R.id.btn_set_class)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)

        // Create a new PlacesClient instance
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        classTime = Date()
        tv_time.text = sp.format(classTime)
        ll_time.setOnClickListener {
            selectDate()
        }
        sw_open_location.setOnCheckedChangeListener { compoundButton, b ->
            isOpenSelectLocation = b
            if (isOpenSelectLocation) {
                ll_select_location.visibility = View.VISIBLE
                changeMap()
            } else {
                ll_select_location.visibility = View.GONE
            }
        }
        sw_activity_tracking.setOnCheckedChangeListener { compoundButton, b ->
            isOpenTracking = b
        }
        sw_using_qr.setOnCheckedChangeListener { compoundButton, b ->
            isOpenUsingQr = b
        }
        sw_other_technique.setOnCheckedChangeListener { compoundButton, b ->
            isOpenOtherTechnique = b
        }
        rl_location.setOnClickListener {
            startAutocompleteIntent()
        }
        btn_set_class.setOnClickListener {
            saveData()
        }

        if (!"".equals(classId)) {
            mDatabaseUtil.getClass(classId, { loadData(it); })
        }
    }

    private fun loadData(classInfo : DatabaseUtil.ClassInfo?) {
        if (classInfo == null) return;
        // set our fields
        className = classInfo.className
        classTime = classInfo.time
        isOpenSelectLocation = classInfo.isOpenSelectLocation
        isOpenTracking = classInfo.isOpenTracking
        isOpenUsingQr = classInfo.isOpenUsingQr
        isOpenOtherTechnique = classInfo.isOpenOtherTechnique


        val classIdString = classInfo.classID
        classID = classIdString.toInt()

        syncFields()
    }

    private fun syncFields()
    {
        ed_class_name.setText(className)
        ed_class_id.setText(classID.toString())
        tv_time.text = sp.format(classTime)
        sw_open_location.isChecked = isOpenSelectLocation
        sw_activity_tracking.isChecked = isOpenTracking
        sw_using_qr.isChecked = isOpenUsingQr
        sw_other_technique.isChecked = isOpenOtherTechnique

    }

    private fun saveData() {
        var idStr = ed_class_id.text.toString()
        className = ed_class_name.text.toString()

        if (idStr.isEmpty()) {
            Toast.makeText(this, "Please set class id", Toast.LENGTH_SHORT).show()
            return
        }
        classID = idStr.toInt()
        if (className.isEmpty()) {
            Toast.makeText(this, "Please set class name", Toast.LENGTH_SHORT).show()
            return
        }
        val classInfo = hashMapOf(
            "className" to className,
            "classID" to classID,
            "time" to classTime,
            "isOpenSelectLocation" to isOpenSelectLocation,
            "isOpenTracking" to isOpenTracking,
            "isOpenOtherTechnique" to isOpenOtherTechnique,
            "isOpenOtherTechnique" to isOpenOtherTechnique,
            "location" to location,
            "address" to address,
        )
        Log.d(TAG, "classInfo: $classInfo")
        Log.d(TAG, "classInfo time: ${classInfo["time"]} ${classInfo["time"]?.javaClass}")
        var classInfo1 = DatabaseUtil.ClassInfo(
            className,
            classID.toString(),
            classTime,
            isOpenSelectLocation,
            isOpenTracking,
            isOpenUsingQr,
            isOpenOtherTechnique
        )
        mDatabaseUtil.setClassInfo(classInfo1).addOnSuccessListener {
            Log.d(
                TAG,
                "classInfo DocumentSnapshot successfully written!"
            )
        }
            .addOnFailureListener { e -> Log.w(TAG, "classInfo Error writing document", e) }
    }

    private fun startAutocompleteIntent() {
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG, Place.Field.VIEWPORT
        )

        // Build the autocomplete intent with field, country, and type filters applied
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setCountries(listOf("US"))
            .setTypesFilter(listOf(PlaceTypes.ADDRESS))
            .build(this)
        startAutocomplete.launch(intent)
    }

    private fun fillInAddress(place: Place) {
        location = place.latLng as LatLng
        address = place.address
        changeMap()
    }

    fun changeMap() {
        if (location != null) {
            rl_location.visibility = View.VISIBLE
            mMap.addMarker(
                MarkerOptions()
                    .position(location!!)
                    .title(address ?: "Localtion")
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location!!))
        } else {
            rl_location.visibility = View.GONE
        }
    }

    fun selectDate() {
        val cd = Calendar.getInstance()
        cd.time = classTime
        val year = cd.get(Calendar.YEAR)
        val monthOfYear = cd.get(Calendar.MONTH)
        val dayOfMonth = cd.get(Calendar.DAY_OF_MONTH)
        val dialog = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                cd.set(Calendar.YEAR, year)
                cd.set(Calendar.MONTH, monthOfYear)
                cd.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                classTime = cd.time
                selectTime()
            }, year, monthOfYear, dayOfMonth
        )
        dialog.show()
    }

    fun selectTime() {
        val cd = Calendar.getInstance()
        cd.time = classTime
        val hourOfDay = cd.get(Calendar.HOUR_OF_DAY)
        val minute = cd.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(this, { view, hourOfDay, minute ->
            cd.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cd.set(Calendar.MINUTE, minute)
            classTime = cd.time
            tv_time.text = sp.format(classTime)
        }, hourOfDay, minute, false)
        dialog.show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}