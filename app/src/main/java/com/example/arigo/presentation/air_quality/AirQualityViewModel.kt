package com.example.arigo.presentation.air_quality

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.arigo.AriGoApplication
import com.example.arigo.data.remote.firebase.HardwareApiService
import com.example.arigo.domain.model.AirQualityChartPoint
import com.example.arigo.domain.model.AqiStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AirQualityViewModel(
    private val deviceId: String,
    private val hardwareApi: HardwareApiService
) : ViewModel() {

    private val _state = MutableStateFlow(AirQualityState(deviceId = deviceId))
    val state: StateFlow<AirQualityState> = _state.asStateFlow()

    init {
        startPeriodicRefresh()
    }

    /**
     * Switching tabs re-aggregates the same raw data — no network request.
     */
    fun onTimeRangeChange(range: TimeRange) {
        _state.update { current ->
            current.copy(
                selectedTimeRange = range,
                chartData = groupByTimeRange(current.rawChartData, range)
            )
        }
    }

    /**
     * Initial load fetches the full history once. Subsequent refreshes only
     * re-fetch the latest date's points (typically a fraction of the data),
     * which dramatically cuts network usage.
     */
    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            loadHistoryData()
            while (true) {
                delay(REFRESH_INTERVAL_MS)
                refreshLatestData()
            }
        }
    }

    fun loadHistoryData() {
        if (deviceId.isBlank()) {
            _state.update { it.copy(error = "No device selected") }
            return
        }

        val isInitialLoad = _state.value.rawChartData.isEmpty()
        if (isInitialLoad) {
            _state.update { it.copy(isLoading = true, error = null) }
        }

        viewModelScope.launch {
            try {
                val fresh = hardwareApi.getAllHistory(deviceId)

                if (fresh.isEmpty() && _state.value.rawChartData.isEmpty()) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "No data available for this device."
                        )
                    }
                    return@launch
                }

                // Empty fresh on a refresh = transient failure, keep what we have.
                val merged = if (fresh.isEmpty()) _state.value.rawChartData else fresh
                applyDataUpdate(merged)
            } catch (e: Exception) {
                Log.e("AirQuality", "loadHistoryData failed", e)
                if (isInitialLoad) {
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
            }
        }
    }

    /**
     * Lightweight refresh: pulls only the most recent date's data, replaces the
     * existing today's points in rawChartData, and leaves all older dates'
     * points untouched. Roughly 1 HTTP request vs the ~7 that getAllHistory
     * triggers (1 dates listing + 1 per date).
     */
    fun refreshLatestData() {
        val current = _state.value
        val latestDate = current.latestDate
        if (deviceId.isBlank() || latestDate.isBlank()) return

        viewModelScope.launch {
            try {
                val fresh = hardwareApi.getHistoryForDate(deviceId, latestDate)
                if (fresh.isEmpty()) return@launch

                // Drop existing points belonging to latestDate, keep older dates,
                // append the freshly-fetched today.
                val priorDates = _state.value.rawChartData.filter { it.date != latestDate }
                applyDataUpdate(priorDates + fresh)
            } catch (e: Exception) {
                Log.e("AirQuality", "refreshLatestData failed", e)
                // Silent — keep showing existing data.
            }
        }
    }

    private fun applyDataUpdate(merged: List<AirQualityChartPoint>) {
        if (merged.isEmpty()) {
            _state.update { it.copy(isLoading = false) }
            return
        }
        val latest = merged.last()
        _state.update {
            it.copy(
                isLoading = false,
                error = null,
                rawChartData = merged,
                chartData = groupByTimeRange(merged, it.selectedTimeRange),
                latestDate = latest.date,
                currentAqi = latest.beforeAqi,
                currentAqiStatus = AqiStatus.fromAqi(latest.beforeAqi),
                currentPm25 = latest.beforeDust,
                currentPm25Status = pm25Status(latest.beforeDust),
                currentCo = latest.beforeCo,
                currentCoStatus = coStatus(latest.beforeCo)
            )
        }
    }

    private fun groupByTimeRange(
        data: List<AirQualityChartPoint>,
        range: TimeRange
    ): List<AirQualityChartPoint> {
        if (data.isEmpty()) return emptyList()
        return when (range) {
            TimeRange.SECONDS -> data
            TimeRange.MINUTES -> data
                .groupBy { it.timestamp.take(5) }
                .map { (key, points) -> averagePoints(key, points) }
                .sortedBy { it.timestamp }
            TimeRange.HOURS -> data
                .groupBy { it.timestamp.take(2) }
                .map { (key, points) -> averagePoints(key, points) }
                .sortedBy { it.timestamp }
        }
    }

    private fun averagePoints(
        timestamp: String,
        points: List<AirQualityChartPoint>
    ): AirQualityChartPoint = AirQualityChartPoint(
        timestamp = timestamp,
        date = points.lastOrNull()?.date.orEmpty(),
        beforeAqi = points.map { it.beforeAqi }.average().toInt(),
        afterAqi = points.map { it.afterAqi }.average(),
        beforeCo = points.map { it.beforeCo }.average(),
        afterCo = points.map { it.afterCo }.average(),
        beforeDust = points.map { it.beforeDust }.average(),
        afterDust = points.map { it.afterDust }.average(),
        beforeNo2 = points.map { it.beforeNo2 }.average(),
        afterNo2 = points.map { it.afterNo2 }.average()
    )

    private fun pm25Status(value: Double): AqiStatus = when {
        value <= 30.0 -> AqiStatus.GOOD
        value <= 60.0 -> AqiStatus.NORMAL
        else -> AqiStatus.BAD
    }

    private fun coStatus(value: Double): AqiStatus = when {
        value <= 10.0 -> AqiStatus.GOOD
        value <= 30.0 -> AqiStatus.NORMAL
        else -> AqiStatus.BAD
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 30_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AriGoApplication
                val savedStateHandle = createSavedStateHandle()
                val deviceId = savedStateHandle.get<String>("deviceId").orEmpty()
                AirQualityViewModel(
                    deviceId = deviceId,
                    hardwareApi = app.container.hardwareApiService
                )
            }
        }
    }
}
