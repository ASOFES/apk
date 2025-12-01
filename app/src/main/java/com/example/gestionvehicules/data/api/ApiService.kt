package com.example.gestionvehicules.data.api

import com.example.gestionvehicules.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Authentification
    @FormUrlEncoded
    @POST("login-api/")  // NOUVELLE URL SANS CSRF
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @GET("profile/")
    suspend fun getProfile(): Response<UserProfile>

    @POST("profile/edit/")
    suspend fun updateProfile(@Body user: UserProfile): Response<UserProfile>

    @POST("set-language/")
    suspend fun setLanguage(@Body language: LanguageRequest): Response<Unit>

    // Véhicules
    @GET("vehicules/")
    suspend fun getVehicles(): Response<List<Vehicle>>

    @GET("vehicules/{id}/")
    suspend fun getVehicleDetails(@Path("id") vehicleId: Int): Response<Vehicle>

    @POST("vehicules/create/")
    suspend fun createVehicle(@Body vehicle: VehicleRequest): Response<Vehicle>

    @POST("vehicules/{id}/edit/")
    suspend fun updateVehicle(
        @Path("id") vehicleId: Int,
        @Body vehicle: VehicleRequest
    ): Response<Vehicle>

    @POST("vehicules/{id}/delete/")
    suspend fun deleteVehicle(@Path("id") vehicleId: Int): Response<Unit>

    // Utilisateurs
    @GET("users/")
    suspend fun getUsers(): Response<List<UserProfile>>

    @POST("users/create/")
    suspend fun createUser(@Body user: UserProfile): Response<UserProfile>

    @POST("users/{id}/edit/")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Body user: UserProfile
    ): Response<UserProfile>

    @POST("users/{id}/password-reset/")
    suspend fun resetPassword(@Path("id") userId: Int): Response<Unit>

    @POST("users/{id}/toggle-active/")
    suspend fun toggleUserActive(@Path("id") userId: Int): Response<Unit>

    @POST("users/{id}/delete/")
    suspend fun deleteUser(@Path("id") userId: Int): Response<Unit>

    @POST("users/{id}/change-departement/")
    suspend fun changeUserDepartment(
        @Path("id") userId: Int,
        @Field("department_id") departmentId: Int
    ): Response<Unit>

    // Départements
    @GET("departements/")
    suspend fun getDepartments(): Response<List<Department>>

    // Maintenance
    @GET("maintenance/")
    suspend fun getMaintenanceRecords(): Response<List<MaintenanceRecord>>

    @POST("maintenance/create/")
    suspend fun createMaintenanceRecord(@Body record: MaintenanceRecord): Response<MaintenanceRecord>

    // Missions
    @GET("missions/")
    suspend fun getMissions(): Response<List<Mission>>

    @POST("missions/create/")
    suspend fun createMission(@Body mission: Mission): Response<Mission>

    @POST("missions/{id}/complete/")
    suspend fun completeMission(@Path("id") missionId: Int): Response<Unit>

    // Courses (pour les demandeurs)
    @POST("courses/create/")
    suspend fun createCourse(
        @Header("Authorization") authorization: String,
        @Body courseRequest: CourseRequest
    ): Response<CourseResponse>

    @GET("courses/my-requests/")
    suspend fun getMyCourses(
        @Header("Authorization") authorization: String
    ): Response<CourseResponse>

    @GET("courses/history/")
    suspend fun getCourseHistory(
        @Header("Authorization") authorization: String
    ): Response<CourseResponse>

    // API endpoints pour chauffeurs
    @GET("api/driver/assigned-course/")
    suspend fun getAssignedCourse(
        @Header("Authorization") authorization: String
    ): Response<CourseResponse>

    @GET("api/driver/course-history-real/")
    suspend fun getDriverCourseHistoryReal(
        @Header("Authorization") authorization: String
    ): Response<CourseResponse>

    @FormUrlEncoded
    @POST("api/driver/course/{courseId}/start/")
    suspend fun startCourse(
        @Header("Authorization") authorization: String,
        @Path("courseId") courseId: Int,
        @Field("kilometrage") kilometrage: Int
    ): Response<CourseResponse>

    @FormUrlEncoded
    @POST("api/driver/course/{courseId}/complete/")
    suspend fun completeCourse(
        @Header("Authorization") authorization: String,
        @Path("courseId") courseId: Int,
        @Field("kilometrage") kilometrage: Int
    ): Response<CourseResponse>

    // API endpoints pour ravitaillement et entretien (chauffeur)
    @GET("api/driver/fuel/")
    suspend fun getFuelList(
        @Header("Authorization") authorization: String
    ): Response<FuelResponse>

    @FormUrlEncoded
    @POST("api/driver/fuel/create/")
    suspend fun createFuel(
        @Header("Authorization") authorization: String,
        @Field("vehicule_id") vehicleId: String,
        @Field("station_id") stationId: String?,
        @Field("nom_station") stationName: String,
        @Field("kilometrage_avant") kmAvant: String,
        @Field("kilometrage_apres") kmApres: String,
        @Field("litres") litres: String,
        @Field("cout_unitaire") coutUnitaire: String
    ): Response<CreateResponse>

    @GET("api/driver/maintenance/")
    suspend fun getMaintenanceList(
        @Header("Authorization") authorization: String
    ): Response<MaintenanceResponse>

    @FormUrlEncoded
    @POST("api/driver/maintenance/create/")
    suspend fun createMaintenance(
        @Header("Authorization") authorization: String,
        @Field("vehicule_id") vehicleId: String,
        @Field("type_entretien") typeEntretien: String,
        @Field("date_entretien") dateEntretien: String,
        @Field("garage") garage: String,
        @Field("motif") motif: String,
        @Field("cout") cout: String,
        @Field("kilometrage") kilometrage: String
    ): Response<CreateResponse>

    @GET("api/driver/vehicles/")
    suspend fun getVehiclesList(
        @Header("Authorization") authorization: String
    ): Response<VehiclesResponse>

    @GET("api/driver/stations/")
    suspend fun getStationsList(
        @Header("Authorization") authorization: String
    ): Response<StationsResponse>

    // API endpoints pour ravitaillement (accessible par chauffeur et dispatch)
    @GET("api/ravitaillement/")
    suspend fun getRavitaillements(
        @Header("Authorization") authorization: String
    ): Response<List<Ravitaillement>>

    @POST("api/ravitaillement/create/")
    suspend fun createRavitaillement(
        @Header("Authorization") authorization: String,
        @Body ravitaillementRequest: RavitaillementRequest
    ): Response<Ravitaillement>

    // API endpoints pour entretien (accessible par chauffeur et dispatch)
    @GET("api/entretien/")
    suspend fun getEntretiens(
        @Header("Authorization") authorization: String
    ): Response<List<Entretien>>

    @POST("api/entretien/create/")
    suspend fun createEntretien(
        @Header("Authorization") authorization: String,
        @Body entretienRequest: EntretienRequest
    ): Response<Entretien>
}
