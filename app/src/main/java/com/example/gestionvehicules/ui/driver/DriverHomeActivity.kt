package com.example.gestionvehicules.ui.driver

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionvehicules.data.api.ApiService
import com.example.gestionvehicules.data.api.RetrofitClient
import com.example.gestionvehicules.databinding.ActivityDriverHomeBinding
import com.example.gestionvehicules.ui.adapters.CourseHistoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET

class DriverHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDriverHomeBinding
    private var userId: Int = -1
    private lateinit var apiService: ApiService
    private lateinit var courseHistoryAdapter: CourseHistoryAdapter

    // Interface pour l'API d'historique
    interface DriverApiService {
        @GET("/driver/course-history/")
        fun getCourseHistory(): Call<DriverCourseResponse>
    }

    data class DriverCourseResponse(
        val success: Boolean,
        val courses: List<CourseHistoryAdapter.DriverCourse>?,
        val message: String?,
        val error: String?
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", -1)
        
        // Initialiser Retrofit
        apiService = com.example.gestionvehicules.data.api.RetrofitClient.getInstance().create(ApiService::class.java)
        
        setupUI()
    }

    private fun setupUI() {
        // Configuration du RecyclerView pour l'historique
        courseHistoryAdapter = CourseHistoryAdapter { course ->
            // Action quand on clique sur une course
            Toast.makeText(this, "Course #${course.id} - ${course.demandeur}", Toast.LENGTH_SHORT).show()
        }
        
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(this@DriverHomeActivity)
            adapter = courseHistoryAdapter
        }

        // Bouton pour rafraîchir
        binding.buttonRefresh.setOnClickListener {
            loadAssignedCourse()
            loadCourseHistory()
        }

        // Navigation vers les autres fonctionnalités
        binding.buttonFuel.setOnClickListener {
            startActivity(Intent(this, com.example.gestionvehicules.ui.ravitaillement.RavitaillementActivity::class.java))
        }

        binding.buttonMaintenance.setOnClickListener {
            startActivity(Intent(this, com.example.gestionvehicules.ui.entretien.EntretienActivity::class.java))
        }

        // Charger les données initiales
        loadAssignedCourse()
        loadCourseHistory()
    }

    private fun loadAssignedCourse() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        // Pour l'instant, afficher un message simple
        binding.textViewCurrentCourse.text = "Aucune course assignée"
        binding.progressBar.visibility = android.view.View.GONE
    }

    private fun loadCourseHistory() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        // Utiliser l'API driver/course-history/
        val driverApiService = com.example.gestionvehicules.data.api.RetrofitClient.getInstance().create(DriverApiService::class.java)
        
        driverApiService.getCourseHistory().enqueue(object : Callback<DriverCourseResponse> {
            override fun onResponse(call: Call<DriverCourseResponse>, response: Response<DriverCourseResponse>) {
                binding.progressBar.visibility = android.view.View.GONE
                
                if (response.isSuccessful) {
                    response.body()?.let { courseResponse ->
                        if (courseResponse.success) {
                            courseResponse.courses?.let { courses ->
                                courseHistoryAdapter.submitList(courses)
                                binding.textViewHistoryCount.text = "Historique: ${courses.size} courses"
                                
                                // Afficher la première course comme course actuelle si elle est en cours
                                courses.firstOrNull { course -> course.statut.contains("en cours", ignoreCase = true) }?.let { currentCourse ->
                                    binding.textViewCurrentCourse.text = """
                                        Course en cours:
                                        ${currentCourse.demandeur} → ${currentCourse.destination}
                                        Date: ${currentCourse.date}
                                        Statut: ${currentCourse.statut}
                                    """.trimIndent()
                                }
                            } ?: run {
                                courseHistoryAdapter.submitList(emptyList())
                                binding.textViewHistoryCount.text = "Historique: 0 course"
                            }
                        } else {
                            binding.textViewHistoryCount.text = "Erreur: ${courseResponse.error}"
                            Toast.makeText(this@DriverHomeActivity, courseResponse.error ?: "Erreur", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    binding.textViewHistoryCount.text = "Erreur serveur: ${response.code()}"
                    Toast.makeText(this@DriverHomeActivity, "Erreur serveur: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DriverCourseResponse>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.textViewHistoryCount.text = "Erreur réseau"
                Toast.makeText(this@DriverHomeActivity, "Erreur réseau: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
