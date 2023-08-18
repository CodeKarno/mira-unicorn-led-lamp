package com.procrastinationcollaboration.miraunicornledlamp.ui.timer

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.procrastinationcollaboration.miraunicornledlamp.repositories.DataStoreRepository
import com.procrastinationcollaboration.miraunicornledlamp.services.Consts
import com.procrastinationcollaboration.miraunicornledlamp.services.LedLamp

class TimerWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private companion object {
        private const val TAG = "WORK"
    }
    private lateinit var dataStoreRepository: DataStoreRepository

    override suspend fun doWork(): Result {
        dataStoreRepository = DataStoreRepository(applicationContext.dataStore)

        return try {
            LedLamp.apiService.changeState(Consts.OFF_MODE, null, null)
            dataStoreRepository.clearTime()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
            Result.failure()
        }
    }
}