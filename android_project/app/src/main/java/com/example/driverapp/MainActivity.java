package com.example.driverapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.driverapp.models.LoginResponse;
import com.example.driverapp.models.CourseResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private ApiService apiService;
    private SharedPreferences prefs;
    private static final String TAG = "DriverApp";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        prefs = getSharedPreferences("DriverApp", MODE_PRIVATE);
        
        // Initialiser Retrofit
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://135-231-109-208.host.secureserver.net:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        
        apiService = retrofit.create(ApiService.class);
        
        // Vérifier si déjà connecté
        String token = prefs.getString("token", null);
        if (token != null) {
            loadDriverData();
        } else {
            showLoginScreen();
        }
    }
    
    private void showLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void loadDriverData() {
        String token = prefs.getString("token", null);
        if (token == null) {
            showLoginScreen();
            return;
        }
        
        // Charger la course assignée
        apiService.getAssignedCourse("Bearer " + token).enqueue(new Callback<CourseResponse>() {
            @Override
            public void onResponse(Call<CourseResponse> call, Response<CourseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CourseResponse courseResponse = response.body();
                    if (courseResponse.success && courseResponse.course != null) {
                        showAssignedCourse(courseResponse.course);
                    } else {
                        loadCourseHistory();
                    }
                } else if (response.code() == 401) {
                    // Token expiré, se reconnecter
                    prefs.edit().remove("token").apply();
                    showLoginScreen();
                } else {
                    loadCourseHistory();
                }
            }
            
            @Override
            public void onFailure(Call<CourseResponse> call, Throwable t) {
                Log.e(TAG, "Erreur réseau", t);
                Toast.makeText(MainActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadCourseHistory() {
        String token = prefs.getString("token", null);
        if (token == null) return;
        
        apiService.getCourseHistory("Bearer " + token).enqueue(new Callback<CourseResponse>() {
            @Override
            public void onResponse(Call<CourseResponse> call, Response<CourseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CourseResponse courseResponse = response.body();
                    if (courseResponse.success && courseResponse.courses != null) {
                        showCourseHistory(courseResponse.courses);
                    }
                } else {
                    Log.e(TAG, "Erreur chargement historique: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<CourseResponse> call, Throwable t) {
                Log.e(TAG, "Erreur réseau historique", t);
            }
        });
    }
    
    private void showAssignedCourse(CourseResponse.Course course) {
        Intent intent = new Intent(this, AssignedCourseActivity.class);
        intent.putExtra("course", course);
        startActivity(intent);
    }
    
    private void showCourseHistory(java.util.List<CourseResponse.Course> courses) {
        Intent intent = new Intent(this, CourseHistoryActivity.class);
        intent.putExtra("courses", new java.util.ArrayList<>(courses));
        startActivity(intent);
    }
}
