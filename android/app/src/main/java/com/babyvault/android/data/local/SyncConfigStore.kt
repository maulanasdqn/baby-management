package com.babyvault.android.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncConfigStore @Inject constructor(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val SERVER_URL = stringPreferencesKey("sync_server_url")
        val API_KEY = stringPreferencesKey("sync_api_key")
    }

    val serverUrl: Flow<String?> = dataStore.data.map { it[Keys.SERVER_URL] }
    val apiKey: Flow<String?> = dataStore.data.map { it[Keys.API_KEY] }

    suspend fun save(serverUrl: String, apiKey: String) {
        dataStore.edit { prefs ->
            prefs[Keys.SERVER_URL] = serverUrl
            prefs[Keys.API_KEY] = apiKey
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.SERVER_URL)
            prefs.remove(Keys.API_KEY)
        }
    }
}
