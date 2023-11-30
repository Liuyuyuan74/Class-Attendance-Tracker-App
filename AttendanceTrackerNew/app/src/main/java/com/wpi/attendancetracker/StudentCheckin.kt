package com.wpi.attendancetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import java.util.Date

class StudentCheckin : AppCompatActivity() {

    private lateinit var courseInfoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_checkin)

        courseInfoTextView = findViewById(R.id.textViewCourseInfo)

        val databaseUtil = DatabaseUtil()
        databaseUtil.getClass("VcFBzK4fFnzD3PZ1OrO8") { classDetail ->
            if (classDetail != null) {
                courseInfoTextView.text = "Class Name: ${classDetail.className}\nOther details here..."
            }
        }

        val checkInButton = findViewById<ImageButton>(R.id.imageButton3)
        checkInButton.setOnClickListener {
            // Replace 'studentID' and 'classID' with actual values or logic to obtain these values
            databaseUtil.addCheckIn("studentID", "classID", Date()).addOnSuccessListener {
                Toast.makeText(this, "Check-in Successful", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Check-in Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        val backButton = findViewById<ImageButton>(R.id.imageButton2)
        backButton.setOnClickListener {
            finish() // Finish the current activity, returning to the previous one in the stack
        }
    }
}
