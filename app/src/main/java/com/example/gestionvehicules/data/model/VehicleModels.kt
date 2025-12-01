package com.example.gestionvehicules.data.model

// NOTE: La classe Vehicle est maintenant définie dans UnifiedModels.kt pour éviter les conflits

data class VehicleRequest(
    val registration_number: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuel_type: String,
    val department_id: Int
)

data class Department(
    val id: Int,
    val name: String,
    val code: String
)

data class MaintenanceRecord(
    val id: Int,
    val vehicle: Vehicle,
    val date: String,
    val type: String,
    val description: String,
    val cost: Double,
    val mechanic: String
)

data class Mission(
    val id: Int,
    val vehicle: Vehicle,
    val driver: UserProfile,
    val start_location: String,
    val end_location: String,
    val start_date: String,
    val end_date: String?,
    val status: String,
    val distance: Double?,
    val fuel_consumed: Double?
)
