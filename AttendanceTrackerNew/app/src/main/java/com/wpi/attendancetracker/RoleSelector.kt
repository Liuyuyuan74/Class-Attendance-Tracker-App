package com.wpi.attendancetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText

class RoleSelector : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selector)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val buttonTeacher = findViewById<Button>(R.id.buttonTeacher)
        val buttonStudent = findViewById<Button>(R.id.buttonStudent)

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Enable buttons if email is not empty
                val enableButtons = s.toString().isNotEmpty()
                buttonTeacher.isEnabled = enableButtons
                buttonStudent.isEnabled = enableButtons
            }

            override fun afterTextChanged(s: Editable) {
                // Not used
            }
        })

        buttonTeacher.setOnClickListener {
            val intent = Intent(this, Teacher::class.java)
            intent.putExtra(Teacher.EMAIL_KEY, emailEditText.text.toString())
            startActivity(intent)
        }

        buttonStudent.setOnClickListener {
            val intent = Intent(this, Student::class.java)
            intent.putExtra(Student.EMAIL_KEY, emailEditText.text.toString())
            startActivity(intent)
        }
    }
}
