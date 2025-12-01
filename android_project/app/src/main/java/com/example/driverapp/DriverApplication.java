package com.example.driverapp;

import android.app.Application;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DriverApplication extends Application {
    private ApiService apiService;
    private static final String BASE_URL = "http://135-231-109-208.host.secureserver.net:8000/";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialiser Retrofit une seule fois
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        
        apiService = retrofit.create(ApiService.class);
    }
    
    public ApiService getApiService() {
        return apiService;
    }
}
