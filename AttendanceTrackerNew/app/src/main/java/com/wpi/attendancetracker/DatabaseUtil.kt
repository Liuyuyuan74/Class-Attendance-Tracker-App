package com.wpi.attendancetracker

import com.google.android.gms.maps.model.LatLng
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

    fun setClass(classInfo : ClassInfo) : Task<Void> {
        return database.collection("classes").document(classInfo.classID).set(classInfo)
    }
    
//    fun getAllClasses(callback: (List<ClassItem?>?) -> Unit) {
//        database.collection("classes").get().addOnSuccessListener { documents ->
//            val classItems = documents.mapNotNull { doc ->
//                doc.getString("className")?.let { name ->
//                    ClassItem(name)
//                }
//            }
//            callback(classItems)
//        }.addOnFailureListener {
//            callback(null)
//        }
//    }
    fun getAllClasses(callback: (List<ClassInfo?>?) -> Unit) {
        database.collection("classes").get().addOnSuccessListener { documents ->
            val classItems = documents.mapNotNull { doc ->
                doc.toObject(ClassInfo::class.java)
            }
            callback(classItems)
        }.addOnFailureListener {
            callback(null)
        }
    }
    fun setClassInfo(classInfo: ClassInfo) : Task<Void> {
        return database.collection("classes").document(classInfo.classID).set(classInfo)
    }
    fun getClass(classID: String, callback: (ClassInfo?) -> Unit) {
        database.collection("classes").document(classID).get().addOnSuccessListener {
                doc -> callback(doc.toObject(ClassInfo::class.java))
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

    fun getCheckIns(studentEmail : String, callback: (List<CheckIn?>?) -> Unit) {
        database.collection("checkIns").whereEqualTo("studentID", studentEmail).
            get().addOnSuccessListener {
                query -> callback(query.toObjects(CheckIn::class.java))
        }.addOnFailureListener {
            callback(null)
        }

    }
    fun getAllEnrollments(callback: (List<Enrollment?>?) -> Unit) {
        database.collection("classEnrollments").get().addOnSuccessListener {
                query -> callback(query.toObjects(Enrollment::class.java))
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun getStudentEnrollments(studentEmail : String, callback: (List<Enrollment?>?) -> Unit) {
        database.collection("classEnrollments").whereEqualTo("studentID", studentEmail).
            get().addOnSuccessListener {
                query -> callback(query.toObjects(Enrollment::class.java))
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun setClassEnrollment(classID: String, studentID: String, enrolled: Boolean) {
        database.collection("classEnrollments").document("$classID-$studentID").set(Enrollment(classID, studentID, enrolled))
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
    data class CheckIn(
        val studentID: String = "",
        val classID: String = "",
        val checkInTime: Date = Date()
    )

    data class Enrollment(
        val studentID: String = "",
        val classID: String = "",
        val enrolled: Boolean = false
    )

    class ClassInfo(
        val className: String = "",
        val classID: String = "",
        val time: Date = Date(),
        val isOpenSelectLocation: Boolean=false,
        val   isOpenTracking: Boolean=false,
        val isOpenUsingQr: Boolean=false,
        val isOpenOtherTechnique: Boolean=false
    ){
        var location: LatLng?=null
        var address:String?=null
    }
}
