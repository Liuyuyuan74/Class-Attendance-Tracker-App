package com.wpi.attendancetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class Student : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        val db = FirebaseFirestore.getInstance()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewClasses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db.collection("classes")
            .get()
            .addOnSuccessListener { documents ->
                val classItems = documents.mapNotNull { doc ->
                    doc.getString("className")?.let { name ->
                        ClassItem(name)
                    }
                }
                recyclerView.adapter = ClassesAdapter(classItems)
            }
            .addOnFailureListener { exception ->
                Log.w("Student", "Error getting documents: ", exception)
            }

        val btnGoToReport = findViewById<Button>(R.id.btnGoToReport)
        btnGoToReport.setOnClickListener {
            val reportIntent = Intent(this, ClassReportActivity::class.java)
            reportIntent.putExtra(ClassReportActivity.CLASS_KEY, "VcFBzK4fFnzD3PZ1OrO8")
            startActivity(reportIntent)
        }
    }
}