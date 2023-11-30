package com.wpi.attendancetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClassesAdapter(private val classList: List<ClassItem>) : RecyclerView.Adapter<ClassesAdapter.ClassViewHolder>() {

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
            // Handle check-in action
        }
    }

    override fun getItemCount() = classList.size
}
