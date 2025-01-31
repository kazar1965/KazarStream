package com.example.kazarstream.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    companion object {
        private val LAST_URL = stringPreferencesKey("last_url")
        private val DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")
    }

    // Obtener la última URL
    val lastUrl: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_URL]
        }

    // Obtener el estado del aviso
    val disclaimerAccepted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DISCLAIMER_ACCEPTED] ?: false
        }

    // Guardar la URL
    suspend fun saveUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_URL] = url
        }
    }

    // Guardar el estado del aviso
    suspend fun setDisclaimerAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DISCLAIMER_ACCEPTED] = accepted
        }
    }
} 