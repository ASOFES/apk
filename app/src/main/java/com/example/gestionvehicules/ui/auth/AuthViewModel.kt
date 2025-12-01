package com.example.gestionvehicules.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionvehicules.data.api.SessionManager
import com.example.gestionvehicules.data.api.ApiService
import com.example.gestionvehicules.data.api.WebSocketApiService
import com.example.gestionvehicules.data.model.LoginRequest
import com.example.gestionvehicules.data.websocket.WebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder

class AuthViewModel(
    private val sessionManager: SessionManager = SessionManager(com.example.gestionvehicules.GestionVehiculesApp.instance),
    private val webSocketManager: WebSocketManager = WebSocketManager.getInstance()
) : ViewModel() {

    private val apiService: ApiService by lazy {
        val gson = com.google.gson.GsonBuilder().setLenient().create()
        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl(com.example.gestionvehicules.data.api.ApiConfig.BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(gson))
            .build()
        retrofit.create(ApiService::class.java)
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(username: String, password: String) {
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            try {
                // Log de la tentative de connexion
                println(" Tentative de connexion:")
                println("URL: ${com.example.gestionvehicules.data.api.ApiConfig.BASE_URL}")
                println("Username: $username")
                println("Password: ${password.replace(Regex("."), "*")}")
                
                val response = apiService.login(username, password)
                
                println(" Réponse API:")
                println("Code: ${response.code()}")
                println("Message: ${response.message()}")
                println("Headers: ${response.headers()}")
                
                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        // Stocker le token et l'utilisateur dans la session
                        sessionManager.saveAuthToken(loginResponse.token)
                        sessionManager.saveCurrentUser(loginResponse.user)
                        
                        // Naviguer selon le type d'utilisateur
                        when (loginResponse.userType) {
                            "chauffeur" -> {
                                _loginState.value = LoginState.Success.Driver(loginResponse.user)
                            }
                            "demandeur" -> {
                                _loginState.value = LoginState.Success.Requester(loginResponse.user)
                            }
                            "dispatch" -> {
                                _loginState.value = LoginState.Success.Dispatcher(loginResponse.user)
                            }
                            else -> {
                                _loginState.value = LoginState.Error("Type d'utilisateur non reconnu")
                            }
                        }
                    } ?: run {
                        _loginState.value = LoginState.Error("Réponse serveur vide")
                    }
                    
                    // Connect to WebSocket notifications after successful login
                    response.body()?.let { loginResponse ->
                        loginResponse.user?.let { user ->
                            webSocketManager.connectToNotifications(user.id)
                        }
                    }
                    
                } else {
                    val errorBody = response.errorBody()?.string()
                    println(" Erreur de connexion:")
                    println("Code: ${response.code()}")
                    println("Message: ${response.message()}")
                    println("Body: $errorBody")
                    
                    when (response.code()) {
                        401 -> _loginState.value = LoginState.Error("Identifiants incorrects")
                        403 -> _loginState.value = LoginState.Error("Accès refusé - Vérifiez vos permissions")
                        404 -> _loginState.value = LoginState.Error("URL de connexion non trouvée")
                        500 -> _loginState.value = LoginState.Error("Erreur serveur - Réessayez plus tard")
                        else -> _loginState.value = LoginState.Error("Erreur ${response.code()}: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                println(" Exception lors de la connexion:")
                println("Message: ${e.message}")
                println("Type: ${e::class.java.simpleName}")
                
                _loginState.value = LoginState.Error(
                    when (e) {
                        is java.net.UnknownHostException -> "Impossible de se connecter au serveur"
                        is java.net.SocketTimeoutException -> "Délai d'attente dépassé"
                        is java.net.ConnectException -> "Connexion refusée par le serveur"
                        else -> "Erreur de connexion: ${e.message}"
                    }
                )
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
    
    override fun onCleared() {
        super.onCleared()
        // Disconnect WebSocket connections when ViewModel is cleared
        webSocketManager.disconnectAll()
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Error(val message: String) : LoginState()
        sealed class Success : LoginState() {
            data class Driver(val user: com.example.gestionvehicules.data.model.User) : Success()
            data class Requester(val user: com.example.gestionvehicules.data.model.User) : Success()
            data class Dispatcher(val user: com.example.gestionvehicules.data.model.User) : Success()
        }
    }
}
