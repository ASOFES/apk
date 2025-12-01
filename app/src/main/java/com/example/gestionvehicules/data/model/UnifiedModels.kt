package com.example.gestionvehicules.data.model

// Modèle Vehicle unifié pour compatibilité entre les deux systèmes
data class Vehicle(
    val id: Int,
    val immatriculation: String = "",
    val marque: String = "",
    val modele: String = "",
    val kilometrage_actuel: Int? = null,
    val consommation_moyenne: Float? = null,
    // Champs pour compatibilité avec VehicleModels
    val registration_number: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val status: String? = null,
    val current_driver: UserProfile? = null,
    val department: Department? = null,
    val fuel_type: String? = null,
    val mileage: Int? = null,
    val last_maintenance: String? = null,
    val next_maintenance: String? = null
) {
    // Helper pour obtenir l'immatriculation depuis le bon champ
    fun getImmatriculationDisplay(): String {
        return immatriculation.ifEmpty { registration_number ?: "" }
    }
    
    // Helper pour obtenir la marque depuis le bon champ
    fun getMarqueDisplay(): String {
        return marque.ifEmpty { brand ?: "" }
    }
    
    // Helper pour obtenir le modèle depuis le bon champ
    fun getModeleDisplay(): String {
        return modele.ifEmpty { model ?: "" }
    }
}

// Modèles pour les nouvelles API endpoints
data class FuelItem(
    val id: Int,
    val vehicule: String,
    val date_ravitaillement: String,
    val station: String,
    val kilometrage_avant: Int,
    val kilometrage_apres: Int,
    val litres: Float,
    val cout_total: Float,
    val consommation: Float?
)

data class FuelResponse(
    val success: Boolean,
    val data: List<FuelItem>
)

data class MaintenanceItem(
    val id: Int,
    val vehicule: String,
    val date_entretien: String,
    val type_entretien: String,
    val statut: String,
    val garage: String,
    val motif: String,
    val cout: Float,
    val kilometrage: Int
)

data class MaintenanceResponse(
    val success: Boolean,
    val data: List<MaintenanceItem>
)

data class Station(
    val id: Int,
    val nom: String,
    val adresse: String,
    val ville: String
)

data class StationsResponse(
    val success: Boolean,
    val data: List<Station>
)

data class VehiclesResponse(
    val success: Boolean,
    val data: List<Vehicle>
)

data class CreateResponse(
    val success: Boolean,
    val message: String,
    val id: Int
)
