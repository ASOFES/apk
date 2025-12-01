package com.example.gestionvehicules.ui.entretien

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionvehicules.data.api.ApiService
import com.example.gestionvehicules.data.api.RetrofitClient
import com.example.gestionvehicules.data.api.SessionManager
import com.example.gestionvehicules.databinding.ActivityEntretienBinding
import com.example.gestionvehicules.ui.entretien.adapters.EntretienAdapter
import kotlinx.coroutines.launch

class EntretienActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEntretienBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService
    private lateinit var adapter: EntretienAdapter
    private var vehicles: List<com.example.gestionvehicules.data.model.Vehicle> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("EntretienActivity onCreate - Début")
        
        try {
            binding = ActivityEntretienBinding.inflate(layoutInflater)
            setContentView(binding.root)
            println("EntretienActivity - Layout initialisé")
            
            sessionManager = SessionManager(this)
            apiService = RetrofitClient.getApiService(this)
            
            setupUI()
            setupRecyclerView()
            setupClickListeners()
            loadData()
            println("EntretienActivity - Initialisation terminée")
        } catch (e: Exception) {
            println("Erreur dans EntretienActivity onCreate: ${e.message}")
            e.printStackTrace()
            
            Toast.makeText(this, "Erreur d'initialisation: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupUI() {
        try {
            println("EntretienActivity setupUI - Début")
            
            // Configuration de la toolbar
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Entretien"
            
            binding.toolbar.setNavigationOnClickListener {
                finish()
            }
            
            println("EntretienActivity setupUI - Terminé")
        } catch (e: Exception) {
            println("Erreur dans setupUI: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = EntretienAdapter()
        binding.recyclerViewEntretiens.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewEntretiens.adapter = adapter
        
        // Swipe to refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }
    }
    
    private fun setupClickListeners() {
        // Bouton pour ajouter un entretien
        binding.fabAddEntretien.setOnClickListener {
            showAddEntretienDialog()
        }
    }
    
    private fun loadData() {
        binding.swipeRefreshLayout.isRefreshing = true
        loadEntretiens()
        loadVehicles()
    }
    
    private fun loadEntretiens() {
        val token = sessionManager.authToken ?: return
        
        lifecycleScope.launch {
            try {
                val response = apiService.getMaintenanceList("Bearer $token")
                
                if (response.isSuccessful) {
                    response.body()?.let { maintenanceResponse ->
                        adapter.updateData(maintenanceResponse.data)
                        binding.textViewEmpty.visibility = if (maintenanceResponse.data.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                        binding.textViewError.visibility = android.view.View.GONE
                    }
                } else {
                    showError("Erreur de chargement: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Erreur réseau: ${e.message}")
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    private fun loadVehicles() {
        val token = sessionManager.authToken ?: return
        
        lifecycleScope.launch {
            try {
                val response = apiService.getVehiclesList("Bearer $token")
                
                if (response.isSuccessful) {
                    response.body()?.let { vehiclesResponse ->
                        vehicles = vehiclesResponse.data
                    }
                }
            } catch (e: Exception) {
                println("Erreur chargement véhicules: ${e.message}")
            }
        }
    }
    
    private fun showAddEntretienDialog() {
        if (vehicles.isEmpty()) {
            Toast.makeText(this, "Chargement des véhicules...", Toast.LENGTH_SHORT).show()
            loadVehicles()
            return
        }
        
        val vehicleNames = vehicles.map { "${it.immatriculation} - ${it.marque} ${it.modele}" }.toTypedArray()
        val typesEntretien = arrayOf("ordinaire", "mecanique")
        val typesDisplay = arrayOf("Entretien ordinaire", "Entretien mécanique")
        
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ajouter un entretien")
        
        // Créer un layout personnalisé pour le formulaire
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        
        // Sélection du véhicule
        val vehicleSpinner = android.widget.Spinner(this).apply {
            adapter = android.widget.ArrayAdapter(this@EntretienActivity, 
                android.R.layout.simple_spinner_item, vehicleNames)
        }
        layout.addView(android.widget.TextView(this).apply { text = "Véhicule:" })
        layout.addView(vehicleSpinner)
        
        // Type d'entretien
        val typeSpinner = android.widget.Spinner(this).apply {
            adapter = android.widget.ArrayAdapter(this@EntretienActivity, 
                android.R.layout.simple_spinner_item, typesDisplay)
        }
        layout.addView(android.widget.TextView(this).apply { text = "Type d'entretien:" })
        layout.addView(typeSpinner)
        
        // Date d'entretien
        val dateInput = android.widget.EditText(this).apply {
            hint = "Date (AAAA-MM-JJ)"
            inputType = android.text.InputType.TYPE_CLASS_DATETIME or android.text.InputType.TYPE_DATETIME_VARIATION_DATE
        }
        layout.addView(android.widget.TextView(this).apply { text = "Date d'entretien:" })
        layout.addView(dateInput)
        
        // Garage
        val garageInput = android.widget.EditText(this).apply {
            hint = "Nom du garage"
        }
        layout.addView(android.widget.TextView(this).apply { text = "Garage:" })
        layout.addView(garageInput)
        
        // Motif
        val motifInput = android.widget.EditText(this).apply {
            hint = "Motif de l'entretien"
            minHeight = 100
        }
        layout.addView(android.widget.TextView(this).apply { text = "Motif:" })
        layout.addView(motifInput)
        
        // Coût
        val coutInput = android.widget.EditText(this).apply {
            hint = "Coût"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        layout.addView(android.widget.TextView(this).apply { text = "Coût:" })
        layout.addView(coutInput)
        
        // Kilométrage
        val kmInput = android.widget.EditText(this).apply {
            hint = "Kilométrage"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        layout.addView(android.widget.TextView(this).apply { text = "Kilométrage:" })
        layout.addView(kmInput)
        
        builder.setView(layout)
        
        builder.setPositiveButton("Ajouter") { _, _ ->
            val selectedVehicle = vehicles[vehicleSpinner.selectedItemPosition]
            val selectedType = typesEntretien[typeSpinner.selectedItemPosition]
            
            val date = dateInput.text.toString()
            val garage = garageInput.text.toString()
            val motif = motifInput.text.toString()
            val cout = coutInput.text.toString()
            val km = kmInput.text.toString()
            
            if (date.isNotEmpty() && garage.isNotEmpty() && motif.isNotEmpty() && cout.isNotEmpty() && km.isNotEmpty()) {
                createEntretien(
                    selectedVehicle.id,
                    selectedType,
                    date,
                    garage,
                    motif,
                    cout,
                    km
                )
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }
        
        builder.setNegativeButton("Annuler", null)
        builder.show()
    }
    
    private fun createEntretien(
        vehicleId: Int,
        typeEntretien: String,
        date: String,
        garage: String,
        motif: String,
        cout: String,
        km: String
    ) {
        val token = sessionManager.authToken ?: return
        
        lifecycleScope.launch {
            try {
                val response = apiService.createMaintenance(
                    "Bearer $token",
                    vehicleId.toString(),
                    typeEntretien,
                    date,
                    garage,
                    motif,
                    cout,
                    km
                )
                
                if (response.isSuccessful) {
                    Toast.makeText(this@EntretienActivity, "Entretien ajouté avec succès!", Toast.LENGTH_SHORT).show()
                    loadEntretiens() // Recharger la liste
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@EntretienActivity, "Erreur: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EntretienActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showError(message: String) {
        binding.errorTitleText.text = message
        binding.textViewError.visibility = android.view.View.VISIBLE
        binding.recyclerViewEntretiens.visibility = android.view.View.GONE
        binding.textViewEmpty.visibility = android.view.View.GONE
    }
}
