package com.procrastinationcollaboration.miraunicornledlamp.repositories

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.procrastinationcollaboration.miraunicornledlamp.services.Consts
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(
    name = Consts.USER_PREFERENCES_NAME
)

@Singleton
class DataStoreRepository @Inject constructor(@ApplicationContext appContext: Context) {
    private val dataStore = appContext.dataStore

    private companion object PreferenceKeys {
        val SERVICE_ADDRESS = stringPreferencesKey(Consts.SERVICE_ADDRESS_PREFERENCE_KEY)
        private const val TAG = "DataStoreRepository"
    }

    suspend fun saveServiceAddressToStore(ipAddress: String) {
        dataStore.edit { preference -> preference[SERVICE_ADDRESS] = ipAddress }
    }

    val readBaseUrlFromStore: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, exception.message.toString())
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { pref ->
            val url = pref[SERVICE_ADDRESS]
            url
        }
}