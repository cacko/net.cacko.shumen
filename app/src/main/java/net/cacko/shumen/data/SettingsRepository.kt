package net.cacko.shumen.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val THRESHOLD_KEY = doublePreferencesKey("noise_threshold")
        private val SENSITIVITY_KEY = doublePreferencesKey("noise_sensitivity")
        private val ALARM_DURATION_KEY = doublePreferencesKey("alarm_duration")
    }

    val thresholdFlow: Flow<Double> = context.dataStore.data
        .map { preferences ->
            preferences[THRESHOLD_KEY] ?: 70.0
        }

    val sensitivityFlow: Flow<Double> = context.dataStore.data
        .map { preferences ->
            preferences[SENSITIVITY_KEY] ?: 1.0
        }

    val alarmDurationFlow: Flow<Double> = context.dataStore.data
        .map { preferences ->
            preferences[ALARM_DURATION_KEY] ?: 3.0
        }

    suspend fun saveThreshold(threshold: Double) {
        context.dataStore.edit { preferences ->
            preferences[THRESHOLD_KEY] = threshold
        }
    }

    suspend fun saveSensitivity(sensitivity: Double) {
        context.dataStore.edit { preferences ->
            preferences[SENSITIVITY_KEY] = sensitivity
        }
    }

    suspend fun saveAlarmDuration(duration: Double) {
        context.dataStore.edit { preferences ->
            preferences[ALARM_DURATION_KEY] = duration
        }
    }
}
