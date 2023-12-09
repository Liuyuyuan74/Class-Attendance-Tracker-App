package com.wpi.attendancetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log

class Student : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        val email = intent.getStringExtra(EMAIL_KEY)?: "default_email"
        val databaseUtil = DatabaseUtil()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewClasses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        databaseUtil.getAllClasses { classItems ->
            if (classItems != null) {
//                val studentID = "jdifao"
//                val classID = "jdsfoiaje"
//                val nonNullClassItems = classItems.filterNotNull()
                val nonNullClassItems = classItems.filterNotNull().map { classItem ->
                    ClassItem(classItem.className, email, classItem.classID)
                }
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
    }

    companion object {
        val EMAIL_KEY: String? = null
    }
}
