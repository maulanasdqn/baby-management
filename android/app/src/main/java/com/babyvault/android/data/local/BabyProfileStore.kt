package com.babyvault.android.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class BabyProfile(val name: String, val dobMillis: Long)

private val KEY_NAME = stringPreferencesKey("baby_name")
private val KEY_DOB = longPreferencesKey("baby_dob_millis")

@Singleton
class BabyProfileStore @Inject constructor(private val dataStore: DataStore<Preferences>) {

    val profile: Flow<BabyProfile?> = dataStore.data.map { prefs ->
        val name = prefs[KEY_NAME] ?: return@map null
        val dob = prefs[KEY_DOB] ?: return@map null
        BabyProfile(name, dob)
    }

    suspend fun save(name: String, dobMillis: Long) {
        dataStore.edit { prefs ->
            prefs[KEY_NAME] = name.trim()
            prefs[KEY_DOB] = dobMillis
        }
    }
}
