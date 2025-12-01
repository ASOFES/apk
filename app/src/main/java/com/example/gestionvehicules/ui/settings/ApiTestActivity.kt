package com.example.gestionvehicules.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gestionvehicules.data.api.ApiConfig
import com.example.gestionvehicules.data.api.ApiService
import com.example.gestionvehicules.data.api.SessionManager
import com.example.gestionvehicules.databinding.ActivityApiTestBinding
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApiTestBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApiTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupApiService()
        setupClickListeners()
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
        binding.btnTestConnection.setOnClickListener {
            testConnection()
        }

        binding.btnTestLogin.setOnClickListener {
            testLogin()
        }

        binding.btnShowConfig.setOnClickListener {
            showCurrentConfig()
        }
    }

    private fun testConnection() {
        binding.tvResult.text = "Test de connexion en cours..."
        
        lifecycleScope.launch {
            try {
                println("🔍 Test de connexion à: ${ApiConfig.BASE_URL}")
                
                val response = apiService.getProfile()
                
                println("📡 Réponse test connexion:")
                println("Code: ${response.code()}")
                println("Message: ${response.message()}")
                
                val result = when (response.code()) {
                    200 -> "✅ API accessible et fonctionnelle"
                    401 -> "✅ API accessible (authentification requise)"
                    404 -> "❌ Endpoint non trouvé - Vérifiez l'URL"
                    500 -> "❌ Erreur serveur"
                    else -> "⚠️ Code ${response.code()}: ${response.message()}"
                }
                
                binding.tvResult.text = result
                
            } catch (e: Exception) {
                println("💥 Erreur test connexion:")
                println("Message: ${e.message}")
                println("Type: ${e::class.java.simpleName}")
                
                val error = when (e) {
                    is java.net.UnknownHostException -> "❌ Serveur introuvable - Vérifiez l'URL"
                    is java.net.SocketTimeoutException -> "❌ Délai d'attente dépassé"
                    is java.net.ConnectException -> "❌ Connexion refusée - Serveur inaccessible"
                    else -> "❌ Erreur: ${e.message}"
                }
                
                binding.tvResult.text = error
            }
        }
    }

    private fun testLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()
        
        if (username.isEmpty() || password.isEmpty()) {
            binding.tvResult.text = "Veuillez entrer username et password"
            return
        }
        
        binding.tvResult.text = "Test de connexion en cours..."
        
        lifecycleScope.launch {
            try {
                println("🔐 Test de connexion:")
                println("URL: ${ApiConfig.BASE_URL}")
                println("Username: $username")
                
                val response = apiService.login(username, password)
                
                println("📡 Réponse connexion:")
                println("Code: ${response.code()}")
                println("Message: ${response.message()}")
                
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        println("✅ Connexion réussie:")
                        println("Token: ${loginResponse.token.take(20)}...")
                        println("User: ${loginResponse.user?.username}")
                        println("User Type: ${loginResponse.user?.userType}")
                        
                        binding.tvResult.text = """
                            ✅ CONNEXION RÉUSSIE!
                            
                            User: ${loginResponse.user?.username}
                            Type: ${loginResponse.user?.userType}
                            Token: ${loginResponse.token.take(30)}...
                            
                            L'API fonctionne correctement avec ces identifiants.
                        """.trimIndent()
                    } else {
                        binding.tvResult.text = "❌ Réponse vide du serveur"
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("❌ Erreur connexion:")
                    println("Code: ${response.code()}")
                    println("Message: ${response.message()}")
                    println("Body: $errorBody")
                    
                    binding.tvResult.text = """
                        ❌ ERREUR DE CONNEXION
                        
                        Code: ${response.code()}
                        Message: ${response.message()}
                        Body: $errorBody
                        
                        Solutions possibles:
                        - Vérifiez les identifiants
                        - Vérifiez que l'utilisateur existe dans la base de données
                        - Vérifiez que l'utilisateur est actif
                    """.trimIndent()
                }
                
            } catch (e: Exception) {
                println("💥 Exception connexion:")
                println("Message: ${e.message}")
                
                binding.tvResult.text = "❌ Exception: ${e.message}"
            }
        }
    }

    private fun showCurrentConfig() {
        val config = """
            📋 CONFIGURATION ACTUELLE
            
            URL Base: ${ApiConfig.BASE_URL}
            URL HTTP: ${ApiConfig.BASE_URL_HTTP}
            URL HTTPS: ${ApiConfig.BASE_URL_HTTPS}
            URL IP: ${ApiConfig.BASE_URL_IP}
            
            Timeout: ${ApiConfig.CONNECTION_TIMEOUT}s
        """.trimIndent()
        
        binding.tvResult.text = config
    }
}
