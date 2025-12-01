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
                // Statut
                textViewStatus.text = "Statut: ${course.statut}"
                
                // Trajets
                textViewFrom.text = "De: ${course.point_embarquement}"
                textViewTo.text = "Vers: ${course.destination}"
                
                // Motif
                textViewMotif.text = "Motif: ${course.motif}"
                
                // Demandeur
                textViewRequester.text = "Demandeur: ${course.demandeur.first_name} ${course.demandeur.last_name}"
                
                // Véhicule
                val vehicle = course.vehicule
                textViewVehicle.text = if (vehicle != null) {
                    "Véhicule: ${vehicle.getImmatriculationDisplay()} - ${vehicle.getMarqueDisplay()} ${vehicle.getModeleDisplay()}"
                } else {
                    "Véhicule: Non assigné"
                }
                
                // Date
                val date = course.date_arrivee ?: course.date_validation ?: course.date_creation
                textViewDate.text = "Date: ${date ?: "Non spécifiée"}"
                
                // Distance
                val distance = course.distance_parcourue
                textViewDistance.text = if (distance != null) {
                    "Distance: ${distance} km"
                } else {
                    "Distance: Non spécifiée"
                }
                
                // Couleur du statut
                val statusColor = when (course.statut) {
                    "terminee" -> android.graphics.Color.parseColor("#4CAF50")
                    "en_cours" -> android.graphics.Color.parseColor("#FF9800")
                    "validee" -> android.graphics.Color.parseColor("#2196F3")
                    "annulee" -> android.graphics.Color.parseColor("#F44336")
                    else -> android.graphics.Color.parseColor("#757575")
                }
                textViewStatus.setTextColor(statusColor)
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
