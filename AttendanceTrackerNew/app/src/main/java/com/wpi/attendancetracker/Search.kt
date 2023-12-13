package com.wpi.attendancetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Search : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val searchButton = findViewById<Button>(R.id.btnSearch)
        val searchEditText = findViewById<EditText>(R.id.editText)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewClasses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val email = intent.getStringExtra("EMAIL_KEY") ?: "default_email"
        var classItemsList : List<ClassItem> = listOf()

        val databaseUtil = DatabaseUtil()

        databaseUtil.getAllClasses { classItems ->
            if (classItems != null) {
                val nonNullClassItems = classItems.filterNotNull().map { classItem ->
                    ClassItem(classItem.className, email, classItem.classID)
                }
                classItemsList = nonNullClassItems
//                recyclerView.adapter = ClassesAdapter(this, nonNullClassItems)
            } else {
                Log.w("Student", "Error getting class items")
            }
        }
        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString().trim()
            val filteredList = classItemsList.filter { it.className.contains(searchText, ignoreCase = true) }
            recyclerView.adapter = ClassesAdapter(this, filteredList)
        }
    }
}