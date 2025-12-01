package com.example.gestionvehicules.ui.driver.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionvehicules.data.model.Course
import com.example.gestionvehicules.databinding.ItemCourseHistoryBinding

class CourseHistoryAdapter : ListAdapter<Course, CourseHistoryAdapter.CourseViewHolder>(CourseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    fun updateCourses(courses: List<Course>) {
        submitList(courses)
    }

    class CourseViewHolder(private val binding: ItemCourseHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(course: Course) {
            binding.apply {
                // Statut (utilise textViewStatut du XML)
                textViewStatut.text = course.statut
                
                // Demandeur
                textViewDemandeur.text = "${course.demandeur.first_name} ${course.demandeur.last_name}"
                
                // Destination (inclut point d'embarquement et destination)
                textViewDestination.text = "${course.point_embarquement} → ${course.destination}"
                
                // Date
                val date = course.date_arrivee ?: course.date_validation ?: course.date_creation
                textViewDate.text = date ?: "Non spécifiée"
                
                // Kilométrage
                val distance = course.distance_parcourue
                textViewKilometrage.text = if (distance != null && distance > 0) {
                    "$distance km"
                } else {
                    "-- km"
                }
                
                // Couleur du statut
                val statusColor = when (course.statut.lowercase()) {
                    "terminee", "terminée" -> android.graphics.Color.parseColor("#4CAF50")
                    "en_cours", "en cours" -> android.graphics.Color.parseColor("#FF9800")
                    "validee", "validée" -> android.graphics.Color.parseColor("#2196F3")
                    "annulee", "annulée" -> android.graphics.Color.parseColor("#F44336")
                    else -> android.graphics.Color.parseColor("#757575")
                }
                textViewStatut.setTextColor(statusColor)
            }
        }
    }
}

class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
    override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
        return oldItem == newItem
    }
}
