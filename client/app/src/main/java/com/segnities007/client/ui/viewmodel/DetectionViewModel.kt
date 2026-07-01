package com.segnities007.client.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.segnities007.client.R
import com.segnities007.client.model.Detection
import com.segnities007.client.network.DetectionApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetectionViewModel(
    application: Application,
    private val apiService: DetectionApiService,
) : AndroidViewModel(application) {

    private val _detections = MutableStateFlow<List<Detection>>(emptyList())
    val detections: StateFlow<List<Detection>> = _detections.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadDetections()
    }

    fun loadDetections() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getDetections()
                if (response.isSuccessful) {
                    _detections.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = getApplication<Application>().getString(
                        R.string.failed_to_load_with_code,
                        response.code(),
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                    ?: getApplication<Application>().getString(R.string.unknown_error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadDetections()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    class Factory(
        private val application: Application,
        private val apiService: DetectionApiService,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetectionViewModel::class.java)) {
                return DetectionViewModel(application, apiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
