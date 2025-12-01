package com.example.gestionvehicules.ui.settings

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionvehicules.databinding.ActivityApiConfigBinding
import com.example.gestionvehicules.data.api.ApiConfig

class ApiConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApiConfigBinding
    private lateinit var viewModel: ApiConfigViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApiConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialiser le ViewModel manuellement
        viewModel = ApiConfigViewModel()

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        setupSpinner()

        // Bouton de sauvegarde
        binding.btnSave.setOnClickListener {
            saveConfiguration()
        }

        // Bouton de test de connexion
        binding.btnTestConnection.setOnClickListener {
            testConnection()
        }

        // Gestion du spinner
        binding.spinnerUrls.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                binding.etCustomUrl.isEnabled = position == 4 // URL personnalisée (maintenant en position 4)
                if (position != 4) {
                    binding.etCustomUrl.text?.clear()
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupSpinner() {
        val urls = listOf(
            "http://mamordc.cc:8000/ (Port 8000)",
            "https://mamordc.cc/ (HTTPS)",
            "http://mamordc.cc/ (HTTP)",
            "http://208.109.231.135:8000/ (IP)",
            "URL personnalisée"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, urls)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUrls.adapter = adapter
        
        // Sélectionner l'URL par défaut (port 8000)
        binding.spinnerUrls.setSelection(0)
    }

    private fun observeViewModel() {
        viewModel.currentConfig.observe(this) { config ->
            // Mettre à jour l'UI avec la configuration actuelle
            val position = when {
                config.usePort -> 0  // Port 8000
                config.useIp -> 3    // IP
                config.useHttps -> 1 // HTTPS
                else -> 2            // HTTP
            }
            binding.spinnerUrls.setSelection(position)
            
            config.customUrl?.let { url ->
                binding.etCustomUrl.setText(url)
                binding.spinnerUrls.setSelection(4)  // URL personnalisée
            }
        }

        viewModel.connectionTestResult.observe(this) { result ->
            when (result) {
                is ApiConfigViewModel.ConnectionResult.Success -> {
                    Toast.makeText(this, "Connexion réussie!", Toast.LENGTH_SHORT).show()
                }
                is ApiConfigViewModel.ConnectionResult.Error -> {
                    Toast.makeText(this, "Erreur de connexion: ${result.message}", Toast.LENGTH_LONG).show()
                }
                is ApiConfigViewModel.ConnectionResult.Loading -> {
                    Toast.makeText(this, "Test de connexion en cours...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveConfiguration() {
        val selectedPosition = binding.spinnerUrls.selectedItemPosition
        val customUrl = binding.etCustomUrl.text?.toString()?.trim() ?: ""

        when (selectedPosition) {
            0 -> viewModel.setApiConfiguration(useHttps = false, useIp = false, usePort = true)  // Port 8000
            1 -> viewModel.setApiConfiguration(useHttps = true, useIp = false, usePort = false)  // HTTPS
            2 -> viewModel.setApiConfiguration(useHttps = false, useIp = false, usePort = false) // HTTP
            3 -> viewModel.setApiConfiguration(useHttps = false, useIp = true, usePort = false)   // IP
            4 -> {
                if (customUrl.isEmpty()) {
                    Toast.makeText(this, "Veuillez entrer une URL personnalisée", Toast.LENGTH_SHORT).show()
                    return
                }
                viewModel.setApiConfiguration(useHttps = false, useIp = false, usePort = false, customUrl = customUrl)
            }
        }

        Toast.makeText(this, "Configuration sauvegardée", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun testConnection() {
        val selectedPosition = binding.spinnerUrls.selectedItemPosition
        val customUrl = binding.etCustomUrl.text?.toString()?.trim() ?: ""

        val testUrl = when (selectedPosition) {
            0 -> ApiConfig.BASE_URL_PORT  // Port 8000
            1 -> ApiConfig.BASE_URL_HTTPS
            2 -> ApiConfig.BASE_URL_HTTP
            3 -> ApiConfig.BASE_URL_IP
            4 -> {
                if (customUrl.isEmpty()) {
                    Toast.makeText(this, "Veuillez entrer une URL personnalisée", Toast.LENGTH_SHORT).show()
                    return
                }
                customUrl
            }
            else -> ApiConfig.BASE_URL_PORT
        }

        viewModel.testConnection(testUrl)
    }
}
