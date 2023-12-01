package com.wpi.attendancetracker

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
//import androidx.core.content.ContextCompat.startActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class ClassesAdapter(private val context: Context, private val classList: List<ClassItem>) : RecyclerView.Adapter<ClassesAdapter.ClassViewHolder>() {

    class ClassViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewClassName: TextView = view.findViewById(R.id.tvClassName)
        val buttonCheckIn: Button = view.findViewById(R.id.btnCheckIn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.class_item, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val classItem = classList[position]
        holder.textViewClassName.text = classItem.className
        holder.buttonCheckIn.setOnClickListener {
            val studentId = classItem.studentId
            val classId = classItem.classId

            val checkInIntent = Intent(context, Student_CheckIn::class.java)
            checkInIntent.putExtra("STUDENT_ID_KEY", studentId)
            checkInIntent.putExtra("CLASS_ID_KEY", classId)
            context.startActivity(checkInIntent)
        }
    }

    override fun getItemCount() = classList.size
}
