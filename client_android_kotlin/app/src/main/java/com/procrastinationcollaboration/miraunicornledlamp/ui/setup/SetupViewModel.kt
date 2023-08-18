package com.procrastinationcollaboration.miraunicornledlamp.ui.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.procrastinationcollaboration.miraunicornledlamp.services.DeviceConnection

class SetupViewModel : ViewModel() {
    private val _deviceConnectionState = MutableLiveData<DeviceConnection>()

    val deviceConnectionState: LiveData<DeviceConnection> get() = _deviceConnectionState

    init {
        _deviceConnectionState.value = DeviceConnection.NOT_AVAILABLE
    }

    fun setConnectionState(state: DeviceConnection) {
        _deviceConnectionState.postValue(state)
    }
}