package com.wpi.attendancetracker
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wpi.attendancetracker.databinding.ActivityStudentCheckInBinding
import java.util.Date


class Student_CheckIn : AppCompatActivity() {

    private lateinit var binding: ActivityStudentCheckInBinding
    private lateinit var databaseUtil : DatabaseUtil
    private lateinit var classId : String
    private lateinit var studentId : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentCheckInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var autoCheckin = false
        studentId = intent.getStringExtra("STUDENT_ID_KEY") ?: "default_student_id"
        classId = intent.getStringExtra("CLASS_ID_KEY") ?: "default_class_id"
        val className = intent.getStringExtra("CLASS_NAME_KEY")
        if (intent.data != null) {
            classId = intent.data!!.host.toString() // "twitter.com"
            val params = intent.data!!.pathSegments
            studentId = getEmail() // "status"
            autoCheckin = true
        }

        databaseUtil = DatabaseUtil()
        databaseUtil.getClass(classId) {
            if (it?.className != null)
                binding.courseName.text = it.className
        }

        binding.CheckInButton.setOnClickListener {
            doCheckIn()
        }

        databaseUtil.isStudentEnrolled(studentId, classId) { b ->
            binding.enrolled.isChecked = b
        }

        binding.enrolled.setOnCheckedChangeListener { compoundButton, b ->
            databaseUtil.setStudentEnrolled(studentId, classId, b) {
                Toast.makeText(this, "Enrollment Updated", Toast.LENGTH_SHORT).show()
            }
        }

        binding.returnButton.setOnClickListener {
            finish() // Finish this activity and return to the previous one in the stack
        }

        if (autoCheckin)
            doCheckIn()
    }

    private fun doCheckIn() {
        val checkInTime = Date() // Current time
        databaseUtil.addCheckIn(studentId, classId, checkInTime)
            .addOnSuccessListener {
                Toast.makeText(this, "Check-in successful", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Check-in failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun getEmail() : String {
        val sharedPref = getSharedPreferences("attendance", Context.MODE_PRIVATE)
        return sharedPref.getString("email", "").toString()
    }
}