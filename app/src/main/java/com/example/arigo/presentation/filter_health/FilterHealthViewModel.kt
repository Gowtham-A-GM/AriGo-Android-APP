package com.example.arigo.presentation.filter_health

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.arigo.AriGoApplication
import com.example.arigo.data.remote.firebase.HardwareApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class FilterHealthViewModel(
    private val deviceId: String,
    private val hardwareApi: HardwareApiService
) : ViewModel() {

    private val _state = MutableStateFlow(FilterHealthState(deviceId = deviceId))
    val state: StateFlow<FilterHealthState> = _state.asStateFlow()

    init {
        if (deviceId.isNotBlank()) {
            observeSensorData()
        }
    }

    private fun observeSensorData() {
        hardwareApi.observeLatestReading(deviceId)
            .catch { e -> Log.e("FilterHealth", "sensor flow failed", e) }
            .onEach { reading ->
                val efficiency = calculateEfficiency(reading.beforeAqi, reading.afterAqi)
                _state.update { it.copy(efficiencyPercent = efficiency) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Filter efficiency = ((before - after) / before) * 100, clamped to [0, 100].
     * If [beforeAqi] is non-positive (no measurable pollution), report 100% so
     * the UI doesn't display 0% on a clean-air reading.
     */
    private fun calculateEfficiency(beforeAqi: Int, afterAqi: Double): Int {
        if (beforeAqi <= 0) return 100
        val pct = (beforeAqi - afterAqi) / beforeAqi.toDouble() * 100.0
        return pct.toInt().coerceIn(0, 100)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AriGoApplication
                val savedStateHandle = createSavedStateHandle()
                val deviceId = savedStateHandle.get<String>("deviceId").orEmpty()
                FilterHealthViewModel(
                    deviceId = deviceId,
                    hardwareApi = app.container.hardwareApiService
                )
            }
        }
    }
}
