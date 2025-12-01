package com.example.gestionvehicules.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionvehicules.databinding.ActivitySplashBinding
import com.example.gestionvehicules.ui.auth.LoginActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Afficher le splash pendant 2 secondes puis rediriger vers login
        // pour éviter les crashs de session au démarrage
        Handler(Looper.getMainLooper()).postDelayed({
            redirectToLogin()
        }, 2000)
    }

    private fun redirectToLogin() {
        try {
            startActivity(Intent(this, LoginActivity::class.java))
        } catch (e: Exception) {
            // En cas d'erreur, redémarrer l'application
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        finish()
    }
}
