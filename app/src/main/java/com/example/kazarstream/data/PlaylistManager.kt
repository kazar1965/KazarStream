package com.example.kazarstream.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PlaylistManager(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "playlists")
        private val PLAYLISTS_KEY = stringPreferencesKey("playlists")
    }

    val playlists: Flow<List<Playlist>> = context.dataStore.data
        .map { preferences ->
            val playlistsJson = preferences[PLAYLISTS_KEY] ?: "[]"
            Json.decodeFromString<List<Playlist>>(playlistsJson)
        }

    suspend fun addPlaylist(name: String, url: String) {
        withContext(Dispatchers.IO) {
            val channels = M3UParser.parse(url)
            val newPlaylist = Playlist(name, url, channels = channels)
            
            context.dataStore.edit { preferences ->
                val currentPlaylists = preferences[PLAYLISTS_KEY]?.let {
                    Json.decodeFromString<List<Playlist>>(it)
                } ?: emptyList()

                val updatedPlaylists = currentPlaylists + newPlaylist
                preferences[PLAYLISTS_KEY] = Json.encodeToString(updatedPlaylists)
            }
        }
    }

    suspend fun removePlaylist(playlist: Playlist) {
        context.dataStore.edit { preferences ->
            val currentPlaylists = preferences[PLAYLISTS_KEY]?.let {
                Json.decodeFromString<List<Playlist>>(it)
            } ?: emptyList()

            val updatedPlaylists = currentPlaylists.filter { it.name != playlist.name }
            preferences[PLAYLISTS_KEY] = Json.encodeToString(updatedPlaylists)
        }
    }
} 