package com.wpi.attendancetracker

import com.google.firebase.firestore.FirebaseFirestore
import com.wpi.attendancetracker.DatabaseUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabaseUtilTest {
    val lock = Object()

    @Test
    fun testSetAndGetProfessor() {
        // Initialize the FirebaseUtil class
        val firebaseUtil = DatabaseUtil()
        var done = false

        // Test data for the professor
        val professorID = "testProfId"
        val name = "Test Prof"
        val email = "testprof@example.com"
        val department = "Test Department"

        // Set a professor in the test database
        done = false
        firebaseUtil.setProfessor(professorID, name, email, department).addOnSuccessListener {
            synchronized(lock) { done = true ; lock.notify() }
        }.addOnFailureListener {
            it.printStackTrace()
            assertTrue("Error executing create: " + it.stackTraceToString(), false)
        }
        synchronized(lock) {
            lock.wait(5000)
        }
        assertTrue("Nothing happened", done)

        done = false
        // Retrieve the professor from the test database
        firebaseUtil.getProfessor(professorID) { professor ->
            // Assertions to ensure the data is as expected
            assertEquals(name, professor?.name)
            assertEquals(email, professor?.email)
            assertEquals(department, professor?.department)

            // Clean up test data from the test database
            FirebaseFirestore.getInstance().collection("professors").document(professorID)
                .delete()

            done = true
            synchronized(lock) { lock.notify() }
        }

        synchronized(lock) {
            lock.wait(5000)
            assertTrue("Firebase never did anything", done)
        }

    }

    @Test
    fun testGetAllCheckins() {
        val firebaseUtil = DatabaseUtil()
        var done = false
        firebaseUtil.getAllCheckIns {
            assertNotNull(it)
            assertNotEquals(0, it!!.size)

            if (it != null) {
                for (checkin in it) {
                    println(checkin.toString())
                }
            } else {
                println("it is null");
            }
            done=true
            synchronized(lock) { lock.notify() }
        }

        synchronized(lock) {
            lock.wait(5000)
            assertTrue("Firebase never did anything", done)
        }


    }
}