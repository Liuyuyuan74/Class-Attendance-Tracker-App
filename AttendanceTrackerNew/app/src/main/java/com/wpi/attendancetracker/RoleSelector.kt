package com.wpi.attendancetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RoleSelector : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selector)

        val buttonTeacher = findViewById<Button>(R.id.buttonTeacher)
        val buttonStudent = findViewById<Button>(R.id.buttonStudent)

        buttonTeacher.setOnClickListener {
            val intent = Intent(this, Teacher::class.java)
            startActivity(intent)
        }

        buttonStudent.setOnClickListener {
            val intent = Intent(this, Student::class.java)
            startActivity(intent)
        }
    }
}
