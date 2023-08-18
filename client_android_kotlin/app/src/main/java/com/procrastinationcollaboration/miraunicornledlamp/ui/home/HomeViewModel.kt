package com.procrastinationcollaboration.miraunicornledlamp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.procrastinationcollaboration.miraunicornledlamp.services.Consts
import com.procrastinationcollaboration.miraunicornledlamp.services.LedLamp
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _mode = MutableLiveData<String>()
    private val _color = MutableLiveData<Int?>()
    private val _brightness = MutableLiveData<Int>()
    private val _modes = MutableLiveData<Array<String>>()

    val modes: LiveData<Array<String>> get() = _modes
    val mode: LiveData<String> get() = _mode
    val color: LiveData<Int?> get() = _color
    val brightness: LiveData<Int> get() = _brightness
    val enabled: LiveData<Boolean> get() = _mode.map { !_mode.value.equals(Consts.OFF_MODE) }
    val colorAvailable: LiveData<Boolean> get() = _mode.map { _mode.value.equals(Consts.COLOR_MODE) }

    init {
        _modes.value = arrayOf("")
    }

    fun getLedLampModes() {
        Log.d(TAG, "get modes called")
        viewModelScope.launch {
            try {
                val response = LedLamp.apiService.getModes()
                _modes.value = response.modes
                    .filter { i -> !i.startsWith(Consts.SPEC_MODE_PREFIX) }
                    .toTypedArray()

            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
        }
    }

    fun getStateFromLamp() {
        Log.d(TAG, "get state")
        viewModelScope.launch {
            try {
                val response = LedLamp.apiService.getState()
                _mode.value = response.mode
                _color.value = response.color
                _brightness.value = response.brightness
            } catch (e: java.lang.Exception) {
                Log.e(TAG, e.message.toString())
            }
        }
    }

    fun setMode(mode: String) {
        _mode.value = mode
    }

    fun setColor(color: Int) {
        _color.value = color
    }

    fun setBrightness(brightness: Int) {
        _brightness.value = brightness
    }

    fun updateLampStateOnServer(mode: String?, color: String?, brightness: String?) {
        viewModelScope.launch {
            try {
                val response = LedLamp.apiService.changeState(
                    mode,
                    color,
                    brightness
                )
                Log.d(TAG, response.message.toString())
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
        }
    }
}
