package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nutriguide_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        private val KEY_LOGGED_IN_EMAIL = stringPreferencesKey("logged_in_email")
        private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language") // "en" or "ru"
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_GROQ_API_KEY = stringPreferencesKey("groq_api_key")
    }

    val loggedInEmailFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_LOGGED_IN_EMAIL]
    }

    val appLanguageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_APP_LANGUAGE] ?: "en"
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE] ?: false
    }

    val groqApiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_GROQ_API_KEY] ?: ""
    }

    suspend fun setLoggedInUser(email: String?) {
        context.dataStore.edit { preferences ->
            if (email != null) {
                preferences[KEY_LOGGED_IN_EMAIL] = email
            } else {
                preferences.remove(KEY_LOGGED_IN_EMAIL)
            }
        }
    }

    suspend fun setAppLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_LANGUAGE] = language
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun setGroqApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GROQ_API_KEY] = key
        }
    }
}
