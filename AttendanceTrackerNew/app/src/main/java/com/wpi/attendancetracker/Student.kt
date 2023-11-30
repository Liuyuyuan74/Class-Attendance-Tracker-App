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

        val databaseUtil = DatabaseUtil()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewClasses)
        recyclerView.layoutManager = LinearLayoutManager(this)


        databaseUtil.getAllClasses { classItems ->
            if (classItems != null) {
                val nonNullClassItems = classItems.filterNotNull()
                recyclerView.adapter = ClassesAdapter(nonNullClassItems)
            } else {
                Log.w("Student", "Error getting class items")
            }
        }

        val btnGoToReport = findViewById<Button>(R.id.btnGoToReport)
        btnGoToReport.setOnClickListener {
            val reportIntent = Intent(this, ClassReportActivity::class.java)
            reportIntent.putExtra(ClassReportActivity.CLASS_KEY, "VcFBzK4fFnzD3PZ1OrO8")
            startActivity(reportIntent)
        }
    }
    companion object {
        val EMAIL_KEY: String? = null
    }
}
