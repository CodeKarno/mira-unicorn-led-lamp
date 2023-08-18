package com.procrastinationcollaboration.miraunicornledlamp.ui.timer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.procrastinationcollaboration.miraunicornledlamp.repositories.DataStoreRepository
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

class TimerViewModel(private val dataStoreRepository: DataStoreRepository) : ViewModel() {
    private companion object {
        private const val WORK_TAG = "TIMER"
    }

    private val workManager = WorkManager.getInstance()

    val turnoffTime = dataStoreRepository.readTimeFromStore.asLiveData()
    val timerEnabled: LiveData<Boolean> get() = turnoffTime.map { turnoffTime.value != null }

    fun turnLampOffAfterDelay(delay: Int) {
        enqueueDelayedWork(delay)

        val timeString = getTurnoffTimeString(delay)

        viewModelScope.launch {
            dataStoreRepository.saveTimeToStore(timeString)
        }
    }

    fun cancelLampOff() {
        workManager.cancelAllWorkByTag(WORK_TAG)

        viewModelScope.launch {
            dataStoreRepository.clearTime()
        }
    }

    private fun getTurnoffTimeString(delay: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, delay)
        val dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
        return dateFormat.format(calendar.time)
    }

    private fun enqueueDelayedWork(delay: Int) {
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val request = OneTimeWorkRequestBuilder<TimerWorker>()
            .setInitialDelay(delay.toLong(), TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag(WORK_TAG)
            .build()

        workManager.enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, request)
    }
}

class TimerViewModelFactory(private val dataStoreRepository: DataStoreRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            return TimerViewModel(dataStoreRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel type")
    }
}