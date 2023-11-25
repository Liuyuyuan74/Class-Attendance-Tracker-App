import com.google.firebase.database.FirebaseDatabase
import com.wpi.attendancetracker.DatabaseUtil
import org.junit.Assert.assertEquals
import org.junit.Test

class DatabaseUtilTest {
    @Test
    fun testSetAndGetProfessor() {
        // Initialize the FirebaseUtil class
        val firebaseUtil = DatabaseUtil()

        // Test data for the professor
        val professorID = "testProfId"
        val name = "Test Prof"
        val email = "testprof@example.com"
        val department = "Test Department"

        // Set a professor in the test database
        firebaseUtil.setProfessor(professorID, name, email, department)

        // Retrieve the professor from the test database
        firebaseUtil.getProfessor(professorID) { professor ->
            // Assertions to ensure the data is as expected
            assertEquals(name, professor?.name)
            assertEquals(email, professor?.email)
            assertEquals(department, professor?.department)

            // Clean up test data from the test database
            FirebaseDatabase.getInstance().getReference("professors").child(professorID)
                .removeValue()
        }
    }
}