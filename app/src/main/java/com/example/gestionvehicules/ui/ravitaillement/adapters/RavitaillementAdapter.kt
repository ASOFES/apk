package com.example.gestionvehicules.ui.ravitaillement.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionvehicules.data.model.FuelItem
import com.example.gestionvehicules.databinding.ItemRavitaillementBinding

class RavitaillementAdapter : ListAdapter<FuelItem, RavitaillementAdapter.RavitaillementViewHolder>(RavitaillementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RavitaillementViewHolder {
        val binding = ItemRavitaillementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RavitaillementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RavitaillementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    fun updateData(data: List<FuelItem>) {
        submitList(data)
    }

    class RavitaillementViewHolder(private val binding: ItemRavitaillementBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: FuelItem) {
            binding.apply {
                // Véhicule
                textViewVehicle.text = "Véhicule: ${item.vehicule}"
                
                // Station
                textViewStation.text = "Station: ${item.station}"
                
                // Date
                textViewDate.text = "Date: ${item.date_ravitaillement}"
                
                // Kilométrage
                textViewKilometrage.text = "Kilométrage: ${item.kilometrage_avant} → ${item.kilometrage_apres} km"
                
                // Quantité et coût
                textViewQuantite.text = "Quantité: ${item.litres} L"
                textViewCout.text = "Coût: ${item.cout_total} $"
                
                // Consommation moyenne
                val consommation = item.consommation
                textViewConsommation.text = if (consommation != null) {
                    "Consommation: ${"%.2f".format(consommation)} L/100km"
                } else {
                    "Consommation: Non calculée"
                }
                
                // Cacher les commentaires pour l'instant
                textViewCommentaires.visibility = android.view.View.GONE
            }
        }
    }
}

class RavitaillementDiffCallback : DiffUtil.ItemCallback<FuelItem>() {
    override fun areItemsTheSame(oldItem: FuelItem, newItem: FuelItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FuelItem, newItem: FuelItem): Boolean {
        return oldItem == newItem
    }
}
