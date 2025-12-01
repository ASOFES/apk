package com.example.gestionvehicules.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionvehicules.data.api.ApiConfig
import com.example.gestionvehicules.data.api.ApiService
import com.example.gestionvehicules.data.api.ApiUrlManager
import com.example.gestionvehicules.di.NetworkModule
import kotlinx.coroutines.launch

class ApiConfigViewModel : ViewModel() {

    data class ApiConfiguration(
        val useHttps: Boolean = false,
        val useIp: Boolean = false,
        val usePort: Boolean = true,
        val customUrl: String? = null
    )

    sealed class ConnectionResult {
        object Loading : ConnectionResult()
        object Success : ConnectionResult()
        data class Error(val message: String) : ConnectionResult()
    }

    private val _currentConfig = MutableLiveData<ApiConfiguration>()
    val currentConfig: LiveData<ApiConfiguration> = _currentConfig

    private val _connectionTestResult = MutableLiveData<ConnectionResult>()
    val connectionTestResult: LiveData<ConnectionResult> = _connectionTestResult

    private val apiUrlManager: ApiUrlManager = ApiUrlManager(com.example.gestionvehicules.GestionVehiculesApp.instance)
    private val apiService: ApiService = NetworkModule.provideApiService(
        NetworkModule.provideRetrofit(
            NetworkModule.provideGson(),
            NetworkModule.provideOkHttpClient(
                NetworkModule.provideHttpLoggingInterceptor(),
                com.example.gestionvehicules.data.api.SessionManager(com.example.gestionvehicules.GestionVehiculesApp.instance)
            ),
            apiUrlManager
        )
    )

    init {
        loadCurrentConfiguration()
    }

    private fun loadCurrentConfiguration() {
        val config = ApiConfiguration(
            useHttps = apiUrlManager.useHttps,
            useIp = apiUrlManager.useIp,
            usePort = apiUrlManager.usePort,  // NOUVEAU
            customUrl = apiUrlManager.customUrl
        )
        _currentConfig.value = config
    }

    fun setApiConfiguration(useHttps: Boolean, useIp: Boolean, usePort: Boolean = true, customUrl: String? = null) {
        apiUrlManager.setApiConfiguration(useHttps, useIp, usePort, customUrl)
        loadCurrentConfiguration()
    }

    fun testConnection(baseUrl: String) {
        viewModelScope.launch {
            _connectionTestResult.value = ConnectionResult.Loading
            
            try {
                // Créer un service API temporaire avec l'URL de test
                val gson = com.google.gson.GsonBuilder().setLenient().create()
                val retrofit = retrofit2.Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(gson))
                    .build()
                val testApiService = retrofit.create(ApiService::class.java)
                
                // Test simple avec l'endpoint de profil
                val response = testApiService.getProfile()
                
                if (response.isSuccessful || response.code() == 401) {
                    // 401 est acceptable car ça signifie que l'API répond
                    _connectionTestResult.value = ConnectionResult.Success
                } else {
                    _connectionTestResult.value = ConnectionResult.Error(
                        "Code d'erreur: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _connectionTestResult.value = ConnectionResult.Error(
                    e.message ?: "Erreur de connexion inconnue"
                )
            }
        }
    }

    fun getCurrentUrl(): String {
        return apiUrlManager.getCurrentBaseUrl()
    }

    fun getAvailableUrls(): List<String> {
        return apiUrlManager.getAvailableUrls()
    }
}
