package com.example.gestionvehicules.ui.driver

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gestionvehicules.data.api.ApiService
import com.example.gestionvehicules.data.api.RetrofitClient
import com.example.gestionvehicules.data.api.SessionManager
import com.example.gestionvehicules.databinding.ActivityDriverHomeEnhancedBinding
import com.example.gestionvehicules.ui.auth.LoginActivity
import com.example.gestionvehicules.ui.driver.CourseHistoryActivity
import com.example.gestionvehicules.ui.ravitaillement.RavitaillementActivity
import com.example.gestionvehicules.ui.entretien.EntretienActivity
import kotlinx.coroutines.launch

class DriverHomeEnhancedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDriverHomeEnhancedBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService
    private var currentCourse: com.example.gestionvehicules.data.model.Course? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("DriverHomeEnhancedActivity onCreate - Début")
        
        try {
            binding = ActivityDriverHomeEnhancedBinding.inflate(layoutInflater)
            setContentView(binding.root)
            println("DriverHomeEnhancedActivity - Layout initialisé")
            
            sessionManager = SessionManager(this)
            apiService = RetrofitClient.getApiService(this)
            println("DriverHomeEnhancedActivity - SessionManager et ApiService créés")
            
            setupUI()
            setupClickListeners()
            loadUserInfo()
            loadAssignedCourse()
            println("DriverHomeEnhancedActivity - Initialisation terminée")
        } catch (e: Exception) {
            println("Erreur dans DriverHomeEnhancedActivity onCreate: ${e.message}")
            e.printStackTrace()
            
            // En cas d'erreur, afficher un message et terminer
            Toast.makeText(this, "Erreur d'initialisation: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupUI() {
        try {
            println("DriverHomeEnhancedActivity setupUI - Début")
            
            // Configuration de la toolbar
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            
            // Afficher les informations de l'utilisateur
            loadUserInfo()
            println("DriverHomeEnhancedActivity setupUI - Terminé")
        } catch (e: Exception) {
            println("Erreur dans setupUI: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun loadUserInfo() {
        try {
            println("DriverHomeEnhancedActivity loadUserInfo - Début")
            val currentUser = sessionManager.currentUser
            if (currentUser != null) {
                binding.tvUserName.text = "${currentUser.first_name} ${currentUser.last_name}"
                println("DriverHomeEnhancedActivity - Utilisateur chargé: ${currentUser.first_name}")
            } else {
                binding.tvUserName.text = "Utilisateur inconnu"
                println("DriverHomeEnhancedActivity - Utilisateur null")
                // Ne pas déconnecter automatiquement - laisser l'utilisateur continuer
            }
        } catch (e: Exception) {
            println("Erreur lors du chargement des infos utilisateur: ${e.message}")
            e.printStackTrace()
            binding.tvUserName.text = "Erreur de chargement"
            // Ne pas déconnecter automatiquement pour éviter les bugs
        }
    }
    
    private fun loadAssignedCourse() {
        val token = sessionManager.authToken
        if (token == null) {
            // Pas de token, ne pas déconnecter automatiquement
            showNoCourse()
            return
        }
        
        binding.progressAssignedCourse.visibility = android.view.View.VISIBLE
        binding.layoutAssignedCourse.visibility = android.view.View.GONE
        binding.tvNoCourse.visibility = android.view.View.GONE
        
        lifecycleScope.launch {
            try {
                val response = apiService.getAssignedCourse("Bearer $token")
                
                if (response.isSuccessful) {
                    response.body()?.let { courseResponse ->
                        currentCourse = courseResponse.course
                        if (courseResponse.course != null) {
                            displayAssignedCourse(courseResponse.course)
                        } else {
                            showNoCourse()
                        }
                    }
                } else {
                    // Ne pas déconnecter automatiquement pour erreur 401
                    showNoCourse()
                    Toast.makeText(this@DriverHomeEnhancedActivity, "Erreur de chargement: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                showNoCourse()
                Toast.makeText(this@DriverHomeEnhancedActivity, "Erreur réseau", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressAssignedCourse.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun displayAssignedCourse(course: com.example.gestionvehicules.data.model.Course) {
        binding.layoutAssignedCourse.visibility = android.view.View.VISIBLE
        binding.tvNoCourse.visibility = android.view.View.GONE
        
        binding.tvCourseStatus.text = "Statut: ${course.statut}"
        binding.tvCourseFrom.text = "De: ${course.point_embarquement}"
        binding.tvCourseTo.text = "Vers: ${course.destination}"
        binding.tvCourseMotif.text = "Motif: ${course.motif}"
        
        val vehicle = course.vehicule
        binding.tvCourseVehicle.text = if (vehicle != null) {
            "Véhicule: ${vehicle.getImmatriculationDisplay()} - ${vehicle.getMarqueDisplay()} ${vehicle.getModeleDisplay()}"
        } else {
            "Véhicule: Non assigné"
        }
        
        // Afficher les boutons selon le statut
        when (course.statut) {
            "validee" -> {
                binding.btnStartCourse.visibility = android.view.View.VISIBLE
                binding.btnCompleteCourse.visibility = android.view.View.GONE
            }
            "en_cours" -> {
                binding.btnStartCourse.visibility = android.view.View.GONE
                binding.btnCompleteCourse.visibility = android.view.View.VISIBLE
            }
            else -> {
                binding.btnStartCourse.visibility = android.view.View.GONE
                binding.btnCompleteCourse.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun showNoCourse() {
        binding.layoutAssignedCourse.visibility = android.view.View.GONE
        binding.tvNoCourse.visibility = android.view.View.VISIBLE
        binding.btnStartCourse.visibility = android.view.View.GONE
        binding.btnCompleteCourse.visibility = android.view.View.GONE
    }
    
    private fun setupClickListeners() {
        try {
            println("DriverHomeEnhancedActivity setupClickListeners - Début")
            
            // Démarrer la course
            binding.btnStartCourse.setOnClickListener {
                try {
                    println("Clic sur btnStartCourse")
                    currentCourse?.let { course ->
                        showStartCourseDialog(course)
                    } ?: run {
                        Toast.makeText(this, "Aucune course assignée", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    println("Erreur dans btnStartCourse: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(this, "Erreur lors du démarrage: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Terminer la course
            binding.btnCompleteCourse.setOnClickListener {
                try {
                    println("Clic sur btnCompleteCourse")
                    currentCourse?.let { course ->
                        showCompleteCourseDialog(course)
                    } ?: run {
                        Toast.makeText(this, "Aucune course en cours", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    println("Erreur dans btnCompleteCourse: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(this, "Erreur lors de la terminaison: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Historique des courses
            binding.btnCourseHistory.setOnClickListener {
                try {
                    println("Clic sur btnCourseHistory")
                    val intent = Intent(this, CourseHistoryActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    println("Erreur dans btnCourseHistory: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(this, "Erreur lors de l'ouverture de l'historique", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Ravitaillement
            binding.btnRavitaillement.setOnClickListener {
                try {
                    println("Clic sur btnRavitaillement")
                    val intent = Intent(this, RavitaillementActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    println("Erreur dans btnRavitaillement: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(this, "Erreur lors de l'ouverture du ravitaillement", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Entretien
            binding.btnEntretien.setOnClickListener {
                try {
                    println("Clic sur btnEntretien")
                    val intent = Intent(this, EntretienActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    println("Erreur dans btnEntretien: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(this, "Erreur lors de l'ouverture de l'entretien", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Profil
            binding.btnProfile.setOnClickListener {
                try {
                    println("Clic sur btnProfile")
                    Toast.makeText(this, "Profil - Bientôt disponible", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    println("Erreur dans btnProfile: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            // Déconnexion
            binding.btnLogout.setOnClickListener {
                try {
                    println("Clic sur btnLogout")
                    logout()
                } catch (e: Exception) {
                    println("Erreur dans btnLogout: ${e.message}")
                    e.printStackTrace()
                    // Forcer la déconnexion même en cas d'erreur
                    try {
                        sessionManager.clearSession()
                        startActivity(Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                    } catch (e2: Exception) {
                        println("Erreur même dans logout forcé: ${e2.message}")
                    }
                }
            }
            
            println("DriverHomeEnhancedActivity setupClickListeners - Terminé")
        } catch (e: Exception) {
            println("Erreur dans setupClickListeners: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun showStartCourseDialog(course: com.example.gestionvehicules.data.model.Course) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Démarrer la course")
        
        // Créer un layout pour le dialogue avec un champ de saisie
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)
        
        val message = android.widget.TextView(this)
        message.text = "Voulez-vous vraiment démarrer cette course?\n\nDe: ${course.point_embarquement}\nVers: ${course.destination}\n\nVeuillez saisir le kilométrage actuel:"
        layout.addView(message)
        
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "Kilométrage (ex: ${course.distance_parcourue ?: 0})"
        if (course.distance_parcourue != null && course.distance_parcourue > 0) {
             input.setText(course.distance_parcourue.toString())
        }
        layout.addView(input)
        
        builder.setView(layout)
        
        builder.setPositiveButton("Démarrer") { _, _ ->
            val kmText = input.text.toString()
            val kilometrage = if (kmText.isNotEmpty()) kmText.toInt() else 0
            startCourse(course.id, kilometrage)
        }
        
        builder.setNegativeButton("Annuler", null)
        builder.show()
    }
    
    private fun showCompleteCourseDialog(course: com.example.gestionvehicules.data.model.Course) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Terminer la course")
        
        // Créer un layout pour le dialogue avec un champ de saisie
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)
        
        val message = android.widget.TextView(this)
        message.text = "Voulez-vous vraiment terminer cette course?\n\nDe: ${course.point_embarquement}\nVers: ${course.destination}\n\nVeuillez saisir le kilométrage d'arrivée:"
        layout.addView(message)
        
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "Kilométrage (ex: ${(course.distance_parcourue ?: 0) + 10})"
        if (course.distance_parcourue != null && course.distance_parcourue > 0) {
             // Si on a déjà une valeur (ce qui ne devrait pas arriver pour une fin, mais bon)
        }
        layout.addView(input)
        
        builder.setView(layout)
        
        builder.setPositiveButton("Terminer") { _, _ ->
            val kmText = input.text.toString()
            val kilometrage = if (kmText.isNotEmpty()) kmText.toInt() else 0
            completeCourse(course.id)
        }
        
        builder.setNegativeButton("Annuler", null)
        builder.show()
    }
    
    private fun startCourse(courseId: Int, kilometrage: Int) {
        val token = sessionManager.authToken ?: return
        
        lifecycleScope.launch {
            try {
                val response = apiService.startCourse("Bearer $token", courseId, kilometrage)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@DriverHomeEnhancedActivity, "Course démarrée à $kilometrage km!", Toast.LENGTH_SHORT).show()
                    loadAssignedCourse() // Recharger pour voir le nouveau statut
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@DriverHomeEnhancedActivity, "Erreur: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DriverHomeEnhancedActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun completeCourse(courseId: Int) {
        val token = sessionManager.authToken ?: return
        
        lifecycleScope.launch {
            try {
                val response = apiService.completeCourse("Bearer $token", courseId)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@DriverHomeEnhancedActivity, "Course terminée!", Toast.LENGTH_SHORT).show()
                    loadAssignedCourse() // Recharger pour voir le nouveau statut
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@DriverHomeEnhancedActivity, "Erreur: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DriverHomeEnhancedActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun logout() {
        try {
            sessionManager.clearSession()
            Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show()
            
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        } catch (e: Exception) {
            println("Erreur lors de la déconnexion: ${e.message}")
            // Forcer la déconnexion même en cas d'erreur
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Simplifié - ne vérifie pas la session à chaque onResume pour éviter les déconnexions
        loadUserInfo()
        loadAssignedCourse()
    }
}
