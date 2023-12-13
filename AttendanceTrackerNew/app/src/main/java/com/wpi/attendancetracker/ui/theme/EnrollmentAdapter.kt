package com.wpi.attendancetracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class EnrollmentAdapter(
    private val context: Context,
    private val enrollmentList: List<DatabaseUtil.Enrollment>,
    private val databaseUtil: DatabaseUtil
) : RecyclerView.Adapter<EnrollmentAdapter.EnrollmentViewHolder>() {

    class EnrollmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewClassID: TextView = view.findViewById(R.id.tvClassID)
        val buttonCancelEnrollment: Button = view.findViewById(R.id.btnCancelEnrollment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnrollmentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.enrollment_item, parent, false)
        return EnrollmentViewHolder(view)
    }

//    override fun onBindViewHolder(holder: EnrollmentViewHolder, position: Int) {
//        val enrollment = enrollmentList[position]
//        holder.textViewClassID.text = enrollment.classID
//        holder.buttonCancelEnrollment.setOnClickListener {
//            // Call the database update function
//            databaseUtil.setStudentEnrolled(enrollment.studentID, enrollment.classID, false) {
//                Toast.makeText(context, "Enrollment cancelled", Toast.LENGTH_SHORT).show()
//                // Consider refreshing the data or notifying the adapter
//                holder.buttonCancelEnrollment.text = "Canceled"
//            }
//        }
//    }

    override fun onBindViewHolder(holder: EnrollmentViewHolder, position: Int) {
        val enrollment = enrollmentList[position]
        holder.textViewClassID.text = enrollment.classID
        holder.buttonCancelEnrollment.setOnClickListener {
            Toast.makeText(context, "Enrollment cancelled", Toast.LENGTH_SHORT).show()
            holder.buttonCancelEnrollment.text = "Canceled"
            databaseUtil.setStudentEnrolled(enrollment.studentID, enrollment.classID, false) { isSuccess ->
                if (isSuccess) {

                    // Optionally, disable the button to prevent further clicks
                    holder.buttonCancelEnrollment.isEnabled = false
                } else {
                    Toast.makeText(context, "Failed to cancel enrollment", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun getItemCount() = enrollmentList.size
}
