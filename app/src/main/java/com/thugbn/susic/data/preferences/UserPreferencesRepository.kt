package com.thugbn.susic.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.thugbn.susic.data.model.RepeatMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * DataStore for user preferences
 */
class UserPreferencesRepository(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

    companion object {
        private val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
        private val REPEAT_MODE = intPreferencesKey("repeat_mode")
        private val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val SLEEP_TIMER_DURATION = longPreferencesKey("sleep_timer_duration")
        private val VISUALIZER_TYPE = intPreferencesKey("visualizer_type")
        private val VISUALIZER_ENABLED = booleanPreferencesKey("visualizer_enabled")
    }

    val shuffleEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SHUFFLE_ENABLED] ?: false
        }

    val repeatMode: Flow<RepeatMode> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val modeOrdinal = preferences[REPEAT_MODE] ?: 0
            RepeatMode.entries[modeOrdinal]
        }

    val playbackSpeed: Flow<Float> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PLAYBACK_SPEED] ?: 1.0f
        }

    val themeMode: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[THEME_MODE] ?: "system"
        }

    val sleepTimerDuration: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SLEEP_TIMER_DURATION] ?: 0L
        }

    val visualizerType: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[VISUALIZER_TYPE] ?: 0 // Default to BAR (0)
        }

    val visualizerEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[VISUALIZER_ENABLED] ?: true
        }

    suspend fun updateShuffleEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHUFFLE_ENABLED] = enabled
        }
    }

    suspend fun updateRepeatMode(mode: RepeatMode) {
        context.dataStore.edit { preferences ->
            preferences[REPEAT_MODE] = mode.ordinal
        }
    }

    suspend fun updatePlaybackSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[PLAYBACK_SPEED] = speed
        }
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun updateSleepTimerDuration(duration: Long) {
        context.dataStore.edit { preferences ->
            preferences[SLEEP_TIMER_DURATION] = duration
        }
    }

    suspend fun updateVisualizerType(type: Int) {
        context.dataStore.edit { preferences ->
            preferences[VISUALIZER_TYPE] = type
        }
    }

    suspend fun updateVisualizerEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VISUALIZER_ENABLED] = enabled
        }
    }
}

