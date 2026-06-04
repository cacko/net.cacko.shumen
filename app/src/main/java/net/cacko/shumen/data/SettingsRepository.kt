package net.cacko.shumen.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val THRESHOLD_KEY = doublePreferencesKey("noise_threshold")
        private val SENSITIVITY_KEY = doublePreferencesKey("noise_sensitivity")
        private val ALARM_DURATION_KEY = doublePreferencesKey("alarm_duration")
        private val ALARM_ENABLED_KEY = booleanPreferencesKey("alarm_enabled")
        private val ALARM_SOUND_URI_KEY = stringPreferencesKey("alarm_sound_uri")
        private val ALARM_VOLUME_KEY = doublePreferencesKey("alarm_volume")
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

    val alarmEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ALARM_ENABLED_KEY] ?: true
        }

    val alarmSoundUriFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[ALARM_SOUND_URI_KEY]
        }

    val alarmVolumeFlow: Flow<Double> = context.dataStore.data
        .map { preferences ->
            preferences[ALARM_VOLUME_KEY] ?: 1.0
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

    suspend fun saveAlarmEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ALARM_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveAlarmSoundUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri == null) {
                preferences.remove(ALARM_SOUND_URI_KEY)
            } else {
                preferences[ALARM_SOUND_URI_KEY] = uri
            }
        }
    }

    suspend fun saveAlarmVolume(volume: Double) {
        context.dataStore.edit { preferences ->
            preferences[ALARM_VOLUME_KEY] = volume
        }
    }
}
