package com.wpi.attendancetracker

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.Date

class DatabaseUtil {


    private val database : FirebaseFirestore = Firebase.firestore

    fun setProfessor(professorID: String, name: String, email: String, department: String): Task<Void> {
        val professor = Professor(name, email, department)
        return database.collection("professors").document(professorID).set(professor)
    }

    fun getProfessor(professorID: String, callback: (Professor?) -> Unit) {
        database.collection("professors").document(professorID).get().addOnSuccessListener {
                doc -> callback(doc.toObject(Professor::class.java))
            }.addOnFailureListener { callback(null) }
    }

    fun setStudent(studentID: String, name: String, email: String, major: String) : Task<Void> {
        val student = Student(name, email, major)
        return database.collection("students").document(studentID).set(student)
    }

    fun getStudent(studentID: String, callback: (Student?) -> Unit) {
        database.collection("students").document(studentID).get().addOnSuccessListener {
                doc -> callback(doc.toObject(Student::class.java))
        }.addOnFailureListener { callback(null) }
    }

    fun getAllStudents(callback: (List<Student?>?) -> Unit) {
        database.collection("students").get().addOnSuccessListener {
            query -> callback(query.toObjects(Student::class.java))
        }.addOnFailureListener {
            callback(null)
        }
    }


    fun setClass(classID: String, className: String, professorID: String, schedule: String) : Task<Void> {
        val classDetail = ClassDetail(className, professorID, schedule)
        return database.collection("classes").document(classID).set(classDetail)
    }

    fun getClass(classID: String, callback: (ClassDetail?) -> Unit) {
        database.collection("classes").document(classID).get().addOnSuccessListener {
                doc -> callback(doc.toObject(ClassDetail::class.java))
        }.addOnFailureListener { callback(null) }
    }

    fun setCheckIn(checkInID: String, studentID: String, classID: String, checkInTime: Date) : Task<Void> {
        val checkin = CheckIn(studentID, classID, checkInTime)
        return database.collection("checkIns").document(classID).set(checkin)
    }

    fun addCheckIn(studentID: String, classID: String, checkInTime: Date) : Task<DocumentReference> {
        val checkin = CheckIn(studentID, classID, checkInTime)
        return database.collection("checkIns").add(checkin)
    }

    fun getCheckIn(checkInID: String, callback: (CheckIn?) -> Unit) {
        database.collection("checkIns").document(checkInID).get().addOnSuccessListener {
                doc -> callback(doc.toObject(CheckIn::class.java))
        }.addOnFailureListener { callback(null) }
    }

    fun getAllCheckIns(callback: (List<CheckIn?>?) -> Unit) {
        database.collection("checkIns").get().addOnSuccessListener {
                query -> callback(query.toObjects(CheckIn::class.java))
        }.addOnFailureListener {
            callback(null)
        }

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
//
//    fun setClassEnrollment(classID: String, studentID: String, enrolled: Boolean) {
//        database.child("classEnrollments").child(classID).child(studentID).setValue(enrolled)
//    }
//
//    fun getClassEnrollment(classID: String, callback: (Map<String, Boolean>?) -> Unit) {
//        database.child("classEnrollments").child(classID).addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val enrollments = snapshot.getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {})
//                callback(enrollments)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle error
//                callback(null)
//            }
//        })
//    }

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
        val checkInTime: Date = Date()
    )
}

