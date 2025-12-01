package com.example.gestionvehicules.ui.ravitaillement

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionvehicules.data.api.ApiService
import com.example.gestionvehicules.data.api.RetrofitClient
import com.example.gestionvehicules.data.api.SessionManager
import com.example.gestionvehicules.databinding.ActivityRavitaillementBinding
import com.example.gestionvehicules.ui.ravitaillement.adapters.RavitaillementAdapter
import kotlinx.coroutines.launch

class RavitaillementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRavitaillementBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService
    private lateinit var adapter: RavitaillementAdapter
    private var vehicles: List<com.example.gestionvehicules.data.model.Vehicle> = emptyList()
    private var stations: List<com.example.gestionvehicules.data.model.Station> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("RavitaillementActivity onCreate - Début")
        
        try {
            binding = ActivityRavitaillementBinding.inflate(layoutInflater)
            setContentView(binding.root)
            println("RavitaillementActivity - Layout initialisé")
            
            sessionManager = SessionManager(this)
            apiService = RetrofitClient.getApiService(this)
            
            setupUI()
            setupRecyclerView()
            setupClickListeners()
            loadData()
            println("RavitaillementActivity - Initialisation terminée")
        } catch (e: Exception) {
            println("Erreur dans RavitaillementActivity onCreate: ${e.message}")
            e.printStackTrace()
            
            Toast.makeText(this, "Erreur d'initialisation: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupUI() {
        try {
            println("RavitaillementActivity setupUI - Début")
            
            // Configuration de la toolbar
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Ravitaillement"
            
            binding.toolbar.setNavigationOnClickListener {
                finish()
            }
            
            println("RavitaillementActivity setupUI - Terminé")
        } catch (e: Exception) {
            println("Erreur dans setupUI: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = RavitaillementAdapter()
        binding.recyclerViewRavitaillements.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRavitaillements.adapter = adapter
        
        // Swipe to refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }
    }
    
    private fun setupClickListeners() {
        // Bouton pour ajouter un ravitaillement
        binding.fabAddRavitaillement.setOnClickListener {
            showAddRavitaillementDialog()
        }
    }
    
    private fun loadData() {
        binding.swipeRefreshLayout.isRefreshing = true
        loadRavitaillements()
        loadVehicles()
        loadStations()
    }
    
    private fun loadRavitaillements() {
        val token = sessionManager.authToken ?: return
        
        lifecycleScope.launch {
            try {
                val response = apiService.getFuelList("Bearer $token")
                
                if (response.isSuccessful) {
                    response.body()?.let { fuelResponse ->
                        adapter.updateData(fuelResponse.data)
                        binding.textViewEmpty.visibility = if (fuelResponse.data.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
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
    
    private fun loadStations() {
        val token = sessionManager.authToken ?: return
        
        lifecycleScope.launch {
            try {
                val response = apiService.getStationsList("Bearer $token")
                
                if (response.isSuccessful) {
                    response.body()?.let { stationsResponse ->
                        stations = stationsResponse.data
                    }
                }
            } catch (e: Exception) {
                println("Erreur chargement stations: ${e.message}")
            }
        }
    }
    
    private fun showAddRavitaillementDialog() {
        if (vehicles.isEmpty()) {
            Toast.makeText(this, "Chargement des véhicules...", Toast.LENGTH_SHORT).show()
            loadVehicles()
            return
        }
        
        val vehicleNames = vehicles.map { "${it.immatriculation} - ${it.marque} ${it.modele}" }.toTypedArray()
        val stationNames = stations.map { it.nom }.toTypedArray()
        
        var selectedVehicleIndex = 0
        var selectedStationIndex = -1
        var customStationName = ""
        
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ajouter un ravitaillement")
        
        // Créer un layout personnalisé pour le formulaire
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        
        // Sélection du véhicule
        val vehicleSpinner = android.widget.Spinner(this).apply {
            adapter = android.widget.ArrayAdapter(this@RavitaillementActivity, 
                android.R.layout.simple_spinner_item, vehicleNames)
        }
        layout.addView(android.widget.TextView(this).apply { text = "Véhicule:" })
        layout.addView(vehicleSpinner)
        
        // Sélection de la station
        val stationSpinner = android.widget.Spinner(this).apply {
            adapter = android.widget.ArrayAdapter(this@RavitaillementActivity, 
                android.R.layout.simple_spinner_item, arrayOf("Autre...") + stationNames)
        }
        layout.addView(android.widget.TextView(this).apply { text = "Station:" })
        layout.addView(stationSpinner)
        
        // Champ pour station personnalisée
        val customStationInput = android.widget.EditText(this).apply {
            hint = "Nom de la station"
            visibility = android.view.View.GONE
        }
        layout.addView(customStationInput)
        
        // Kilométrage avant
        val kmAvantInput = android.widget.EditText(this).apply {
            hint = "Kilométrage avant"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        layout.addView(android.widget.TextView(this).apply { text = "Kilométrage avant:" })
        layout.addView(kmAvantInput)
        
        // Kilométrage après
        val kmApresInput = android.widget.EditText(this).apply {
            hint = "Kilométrage après"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        layout.addView(android.widget.TextView(this).apply { text = "Kilométrage après:" })
        layout.addView(kmApresInput)
        
        // Litres
        val litresInput = android.widget.EditText(this).apply {
            hint = "Litres"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        layout.addView(android.widget.TextView(this).apply { text = "Litres:" })
        layout.addView(litresInput)
        
        // Coût unitaire
        val coutInput = android.widget.EditText(this).apply {
            hint = "Coût unitaire"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        layout.addView(android.widget.TextView(this).apply { text = "Coût unitaire:" })
        layout.addView(coutInput)
        
        // Gestion de la sélection de station
        stationSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedStationIndex = position - 1 // -1 car "Autre..." est à l'index 0
                customStationInput.visibility = if (position == 0) android.view.View.VISIBLE else android.view.View.GONE
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        builder.setView(layout)
        
        builder.setPositiveButton("Ajouter") { _, _ ->
            val selectedVehicle = vehicles[vehicleSpinner.selectedItemPosition]
            val selectedStation = if (selectedStationIndex >= 0) stations[selectedStationIndex] else null
            val stationName = if (selectedStation != null) selectedStation.nom else customStationInput.text.toString()
            
            val kmAvant = kmAvantInput.text.toString()
            val kmApres = kmApresInput.text.toString()
            val litres = litresInput.text.toString()
            val cout = coutInput.text.toString()
            
            if (kmApres.isNotEmpty() && litres.isNotEmpty() && cout.isNotEmpty() && stationName.isNotEmpty()) {
                createRavitaillement(
                    selectedVehicle.id,
                    selectedStation?.id,
                    stationName,
                    kmAvant,
                    kmApres,
                    litres,
                    cout
                )
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show()
            }
        }
        
        builder.setNegativeButton("Annuler", null)
        builder.show()
    }
    
    private fun createRavitaillement(
        vehicleId: Int,
        stationId: Int?,
        stationName: String,
        kmAvant: String,
        kmApres: String,
        litres: String,
        cout: String
    ) {
        val token = sessionManager.authToken ?: return
        
        lifecycleScope.launch {
            try {
                val response = apiService.createFuel(
                    "Bearer $token",
                    vehicleId.toString(),
                    stationId?.toString(),
                    stationName,
                    kmAvant,
                    kmApres,
                    litres,
                    cout
                )
                
                if (response.isSuccessful) {
                    Toast.makeText(this@RavitaillementActivity, "Ravitaillement ajouté avec succès!", Toast.LENGTH_SHORT).show()
                    loadRavitaillements() // Recharger la liste
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@RavitaillementActivity, "Erreur: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RavitaillementActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showError(message: String) {
        binding.errorTitleText.text = message
        binding.textViewError.visibility = android.view.View.VISIBLE
        binding.recyclerViewRavitaillements.visibility = android.view.View.GONE
        binding.textViewEmpty.visibility = android.view.View.GONE
    }
}
