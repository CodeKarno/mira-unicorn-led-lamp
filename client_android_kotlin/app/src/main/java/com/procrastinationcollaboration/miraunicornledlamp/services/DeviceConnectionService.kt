package com.procrastinationcollaboration.miraunicornledlamp.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DeviceConnectionService {
    private const val TAG = "DeviceConnectionService"

    suspend fun checkDeviceServiceAvailable(serviceUrl: String?): Boolean = withContext(Dispatchers.Default) {
        val connectedSuccessfully: Boolean = try {
            LedLamp.getApiService(serviceUrl).getModes()
            true
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
            false
        }
        return@withContext connectedSuccessfully
    }
}