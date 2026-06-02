package net.cacko.shumen.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.math.log10
import kotlin.math.sqrt

class AudioAnalyzer {

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    }

    @SuppressLint("MissingPermission")
    fun startMonitoring(): Flow<Double> = flow {
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE
        )

        val buffer = ShortArray(BUFFER_SIZE)
        audioRecord.startRecording()

        try {
            while (true) {
                val readSize = audioRecord.read(buffer, 0, BUFFER_SIZE)
                if (readSize > 0) {
                    val db = calculateDecibels(buffer, readSize)
                    emit(db)
                }
                delay(100) // Update every 100ms
            }
        } finally {
            audioRecord.stop()
            audioRecord.release()
        }
    }.flowOn(Dispatchers.IO)

    private fun calculateDecibels(buffer: ShortArray, readSize: Int): Double {
        var sum = 0.0
        for (i in 0 until readSize) {
            sum += buffer[i] * buffer[i]
        }
        val rms = sqrt(sum / readSize)
        
        // Decibel calculation: 20 * log10(RMS / reference)
        // Reference for 16-bit PCM is 32767.0
        // We use a small offset to avoid log10(0)
        val reference = 32767.0
        val db = if (rms > 0) 20 * log10(rms / reference) + 90 else 0.0 
        // Adding 90 is a common heuristic to map it to a positive range (0-90 dB)
        // since log10(rms/ref) will be negative.
        return db.coerceIn(0.0, 100.0)
    }
}
