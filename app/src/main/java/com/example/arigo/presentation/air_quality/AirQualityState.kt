package com.example.arigo.presentation.air_quality

import com.example.arigo.domain.model.AirQualityChartPoint
import com.example.arigo.domain.model.AqiStatus

data class AirQualityState(
    val deviceId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTimeRange: TimeRange = TimeRange.MINUTES,

    // Current values
    val currentAqi: Int = 0,
    val currentAqiStatus: AqiStatus = AqiStatus.GOOD,
    val currentPm25: Double = 0.0,
    val currentPm25Status: AqiStatus = AqiStatus.GOOD,
    val currentCo: Double = 0.0,
    val currentCoStatus: AqiStatus = AqiStatus.GOOD,
    val currentPm10: String = "N/A",

    // Raw chart data fetched from Firebase (preserved across tab changes)
    val rawChartData: List<AirQualityChartPoint> = emptyList(),
    // Chart data after applying selectedTimeRange grouping/aggregation
    val chartData: List<AirQualityChartPoint> = emptyList(),
    // Most recent date folder seen in the history; used to scope incremental refreshes.
    val latestDate: String = ""
)

enum class TimeRange(val label: String) {
    HOURS("Hours"),
    MINUTES("Minutes"),
    SECONDS("Seconds")
}
