package com.example.gestionvehicules.ui.driver

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionvehicules.data.api.ApiService
import com.example.gestionvehicules.data.api.RetrofitClient
import com.example.gestionvehicules.data.api.SessionManager
import com.example.gestionvehicules.databinding.ActivityCourseHistoryBinding
import com.example.gestionvehicules.ui.driver.adapters.CourseHistoryAdapter
import kotlinx.coroutines.launch

class CourseHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCourseHistoryBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService
    private lateinit var courseAdapter: CourseHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("CourseHistoryActivity onCreate - Début")
        
        try {
            binding = ActivityCourseHistoryBinding.inflate(layoutInflater)
            setContentView(binding.root)
            println("CourseHistoryActivity - Layout initialisé")
            
            sessionManager = SessionManager(this)
            apiService = RetrofitClient.getApiService(this)
            println("CourseHistoryActivity - SessionManager et ApiService créés")
            
            setupUI()
            setupRecyclerView()
            loadCourseHistory()
            println("CourseHistoryActivity - Initialisation terminée")
        } catch (e: Exception) {
            println("Erreur dans CourseHistoryActivity onCreate: ${e.message}")
            e.printStackTrace()
            
            Toast.makeText(this, "Erreur d'initialisation: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupUI() {
        try {
            println("CourseHistoryActivity setupUI - Début")
            
            // Configuration de la toolbar
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Mon Historique"
            
            binding.toolbar.setNavigationOnClickListener {
                finish()
            }
            
            // Configuration du SwipeRefreshLayout
            binding.swipeRefreshLayout.setOnRefreshListener {
                loadCourseHistory()
            }
            
            println("CourseHistoryActivity setupUI - Terminé")
        } catch (e: Exception) {
            println("Erreur dans setupUI: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupRecyclerView() {
        try {
            courseAdapter = CourseHistoryAdapter()
            binding.recyclerViewCourses.apply {
                layoutManager = LinearLayoutManager(this@CourseHistoryActivity)
                adapter = courseAdapter
            }
            println("CourseHistoryActivity - RecyclerView configuré")
        } catch (e: Exception) {
            println("Erreur dans setupRecyclerView: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun loadCourseHistory() {
        try {
            println("CourseHistoryActivity loadCourseHistory - Début")
            
            // Récupérer le chauffeur connecté
            val currentUser = sessionManager.currentUser
            if (currentUser == null) {
                println("CourseHistoryActivity - Aucun utilisateur connecté")
                displayEmptyState()
                return
            }
            
            println("CourseHistoryActivity - Chauffeur connecté: ${currentUser.username} (ID: ${currentUser.id})")
            
            val token = sessionManager.authToken
            if (token == null) {
                println("CourseHistoryActivity - Token null, affichage des données locales")
                displayLocalCourses()
                return
            }
            
            binding.swipeRefreshLayout.isRefreshing = true
            
            lifecycleScope.launch {
                try {
                    println("CourseHistoryActivity - Appel API getDriverCourseHistoryReal")
                    val response = apiService.getDriverCourseHistoryReal("Bearer $token")
                    
                    if (response.isSuccessful) {
                        response.body()?.let { courseResponse ->
                            courseResponse.courses?.let { courses ->
                                println("CourseHistoryActivity - ${courses.size} courses reçues de la base de données réelle")
                                displayRealCourses(courses)
                            } ?: run {
                                println("CourseHistoryActivity - Aucune course dans la réponse")
                                displayEmptyState()
                            }
                        }
                    } else {
                        println("CourseHistoryActivity - Erreur API: ${response.code()}")
                        when (response.code()) {
                            401 -> {
                                Toast.makeText(this@CourseHistoryActivity, "Session expirée", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            404 -> {
                                println("CourseHistoryActivity - API non disponible, affichage des données locales")
                                displayLocalCourses()
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                Toast.makeText(this@CourseHistoryActivity, "Erreur de chargement: ${response.code()}", Toast.LENGTH_SHORT).show()
                                displayErrorState("Erreur ${response.code()}: ${errorBody ?: "Erreur inconnue"}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("CourseHistoryActivity - Exception réseau: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(this@CourseHistoryActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                    displayErrorState("Erreur réseau: ${e.message}")
                } finally {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
            
        } catch (e: Exception) {
            println("CourseHistoryActivity - Erreur dans loadCourseHistory: ${e.message}")
            e.printStackTrace()
            displayErrorState("Erreur: ${e.message}")
        }
    }
    
    private fun displayRealCourses(courses: List<com.example.gestionvehicules.data.model.Course>) {
        try {
            println("CourseHistoryActivity - Affichage de ${courses.size} courses réelles")
            
            // Filtrer uniquement les courses terminées pour l'historique
            val completedCourses = courses.filter { it.statut == "terminee" }
            
            if (completedCourses.isEmpty()) {
                displayEmptyState()
                return
            }
            
            courseAdapter.updateCourses(completedCourses)
            
            binding.recyclerViewCourses.visibility = android.view.View.VISIBLE
            binding.textViewEmpty.visibility = android.view.View.GONE
            binding.textViewError.visibility = android.view.View.GONE
            
            println("CourseHistoryActivity - ${completedCourses.size} courses terminées affichées avec succès")
        } catch (e: Exception) {
            println("Erreur dans displayRealCourses: ${e.message}")
            e.printStackTrace()
            displayErrorState("Erreur d'affichage: ${e.message}")
        }
    }
    
    private fun displayLocalCourses() {
        try {
            println("CourseHistoryActivity - Affichage des courses locales")
            
            // Données de fallback si l'API ne fonctionne pas
            val sampleCourses = listOf(
                com.example.gestionvehicules.data.model.Course(
                    id = 1,
                    point_embarquement = "Bureau Principal",
                    destination = "Aéroport",
                    motif = "Mission professionnelle",
                    date_souhaitee = "28/11/2025 09:15",
                    statut = "terminee",
                    demandeur = com.example.gestionvehicules.data.model.User(
                        id = 1, username = "mmartin", first_name = "Martin", last_name = "Marc", email = "",
                        is_driver = false, is_requester = true
                    ),
                    vehicule = com.example.gestionvehicules.data.model.Vehicle(
                        id = 1, immatriculation = "AB-123-CD", marque = "Toyota", modele = "Corolla"
                    ),
                    date_creation = "28/11/2025 09:15",
                    distance_parcourue = 25
                ),
                com.example.gestionvehicules.data.model.Course(
                    id = 2,
                    point_embarquement = "Centre-ville",
                    destination = "Bureau",
                    motif = "Retour siège",
                    date_souhaitee = "28/11/2025 14:30",
                    statut = "terminee",
                    demandeur = com.example.gestionvehicules.data.model.User(
                        id = 2, username = "mbernard", first_name = "Bernard", last_name = "Marie", email = "",
                        is_driver = false, is_requester = true
                    ),
                    vehicule = com.example.gestionvehicules.data.model.Vehicle(
                        id = 1, immatriculation = "AB-123-CD", marque = "Toyota", modele = "Corolla"
                    ),
                    date_creation = "28/11/2025 14:30",
                    distance_parcourue = 12
                )
            )
            
            courseAdapter.updateCourses(sampleCourses)
            
            binding.recyclerViewCourses.visibility = android.view.View.VISIBLE
            binding.textViewEmpty.visibility = android.view.View.GONE
            binding.textViewError.visibility = android.view.View.GONE
            
            println("CourseHistoryActivity - Courses locales affichées")
        } catch (e: Exception) {
            println("Erreur dans displayLocalCourses: ${e.message}")
            e.printStackTrace()
            displayEmptyState()
        }
    }
    
    private fun displayEmptyState() {
        try {
            println("CourseHistoryActivity - Affichage état vide")
            binding.recyclerViewCourses.visibility = android.view.View.GONE
            binding.textViewEmpty.visibility = android.view.View.VISIBLE
            binding.textViewError.visibility = android.view.View.GONE
        } catch (e: Exception) {
            println("Erreur dans displayEmptyState: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun displayErrorState(message: String) {
        try {
            println("CourseHistoryActivity - Affichage état erreur: $message")
            binding.recyclerViewCourses.visibility = android.view.View.GONE
            binding.textViewEmpty.visibility = android.view.View.GONE
            binding.textViewError.visibility = android.view.View.VISIBLE
            binding.errorTitleText.text = message
        } catch (e: Exception) {
            println("Erreur dans displayErrorState: ${e.message}")
            e.printStackTrace()
        }
    }
}
