package com.example.gestionvehicules

import android.app.Application

class GestionVehiculesApp : Application() {
    
    companion object {
        lateinit var instance: GestionVehiculesApp
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        // Initialisation des composants globaux
    }
}
