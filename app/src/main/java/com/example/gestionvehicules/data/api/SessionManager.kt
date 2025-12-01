package com.example.gestionvehicules.data.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.gestionvehicules.data.model.User
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("GestionVehiculesPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var authToken: String?
        get() = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        set(value) {
            sharedPreferences.edit { putString(KEY_AUTH_TOKEN, value) }
            Log.d("SessionManager", "Token sauvegardé: ${value?.take(10)}...")
        }

    var currentUser: User?
        get() {
            try {
                val userJson = sharedPreferences.getString(KEY_USER, null)
                return if (userJson != null && userJson.isNotEmpty()) {
                    gson.fromJson(userJson, User::class.java)
                } else {
                    Log.d("SessionManager", "Aucun utilisateur trouvé en session")
                    null
                }
            } catch (e: Exception) {
                Log.e("SessionManager", "Erreur lecture utilisateur: ${e.message}")
                return null
            }
        }
        set(value) {
            try {
                val userJson = gson.toJson(value)
                sharedPreferences.edit { putString(KEY_USER, userJson) }
                Log.d("SessionManager", "Utilisateur sauvegardé: ${value?.first_name}")
            } catch (e: Exception) {
                Log.e("SessionManager", "Erreur sauvegarde utilisateur: ${e.message}")
            }
        }

    var isLoggedIn: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_IS_LOGGED_IN, value) }

    var userType: String?
        get() = sharedPreferences.getString(KEY_USER_TYPE, null)
        set(value) = sharedPreferences.edit { 
            if (value != null) {
                putString(KEY_USER_TYPE, value)
                Log.d("SessionManager", "Type utilisateur: $value")
            }
        }

    fun saveAuthToken(token: String) {
        if (token.isNotEmpty()) {
            authToken = token
            Log.d("SessionManager", "✅ Token sauvegardé avec succès")
        } else {
            Log.e("SessionManager", "❌ Token vide non sauvegardé")
        }
    }
    
    fun saveCurrentUser(user: User) {
        currentUser = user
        isLoggedIn = true
        userType = user.userType
        Log.d("SessionManager", "✅ Session utilisateur créée: ${user.first_name} ${user.last_name}")
    }
    
    fun getTokenWithBearer(): String? {
        return authToken?.let { "Bearer $it" }
    }
    
    fun isTokenValid(): Boolean {
        val token = authToken
        return !token.isNullOrEmpty() && token.length > 10
    }
    
    fun refreshSession(): Boolean {
        return try {
            val user = currentUser
            val token = authToken
            
            if (user != null && token != null && isTokenValid()) {
                isLoggedIn = true
                userType = user.userType
                Log.d("SessionManager", "✅ Session rafraîchie")
                true
            } else {
                Log.w("SessionManager", "⚠️ Session invalide, nettoyage")
                clearSession()
                false
            }
        } catch (e: Exception) {
            Log.e("SessionManager", "Erreur rafraîchissement: ${e.message}")
            clearSession()
            false
        }
    }

    fun clearSession() {
        sharedPreferences.edit {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER)
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USER_TYPE)
            clear()
        }
        Log.d("SessionManager", "🗑️ Session complètement effacée")
    }
    
    fun getSessionInfo(): String {
        return try {
            val user = currentUser
            val loggedIn = isLoggedIn
            val tokenValid = isTokenValid()
            
            "Utilisateur: ${user?.first_name} ${user?.last_name}, " +
            "Connecté: $loggedIn, Token valide: $tokenValid"
        } catch (e: Exception) {
            "Erreur info session: ${e.message}"
        }
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER = "user"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_TYPE = "user_type"
    }
}
