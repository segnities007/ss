package com.segnities007.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.segnities007.client.model.IoTControlStatus
import com.segnities007.client.model.IoTDeviceSettings
import com.segnities007.client.model.UpdateIoTControlRequest
import com.segnities007.client.network.IoTControlApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val apiService: IoTControlApiService,
) : ViewModel() {

    private val _status = MutableStateFlow<IoTControlStatus?>(null)
    val status: StateFlow<IoTControlStatus?> = _status.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                loadStatus(showLoading = _status.value == null)
                delay(STATUS_REFRESH_INTERVAL_MILLIS)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadStatus(showLoading = true)
        }
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        if (_isUpdating.value) return

        viewModelScope.launch {
            _isUpdating.value = true
            _errorMessage.value = null
            try {
                val response = apiService.updateIoTControl(
                    UpdateIoTControlRequest(monitoringEnabled = enabled),
                )
                if (response.isSuccessful) {
                    _status.value = response.body()
                        ?: throw IllegalStateException("Server response is empty")
                } else {
                    _errorMessage.value = "監視状態を変更できませんでした: ${response.code()}"
                }
            } catch (error: Exception) {
                _errorMessage.value = error.message ?: "監視状態を変更できませんでした"
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun setBuzzerEnabled(enabled: Boolean) {
        val current = _status.value ?: return
        updateDeviceSettings(current.settings.copy(buzzerEnabled = enabled))
    }

    fun setCameraEnabled(enabled: Boolean) {
        val current = _status.value ?: return
        updateDeviceSettings(current.settings.copy(cameraEnabled = enabled))
    }

    fun setPirSensorEnabled(enabled: Boolean) {
        val current = _status.value ?: return
        updateDeviceSettings(current.settings.copy(pirSensorEnabled = enabled))
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun updateDeviceSettings(settings: IoTDeviceSettings) {
        if (_isUpdating.value) return

        viewModelScope.launch {
            _isUpdating.value = true
            _errorMessage.value = null
            try {
                val response = apiService.updateIoTDeviceSettings(settings)
                if (response.isSuccessful) {
                    _status.value = response.body()
                        ?: throw IllegalStateException("Server response is empty")
                } else {
                    _errorMessage.value = "デバイス設定を変更できませんでした: ${response.code()}"
                }
            } catch (error: Exception) {
                _errorMessage.value = error.message ?: "デバイス設定を変更できませんでした"
            } finally {
                _isUpdating.value = false
            }
        }
    }

    private suspend fun loadStatus(showLoading: Boolean) {
        if (showLoading) _isLoading.value = true
        try {
            val response = apiService.getIoTControlStatus()
            if (response.isSuccessful) {
                _status.value = response.body()
                    ?: throw IllegalStateException("Server response is empty")
            } else {
                _errorMessage.value = "IoT状態を取得できませんでした: ${response.code()}"
            }
        } catch (error: Exception) {
            _errorMessage.value = error.message ?: "IoT状態を取得できませんでした"
        } finally {
            if (showLoading) _isLoading.value = false
        }
    }

    class Factory(
        private val apiService: IoTControlApiService,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(apiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private companion object {
        const val STATUS_REFRESH_INTERVAL_MILLIS = 5_000L
    }
}
