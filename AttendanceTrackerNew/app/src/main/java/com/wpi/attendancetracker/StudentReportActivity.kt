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

// To start the report for a given class ID:
// val intent = Intent(this, ClassReportActivity.class)
// intent.putExtra(ClassReportActivity.Companion.CLASS_ID, classID);
// startActivity(newIntent);
class StudentReportActivity : AppCompatActivity() {
    private lateinit var studentID : String

    private var dataElements : Int = 0

    private lateinit var pieChart : PieChart
    private lateinit var listView : ListView
    private lateinit var dbUtil : DatabaseUtil

    // These are our data query results
    private var checkIns : List<DatabaseUtil.CheckIn?>? = null
    private var classes : List<ClassInfo?>? = null
    private var enrollments : List<DatabaseUtil.Enrollment?>? = null
    private var totalCheckIns : Int = 0
    private var totalSessions : Int = 0

    // these two maps are individual maps of clas to number of sessions and class to number of checkins
    private var classMap : HashMap<String, Int> = HashMap()
    private var checkInMap : HashMap<String, Int> = HashMap()
    private var classDetailMap : HashMap<String, ClassInfo> = HashMap()
    //private var studentMap : HashMap<String,DatabaseUtil.Student> = HashMap()

    companion object {
        const val STUDENT_KEY = "com.wpi.attendancetracker.studentID"
        const val NUM_QUERIES = 3 // how many queries are we waiting for to complete
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbUtil = DatabaseUtil()

        setContentView(R.layout.activity_student_report)
        pieChart = findViewById(R.id.pie_chart_view)
        listView = findViewById(R.id.student_list)

        val studentID = intent.extras!!.getString(STUDENT_KEY) ?: ""
        resetData(studentID)
    }

    private fun resetData(studentID : String) {
        this.studentID = studentID
        dataElements = 0

        classMap.clear()
        checkInMap.clear()
        classDetailMap.clear()

        totalCheckIns = 0
        totalSessions = 0
        runQueries()
    }

    private fun updateData() {
        if (this.classes == null || this.enrollments == null || this.checkIns == null) {
            return
        }

        // TODO: only count classes that we are enrolled in, but for now, all classes
        val enrolledClasses : ArrayList<ClassInfo> = ArrayList()
        this.classes!!.forEach {
            if (it != null) {
                enrolledClasses.add(it)
                classDetailMap[it.classID] = it
            }
        }

        enrolledClasses.forEach {
            classMap.set(it.classID, 1) // should be the number of sessions, not 1
            totalSessions+=1
        }

        checkIns!!.forEach {
            if (it != null) {
                var count = checkInMap[it.classID]
                if (count == null) count = 0
                count++
                totalCheckIns++
                checkInMap[it.classID] = count
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

        val pieEntries : ArrayList<PieEntry> = ArrayList()
        pieEntries.add(PieEntry((totalSessions - totalCheckIns).toFloat(), "Missed"))
        pieEntries.add(PieEntry(totalCheckIns.toFloat(), "Attended"))
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

        for (classInfo in classDetailMap)
        {
            val classKey = classInfo.key
            val classDetail = classInfo.value
            val sessions = classMap[classKey] ?: 0
            val attended = checkInMap[classKey] ?: 0
            classAttendanceList.add(classDetail.className + " " + attended + "/" + sessions)
        }

        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, classAttendanceList)
        listView.invalidate()
    }

    private fun runQueries() {
        dbUtil.getCheckIns(studentID, { setCheckIns(it) })
        dbUtil.getAllClasses( { setClasses(it) } )
        dbUtil.getStudentEnrollments(studentID, { setEnrollments(it) })
    }

    private fun setCheckIns(checkins: List<DatabaseUtil.CheckIn?>?) {
        this.checkIns = checkins
        dataElements++
        checkUpdate()
    }

    private fun setClasses(classes: List<ClassInfo?>?) {
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