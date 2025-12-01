package com.example.driverapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.driverapp.models.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ApiService apiService;
    private SharedPreferences prefs;
    private static final String TAG = "LoginActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        
        prefs = getSharedPreferences("DriverApp", MODE_PRIVATE);
        apiService = ((DriverApplication) getApplication()).getApiService();
        
        btnLogin.setOnClickListener(v -> attemptLogin());
    }
    
    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnLogin.setEnabled(false);
        btnLogin.setText("Connexion...");
        
        apiService.login(username, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Se connecter");
                
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    
                    if (loginResponse.success) {
                        // Sauvegarder le token et les infos utilisateur
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("token", loginResponse.token);
                        editor.putString("username", loginResponse.user.username);
                        editor.putString("userType", loginResponse.userType);
                        editor.putInt("userId", loginResponse.user.id);
                        editor.apply();
                        
                        Log.d(TAG, "Connexion réussie: " + loginResponse.token);
                        
                        // Rediriger vers MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        
                    } else {
                        Toast.makeText(LoginActivity.this, 
                            loginResponse.error != null ? loginResponse.error : "Échec de la connexion", 
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "Erreur serveur: " + response.code());
                    Toast.makeText(LoginActivity.this, "Erreur serveur: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Se connecter");
                Log.e(TAG, "Erreur réseau", t);
                Toast.makeText(LoginActivity.this, "Erreur de connexion réseau", Toast.LENGTH_LONG).show();
            }
        });
    }
}
