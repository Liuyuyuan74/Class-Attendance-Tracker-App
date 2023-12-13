package com.wpi.attendancetracker

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
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
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.wpi.attendancetracker.databinding.ItemTimeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class EditClass : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    var className = ""
    var classID = 0
    var classTimes:MutableList<Date> = arrayListOf<Date>()
    var curTimeIndex= 0
    var isOpenSelectLocation = false
    var isOpenTracking = false
    var isOpenUsingQr = false
    var location: LatLng? = null
    lateinit var ed_class_name: EditText
    lateinit var ed_class_id: EditText
    lateinit var ll_times: LinearLayoutCompat
    lateinit var iv_add_time: AppCompatImageView
    lateinit var sw_open_location: Switch
    lateinit var ll_select_location: LinearLayoutCompat
    lateinit var rl_location: RelativeLayout
    private lateinit var sw_activity_tracking: Switch
    lateinit var sw_using_qr: Switch
    lateinit var btn_set_class: Button
    lateinit var btn_enrollments: Button
    lateinit var btn_qrcode: Button
    var lat:Double?=null
    var lon:Double?=null
    var address:String?=null
    var sp = SimpleDateFormat("yyyy-MM-dd HH:mm")
    var mDatabaseUtil = DatabaseUtil()
    val TAG = "jj-EditClass"
    val apiKey = "AIzaSyAa8xUgJLfBBuPYmVi3WgGm1cfnLLfVIJE"
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
        })


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val classId = intent.getStringExtra("CLASS_ID_KEY") ?: "default_class_id"

        setContentView(R.layout.activity_edit_class)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        ed_class_name = findViewById(R.id.ed_class_name)
        ed_class_id = findViewById(R.id.ed_class_id)
        iv_add_time = findViewById(R.id.iv_add_time)
        sw_open_location = findViewById(R.id.sw_open_location)
        ll_select_location = findViewById(R.id.ll_select_location)
        rl_location = findViewById(R.id.rl_location)
        sw_activity_tracking = findViewById(R.id.sw_activity_tracking)
        sw_using_qr = findViewById(R.id.sw_using_qr)
        ll_times = findViewById(R.id.ll_times)
        btn_set_class = findViewById(R.id.btn_set_class)
        btn_enrollments = findViewById(R.id.btn_enrollments)
        btn_qrcode = findViewById(R.id.btn_qrcode)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Initialize the SDK
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        placesClient = Places.createClient(this)
        // Create a new PlacesClient instance
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        iv_add_time.setOnClickListener {
            curTimeIndex=-1
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
        ll_select_location.setOnClickListener {
            startAutocompleteIntent()
        }
        btn_set_class.setOnClickListener {
            saveData()
        }

        btn_enrollments.setOnClickListener {
            launchReport()
        }

        btn_qrcode.setOnClickListener {
            launchQrCode()
        }

        if (!"".equals(classId)) {
            mDatabaseUtil.getClass(classId) { loadData(it); }
        }
    }

    private fun loadData(classInfo : DatabaseUtil.ClassInfo?) {
        if (classInfo == null) return;
        // set our fields
        className = classInfo.className
        classTimes = classInfo.times
        isOpenSelectLocation = classInfo.openSelectLocation
        isOpenTracking = classInfo.openTracking
        isOpenUsingQr = classInfo.openUsingQr
        val classIdString = classInfo.classID
        classID = classIdString.toInt()
        syncFields()
    }

    private fun launchReport()
    {
        val reportIntent = Intent(this, ClassReportActivity::class.java)
        reportIntent.putExtra(ClassReportActivity.CLASS_KEY, classID.toString())
        startActivity(reportIntent)
    }

    private fun launchQrCode()
    {
        val qrIntent = Intent(this, ClassQRActivity::class.java)
        qrIntent.putExtra(ClassReportActivity.CLASS_KEY, classID.toString())
        startActivity(qrIntent)
    }

    private fun syncFields()
    {
        ed_class_name.setText(className)
        ed_class_id.setText(classID.toString())
      //  tv_time.text = sp.format(classTime)
        sw_open_location.isChecked = isOpenSelectLocation
        sw_activity_tracking.isChecked = isOpenTracking
        sw_using_qr.isChecked = isOpenUsingQr
        updateTimes()
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

        var classInfo1 = DatabaseUtil.ClassInfo(
            className,
            classID.toString(),
            classTimes,
            isOpenSelectLocation,
            isOpenTracking,
            isOpenUsingQr,
            false,
        )
        classInfo1.lat = 42.347800
        classInfo1.lon = -71.535420

        mDatabaseUtil.setClassInfo(classInfo1).addOnSuccessListener {
            Log.d(TAG,"classInfo DocumentSnapshot successfully written!")
            Toast.makeText(this, "Saved Class", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            e -> Log.w(TAG, "classInfo Error writing document", e)
            Toast.makeText(this, "Error Saving", Toast.LENGTH_SHORT).show()
        }
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
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
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
        if(curTimeIndex==-1){
            cd.time = Date()

        }else{
            cd.time = classTimes[curTimeIndex]
        }

        val year = cd.get(Calendar.YEAR)
        val monthOfYear = cd.get(Calendar.MONTH)
        val dayOfMonth = cd.get(Calendar.DAY_OF_MONTH)
        val dialog = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                cd.set(Calendar.YEAR, year)
                cd.set(Calendar.MONTH, monthOfYear)
                cd.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                if(curTimeIndex==-1){
                    classTimes.add(cd.time)

                }else{
                    classTimes[curTimeIndex]=cd.time ;
                }
                selectTime()
            }, year, monthOfYear, dayOfMonth
        )
        dialog.show()
    }

    fun selectTime() {
        val cd = Calendar.getInstance()
        if(curTimeIndex==-1){
            cd.time = Date()

        }else{
            cd.time = classTimes[curTimeIndex]
        }
        val hourOfDay = cd.get(Calendar.HOUR_OF_DAY)
        val minute = cd.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(this, { view, hourOfDay, minute ->
            cd.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cd.set(Calendar.MINUTE, minute)
            if(curTimeIndex==-1){
                classTimes.add(cd.time)
            }else{
                classTimes[curTimeIndex]=cd.time ;
            }
            updateTimes()
        }, hourOfDay, minute, false)
        dialog.show()
    }

    fun updateTimes(){
        ll_times.removeAllViews()
        for (index in 0 until classTimes.size ){
            var item = ItemTimeBinding.inflate(LayoutInflater.from(this))
            item.tvTime.setText(sp.format(classTimes[index]))
            item.root.setOnClickListener {
                curTimeIndex = index
                selectDate()
            }
            ll_times.addView(item.root)
        }
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

    /**
     * Check whether permissions have been granted or not, and ultimately proceeds to either
     * request them or runs {@link #findCurrentPlaceWithPermissions() findCurrentPlaceWithPermissions}
     */
    @SuppressLint("MissingPermission")
    private fun findCurrentPlace() {
        if (hasOnePermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            findCurrentPlaceWithPermissions()
            return
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /**
     * Fetches a list of [com.google.android.libraries.places.api.model.PlaceLikelihood] instances that represent the Places the user is
     * most likely to be at currently.
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE])
    private fun findCurrentPlaceWithPermissions() {
        setLoading(true)
        val placeFields = arrayListOf<Place.Field>(
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.CURBSIDE_PICKUP,
            Place.Field.CURRENT_OPENING_HOURS,
            Place.Field.DELIVERY,
            Place.Field.DINE_IN,
            Place.Field.EDITORIAL_SUMMARY,
            Place.Field.OPENING_HOURS,
            Place.Field.PHONE_NUMBER,
            Place.Field.RESERVABLE,
            Place.Field.SECONDARY_OPENING_HOURS,
            Place.Field.SERVES_BEER,
            Place.Field.SERVES_BREAKFAST,
            Place.Field.SERVES_BRUNCH,
            Place.Field.SERVES_DINNER,
            Place.Field.SERVES_LUNCH,
            Place.Field.SERVES_VEGETARIAN_FOOD,
            Place.Field.SERVES_WINE,
            Place.Field.TAKEOUT,
            Place.Field.UTC_OFFSET,
            Place.Field.WEBSITE_URI,
            Place.Field.WHEELCHAIR_ACCESSIBLE_ENTRANCE
        )
        val currentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)
        val currentPlaceTask = placesClient.findCurrentPlace(currentPlaceRequest)
        currentPlaceTask.addOnSuccessListener { response: FindCurrentPlaceResponse? ->
            response?.let {
                if(it.placeLikelihoods.isNotEmpty()){
                    it.placeLikelihoods.first().place.let {
                        address=it.address;
                        lat=it.latLng.latitude
                        lon=it.latLng.longitude
                    }
                }
               // binding.response.text = StringUtil.stringify(it, isDisplayRawResultsChecked)
            }
        }
        currentPlaceTask.addOnFailureListener { exception: Exception ->

        }
        currentPlaceTask.addOnCompleteListener {  setLoading(false)}
    }

    private fun setLoading(loading: Boolean) {

    }
    @SuppressLint("MissingPermission")
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    // Only approximate location access granted.
                    findCurrentPlaceWithPermissions()
                }
                else -> {
                    Toast.makeText(
                        this,
                        "Either ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permissions are required",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    private fun hasOnePermissionGranted(vararg permissions: String): Boolean =
        permissions.any {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
}