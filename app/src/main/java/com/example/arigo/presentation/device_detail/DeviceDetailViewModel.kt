package com.example.arigo.presentation.device_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.arigo.AriGoApplication
import com.example.arigo.data.remote.firebase.HardwareApiService
import com.example.arigo.domain.model.AqiStatus
import com.example.arigo.domain.repository.DeviceRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class DeviceDetailViewModel(
    private val deviceId: String,
    private val firebaseAuth: FirebaseAuth,
    private val deviceRepository: DeviceRepository,
    private val hardwareApi: HardwareApiService
) : ViewModel() {

    private val _state = MutableStateFlow(DeviceDetailState(deviceId = deviceId))
    val state: StateFlow<DeviceDetailState> = _state.asStateFlow()

    init {
        observeNickname()
        observeSensorData()
    }

    private fun observeNickname() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        deviceRepository.observeDevices(uid)
            .catch { /* swallow */ }
            .onEach { paired ->
                val match = paired.firstOrNull { it.deviceId == deviceId }
                if (match != null) {
                    _state.update { it.copy(deviceName = match.nickname.ifBlank { "Filter Pro" }) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSensorData() {
        if (deviceId.isBlank()) {
            Log.w("DeviceDetail", "Cannot observe sensor data: deviceId is blank")
            return
        }
        hardwareApi.observeLatestReading(deviceId)
            .catch { e -> Log.e("DeviceDetail", "Sensor flow failed: ${e.message}", e) }
            .onEach { reading ->
                val status = AqiStatus.fromAqi(reading.beforeAqi)
                _state.update { current ->
                    current.copy(
                        aqi = reading.beforeAqi,
                        pm25 = reading.dustDensity,
                        co = reading.coPpm,
                        no2 = reading.no2Ppm,
                        afterAqi = reading.afterAqi,
                        // Local toggle state for isAutoMode is preserved across sensor updates
                        motorState = reading.motorState,
                        aqiStatus = status,
                        statusMessage = statusMessageFor(status)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun togglePurifier() {
        _state.update { it.copy(motorState = !it.motorState) }
    }

    fun toggleAutoMode() {
        _state.update { it.copy(isAutoMode = !it.isAutoMode) }
    }

    private fun statusMessageFor(status: AqiStatus): String = when (status) {
        AqiStatus.GOOD -> "The air quality is looking good at this moment."
        AqiStatus.NORMAL -> "Air quality is moderate. Wear the mask for safety."
        AqiStatus.BAD, AqiStatus.HAZARDOUS -> "Air quality is very harmful. Please, wear the mask !"
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AriGoApplication
                val savedStateHandle = createSavedStateHandle()
                val deviceId = savedStateHandle.get<String>("deviceId").orEmpty()
                DeviceDetailViewModel(
                    deviceId = deviceId,
                    firebaseAuth = app.container.firebaseAuth,
                    deviceRepository = app.container.deviceRepository,
                    hardwareApi = app.container.hardwareApiService
                )
            }
        }
    }
}
