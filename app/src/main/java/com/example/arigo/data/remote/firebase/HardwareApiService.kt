package com.example.arigo.data.remote.firebase

import android.util.Log
import com.example.arigo.domain.model.SensorReading
import com.example.arigo.domain.model.SensorReadingDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import java.net.URL

private const val TAG = "SensorData"
private const val BASE_URL = "https://test-1-eee0e-default-rtdb.firebaseio.com"

/**
 * Reads sensor data from the hardware team's Firebase Realtime Database via the
 * REST API. Their database has public read rules but lives in a different
 * Firebase project, which means our SDK cannot authenticate against it. Hitting
 * `<url>.json` over HTTP bypasses authentication entirely (allowed because their
 * rules are public).
 *
 * Path: airguard_devices/{deviceId}/history/{date}/{time} → SensorReading map
 */
class HardwareApiService {

    suspend fun getLatestSensorReading(deviceId: String): SensorReading? {
        return try {
            // Step 1: list dates (shallow returns just keys)
            val historyUrl = "$BASE_URL/airguard_devices/$deviceId/history.json?shallow=true"
            val datesJson = URL(historyUrl).readText()
            Log.d(TAG, "Dates response: $datesJson")

            if (datesJson == "null" || datesJson.isEmpty()) {
                Log.w(TAG, "No history data for device $deviceId")
                return null
            }

            val datesObj = JSONObject(datesJson)
            val dates = datesObj.keys().asSequence().toList().sorted()
            val latestDate = dates.lastOrNull() ?: return null
            Log.d(TAG, "Latest date: $latestDate")

            // Step 2: list times within the latest date
            val timeUrl = "$BASE_URL/airguard_devices/$deviceId/history/$latestDate.json?shallow=true"
            val timesJson = URL(timeUrl).readText()

            if (timesJson == "null" || timesJson.isEmpty()) return null

            val timesObj = JSONObject(timesJson)
            val times = timesObj.keys().asSequence().toList().sorted()
            val latestTime = times.lastOrNull() ?: return null
            Log.d(TAG, "Latest time: $latestTime")

            // Step 3: fetch the actual reading
            val readingUrl = "$BASE_URL/airguard_devices/$deviceId/history/$latestDate/$latestTime.json"
            val readingJson = URL(readingUrl).readText()
            Log.d(TAG, "Reading response: $readingJson")

            if (readingJson == "null" || readingJson.isEmpty()) return null

            val readingObj = JSONObject(readingJson)
            val map = mutableMapOf<String, Any?>()
            readingObj.keys().forEach { key ->
                map[key] = if (readingObj.isNull(key)) null else readingObj.get(key)
            }

            val reading = SensorReadingDto.fromFirebaseMap(map).toDomain(latestDate, latestTime)
            Log.d(
                TAG,
                "Parsed: aqi=${reading.beforeAqi}, temp=${reading.temperature}, " +
                    "humidity=${reading.humidity}, co=${reading.coPpm}, dust=${reading.dustDensity}"
            )
            reading
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching sensor data", e)
            null
        }
    }

    /**
     * Polls the hardware DB every [intervalMs] and emits readings as they arrive.
     * Cancellation propagates through delay() automatically when the consuming
     * coroutine is cancelled.
     */
    fun observeLatestReading(deviceId: String, intervalMs: Long = 10_000L): Flow<SensorReading> = flow {
        Log.d(TAG, "Starting REST polling for device: $deviceId (every ${intervalMs}ms)")
        while (true) {
            val reading = getLatestSensorReading(deviceId)
            if (reading != null) emit(reading)
            delay(intervalMs)
        }
    }.flowOn(Dispatchers.IO)
}
