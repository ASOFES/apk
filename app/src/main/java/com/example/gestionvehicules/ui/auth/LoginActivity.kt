package com.example.gestionvehicules.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gestionvehicules.data.api.SessionManager
import com.example.gestionvehicules.data.model.User
import com.example.gestionvehicules.databinding.ActivityLoginBinding
import com.example.gestionvehicules.ui.driver.DriverHomeEnhancedActivity
import com.example.gestionvehicules.ui.requester.RequesterHomeActivity
import com.example.gestionvehicules.ui.settings.ApiConfigActivity
import com.example.gestionvehicules.ui.settings.ApiTestActivity
import com.example.gestionvehicules.ui.settings.DiagnosticActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialiser le ViewModel manuellement
        viewModel = AuthViewModel()

        // Désactiver la vérification automatique pour éviter les déconnexions
        // L'utilisateur doit se connecter manuellement
        // checkIfAlreadyLoggedIn() // DÉSACTIVÉ

        setupClickListeners()
        observeLoginState()
    }

    private fun checkIfAlreadyLoggedIn() {
        try {
            val sessionManager = SessionManager(this)
            val isLoggedIn = sessionManager.isLoggedIn
            val currentUser = sessionManager.currentUser
            val userType = sessionManager.userType

            if (isLoggedIn && currentUser != null && userType != null) {
                when (userType) {
                    "chauffeur" -> {
                        navigateToDriverHome(currentUser)
                    }
                    "demandeur" -> {
                        navigateToRequesterHome(currentUser)
                    }
                    "dispatch" -> {
                        navigateToDispatcherHome(currentUser)
                    }
                    else -> {
                        // Continuer vers l'écran de login
                    }
                }
            }
        } catch (e: Exception) {
            println("Erreur dans checkIfAlreadyLoggedIn: ${e.message}")
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            when {
                username.isEmpty() -> {
                    binding.etEmail.error = "Veuillez entrer votre nom d'utilisateur"
                    binding.etEmail.requestFocus()
                }
                password.isEmpty() -> {
                    binding.etPassword.error = "Veuillez entrer votre mot de passe"
                    binding.etPassword.requestFocus()
                }
                else -> {
                    viewModel.login(username, password)
                }
            }
        }

        binding.tvApiConfig.setOnClickListener {
            startActivity(Intent(this, ApiConfigActivity::class.java))
        }

        // Ajout du bouton de test API
        binding.tvTestApi.setOnClickListener {
            startActivity(Intent(this, ApiTestActivity::class.java))
        }

        // Ajout du bouton de diagnostic complet
        binding.tvDiagnostic.setOnClickListener {
            startActivity(Intent(this, DiagnosticActivity::class.java))
        }
    }

    private fun observeLoginState() {
        lifecycleScope.launch {
            viewModel.loginState.collectLatest { state ->
                when (state) {
                    is AuthViewModel.LoginState.Loading -> showLoading(true)
                    is AuthViewModel.LoginState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    is AuthViewModel.LoginState.Success.Driver -> {
                        showLoading(false)
                        navigateToDriverHome(state.user)
                    }
                    is AuthViewModel.LoginState.Success.Requester -> {
                        showLoading(false)
                        navigateToRequesterHome(state.user)
                    }
                    is AuthViewModel.LoginState.Success.Dispatcher -> {
                        showLoading(false)
                        navigateToDispatcherHome(state.user)
                    }
                    else -> showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToDriverHome(user: com.example.gestionvehicules.data.model.User) {
        try {
            println("Tentative de navigation vers DriverHomeEnhancedActivity")
            println("User: ${user.first_name} ${user.last_name}, Type: ${user.userType}")
            
            val intent = Intent(this, DriverHomeEnhancedActivity::class.java).apply {
                putExtra("USER_ID", user.id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            startActivity(intent)
            finish()
            println("Navigation vers DriverHomeEnhancedActivity réussie")
        } catch (e: Exception) {
            println("Erreur lors de la navigation vers DriverHomeEnhancedActivity: ${e.message}")
            e.printStackTrace()
            
            // En cas d'erreur, essayer avec l'ancienne activité
            try {
                val intent = Intent(this, com.example.gestionvehicules.ui.driver.DriverHomeActivity::class.java).apply {
                    putExtra("USER_ID", user.id)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
                println("Navigation vers DriverHomeActivity réussie (fallback)")
            } catch (e2: Exception) {
                println("Erreur même avec fallback: ${e2.message}")
                showError("Erreur de navigation: ${e.message}")
            }
        }
    }

    private fun navigateToRequesterHome(user: com.example.gestionvehicules.data.model.User) {
        val intent = Intent(this, RequesterHomeActivity::class.java).apply {
            putExtra("USER_ID", user.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToDispatcherHome(user: com.example.gestionvehicules.data.model.User) {
        // TODO: Créer DispatcherHomeActivity
        Toast.makeText(this, "Interface Dispatch - Bientôt disponible", Toast.LENGTH_LONG).show()
        // Pour l'instant, on redirige vers l'interface chauffeur
        navigateToDriverHome(user)
    }

    override fun onResume() {
        super.onResume()
        viewModel.resetState()
    }
}
