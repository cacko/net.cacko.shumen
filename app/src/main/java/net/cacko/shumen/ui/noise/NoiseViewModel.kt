package net.cacko.shumen.ui.noise

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.net.Uri
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

    val alarmDuration: StateFlow<Double> = repository.alarmDurationFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3.0)

    val alarmEnabled: StateFlow<Boolean> = repository.alarmEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val alarmSoundUri: StateFlow<String?> = repository.alarmSoundUriFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val alarmVolume: StateFlow<Double> = repository.alarmVolumeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0)

    private val _isQuietModeActive = MutableStateFlow(false)
    val isQuietModeActive: StateFlow<Boolean> = _isQuietModeActive.asStateFlow()

    private val _isAlarmActive = MutableStateFlow(false)
    val isAlarmActive: StateFlow<Boolean> = _isAlarmActive.asStateFlow()

    private var monitorJob: Job? = null
    private var quietModeStartTime: Long = 0
    private var toneGenerator1: ToneGenerator? = null
    private var toneGenerator2: ToneGenerator? = null
    private var mediaPlayer: MediaPlayer? = null
    private var lastUsedVolume: Int = -1

    init {
        // We will initialize tone generators with current volume when needed
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
            // 1. Mark alarm as active and STOP monitoring IMMEDIATELY
            _isAlarmActive.value = true
            stopMonitoring()
            
            // 2. Reset the displayed dB to 0 to reflect that we aren't measuring during alarm
            _currentDb.value = 0.0
            
            val totalDurationMs = (alarmDuration.value * 1000).toLong()
            val currentVolInt = (alarmVolume.value * 100).toInt().coerceIn(0, 100)
            val currentVolFloat = alarmVolume.value.toFloat().coerceIn(0f, 1f)
            
            // 3. Play sound (after monitoring is confirmed stopped) if enabled
            if (alarmEnabled.value) {
                val uriStr = alarmSoundUri.value
                if (uriStr != null) {
                    try {
                        mediaPlayer?.release()
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(getApplication(), Uri.parse(uriStr))
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_ALARM)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build()
                            )
                            setVolume(currentVolFloat, currentVolFloat)
                            isLooping = true
                            prepare()
                            start()
                        }
                        delay(totalDurationMs)
                        mediaPlayer?.stop()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // No automatic fallback to siren if a specific sound was selected.
                        // We wait for the duration to maintain visual consistency.
                        delay(totalDurationMs)
                    }
                } else {
                    playSuperAlarm(totalDurationMs, currentVolInt)
                }
            } else {
                // If audio alarm is disabled, just wait for the visual alarm duration
                delay(totalDurationMs)
            }
            
            // 4. Clean up sound
            toneGenerator1?.stopTone()
            toneGenerator2?.stopTone()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            
            // 5. Short "Cool Down" delay to let any echo/residual sound clear
            delay(500)
            
            // 6. Reset state and RESUME monitoring
            _isAlarmActive.value = false
            startMonitoring()
        }
    }

    private suspend fun playSuperAlarm(totalDurationMs: Long, volume: Int) {
        // Re-initialize tone generators if volume changed
        if (volume != lastUsedVolume || toneGenerator1 == null) {
            toneGenerator1?.release()
            toneGenerator2?.release()
            try {
                toneGenerator1 = ToneGenerator(AudioManager.STREAM_ALARM, volume)
                toneGenerator2 = ToneGenerator(AudioManager.STREAM_ALARM, volume)
                lastUsedVolume = volume
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
        }

        val startTime = System.currentTimeMillis()
        var toggle = false
        while (System.currentTimeMillis() - startTime < totalDurationMs && _isAlarmActive.value) {
            val remaining = totalDurationMs - (System.currentTimeMillis() - startTime)
            if (remaining > 0) {
                val toneA = if (toggle) ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK else ToneGenerator.TONE_SUP_ERROR
                val toneB = if (toggle) ToneGenerator.TONE_DTMF_D else ToneGenerator.TONE_CDMA_HIGH_L
                
                toneGenerator1?.startTone(toneA, remaining.toInt())
                toneGenerator2?.startTone(toneB, remaining.toInt())
            }
            toggle = !toggle
            delay(1000)
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

    fun setAlarmDuration(value: Double) {
        viewModelScope.launch {
            repository.saveAlarmDuration(value)
        }
    }

    fun setAlarmEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveAlarmEnabled(enabled)
        }
    }

    fun setAlarmSoundUri(uri: String?) {
        viewModelScope.launch {
            repository.saveAlarmSoundUri(uri)
        }
    }

    fun setAlarmVolume(volume: Double) {
        viewModelScope.launch {
            repository.saveAlarmVolume(volume)
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
        toneGenerator1?.release()
        toneGenerator2?.release()
        mediaPlayer?.release()
    }
}
