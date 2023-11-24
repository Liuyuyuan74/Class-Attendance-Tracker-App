package com.wpi.attendancetracker

import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.Firebase
import com.google.firebase.database.database

// To start the report for a given class ID:
// val intent = Intent(this, ClassReportActivity.class)
// intent.putExtra(ClassReportActivity.Companion.CLASS_ID, classID);
// startActivity(newIntent);
class ClassReportActivity : AppCompatActivity() {
    private lateinit var classID : String
    private lateinit var studentAttendance : HashMap<String, Int>
    private var numTotalStudents : Int = 0
    private var numStudents : Int = 0
    private var numClasses : Int = 0
    private var numAttendees : Int = 0
    private var dataElements : Int = 0

    private lateinit var pieChart : PieChart
    private lateinit var listView : ListView

    companion object {
        const val CLASS_KEY = "com.wpi.attendancetracker.classID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_report)
        pieChart = findViewById(R.id.pie_chart_view)
        listView = findViewById(R.id.student_list)

        val passedClass = intent.extras!!.getString(CLASS_KEY) ?: ""
        resetData(passedClass)
    }

    fun resetData(classID : String) {
        this.classID = classID
        numStudents = 0
        numClasses = 0
        numTotalStudents = 0
        numAttendees = 0
        dataElements = 0
        studentAttendance = HashMap()
        runQueries()
    }

    private fun updateView() {
        updatePieView()
        updateListView()
    }
    private fun updatePieView() {
        val colors = ArrayList<Int>()
        colors.add(Color.parseColor("#FF0000"))
        colors.add(Color.parseColor("#00FF00"))

        val pieEntries : ArrayList<PieEntry> = ArrayList()
        pieEntries.add(PieEntry((numStudents - numAttendees).toFloat(), "Absences"))
        pieEntries.add(PieEntry(numAttendees.toFloat(), "Attendees"))
        val pieDataSet = PieDataSet(pieEntries, "Attendance")
        pieDataSet.valueTextSize = 12f
        pieDataSet.colors = colors
        val pieData = PieData(pieDataSet)
        pieData.setDrawValues(true)

        pieChart.data = pieData
        pieChart.invalidate()
    }

    private fun updateListView() {
        val studentList : ArrayList<String> = ArrayList()
        for ( key in studentAttendance.keys ) {
            val student = key + " " + studentAttendance[key]
            studentList.add(student)
        }

        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, studentList)
        listView.invalidate()
    }

    private fun runQueries() {
        val db = Firebase.database.reference
        db.child("enrollments").child(classID).get()
            .addOnSuccessListener { setEnrollments(it.value as List<Any>) }

        db.child("checkIns").get()
            .addOnSuccessListener { countAttendance(it.value as List<Any>) }
    }

    private fun setEnrollments(value : List<Any>) {
        // format List<Map<String,String>> : studentID: Boolean
        numStudents = value.size
        dataElements++
        checkUpdate()
    }

    private fun countAttendance(values : List<Any>) {
        // format List<Map<String,Object>> {
        // studentID : String
        // classID : String
        // checkInTime : Timestamp
        for ( value in values ) {
            val map : Map<String, Any> = value as Map<String, Any>
            if ( classID.equals(map.get("classID"))) {
                val studentID : String = map.get("studentID") as String;
                var classCount = studentAttendance.get(studentID) ?: 0
                classCount++
                studentAttendance[studentID] = classCount
            }
        }
        dataElements++;
        checkUpdate()
    }

    private fun checkUpdate() {
        if (dataElements >= 2)
            updateView()
    }
}