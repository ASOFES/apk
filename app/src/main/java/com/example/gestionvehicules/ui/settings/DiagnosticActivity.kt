package com.example.gestionvehicules.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gestionvehicules.data.api.ApiConfig
import com.example.gestionvehicules.data.api.ApiService
import com.example.gestionvehicules.databinding.ActivityDiagnosticBinding
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DiagnosticActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiagnosticBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiagnosticBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupApiService()
        setupClickListeners()
        showInitialInfo()
    }

    private fun setupApiService() {
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    private fun setupClickListeners() {
        binding.btnTestBasic.setOnClickListener { testBasicConnection() }
        binding.btnTestLogin.setOnClickListener { testLoginConnection() }
        binding.btnTestCORS.setOnClickListener { testCORS() }
        binding.btnShowHeaders.setOnClickListener { showHeaders() }
        binding.btnClearLogs.setOnClickListener { clearLogs() }
    }

    private fun showInitialInfo() {
        val info = """
            🔍 DIAGNOSTIC API COMPLET
            
            URL de base: ${ApiConfig.BASE_URL}
            URL HTTP: ${ApiConfig.BASE_URL_HTTP}
            URL HTTPS: ${ApiConfig.BASE_URL_HTTPS}
            URL IP: ${ApiConfig.BASE_URL_IP}
            
            Timeout connexion: ${ApiConfig.CONNECTION_TIMEOUT}s
            Timeout lecture: ${ApiConfig.READ_TIMEOUT}s
            
            Tests disponibles:
            1. Test de connexion basic
            2. Test d'authentification
            3. Test CORS
            4. Affichage des headers
        """.trimIndent()
        
        binding.tvLogs.text = info
    }

    private fun testBasicConnection() {
        addLog("🌐 Test de connexion basic à ${ApiConfig.BASE_URL}")
        
        lifecycleScope.launch {
            try {
                val response = apiService.getProfile()
                
                addLog("📡 Réponse basic:")
                addLog("   Code: ${response.code()}")
                addLog("   Message: ${response.message()}")
                addLog("   Headers: ${response.headers()}")
                
                when (response.code()) {
                    200 -> addLog("✅ API accessible et fonctionnelle")
                    401 -> addLog("✅ API accessible (authentification requise)")
                    403 -> addLog("⚠️ API accessible mais accès refusé (CORS?)")
                    404 -> addLog("❌ Endpoint non trouvé")
                    500 -> addLog("❌ Erreur serveur interne")
                    else -> addLog("⚠️ Code ${response.code()}: ${response.message()}")
                }
                
            } catch (e: Exception) {
                addLog("💥 Erreur connexion basic:")
                addLog("   Type: ${e::class.java.simpleName}")
                addLog("   Message: ${e.message}")
                
                when (e) {
                    is java.net.UnknownHostException -> addLog("❌ Serveur introuvable")
                    is java.net.SocketTimeoutException -> addLog("❌ Délai d'attente dépassé")
                    is java.net.ConnectException -> addLog("❌ Connexion refusée")
                    else -> addLog("❌ Erreur réseau: ${e.message}")
                }
            }
        }
    }

    private fun testLoginConnection() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()
        
        if (username.isEmpty() || password.isEmpty()) {
            addLog("⚠️ Veuillez entrer username et password")
            return
        }
        
        addLog("🔐 Test d'authentification")
        addLog("   Username: $username")
        addLog("   Password: ${password.replace(Regex("."), "*")}")
        
        lifecycleScope.launch {
            try {
                val response = apiService.login(username, password)
                
                addLog("📡 Réponse authentification:")
                addLog("   Code: ${response.code()}")
                addLog("   Message: ${response.message()}")
                addLog("   Headers: ${response.headers()}")
                
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        addLog("✅ Authentification réussie!")
                        addLog("   Token: ${loginResponse.token.take(30)}...")
                        addLog("   User ID: ${loginResponse.user.id}")
                        addLog("   Username: ${loginResponse.user.username}")
                        addLog("   Email: ${loginResponse.user.email}")
                        addLog("   Driver: ${loginResponse.user.is_driver}")
                        addLog("   Requester: ${loginResponse.user.is_requester}")
                        addLog("   User Type: ${loginResponse.user.userType}")
                    } else {
                        addLog("❌ Réponse vide du serveur")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    addLog("❌ Erreur authentification:")
                    addLog("   Code: ${response.code()}")
                    addLog("   Message: ${response.message()}")
                    addLog("   Body: $errorBody")
                    
                    when (response.code()) {
                        400 -> addLog("🔧 Requête invalide - vérifiez les champs")
                        401 -> addLog("🔑 Identifiants incorrects")
                        403 -> addLog("🚫 Accès refusé - permissions insuffisantes")
                        404 -> addLog("🔍 Endpoint login non trouvé")
                        500 -> addLog("💥 Erreur serveur interne")
                        else -> addLog("⚠️ Erreur ${response.code()}")
                    }
                }
                
            } catch (e: Exception) {
                addLog("💥 Exception authentification:")
                addLog("   Type: ${e::class.java.simpleName}")
                addLog("   Message: ${e.message}")
            }
        }
    }

    private fun testCORS() {
        addLog("🌍 Test CORS (Cross-Origin Resource Sharing)")
        
        lifecycleScope.launch {
            try {
                // Test avec un endpoint simple pour vérifier les headers CORS
                val response = apiService.getProfile()
                
                addLog("📡 Headers CORS reçus:")
                val corsHeaders = listOf(
                    "Access-Control-Allow-Origin",
                    "Access-Control-Allow-Methods",
                    "Access-Control-Allow-Headers",
                    "Access-Control-Max-Age"
                )
                
                corsHeaders.forEach { header ->
                    val value = response.headers()[header]
                    if (value != null) {
                        addLog("   $header: $value")
                    }
                }
                
                if (corsHeaders.any { response.headers()[it] != null }) {
                    addLog("✅ Headers CORS présents")
                } else {
                    addLog("⚠️ Aucun header CORS détecté")
                    addLog("  这可能就是 'accès refusé' 的原因")
                    addLog("   Configurez CORS dans votre backend Django")
                }
                
            } catch (e: Exception) {
                addLog("💥 Erreur test CORS: ${e.message}")
            }
        }
    }

    private fun showHeaders() {
        addLog("📋 Headers de la requête:")
        addLog("   Content-Type: application/x-www-form-urlencoded")
        addLog("   Accept: application/json")
        addLog("   User-Agent: Android-Retrofit")
        addLog("")
        addLog("📋 Headers attendus en réponse:")
        addLog("   Content-Type: application/json")
        addLog("   Access-Control-Allow-Origin: *")
        addLog("   Access-Control-Allow-Methods: POST, GET, OPTIONS")
        addLog("   Access-Control-Allow-Headers: Content-Type, Authorization")
    }

    private fun clearLogs() {
        binding.tvLogs.text = "🗑️ Logs effacés\n\n"
        showInitialInfo()
    }

    private fun addLog(message: String) {
        val currentLogs = binding.tvLogs.text.toString()
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        binding.tvLogs.text = "$currentLogs\n[$timestamp] $message"
        
        // Auto-scroll vers le bas
        val scrollView = binding.scrollView
        scrollView.post {
            scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }
}
