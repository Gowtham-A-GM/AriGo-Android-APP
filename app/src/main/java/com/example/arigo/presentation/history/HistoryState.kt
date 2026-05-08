package com.example.arigo.presentation.history

import com.example.arigo.domain.model.AirQualityChartPoint
import com.example.arigo.domain.model.AqiStatus

data class HistoryState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPeriod: HistoryPeriod = HistoryPeriod.MONTH,
    val selectedDate: String = "",
    val selectedYear: Int = 2026,
    val selectedMonth: Int = 5,        // 1-12
    val selectedDay: Int = 1,          // 1-31, used by DAY/WEEK
    val searchCity: String = "",
    // City of the signed-in user from their profile — shown under each metric label.
    val city: String = "",

    // Current values from latest reading
    val currentAqi: Int = 0,
    val currentAqiStatus: AqiStatus = AqiStatus.GOOD,
    val currentPm25: Double = 0.0,
    val currentPm25Status: AqiStatus = AqiStatus.GOOD,
    val currentCo: Double = 0.0,
    val currentCoStatus: AqiStatus = AqiStatus.GOOD,

    // Chart data
    val chartData: List<AirQualityChartPoint> = emptyList(),
    val rawChartData: List<AirQualityChartPoint> = emptyList(),

    // Available dates from Firebase (yyyy-MM-dd, sorted ascending)
    val availableDates: List<String> = emptyList()
)

enum class HistoryPeriod(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}
