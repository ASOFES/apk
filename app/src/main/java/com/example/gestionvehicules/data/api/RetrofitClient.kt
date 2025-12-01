package com.example.gestionvehicules.data.api

import android.content.Context
import android.util.Log
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.io.IOException

class RetrofitClient {
    
    companion object {
        private var retrofit: Retrofit? = null
        private var apiService: ApiService? = null
        private var currentBaseUrl: String? = null
        
        fun getApiService(context: Context): ApiService {
            val urlManager = ApiUrlManager(context)
            val baseUrl = urlManager.getCurrentBaseUrl()
            
            if (apiService == null || currentBaseUrl != baseUrl) {
                Log.d("RetrofitClient", " Création nouvelle instance pour: $baseUrl")
                retrofit = buildRetrofit(baseUrl)
                apiService = retrofit!!.create(ApiService::class.java)
                currentBaseUrl = baseUrl
            }
            return apiService!!
        }
        
        private fun buildRetrofit(baseUrl: String): Retrofit {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            
            // Intercepteur personnalisé pour les headers et logging
            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                
                // Ajouter des headers par défaut
                val requestBuilder = originalRequest.newBuilder()
                    .header("Accept", ApiConfig.HEADER_ACCEPT)
                    .header("Content-Type", ApiConfig.HEADER_CONTENT_TYPE)
                    .method(originalRequest.method, originalRequest.body)
                
                val request = requestBuilder.build()
                Log.d("RetrofitClient", " Requête: ${request.method} ${request.url}")
                
                try {
                    val response = chain.proceed(request)
                    Log.d("RetrofitClient", " Réponse: ${response.code} ${response.message}")
                    response
                } catch (e: IOException) {
                    Log.e("RetrofitClient", " Erreur réseau: ${e.message}")
                    throw e
                }
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(ApiConfig.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
            
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(
                    GsonConverterFactory.create(
                        com.google.gson.GsonBuilder()
                            .setLenient()
                            .create()
                    )
                )
                .build()
        }
        
        fun resetInstance() {
            Log.d("RetrofitClient", " Reset instance")
            apiService = null
            retrofit = null
            currentBaseUrl = null
        }
        
        fun testConnection(baseUrl: String): Boolean {
            return try {
                Log.d("RetrofitClient", " Test connexion: $baseUrl")
                
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
                
                val request = Request.Builder()
                    .url("${baseUrl}login-api/")
                    .get()
                    .build()
                
                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                
                Log.d("RetrofitClient", " Test résultat: $success (${response.code})")
                response.close()
                success
            } catch (e: Exception) {
                Log.e("RetrofitClient", " Test échec: ${e.message}")
                false
            }
        }
        
        fun getServerInfo(baseUrl: String): String {
            return try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .build()
                
                val request = Request.Builder()
                    .url("${baseUrl}login-api/")
                    .get()
                    .build()
                
                val response = client.newCall(request).execute()
                val info = "Serveur: ${response.code} - ${response.message}"
                response.close()
                info
            } catch (e: Exception) {
                "Erreur: ${e.message}"
            }
        }
    }
}
