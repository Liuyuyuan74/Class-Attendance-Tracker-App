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
import java.util.Date

class ClassReportActivity : AppCompatActivity() {
    private lateinit var classID : String

    private var dataElements : Int = 0

    private lateinit var pieChart : PieChart
    private lateinit var listView : ListView
    private lateinit var dbUtil : DatabaseUtil

    // These are our data query results
    private var checkIns : List<DatabaseUtil.CheckIn?>? = null
    private var classInfo : DatabaseUtil.ClassInfo? = null
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
        if (this.classInfo == null || this.enrollments == null || this.checkIns == null) {
            return
        }

        this.enrollments!!.forEach {
            if ((it != null) && it.enrolled)
                checkInMap[it.studentID] = 0
        }

        classInfo!!.times.forEach { classDate ->
            // Go through each time, and check who checked in during that time
            val classTime = classDate.time
            val classTimeEnd = classDate.time + 90 * 60 * 1000 // assume 90 min class
            if (classDate <= Date()) {
                totalSessions++
                // only count classes that are before now
                enrollments!!.filterNotNull().forEach { student ->
                    checkIns!!.filterNotNull().forEach { checkin ->
                        // look only at this students' checkins
                        if (checkin.studentID == student.studentID) {
                            if (checkin.checkInTime.time in classTime..classTimeEnd) {
                                // if it is during this class time, increment their checkins
                                var count = checkInMap[checkin.studentID]
                                if (count == null) count = 0
                                count++
                                totalCheckIns++
                                checkInMap[checkin.studentID] = count
                            }
                        }
                    }
                }
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

        val total = checkInMap.size * totalSessions
        val attended = totalCheckIns
        val missed = total - attended

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

            classAttendanceList.add("$studentEmail $studentCheckins / $totalSessions")
        }

        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, classAttendanceList)
        listView.invalidate()
    }

    private fun runQueries() {
        dbUtil.getClassCheckIns(classID) { setCheckIns(it) }
        dbUtil.getClass(classID) { setClass(it) }
        dbUtil.getClassEnrollments(classID) { setEnrollments(it) }
    }

    private fun setCheckIns(checkins: List<DatabaseUtil.CheckIn?>?) {
        this.checkIns = checkins
        dataElements++
        checkUpdate()
    }

    private fun setClass(classInfo: DatabaseUtil.ClassInfo?) {
        this.classInfo = classInfo
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