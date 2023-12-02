package com.wpi.attendancetracker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wpi.attendancetracker.databinding.ActivityStudentCheckInBinding
import java.util.Date
class Student_CheckIn : AppCompatActivity() {

    private lateinit var binding: ActivityStudentCheckInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentCheckInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val studentId = intent.getStringExtra("STUDENT_ID_KEY") ?: "default_student_id"
        val classId = intent.getStringExtra("CLASS_ID_KEY") ?: "default_class_id"
        val className = intent.getStringExtra("CLASS_NAME_KEY")

        binding.courseName.text = className ?: "Default Course Name"

        val databaseUtil = DatabaseUtil()

        binding.CheckInButton.setOnClickListener {
            val checkInTime = Date() // Current time

            databaseUtil.setCheckIn(classId, studentId, classId, checkInTime)
                .addOnSuccessListener {
                    Toast.makeText(this, "Check-in successful", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Check-in failed: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        binding.returnButton.setOnClickListener {
            finish() // Finish this activity and return to the previous one in the stack
        }
    }
}