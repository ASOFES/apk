package com.example.gestionvehicules.ui.entretien.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionvehicules.data.model.MaintenanceItem
import com.example.gestionvehicules.databinding.ItemEntretienBinding

class EntretienAdapter : ListAdapter<MaintenanceItem, EntretienAdapter.EntretienViewHolder>(EntretienDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntretienViewHolder {
        val binding = ItemEntretienBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EntretienViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntretienViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    fun updateData(data: List<MaintenanceItem>) {
        submitList(data)
    }

    class EntretienViewHolder(private val binding: ItemEntretienBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: MaintenanceItem) {
            binding.apply {
                // Véhicule
                textViewVehicle.text = "Véhicule: ${item.vehicule}"
                
                // Type et statut
                textViewType.text = "Type: ${item.type_entretien}"
                textViewStatus.text = "Statut: ${item.statut}"
                
                // Garage
                textViewGarage.text = "Garage: ${item.garage}"
                
                // Date
                textViewDate.text = "Date: ${item.date_entretien}"
                
                // Kilométrage
                textViewKilometrage.text = "Kilométrage: ${item.kilometrage} km"
                
                // Coût
                textViewCout.text = "Coût: ${item.cout} $"
                
                // Motif
                textViewMotif.text = "Motif: ${item.motif}"
                
                // Cacher les commentaires pour l'instant
                textViewCommentaires.visibility = android.view.View.GONE
                
                // Couleur du statut
                val statusColor = when (item.statut.lowercase()) {
                    "terminé" -> android.graphics.Color.parseColor("#4CAF50")
                    "en cours" -> android.graphics.Color.parseColor("#FF9800")
                    "planifié" -> android.graphics.Color.parseColor("#2196F3")
                    "annulé" -> android.graphics.Color.parseColor("#F44336")
                    else -> android.graphics.Color.parseColor("#757575")
                }
                textViewStatus.setTextColor(statusColor)
            }
        }
    }
}

class EntretienDiffCallback : DiffUtil.ItemCallback<MaintenanceItem>() {
    override fun areItemsTheSame(oldItem: MaintenanceItem, newItem: MaintenanceItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MaintenanceItem, newItem: MaintenanceItem): Boolean {
        return oldItem == newItem
    }
}
