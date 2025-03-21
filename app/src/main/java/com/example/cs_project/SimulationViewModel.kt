package com.example.cs_project

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class SimulationViewModel(application: Application) : AndroidViewModel(application) {

    private val _simulations = MutableStateFlow<Map<String, List<Simulation>>>(emptyMap())
    val simulations: StateFlow<Map<String, List<Simulation>>> get() = _simulations.asStateFlow()

    private val jsonFormat = Json { prettyPrint = true }

    fun loadSimulationsFromAssets() {
        viewModelScope.launch {
            try {
                val jsonContent = getApplication<Application>().assets.open("simulations.json")
                    .bufferedReader().use { it.readText() }

                val simulationsMap: Map<String, List<Simulation>> =
                    jsonFormat.decodeFromString(jsonContent)

                _simulations.value = simulationsMap
                Log.d("JSON", "Loaded simulations from assets: $simulationsMap")

            } catch (e: Exception) {
                Log.e("JSON", "Error loading simulations from assets", e)
            }
        }
    }
}