package com.example.gestionvehicules.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionvehicules.R

class CourseHistoryAdapter(
    private val onCourseClick: (DriverCourse) -> Unit
) : ListAdapter<CourseHistoryAdapter.DriverCourse, CourseHistoryAdapter.CourseViewHolder>(CourseDiffCallback()) {

    data class DriverCourse(
        val id: Int,
        val date: String,
        val demandeur: String,
        val destination: String,
        val kilometrage: String,
        val statut: String
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_history, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        private val textViewDemandeur: TextView = itemView.findViewById(R.id.textViewDemandeur)
        private val textViewDestination: TextView = itemView.findViewById(R.id.textViewDestination)
        private val textViewKilometrage: TextView = itemView.findViewById(R.id.textViewKilometrage)
        private val textViewStatut: TextView = itemView.findViewById(R.id.textViewStatut)

        fun bind(course: DriverCourse) {
            textViewDate.text = course.date
            textViewDemandeur.text = course.demandeur
            textViewDestination.text = course.destination
            textViewKilometrage.text = course.kilometrage
            textViewStatut.text = course.statut

            // Couleur selon le statut
            when (course.statut.lowercase()) {
                "terminée" -> textViewStatut.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                "en cours" -> textViewStatut.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                else -> textViewStatut.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
            }

            itemView.setOnClickListener {
                onCourseClick(course)
            }
        }
    }

    private class CourseDiffCallback : DiffUtil.ItemCallback<DriverCourse>() {
        override fun areItemsTheSame(oldItem: DriverCourse, newItem: DriverCourse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DriverCourse, newItem: DriverCourse): Boolean {
            return oldItem == newItem
        }
    }
}
