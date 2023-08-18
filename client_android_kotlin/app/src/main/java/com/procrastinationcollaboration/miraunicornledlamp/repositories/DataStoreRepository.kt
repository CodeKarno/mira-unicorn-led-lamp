package com.procrastinationcollaboration.miraunicornledlamp.repositories

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.procrastinationcollaboration.miraunicornledlamp.services.Consts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreRepository(private val dataStore: DataStore<Preferences>) {
    private companion object PreferenceKeys {
        val TURNOFF_TIME = stringPreferencesKey(Consts.TURNOFF_TIME_PREFERENCE_KEY)
    }

    suspend fun saveTimeToStore(time: String) {
        dataStore.edit { preference -> preference[TURNOFF_TIME] = time }
    }

    suspend fun clearTime() {
        dataStore.edit { preference -> preference.remove(TURNOFF_TIME) }
    }

    val readTimeFromStore: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.d("DataStoreRepository", exception.message.toString())
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { pref ->
            val time = pref[TURNOFF_TIME]
            time
        }
}


