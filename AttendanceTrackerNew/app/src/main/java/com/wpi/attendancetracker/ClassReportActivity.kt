package com.wpi.attendancetracker

import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class ClassReportActivity : AppCompatActivity() {
    private lateinit var classID : String

    private var dataElements : Int = 0

    private lateinit var pieChart : PieChart
    private lateinit var listView : ListView
    private lateinit var dbUtil : DatabaseUtil

    // These are our data query results
    private var checkIns : List<DatabaseUtil.CheckIn?>? = null
    private var classes : List<DatabaseUtil.ClassInfo?>? = null
    private var enrollments : List<DatabaseUtil.Enrollment?>? = null
    private var totalCheckIns : Int = 0
    private var totalSessions : Int = 0

    // these two maps are individual maps of clas to number of sessions and class to number of checkins
    private var checkInMap : HashMap<String, Int> = HashMap()

    companion object {
        const val CLASS_KEY = "com.wpi.attendancetracker.classID"
        const val NUM_QUERIES = 3 // how many queries are we waiting for to complete
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbUtil = DatabaseUtil()

        setContentView(R.layout.activity_student_report)
        pieChart = findViewById(R.id.pie_chart_view)
        listView = findViewById(R.id.student_list)

        val classID = intent.extras!!.getString(CLASS_KEY) ?: ""
        resetData(classID)
    }

    private fun resetData(classID : String) {
        this.classID = classID
        dataElements = 0

        checkInMap.clear()
        totalCheckIns = 0
        totalSessions = 0
        runQueries()
    }

    private fun updateData() {
        if (this.classes == null || this.enrollments == null || this.checkIns == null) {
            return
        }

        this.enrollments!!.forEach {
            if ((it != null) && it.enrolled)
                checkInMap[it.studentID] = 0
        }

        checkIns!!.forEach {
            if (it != null) {
                var count = checkInMap[it.studentID]
                if (count == null) count = 0
                count++
                totalCheckIns++
                checkInMap[it.studentID] = count
            }
        }
    }

    private fun updateView() {
        updatePieView()
        updateListView()
    }

    private fun updatePieView() {
        val colors = ArrayList<Int>()
        colors.add(Color.parseColor("#FF0000"))
        colors.add(Color.parseColor("#00FF00"))

        val attended = checkInMap.count { it.value != 0 }
        val missed = checkInMap.count { it.value == 0 }


        val pieEntries : ArrayList<PieEntry> = ArrayList()
        pieEntries.add(PieEntry(missed.toFloat(), "Missed"))
        pieEntries.add(PieEntry(attended.toFloat(), "Attended"))
        val pieDataSet = PieDataSet(pieEntries, "Attendance")
        pieDataSet.valueTextSize = 12f
        pieDataSet.colors = colors
        val pieData = PieData(pieDataSet)
        pieData.setDrawValues(true)

        pieChart.data = pieData
        pieChart.setEntryLabelColor(Color.parseColor("#808080"))
        pieChart.setCenterTextColor(Color.parseColor("#808080"))
        pieChart.setBackgroundColor(Color.WHITE)
        pieChart.description = Description()
        pieChart.description.text = "Overall Attendance"
        pieChart.invalidate()
    }

    private fun updateListView() {
        val classAttendanceList : ArrayList<String> = ArrayList()

        for (studentDetail in checkInMap)
        {
            val studentEmail = studentDetail.key
            val studentCheckins = studentDetail.value
            classAttendanceList.add("$studentEmail $studentCheckins / sessions")
            // todo need to compute how many sessions
        }

        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, classAttendanceList)
        listView.invalidate()
    }

    private fun runQueries() {
        dbUtil.getClassCheckIns(classID) { setCheckIns(it) }
        dbUtil.getAllClasses { setClasses(it) }
        dbUtil.getClassEnrollments(classID) { setEnrollments(it) }
    }

    private fun setCheckIns(checkins: List<DatabaseUtil.CheckIn?>?) {
        this.checkIns = checkins
        dataElements++
        checkUpdate()
    }

    private fun setClasses(classes: List<DatabaseUtil.ClassInfo?>?) {
        this.classes = classes
        dataElements++
        checkUpdate()
    }

    private fun setEnrollments(enrollments: List<DatabaseUtil.Enrollment?>?) {
        this.enrollments = enrollments
        dataElements++
        checkUpdate()
    }

    private fun checkUpdate() {
        if (dataElements >= NUM_QUERIES) {
            updateData()
            updateView()
        }
    }
}