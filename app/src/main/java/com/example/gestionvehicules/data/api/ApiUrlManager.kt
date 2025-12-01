package com.example.gestionvehicules.data.api

import android.content.Context
import android.content.SharedPreferences

class ApiUrlManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("ApiConfigPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USE_HTTPS = "use_https"
        private const val KEY_USE_IP = "use_ip"
        private const val KEY_USE_PORT = "use_port"
        private const val KEY_CUSTOM_URL = "custom_url"
    }

    var useHttps: Boolean
        get() = sharedPreferences.getBoolean(KEY_USE_HTTPS, false)  // Changé à false
        set(value) = sharedPreferences.edit().putBoolean(KEY_USE_HTTPS, value).apply()

    var useIp: Boolean
        get() = sharedPreferences.getBoolean(KEY_USE_IP, false)   // Changé à false
        set(value) = sharedPreferences.edit().putBoolean(KEY_USE_IP, value).apply()

    var usePort: Boolean
        get() = sharedPreferences.getBoolean(KEY_USE_PORT, true)   // NOUVEAU - par défaut true
        set(value) = sharedPreferences.edit().putBoolean(KEY_USE_PORT, value).apply()

    var customUrl: String?
        get() = sharedPreferences.getString(KEY_CUSTOM_URL, null)
        set(value) = sharedPreferences.edit().putString(KEY_CUSTOM_URL, value).apply()

    fun getCurrentBaseUrl(): String {
        return customUrl ?: ApiConfig.getBaseUrl(useHttps, useIp, usePort)
    }

    fun setApiConfiguration(useHttps: Boolean, useIp: Boolean, usePort: Boolean = true, customUrl: String? = null) {
        this.useHttps = useHttps
        this.useIp = useIp
        this.usePort = usePort
        this.customUrl = customUrl
    }

    fun getAvailableUrls(): List<String> {
        return listOf(
            ApiConfig.BASE_URL_PORT,  // NOUVELLE URL EN PREMIER
            ApiConfig.BASE_URL_HTTPS,
            ApiConfig.BASE_URL_HTTP,
            ApiConfig.BASE_URL_IP
        )
    }
}
