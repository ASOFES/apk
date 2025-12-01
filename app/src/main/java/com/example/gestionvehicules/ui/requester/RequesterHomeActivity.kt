package com.example.gestionvehicules.ui.requester

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionvehicules.data.api.SessionManager
import com.example.gestionvehicules.databinding.ActivityRequesterHomeBinding
import com.example.gestionvehicules.ui.auth.LoginActivity
import com.example.gestionvehicules.ui.requester.NewRequestActivity

class RequesterHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRequesterHomeBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequesterHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        val userId = intent.getIntExtra("USER_ID", -1)
        
        setupUI()
        setupClickListeners()
        loadUserInfo()
    }

    private fun setupUI() {
        // Configuration de la toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        
        // Afficher les informations de l'utilisateur
        loadUserInfo()
    }
    
    private fun loadUserInfo() {
        try {
            val currentUser = sessionManager.currentUser
            if (currentUser != null) {
                binding.tvUserName.text = "${currentUser.first_name} ${currentUser.last_name}"
            } else {
                binding.tvUserName.text = "Utilisateur inconnu"
                // Ne pas déconnecter automatiquement
            }
        } catch (e: Exception) {
            println("Erreur lors du chargement des infos utilisateur: ${e.message}")
            binding.tvUserName.text = "Erreur de chargement"
            // Ne pas déconnecter automatiquement
        }
    }
    
    private fun setupClickListeners() {
        // Nouvelle demande
        binding.btnNewRequest.setOnClickListener {
            startActivity(Intent(this, NewRequestActivity::class.java))
        }
        
        // Mes demandes
        binding.btnMyRequests.setOnClickListener {
            Toast.makeText(this, "Mes demandes - Bientôt disponible", Toast.LENGTH_SHORT).show()
            // TODO: Implémenter l'activité des demandes
        }
        
        // Historique
        binding.btnHistory.setOnClickListener {
            Toast.makeText(this, "Historique - Bientôt disponible", Toast.LENGTH_SHORT).show()
            // TODO: Implémenter l'activité d'historique
        }
        
        // Profil
        binding.btnProfile.setOnClickListener {
            Toast.makeText(this, "Profil - Bientôt disponible", Toast.LENGTH_SHORT).show()
            // TODO: Implémenter l'activité de profil
        }
        
        // Déconnexion
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }
    
    private fun logout() {
        sessionManager.clearSession()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
    
    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }
}
