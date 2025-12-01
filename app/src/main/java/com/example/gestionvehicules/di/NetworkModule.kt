package com.example.gestionvehicules.di

import android.content.Context
import com.example.gestionvehicules.data.api.ApiConfig
import com.example.gestionvehicules.data.api.ApiService
import com.example.gestionvehicules.data.api.ApiUrlManager
import com.example.gestionvehicules.data.api.SessionManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        sessionManager: SessionManager
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Accept", "application/json")
                
                // Ajouter le token d'authentification s'il existe
                sessionManager.authToken?.let { token ->
                    requestBuilder.header("Authorization", "Token $token")
                }

                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .connectTimeout(ApiConfig.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    fun provideRetrofit(
        gson: Gson,
        okHttpClient: OkHttpClient,
        apiUrlManager: ApiUrlManager
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(apiUrlManager.getCurrentBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    fun provideApiUrlManager(context: Context): ApiUrlManager {
        return ApiUrlManager(context)
    }

    fun provideSessionManager(context: Context): SessionManager {
        return SessionManager(context)
    }
}
