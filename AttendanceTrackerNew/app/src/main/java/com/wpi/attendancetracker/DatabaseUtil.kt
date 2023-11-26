package com.wpi.attendancetracker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener


class DatabaseUtil {


    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun setProfessor(professorID: String, name: String, email: String, department: String) {
        val professor = Professor(name, email, department)
        database.child("professors").child(professorID).setValue(professor)
    }

    fun getProfessor(professorID: String, callback: (Professor?) -> Unit) {
        database.child("professors").child(professorID).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val professor = snapshot.getValue(Professor::class.java)
                callback(professor)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                callback(null)
            }
        })
    }

    fun setStudent(studentID: String, name: String, email: String, major: String) {
        val student = Student(name, email, major)
        database.child("students").child(studentID).setValue(student)
    }

    fun getStudent(studentID: String, callback: (Student?) -> Unit) {
        database.child("students").child(studentID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val student = snapshot.getValue(Student::class.java)
                callback(student)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                callback(null)
            }
        })
    }

    fun getAllStudents(callback: (List<Student?>?) -> Unit) {
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list: MutableList<Student?> = ArrayList()
                for (ds in dataSnapshot.children) {
                    val yourClass: Student? = ds.getValue(Student::class.java)
                    list.add(yourClass)
                }
                callback(list)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(null)
            }
        }
        database.child("checkIns").addListenerForSingleValueEvent(valueEventListener)
    }


    fun setClass(classID: String, className: String, professorID: String, schedule: String) {
        val classDetail = ClassDetail(className, professorID, schedule)
        database.child("classes").child(classID).setValue(classDetail)
    }

    fun getClass(classID: String, callback: (ClassDetail?) -> Unit) {
        database.child("classes").child(classID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val classDetail = snapshot.getValue(ClassDetail::class.java)
                callback(classDetail)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                callback(null)
            }
        })
    }

    fun setCheckIn(checkInID: String, studentID: String, classID: String, checkInTime: String) {
        val checkIn = CheckIn(studentID, classID, checkInTime)
        database.child("checkIns").child(checkInID).setValue(checkIn)
    }

    fun getCheckIn(checkInID: String, callback: (CheckIn?) -> Unit) {
        database.child("checkIns").child(checkInID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val checkIn = snapshot.getValue(CheckIn::class.java)
                callback(checkIn)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                callback(null)
            }
        })
    }

    fun getAllCheckIns(callback: (List<CheckIn?>?) -> Unit) {
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list: MutableList<CheckIn?> = ArrayList<CheckIn?>()
                for (ds in dataSnapshot.children) {
                    val yourClass: CheckIn? = ds.getValue(CheckIn::class.java)
                    list.add(yourClass)
                }
                callback(list)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(null)
            }
        }
        database.child("checkIns").addListenerForSingleValueEvent(valueEventListener)
    }

//    fun getAllEnrollments(classID: String, callback: (List<Enrollment?>?) -> Unit) {
//        val valueEventListener: ValueEventListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val list: MutableList<CheckIn?> = ArrayList<CheckIn?>()
//                for (ds in dataSnapshot.children) {
//                    val yourClass: CheckIn? = ds.getValue(CheckIn::class.java)
//                    list.add(yourClass)
//                }
//                callback(list)
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                callback(null)
//            }
//        }
//        database.child("checkIns").addListenerForSingleValueEvent(valueEventListener)
//    }

    fun setClassEnrollment(classID: String, studentID: String, enrolled: Boolean) {
        database.child("classEnrollments").child(classID).child(studentID).setValue(enrolled)
    }

    fun getClassEnrollment(classID: String, callback: (Map<String, Boolean>?) -> Unit) {
        database.child("classEnrollments").child(classID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val enrollments = snapshot.getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {})
                callback(enrollments)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                callback(null)
            }
        })
    }

    fun setDatabaseReference(mockDatabaseRef: DatabaseReference?) {

    }

    data class Professor(
        val name: String = "",
        val email: String = "",
        val department: String = ""
    )

    data class Student(
        val name: String = "",
        val email: String = "",
        val major: String = ""
    )

    data class ClassDetail(
        val className: String = "",
        val professorID: String = "",
        val schedule: String = ""
    )

    data class CheckIn(
        val studentID: String = "",
        val classID: String = "",
        val checkInTime: String = ""
    )

}

