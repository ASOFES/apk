package com.example.gestionvehicules.data.api

object ApiConfig {
    // URLs de base pour l'API
    const val BASE_URL_HTTP = "http://mamordc.cc/"
    const val BASE_URL_HTTPS = "https://mamordc.cc/"
    const val BASE_URL_IP = "http://208.109.231.135:8000/"
    const val BASE_URL_PORT = "http://mamordc.cc:8000/"  // NOUVELLE URL FONCTIONNELLE
    const val BASE_URL_LOCAL = "http://192.168.97.98:8000/"  // URL locale pour tests
    
    // URL par défaut (serveur en ligne)
    const val BASE_URL = BASE_URL_IP
    
    const val CONNECTION_TIMEOUT = 30L // secondes
    const val READ_TIMEOUT = 30L // secondes
    
    // Fonction pour obtenir l'URL de base selon la configuration
    fun getBaseUrl(useHttps: Boolean = true, useIp: Boolean = false, usePort: Boolean = true): String {
        return when {
            usePort -> BASE_URL_PORT
            useIp -> BASE_URL_IP
            useHttps -> BASE_URL_HTTPS
            else -> BASE_URL_HTTP
        }
    }
}
