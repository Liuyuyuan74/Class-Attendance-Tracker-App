package com.wpi.attendancetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.widget.EditText
import android.widget.Toast

class Student : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        val email = intent.getStringExtra(EMAIL_KEY)?: "default_email"
        val databaseUtil = DatabaseUtil()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewClasses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val searchEditText = findViewById<EditText>(R.id.editText)
        val searchButton = findViewById<Button>(R.id.btnSearch)
        var classItemsList: List<ClassItem> = listOf()
        val btnEnrolledClasses = findViewById<Button>(R.id.btnErolledClasses)

        val btnAllClasses = findViewById<Button>(R.id.btnAllClasses)
        var nonNullClassItems: List<ClassItem> = listOf()

        databaseUtil.getAllClasses { classItems ->
            if (classItems != null) {
                nonNullClassItems = classItems.filterNotNull().map { classItem ->
                    ClassItem(classItem.className, email, classItem.classID)
                }
                classItemsList = nonNullClassItems
                recyclerView.adapter = ClassesAdapter(this, nonNullClassItems)
            } else {
                Log.w("Student", "Error getting class items")
            }
        }

        val btnGoToReport = findViewById<Button>(R.id.btnGoToReport)
        btnGoToReport.setOnClickListener {
            val reportIntent = Intent(this, StudentReportActivity::class.java)
            reportIntent.putExtra(StudentReportActivity.STUDENT_KEY, email)
            startActivity(reportIntent)
        }

        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString().trim()
            val filteredList = classItemsList.filter { it.className.contains(searchText, ignoreCase = true) }
            recyclerView.adapter = ClassesAdapter(this, filteredList)
        }

        btnEnrolledClasses.setOnClickListener {
            loadEnrollments(email, databaseUtil, recyclerView)
        }

        btnAllClasses.setOnClickListener{
            recyclerView.adapter = ClassesAdapter(this, nonNullClassItems)
        }
    }

    private fun loadEnrollments(email: String, databaseUtil: DatabaseUtil, recyclerView: RecyclerView) {
        databaseUtil.getStudentEnrollments(email) { enrollments ->
            if (enrollments != null) {
                // Filter out null values and ensure 'enrolled' is true
                val filteredEnrollments = enrollments.filterNotNull().filter { it.enrolled }

                // Pass the filtered list to the EnrollmentAdapter
                val adapter = EnrollmentAdapter(this, filteredEnrollments.toMutableList(), databaseUtil)
                recyclerView.adapter = adapter
            } else {
                Log.w("Student", "Error getting enrollments")
            }
        }
    }

    companion object {
        val EMAIL_KEY: String? = null
    }
}
