package net.cacko.shumen.ui.noise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.cacko.shumen.audio.AudioAnalyzer
import net.cacko.shumen.data.SettingsRepository

class NoiseViewModel(application: Application) : AndroidViewModel(application) {
    private val audioAnalyzer = AudioAnalyzer()
    private val repository = SettingsRepository(application)
    
    private val _currentDb = MutableStateFlow(0.0)
    val currentDb: StateFlow<Double> = _currentDb.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    val threshold: StateFlow<Double> = repository.thresholdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 70.0)

    val sensitivity: StateFlow<Double> = repository.sensitivityFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0)

    private val _isQuietModeActive = MutableStateFlow(false)
    val isQuietModeActive: StateFlow<Boolean> = _isQuietModeActive.asStateFlow()

    private var monitorJob: Job? = null
    private var quietModeStartTime: Long = 0

    fun startMonitoring() {
        if (_isMonitoring.value) return
        
        monitorJob = viewModelScope.launch {
            _isMonitoring.value = true
            audioAnalyzer.startMonitoring().collect { db ->
                // Apply sensitivity multiplier if needed
                val adjustedDb = db * sensitivity.value
                _currentDb.value = adjustedDb
                checkQuietMode(adjustedDb)
            }
        }
    }

    private fun checkQuietMode(db: Double) {
        if (db > threshold.value) {
            if (quietModeStartTime == 0L) {
                quietModeStartTime = System.currentTimeMillis()
            } else if (System.currentTimeMillis() - quietModeStartTime > 3000) {
                _isQuietModeActive.value = true
            }
        } else {
            quietModeStartTime = 0
            _isQuietModeActive.value = false
        }
    }

    fun setThreshold(value: Double) {
        viewModelScope.launch {
            repository.saveThreshold(value)
        }
    }

    fun setSensitivity(value: Double) {
        viewModelScope.launch {
            repository.saveSensitivity(value)
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        _isMonitoring.value = false
        _currentDb.value = 0.0
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
