package com.wpi.attendancetracker

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText

class RoleSelector : AppCompatActivity() {
    private lateinit var emailEditText : EditText
    private lateinit var buttonTeacher : Button
    private lateinit var buttonStudent : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selector)

        emailEditText = findViewById<EditText>(R.id.emailEditText)
        buttonTeacher = findViewById<Button>(R.id.buttonTeacher)
        buttonStudent = findViewById<Button>(R.id.buttonStudent)
        emailEditText.setText(getEmail())

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                enableButtons()
            }

            override fun afterTextChanged(s: Editable) {
                // Not used
            }
        })

        buttonTeacher.setOnClickListener {
            val email = emailEditText.text.toString()
            saveEmail(email)
            val intent = Intent(this, Teacher::class.java)
            intent.putExtra(Teacher.EMAIL_KEY, email)
            startActivity(intent)
        }
        buttonStudent.setOnClickListener {
            val email = emailEditText.text.toString()
            saveEmail(email)
            val intent = Intent(this, Student::class.java)
            intent.putExtra(Student.EMAIL_KEY, email)
            startActivity(intent)
        }

        enableButtons()
    }

    private fun enableButtons() {
        // Not used
        val enableButtons = emailEditText.text.toString().isNotEmpty()
        buttonTeacher.isEnabled = enableButtons
        buttonStudent.isEnabled = enableButtons
    }

    fun saveEmail(email : String) {
        val sharedPref = getSharedPreferences("attendance", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString("email", email)
            apply()
        }
    }

    fun getEmail() : String {
        val sharedPref = getSharedPreferences("attendance", Context.MODE_PRIVATE)
        return sharedPref.getString("email", "").toString()
    }
}
