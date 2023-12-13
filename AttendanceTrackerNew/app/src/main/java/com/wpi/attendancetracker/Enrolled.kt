package com.wpi.attendancetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Enrolled : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enrolled)


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewClasses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val email = intent.getStringExtra("EMAIL_KEY") ?: "default_email"
        val databaseUtil = DatabaseUtil()

        loadEnrollments(email, databaseUtil, recyclerView)

        val returnButton = findViewById<ImageButton>(R.id.returnButton2)
        returnButton.setOnClickListener {
            finish() // Finish this activity and return to the previous one in the stack
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
}