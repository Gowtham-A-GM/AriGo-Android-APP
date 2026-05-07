package com.example.arigo.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.arigo.AriGoApplication
import com.example.arigo.data.remote.firebase.SensorDataSource
import com.example.arigo.domain.model.AqiStatus
import com.example.arigo.domain.model.SensorReading
import com.example.arigo.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val sensorDataSource: SensorDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadUserProfile()
        loadLatestSensorData(DEFAULT_DEVICE_ID)
    }

    private fun loadUserProfile() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        userRepository.observeUserProfile(uid)
            .catch { /* swallow for now; surface via state.error in a later milestone */ }
            .onEach { profile ->
                _state.update { current ->
                    current.copy(
                        userName = profile?.name.orEmpty(),
                        environmentInfo = current.environmentInfo.copy(
                            city = profile?.city.orEmpty()
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadLatestSensorData(deviceId: String) {
        sensorDataSource.observeLatestReading(deviceId)
            .catch { /* swallow for now */ }
            .onEach { reading ->
                if (reading != null) applySensorReading(deviceId, reading)
            }
            .launchIn(viewModelScope)
    }

    private fun applySensorReading(deviceId: String, reading: SensorReading) {
        _state.update { current ->
            val aqiStatus = AqiStatus.fromAqi(reading.beforeAqi)
            val pm25Status = pm25Status(reading.dustDensity)

            // Preserve any local-only toggle state (e.g. isAutoMode toggled by user)
            val previous = current.devices.firstOrNull { it.deviceId == deviceId }
            val card = DeviceCardData(
                deviceId = deviceId,
                deviceName = "Filter Pro",
                aqi = reading.beforeAqi,
                aqiStatus = aqiStatus,
                pm25 = reading.dustDensity,
                pm25Status = pm25Status,
                motorState = reading.motorState,
                isAutoMode = previous?.isAutoMode ?: true,
                isOnline = true
            )

            current.copy(
                environmentInfo = current.environmentInfo.copy(
                    temperature = reading.temperature,
                    humidity = reading.humidity,
                    aqi = reading.beforeAqi,
                    aqiStatus = aqiStatus,
                    dustDensity = reading.dustDensity,
                    coPpm = reading.coPpm,
                    no2Ppm = reading.no2Ppm
                ),
                devices = listOf(card),
                hasDevices = true
            )
        }
    }

    fun togglePurifier(deviceId: String) {
        _state.update { current ->
            current.copy(
                devices = current.devices.map { device ->
                    if (device.deviceId == deviceId) device.copy(motorState = !device.motorState)
                    else device
                }
            )
        }
    }

    fun toggleAutoMode(deviceId: String) {
        _state.update { current ->
            current.copy(
                devices = current.devices.map { device ->
                    if (device.deviceId == deviceId) device.copy(isAutoMode = !device.isAutoMode)
                    else device
                }
            )
        }
    }

    private fun pm25Status(dust: Double): AqiStatus = when {
        dust <= 12.0 -> AqiStatus.GOOD
        dust <= 35.0 -> AqiStatus.NORMAL
        dust <= 55.0 -> AqiStatus.BAD
        else -> AqiStatus.HAZARDOUS
    }

    companion object {
        private const val DEFAULT_DEVICE_ID = "ARIGO_001"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AriGoApplication
                HomeViewModel(
                    firebaseAuth = app.container.firebaseAuth,
                    userRepository = app.container.userRepository,
                    sensorDataSource = SensorDataSource(app.container.firebaseDatabase)
                )
            }
        }
    }
}
