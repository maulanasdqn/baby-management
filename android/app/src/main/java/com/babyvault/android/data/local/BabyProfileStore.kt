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
data class BabyProfile(val name: String, val dobMillis: Long, val photoUri: String? = null)
private val KEY_NAME = stringPreferencesKey("baby_name")
private val KEY_DOB = longPreferencesKey("baby_dob_millis")
private val KEY_PHOTO_URI = stringPreferencesKey("baby_photo_uri")
@Singleton
class BabyProfileStore @Inject constructor(private val dataStore: DataStore<Preferences>) {
    val profile: Flow<BabyProfile?> = dataStore.data.map { prefs ->
        val name = prefs[KEY_NAME] ?: return@map null
        val dob = prefs[KEY_DOB] ?: return@map null
        BabyProfile(name, dob, prefs[KEY_PHOTO_URI])
    }
    suspend fun save(name: String, dobMillis: Long) {
        dataStore.edit { prefs ->
            prefs[KEY_NAME] = name.trim()
            prefs[KEY_DOB] = dobMillis
        }
    }
    suspend fun savePhoto(uri: String?) {
        dataStore.edit { prefs ->
            if (uri != null) prefs[KEY_PHOTO_URI] = uri
            else prefs.remove(KEY_PHOTO_URI)
        }
    }
}
