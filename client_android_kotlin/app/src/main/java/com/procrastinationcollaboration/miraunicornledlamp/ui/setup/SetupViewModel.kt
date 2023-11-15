package com.procrastinationcollaboration.miraunicornledlamp.ui.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.procrastinationcollaboration.miraunicornledlamp.repositories.DataStoreRepository
import com.procrastinationcollaboration.miraunicornledlamp.services.DeviceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(private val dataStoreRepository: DataStoreRepository) : ViewModel() {
    private val _deviceConnectionState = MutableLiveData<DeviceConnection>()
    private val _extendedSettingsEnabled = MutableLiveData<Boolean>()
    val extendedSettingsEnabled: LiveData<Boolean> get() = _extendedSettingsEnabled
    val ledLampServiceUrl = dataStoreRepository.readBaseUrlFromStore.asLiveData()
    val connectToDeviceEnabled: LiveData<Boolean>
        get() = _deviceConnectionState.map {
            _deviceConnectionState.value == DeviceConnection.NOT_AVAILABLE ||
                    _deviceConnectionState.value == DeviceConnection.OLD_API_CONNECTED
        }
    val connectToWiFiEnabled: LiveData<Boolean>
        get() = _deviceConnectionState.map {
            _deviceConnectionState.value == DeviceConnection.TEMP_AP_CONNECTED ||
                    _deviceConnectionState.value == DeviceConnection.WIFI_CONNECTION_FAILED ||
                    _deviceConnectionState.value == DeviceConnection.OLD_API_CONNECTED
        }

    init {
        _deviceConnectionState.value = DeviceConnection.NOT_AVAILABLE
        _extendedSettingsEnabled.value = false
    }

    fun setConnectionState(state: DeviceConnection) {
        _deviceConnectionState.postValue(state)
    }

    fun setExtendedSettingsEnabled(enabled: Boolean) {
        _extendedSettingsEnabled.value = enabled
    }

    fun setServiceIpAddress(ipAddr: String) {
        viewModelScope.launch {
            dataStoreRepository.saveServiceAddressToStore(ipAddr)
        }
    }
}