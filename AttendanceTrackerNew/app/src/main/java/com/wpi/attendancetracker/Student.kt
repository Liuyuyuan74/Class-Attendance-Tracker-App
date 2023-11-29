package com.wpi.attendancetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Student : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        val classItems = listOf(ClassItem("Math 101"), ClassItem("Science 202"))
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewClasses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ClassesAdapter(classItems)

        val btnGoToReport = findViewById<Button>(R.id.btnGoToReport)

        btnGoToReport.setOnClickListener {
            val reportIntent = Intent(this, ClassReportActivity::class.java)
            reportIntent.putExtra(ClassReportActivity.CLASS_KEY, "VcFBzK4fFnzD3PZ1OrO8");
            startActivity(reportIntent);
        }
    }
}