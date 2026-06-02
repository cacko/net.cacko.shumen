package net.cacko.shumen.ui.noise

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private val _isAlarmActive = MutableStateFlow(false)
    val isAlarmActive: StateFlow<Boolean> = _isAlarmActive.asStateFlow()

    private var monitorJob: Job? = null
    private var quietModeStartTime: Long = 0
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startMonitoring() {
        if (_isMonitoring.value || _isAlarmActive.value) return
        
        monitorJob = viewModelScope.launch {
            _isMonitoring.value = true
            audioAnalyzer.startMonitoring().collect { db ->
                if (!_isAlarmActive.value) {
                    val adjustedDb = db * sensitivity.value
                    _currentDb.value = adjustedDb
                    checkQuietMode(adjustedDb)
                    
                    if (adjustedDb > threshold.value) {
                        triggerAlarm()
                    }
                }
            }
        }
    }

    private fun triggerAlarm() {
        viewModelScope.launch {
            _isAlarmActive.value = true
            stopMonitoring()
            
            // Play alarm sound
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 3000)
            
            delay(3000)
            
            _isAlarmActive.value = false
            startMonitoring()
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
        toneGenerator?.release()
    }
}
