package com.example.arigo.presentation.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.arigo.AriGoApplication
import com.example.arigo.data.remote.firebase.HardwareApiService
import com.example.arigo.domain.model.AirQualityChartPoint
import com.example.arigo.domain.model.AqiStatus
import com.example.arigo.domain.repository.DeviceRepository
import com.example.arigo.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class HistoryViewModel(
    private val firebaseAuth: FirebaseAuth,
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository,
    private val hardwareApi: HardwareApiService
) : ViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    private var deviceId: String = ""

    init {
        loadUserCity()
        loadDeviceAndData()
    }

    private fun loadUserCity() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        userRepository.observeUserProfile(uid)
            .catch { /* swallow */ }
            .onEach { profile ->
                _state.update { it.copy(city = profile?.city.orEmpty()) }
            }
            .launchIn(viewModelScope)
    }

    private fun initialState(): HistoryState {
        val today = LocalDate.now()
        return HistoryState(
            selectedYear = today.year,
            selectedMonth = today.monthValue,
            selectedDay = today.dayOfMonth,
            selectedDate = formatPeriodLabel(HistoryPeriod.MONTH, today)
        )
    }

    private fun loadDeviceAndData() {
        viewModelScope.launch {
            val uid = firebaseAuth.currentUser?.uid
            if (uid == null) {
                _state.update { it.copy(error = "Not signed in.") }
                return@launch
            }
            try {
                val devices = deviceRepository.observeDevices(uid).first()
                val first = devices.firstOrNull()
                if (first == null) {
                    _state.update { it.copy(error = "No paired devices.") }
                    return@launch
                }
                deviceId = first.deviceId
                loadHistoryData()
            } catch (e: Exception) {
                Log.e("History", "loadDeviceAndData failed", e)
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun onPeriodChange(period: HistoryPeriod) {
        _state.update { current ->
            val anchor = anchorDateOf(current)
            current.copy(
                selectedPeriod = period,
                selectedDate = formatPeriodLabel(period, anchor)
            )
        }
        applyPeriodFilter()
    }

    fun onNavigateForward() {
        shiftAnchor(forward = true)
    }

    fun onNavigateBackward() {
        shiftAnchor(forward = false)
    }

    fun onSearchCityChange(city: String) {
        _state.update { it.copy(searchCity = city) }
    }

    fun loadHistoryData() {
        if (deviceId.isBlank()) return
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val raw = hardwareApi.getAllHistory(deviceId)
                val latest = raw.lastOrNull()
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        rawChartData = raw,
                        availableDates = raw.map { it.date }.distinct().sorted(),
                        currentAqi = latest?.beforeAqi ?: current.currentAqi,
                        currentAqiStatus = latest?.let { AqiStatus.fromAqi(it.beforeAqi) }
                            ?: current.currentAqiStatus,
                        currentPm25 = latest?.beforeDust ?: current.currentPm25,
                        currentPm25Status = latest?.let { pm25Status(it.beforeDust) }
                            ?: current.currentPm25Status,
                        currentCo = latest?.beforeCo ?: current.currentCo,
                        currentCoStatus = latest?.let { coStatus(it.beforeCo) }
                            ?: current.currentCoStatus
                    )
                }
                applyPeriodFilter()
            } catch (e: Exception) {
                Log.e("History", "loadHistoryData failed", e)
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // ---- Period filtering & grouping --------------------------------------

    private fun applyPeriodFilter() {
        val current = _state.value
        val filtered = filterByPeriod(current.rawChartData, current)
        val grouped = groupByPeriod(filtered, current.selectedPeriod)
        _state.update { it.copy(chartData = grouped) }
    }

    private fun filterByPeriod(
        data: List<AirQualityChartPoint>,
        state: HistoryState
    ): List<AirQualityChartPoint> {
        if (data.isEmpty()) return emptyList()
        return when (state.selectedPeriod) {
            HistoryPeriod.DAY -> {
                val target = LocalDate.of(state.selectedYear, state.selectedMonth, state.selectedDay)
                val key = target.format(DATE_FORMAT)
                data.filter { it.date == key }
            }
            HistoryPeriod.WEEK -> {
                val start = LocalDate.of(state.selectedYear, state.selectedMonth, state.selectedDay)
                val end = start.plusDays(6)
                data.filter { runCatching { LocalDate.parse(it.date) }.getOrNull() in start..end }
            }
            HistoryPeriod.MONTH -> data.filter { p ->
                val d = runCatching { LocalDate.parse(p.date) }.getOrNull()
                d != null && d.year == state.selectedYear && d.monthValue == state.selectedMonth
            }
            HistoryPeriod.YEAR -> data.filter { p ->
                runCatching { LocalDate.parse(p.date) }.getOrNull()?.year == state.selectedYear
            }
        }
    }

    private fun groupByPeriod(
        data: List<AirQualityChartPoint>,
        period: HistoryPeriod
    ): List<AirQualityChartPoint> {
        if (data.isEmpty()) return emptyList()
        return when (period) {
            // Group raw points by hour-of-day across the chosen day.
            HistoryPeriod.DAY -> data
                .groupBy { it.timestamp.take(2) }
                .map { (key, points) -> averagePoints(timestamp = key, date = points.first().date, points = points) }
                .sortedBy { it.timestamp }

            // Group by full date (one bucket per day in the week / month).
            HistoryPeriod.WEEK,
            HistoryPeriod.MONTH -> data
                .groupBy { it.date }
                .map { (date, points) ->
                    averagePoints(timestamp = formatDayLabel(date), date = date, points = points)
                }
                .sortedBy { it.date }

            // Group by month (one bucket per month in the year).
            HistoryPeriod.YEAR -> data
                .groupBy { it.date.take(7) } // "yyyy-MM"
                .map { (yearMonth, points) ->
                    averagePoints(timestamp = formatMonthLabel(yearMonth), date = yearMonth, points = points)
                }
                .sortedBy { it.date }
        }
    }

    private fun averagePoints(
        timestamp: String,
        date: String,
        points: List<AirQualityChartPoint>
    ): AirQualityChartPoint = AirQualityChartPoint(
        timestamp = timestamp,
        date = date,
        beforeAqi = points.map { it.beforeAqi }.average().toInt(),
        afterAqi = points.map { it.afterAqi }.average(),
        beforeCo = points.map { it.beforeCo }.average(),
        afterCo = points.map { it.afterCo }.average(),
        beforeDust = points.map { it.beforeDust }.average(),
        afterDust = points.map { it.afterDust }.average(),
        beforeNo2 = points.map { it.beforeNo2 }.average(),
        afterNo2 = points.map { it.afterNo2 }.average()
    )

    // ---- Date navigation --------------------------------------------------

    private fun shiftAnchor(forward: Boolean) {
        _state.update { current ->
            val anchor = anchorDateOf(current)
            val shifted = when (current.selectedPeriod) {
                HistoryPeriod.DAY -> if (forward) anchor.plusDays(1) else anchor.minusDays(1)
                HistoryPeriod.WEEK -> if (forward) anchor.plusDays(7) else anchor.minusDays(7)
                HistoryPeriod.MONTH -> if (forward) anchor.plusMonths(1) else anchor.minusMonths(1)
                HistoryPeriod.YEAR -> if (forward) anchor.plusYears(1) else anchor.minusYears(1)
            }
            current.copy(
                selectedYear = shifted.year,
                selectedMonth = shifted.monthValue,
                selectedDay = shifted.dayOfMonth,
                selectedDate = formatPeriodLabel(current.selectedPeriod, shifted)
            )
        }
        applyPeriodFilter()
    }

    private fun anchorDateOf(state: HistoryState): LocalDate =
        runCatching {
            LocalDate.of(state.selectedYear, state.selectedMonth, state.selectedDay)
        }.getOrElse { LocalDate.now() }

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
        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val DAY_LABEL_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")
        private val FULL_DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
        private val MONTH_LABEL_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM, yyyy")

        private fun formatPeriodLabel(period: HistoryPeriod, anchor: LocalDate): String =
            when (period) {
                HistoryPeriod.DAY -> anchor.format(FULL_DAY_FORMAT)
                HistoryPeriod.WEEK -> {
                    val end = anchor.plusDays(6)
                    "${anchor.format(DAY_LABEL_FORMAT)} - ${end.format(DAY_LABEL_FORMAT)}, ${anchor.year}"
                }
                HistoryPeriod.MONTH -> anchor.format(MONTH_LABEL_FORMAT)
                HistoryPeriod.YEAR -> anchor.year.toString()
            }

        private fun formatDayLabel(date: String): String =
            runCatching { LocalDate.parse(date).dayOfMonth.toString() }.getOrDefault(date)

        private fun formatMonthLabel(yearMonth: String): String {
            val parsed = runCatching {
                LocalDate.parse("$yearMonth-01").month
                    .getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
            return parsed.getOrDefault(yearMonth)
        }

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AriGoApplication
                HistoryViewModel(
                    firebaseAuth = app.container.firebaseAuth,
                    deviceRepository = app.container.deviceRepository,
                    userRepository = app.container.userRepository,
                    hardwareApi = app.container.hardwareApiService
                )
            }
        }
    }
}
