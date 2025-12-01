package com.example.gestionvehicules.ui.requester

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gestionvehicules.data.api.ApiService
import com.example.gestionvehicules.data.api.RetrofitClient
import com.example.gestionvehicules.data.api.SessionManager
import com.example.gestionvehicules.data.model.CourseRequest
import com.example.gestionvehicules.databinding.ActivityNewRequestBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NewRequestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewRequestBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService
    
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        apiService = RetrofitClient.getApiService(this)
        
        setupUI()
        setupClickListeners()
    }
    
    private fun setupUI() {
        // Configuration de la toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nouvelle demande"
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Valeurs par défaut
        binding.etDate.setText(dateFormat.format(calendar.time))
        binding.etTime.setText(timeFormat.format(calendar.time))
        binding.etPassengers.setText("1")
    }
    
    private fun setupClickListeners() {
        // Sélection de la date
        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }
        
        // Sélection de l'heure
        binding.btnSelectTime.setOnClickListener {
            showTimePicker()
        }
        
        // Soumettre la demande
        binding.btnSubmit.setOnClickListener {
            submitRequest()
        }
        
        // Réinitialiser
        binding.btnReset.setOnClickListener {
            resetForm()
        }
    }
    
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                binding.etDate.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Limiter la date minimale à aujourd'hui
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }
    
    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                binding.etTime.setText(timeFormat.format(calendar.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }
    
    private fun submitRequest() {
        // Validation
        if (!validateForm()) {
            return
        }
        
        val token = sessionManager.authToken ?: run {
            Toast.makeText(this, "Erreur de session", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.btnSubmit.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        val request = CourseRequest(
            point_embarquement = binding.etEmbarquement.text?.toString()?.trim() ?: "",
            destination = binding.etDestination.text?.toString()?.trim() ?: "",
            motif = binding.etMotif.text?.toString()?.trim() ?: "",
            nombre_passagers = binding.etPassengers.text?.toString()?.toIntOrNull() ?: 1,
            date_souhaitee = "${binding.etDate.text} ${binding.etTime.text}",
            observations = binding.etObservations.text?.toString()?.trim()
        )
        
        lifecycleScope.launch {
            try {
                val response = apiService.createCourse("Bearer $token", request)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@NewRequestActivity, "Demande créée avec succès!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(
                        this@NewRequestActivity,
                        "Erreur: ${response.code()} - $errorBody",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@NewRequestActivity,
                    "Erreur réseau: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnSubmit.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        // Point d'embarquement
        val embarquement = binding.etEmbarquement.text?.toString()?.trim()
        if (embarquement.isNullOrEmpty()) {
            binding.etEmbarquement.error = "Obligatoire"
            isValid = false
        }
        
        // Destination
        val destination = binding.etDestination.text?.toString()?.trim()
        if (destination.isNullOrEmpty()) {
            binding.etDestination.error = "Obligatoire"
            isValid = false
        }
        
        // Motif
        val motif = binding.etMotif.text?.toString()?.trim()
        if (motif.isNullOrEmpty()) {
            binding.etMotif.error = "Obligatoire"
            isValid = false
        }
        
        // Nombre de passagers
        val passengers = binding.etPassengers.text?.toString()?.toIntOrNull()
        if (passengers == null || passengers < 1 || passengers > 10) {
            binding.etPassengers.error = "Entre 1 et 10"
            isValid = false
        }
        
        // Date
        val date = binding.etDate.text?.toString()?.trim()
        if (date.isNullOrEmpty()) {
            binding.etDate.error = "Obligatoire"
            isValid = false
        }
        
        // Heure
        val time = binding.etTime.text?.toString()?.trim()
        if (time.isNullOrEmpty()) {
            binding.etTime.error = "Obligatoire"
            isValid = false
        }
        
        return isValid
    }
    
    private fun resetForm() {
        binding.etEmbarquement.text?.clear()
        binding.etDestination.text?.clear()
        binding.etMotif.text?.clear()
        binding.etPassengers.setText("1")
        binding.etObservations.text?.clear()
        
        // Réinitialiser à la date et heure actuelles
        calendar.timeInMillis = System.currentTimeMillis()
        binding.etDate.setText(dateFormat.format(calendar.time))
        binding.etTime.setText(timeFormat.format(calendar.time))
        
        // Effacer les erreurs
        binding.etEmbarquement.error = null
        binding.etDestination.error = null
        binding.etMotif.error = null
        binding.etPassengers.error = null
        binding.etDate.error = null
        binding.etTime.error = null
    }
}
