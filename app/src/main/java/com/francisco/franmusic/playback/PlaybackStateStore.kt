package com.francisco.franmusic.playback

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.playbackDataStore: DataStore<Preferences> by preferencesDataStore(name = "franmusic_playback_session")

data class SavedPlaybackSession(
    val currentSongId: String? = null,
    val queueIds: List<String> = emptyList(),
    val positionMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = 0,
    val lastUpdated: Long = 0L
) {
    val isValid: Boolean get() = !currentSongId.isNullOrBlank() && queueIds.isNotEmpty()
}

class PlaybackStateStore(private val context: Context) {

    private object PreferencesKeys {
        val CURRENT_SONG_ID = stringPreferencesKey("current_song_id")
        val QUEUE_IDS = stringPreferencesKey("queue_ids")
        val POSITION_MS = longPreferencesKey("position_ms")
        val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val LAST_UPDATED = longPreferencesKey("last_updated")
        val RESTORE_SESSION_ENABLED = booleanPreferencesKey("restore_session_enabled")
    }

    val sessionFlow: Flow<SavedPlaybackSession> = context.playbackDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val songId = preferences[PreferencesKeys.CURRENT_SONG_ID]
            val queueRaw = preferences[PreferencesKeys.QUEUE_IDS] ?: ""
            val queueList = if (queueRaw.isNotBlank()) queueRaw.split(",") else emptyList()
            val position = preferences[PreferencesKeys.POSITION_MS] ?: 0L
            val shuffle = preferences[PreferencesKeys.SHUFFLE_ENABLED] ?: false
            val repeat = preferences[PreferencesKeys.REPEAT_MODE] ?: 0
            val updated = preferences[PreferencesKeys.LAST_UPDATED] ?: 0L

            SavedPlaybackSession(
                currentSongId = songId,
                queueIds = queueList,
                positionMs = position,
                shuffleEnabled = shuffle,
                repeatMode = repeat,
                lastUpdated = updated
            )
        }

    val restoreSessionEnabledFlow: Flow<Boolean> = context.playbackDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.RESTORE_SESSION_ENABLED] ?: true
        }

    suspend fun saveSession(
        currentSongId: String?,
        queueIds: List<String>,
        positionMs: Long,
        shuffleEnabled: Boolean,
        repeatMode: Int
    ) {
        context.playbackDataStore.edit { preferences ->
            if (currentSongId != null) {
                preferences[PreferencesKeys.CURRENT_SONG_ID] = currentSongId
            } else {
                preferences.remove(PreferencesKeys.CURRENT_SONG_ID)
            }
            preferences[PreferencesKeys.QUEUE_IDS] = queueIds.joinToString(",")
            preferences[PreferencesKeys.POSITION_MS] = positionMs
            preferences[PreferencesKeys.SHUFFLE_ENABLED] = shuffleEnabled
            preferences[PreferencesKeys.REPEAT_MODE] = repeatMode
            preferences[PreferencesKeys.LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    suspend fun updatePosition(positionMs: Long) {
        context.playbackDataStore.edit { preferences ->
            preferences[PreferencesKeys.POSITION_MS] = positionMs
            preferences[PreferencesKeys.LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    suspend fun setRestoreSessionEnabled(enabled: Boolean) {
        context.playbackDataStore.edit { preferences ->
            preferences[PreferencesKeys.RESTORE_SESSION_ENABLED] = enabled
        }
    }

    suspend fun clearSession() {
        context.playbackDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.CURRENT_SONG_ID)
            preferences.remove(PreferencesKeys.QUEUE_IDS)
            preferences.remove(PreferencesKeys.POSITION_MS)
            preferences.remove(PreferencesKeys.SHUFFLE_ENABLED)
            preferences.remove(PreferencesKeys.REPEAT_MODE)
            preferences.remove(PreferencesKeys.LAST_UPDATED)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PlaybackStateStore? = null

        fun getInstance(context: Context): PlaybackStateStore {
            return INSTANCE ?: synchronized(this) {
                val instance = PlaybackStateStore(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
