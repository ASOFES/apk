package com.example.gestionvehicules.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User,
    val userType: String // "chauffeur" ou "demandeur"
)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val is_driver: Boolean,
    val is_requester: Boolean,
    val is_dispatcher: Boolean = false,
    val userType: String? = null // "chauffeur" ou "demandeur"
)

data class UserProfile(
    val id: Int,
    val username: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val is_driver: Boolean,
    val is_requester: Boolean,
    val department: String?,
    val phone: String?
)

data class LanguageRequest(
    val language: String
)

data class CourseRequest(
    val point_embarquement: String,
    val destination: String,
    val motif: String,
    val nombre_passagers: Int,
    val date_souhaitee: String,
    val observations: String? = null
)

data class Course(
    val id: Int,
    val point_embarquement: String,
    val destination: String,
    val motif: String,
    val date_souhaitee: String,
    val statut: String,
    val demandeur: User,
    val chauffeur: User? = null,
    val vehicule: Vehicle? = null,
    val observations: String? = null,
    val date_creation: String? = null,
    val date_validation: String? = null,
    val date_depart: String? = null,
    val date_arrivee: String? = null,
    val distance_parcourue: Int? = null
)

data class CourseResponse(
    val course: Course? = null,
    val courses: List<Course>? = null,
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class RavitaillementRequest(
    val vehicule_id: Int,
    val station_id: Int? = null,
    val nom_station: String? = null,
    val kilometrage_avant: Int,
    val kilometrage_apres: Int,
    val litres: Double,
    val cout_unitaire: Double,
    val commentaires: String? = null
)

data class Ravitaillement(
    val id: Int,
    val vehicule: Vehicle,
    val date_ravitaillement: String,
    val station: Station? = null,
    val nom_station: String,
    val kilometrage_avant: Int,
    val kilometrage_apres: Int,
    val litres: Double,
    val cout_unitaire: Double,
    val cout_total: Double,
    val createur: User,
    val chauffeur: User? = null,
    val commentaires: String? = null,
    val consommation_moyenne_reelle: Float? = null
)

// Renommé pour éviter les conflits
data class StationInfo(
    val id: Int,
    val nom: String,
    val adresse: String? = null,
    val ville: String? = null,
    val telephone: String? = null
)

data class EntretienRequest(
    val vehicule_id: Int,
    val type_entretien: String,
    val garage: String,
    val date_entretien: String,
    val motif: String,
    val cout: Double,
    val kilometrage: Int,
    val kilometrage_apres: Int,
    val commentaires: String? = null
)

data class Entretien(
    val id: Int,
    val vehicule: Vehicle,
    val type_entretien: String,
    val garage: String,
    val date_entretien: String,
    val statut: String,
    val motif: String,
    val cout: Double,
    val kilometrage: Int,
    val kilometrage_apres: Int,
    val createur: User,
    val commentaires: String? = null,
    val date_creation: String
)
