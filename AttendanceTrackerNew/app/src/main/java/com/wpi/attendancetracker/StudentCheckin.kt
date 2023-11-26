package com.wpi.attendancetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log


class StudentCheckin : AppCompatActivity() {

    val logClassDetails: (classDetail: DatabaseUtil.ClassDetail?) -> Unit = { classDetail ->
        if (classDetail != null) {
            Log.d("className", classDetail.className)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_checkin)
        val databaseUtil = DatabaseUtil()
        databaseUtil.getClass("VcFBzK4fFnzD3PZ1OrO8", logClassDetails)
    }
}