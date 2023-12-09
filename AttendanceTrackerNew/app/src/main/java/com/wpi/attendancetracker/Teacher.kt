package com.wpi.attendancetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Teacher : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher)

        val email = intent.getStringExtra(EMAIL_KEY)?: "default_email"
        val databaseUtil = DatabaseUtil()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewClasses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        databaseUtil.getAllClasses { classItems ->
            if (classItems != null) {
                val nonNullClassItems = classItems.filterNotNull().map { classItem ->
                    ClassItem(classItem.className, email, classItem.classID)
                }
                val adapter = ClassesAdapter(this, nonNullClassItems)
                adapter.buttonLabel = "Edit"
                adapter.intentClass = CreateClass::class.java
                recyclerView.adapter = adapter
            } else {
                Log.w("Teacher", "Error getting class items")
            }
        }

        val btnCreate = findViewById<Button>(R.id.btnCreate)
        btnCreate.setOnClickListener {
            val reportIntent = Intent(this, CreateClass::class.java)
            reportIntent.putExtra(StudentReportActivity.STUDENT_KEY, email)
            startActivity(reportIntent)
        }

    }

    companion object {
        val EMAIL_KEY: String? = null
    }
}