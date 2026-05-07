package com.example.arigo.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import android.util.Log
import com.example.arigo.AriGoApplication
import com.example.arigo.core.common.Resource
import com.example.arigo.data.remote.firebase.HardwareApiService
import com.example.arigo.domain.model.AqiStatus
import com.example.arigo.domain.model.PairedDevice
import com.example.arigo.domain.model.SensorReading
import com.example.arigo.domain.repository.DeviceRepository
import com.example.arigo.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

class HomeViewModel(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository,
    private val hardwareApi: HardwareApiService
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    /** Per-device sensor subscriptions, keyed by deviceId. */
    private val sensorJobs: MutableMap<String, Job> = mutableMapOf()

    init {
        loadUserProfile()
        observePairedDevices()
    }

    private fun loadUserProfile() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        userRepository.observeUserProfile(uid)
            .catch { /* swallow */ }
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

    private fun observePairedDevices() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        deviceRepository.observeDevices(uid)
            .catch { /* swallow */ }
            .onEach { paired ->
                _state.update { current ->
                    // Keep existing cards for devices that are still paired
                    val keptIds = paired.map { it.deviceId }.toSet()
                    val existingCards = current.devices.filter { it.deviceId in keptIds }

                    // Create placeholder cards for newly paired devices that don't have a card yet.
                    // applySensorReading() will upgrade these with real values when sensor data arrives.
                    val newCards = paired
                        .filter { p -> existingCards.none { it.deviceId == p.deviceId } }
                        .map { p ->
                            DeviceCardData(
                                deviceId = p.deviceId,
                                deviceName = p.nickname.ifBlank { "Filter Pro" }
                            )
                        }

                    current.copy(
                        pairedDevices = paired,
                        hasDevices = paired.isNotEmpty(),
                        devices = existingCards + newCards
                    )
                }
                syncSensorSubscriptions(paired)
            }
            .launchIn(viewModelScope)
    }

    private fun syncSensorSubscriptions(paired: List<PairedDevice>) {
        val pairedIds = paired.map { it.deviceId }.toSet()

        // Cancel sensor subscriptions for devices that are no longer paired
        sensorJobs.keys.toList().forEach { id ->
            if (id !in pairedIds) sensorJobs.remove(id)?.cancel()
        }

        // Start sensor subscriptions for newly paired devices
        paired.forEach { device ->
            if (device.deviceId !in sensorJobs) {
                Log.d("SensorData", "Starting sensor subscription for device: ${device.deviceId}")
                val job = hardwareApi.observeLatestReading(device.deviceId)
                    .catch { e ->
                        Log.e("SensorData", "Flow failed for ${device.deviceId}: ${e.message}", e)
                    }
                    .onEach { reading ->
                        Log.d(
                            "SensorData",
                            "Received reading for ${device.deviceId}: aqi=${reading.beforeAqi}"
                        )
                        applySensorReading(device, reading)
                    }
                    .launchIn(viewModelScope)
                sensorJobs[device.deviceId] = job
            }
        }
    }

    private fun applySensorReading(device: PairedDevice, reading: SensorReading) {
        _state.update { current ->
            val aqiStatus = AqiStatus.fromAqi(reading.beforeAqi)
            val pm25Status = pm25Status(reading.dustDensity)

            // Preserve any local-only toggle state on this card (e.g. isAutoMode)
            val previous = current.devices.firstOrNull { it.deviceId == device.deviceId }
            val card = DeviceCardData(
                deviceId = device.deviceId,
                deviceName = device.nickname.ifBlank { "Filter Pro" },
                aqi = reading.beforeAqi,
                aqiStatus = aqiStatus,
                pm25 = reading.dustDensity,
                pm25Status = pm25Status,
                motorState = reading.motorState,
                isAutoMode = previous?.isAutoMode ?: true,
                isOnline = true
            )

            val updatedDevices = if (previous != null) {
                current.devices.map { if (it.deviceId == device.deviceId) card else it }
            } else {
                current.devices + card
            }

            // The weather card mirrors the FIRST paired device's reading
            val isFirst = current.pairedDevices.firstOrNull()?.deviceId == device.deviceId
            val newEnv = if (isFirst) {
                current.environmentInfo.copy(
                    temperature = reading.temperature,
                    humidity = reading.humidity,
                    aqi = reading.beforeAqi,
                    aqiStatus = aqiStatus,
                    dustDensity = reading.dustDensity,
                    coPpm = reading.coPpm,
                    no2Ppm = reading.no2Ppm
                )
            } else current.environmentInfo

            current.copy(
                devices = updatedDevices,
                environmentInfo = newEnv
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

    // ----- Add Device flow ------------------------------------------------

    fun openMyDevicesSheet() = _state.update { it.copy(showMyDevicesSheet = true) }
    fun closeMyDevicesSheet() = _state.update { it.copy(showMyDevicesSheet = false) }

    fun openAddNewDeviceSheet() = _state.update {
        it.copy(showMyDevicesSheet = false, showAddNewDeviceSheet = true, addDeviceError = null)
    }

    fun closeAddNewDeviceSheet() = _state.update {
        it.copy(showAddNewDeviceSheet = false, addDeviceError = null)
    }

    fun clearAddDeviceError() = _state.update { it.copy(addDeviceError = null) }

    fun addNewDevice(productId: String, nickname: String) {
        val trimmedId = productId.trim()
        val trimmedNickname = nickname.trim()
        if (trimmedId.isEmpty() || trimmedNickname.isEmpty()) {
            _state.update { it.copy(addDeviceError = "Both fields are required") }
            return
        }

        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            _state.update { it.copy(addDeviceError = "Not signed in. Please log in again.") }
            return
        }

        _state.update { it.copy(isAddingDevice = true, addDeviceError = null) }

        viewModelScope.launch {
            val device = PairedDevice(
                deviceId = trimmedId,
                nickname = trimmedNickname,
                productId = trimmedId,
                pairedAt = Instant.now().toString(),
                active = true
            )
            when (val result = deviceRepository.addDevice(uid, device)) {
                is Resource.Success -> _state.update {
                    it.copy(
                        isAddingDevice = false,
                        showAddNewDeviceSheet = false,
                        showMyDevicesSheet = false,
                        addDeviceError = null
                    )
                }
                is Resource.Error -> _state.update {
                    it.copy(isAddingDevice = false, addDeviceError = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun removeDevice(deviceId: String) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            deviceRepository.removeDevice(uid, deviceId)
        }
    }

    private fun pm25Status(dust: Double): AqiStatus = when {
        dust <= 12.0 -> AqiStatus.GOOD
        dust <= 35.0 -> AqiStatus.NORMAL
        dust <= 55.0 -> AqiStatus.BAD
        else -> AqiStatus.HAZARDOUS
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AriGoApplication
                HomeViewModel(
                    firebaseAuth = app.container.firebaseAuth,
                    userRepository = app.container.userRepository,
                    deviceRepository = app.container.deviceRepository,
                    hardwareApi = app.container.hardwareApiService
                )
            }
        }
    }
}
