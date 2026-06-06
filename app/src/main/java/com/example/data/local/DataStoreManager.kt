package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "battery_prefs")

class DataStoreManager(private val context: Context) {

    private val alertedFullKey = booleanPreferencesKey("alerted_full_for_current_session")

    val alertedFullForCurrentSessionFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[alertedFullKey] ?: false
        }

    suspend fun setAlertedFullForCurrentSession(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[alertedFullKey] = value
        }
    }
}
