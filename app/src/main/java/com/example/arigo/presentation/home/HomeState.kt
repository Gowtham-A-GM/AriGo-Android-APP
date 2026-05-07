package com.example.arigo.presentation.home

import com.example.arigo.domain.model.AqiStatus
import com.example.arigo.domain.model.EnvironmentInfo
import com.example.arigo.domain.model.PairedDevice

data class HomeState(
    val userName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,

    // Weather/Environment card data (from latest sensor reading)
    val environmentInfo: EnvironmentInfo = EnvironmentInfo(),

    // Devices
    val devices: List<DeviceCardData> = emptyList(),
    val hasDevices: Boolean = false,

    // Paired devices (Firestore source of truth)
    val pairedDevices: List<PairedDevice> = emptyList(),

    // Add Device flow
    val showMyDevicesSheet: Boolean = false,
    val showAddNewDeviceSheet: Boolean = false,
    val isAddingDevice: Boolean = false,
    val addDeviceError: String? = null
)

data class DeviceCardData(
    val deviceId: String = "",
    val deviceName: String = "Filter Pro",
    val aqi: Int = 0,
    val aqiStatus: AqiStatus = AqiStatus.GOOD,
    val pm25: Double = 0.0,
    val pm25Status: AqiStatus = AqiStatus.GOOD,
    val motorState: Boolean = false,
    val isAutoMode: Boolean = true,
    val isOnline: Boolean = false
)
