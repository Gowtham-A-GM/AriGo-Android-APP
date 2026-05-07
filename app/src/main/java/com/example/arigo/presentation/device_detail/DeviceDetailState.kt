package com.example.arigo.presentation.device_detail

import com.example.arigo.domain.model.AqiStatus

data class DeviceDetailState(
    val deviceId: String = "",
    val deviceName: String = "Filter Pro",
    val isLoading: Boolean = false,
    val error: String? = null,

    // Sensor data
    val aqi: Int = 0,
    val pm25: Double = 0.0,
    val pm10: String = "N/A",       // placeholder (no sensor yet)
    val co: Double = 0.0,
    val no2: Double = 0.0,
    val so2: String = "N/A",        // placeholder (no sensor yet)
    val aqiStatus: AqiStatus = AqiStatus.GOOD,

    // After filtration
    val afterAqi: Double = 0.0,

    // Controls
    val motorState: Boolean = false,
    val isAutoMode: Boolean = true,

    // Status message
    val statusMessage: String = "The air quality is looking good at this moment."
)
