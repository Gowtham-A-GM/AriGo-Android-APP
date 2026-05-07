package com.example.arigo.domain.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val address: String = "",
    val city: String = "",
    val pinCode: String = "",
    val emergencyPhone: String = "",
    val healthIssues: String = "",
    val profileImageUrl: String = ""
)

data class SensorReading(
    val beforeAqi: Int = 0,
    val coPpm: Double = 0.0,
    val dustDensity: Double = 0.0,
    val no2Ppm: Double = 0.0,
    val humidity: Double = 0.0,
    val temperature: Double = 0.0,
    val afterAqi: Double = 0.0,
    val afterCoPpm: Double = 0.0,
    val afterDustDensity: Double = 0.0,
    val afterNo2Ppm: Double = 0.0,
    val motorState: Boolean = false,
    val userLatitude: Double = 0.0,
    val userLongitude: Double = 0.0,
    val recordedAt: String = "",
    val date: String = "",
    val time: String = ""
)

data class SensorReadingDto(
    val beforeAqi: Int = 0,
    val coPpm: Double = 0.0,
    val dustDensity: Double = 0.0,
    val no2Ppm: Double = 0.0,
    val humidity: Double = 0.0,
    val temperature: Double = 0.0,
    val afterAqi: Double = 0.0,
    val afterCoPpm: Double = 0.0,
    val afterDustDensity: Double = 0.0,
    val afterNo2Ppm: Double = 0.0,
    val motorState: Boolean = false,
    val userLatitude: Double = 0.0,
    val userLongitude: Double = 0.0,
    val recordedAt: String = ""
) {
    companion object {
        fun fromFirebaseMap(map: Map<String, Any?>): SensorReadingDto {
            return SensorReadingDto(
                beforeAqi = (map["Before aqi"] as? Number)?.toInt() ?: 0,
                coPpm = (map["co_ppm"] as? Number)?.toDouble() ?: 0.0,
                dustDensity = (map["dust_density"] as? Number)?.toDouble() ?: 0.0,
                no2Ppm = (map["no2_ppm"] as? Number)?.toDouble() ?: 0.0,
                humidity = (map["humidity"] as? Number)?.toDouble() ?: 0.0,
                temperature = (map["temperature"] as? Number)?.toDouble() ?: 0.0,
                afterAqi = (map["After aqi"] as? Number)?.toDouble() ?: 0.0,
                afterCoPpm = (map["After co_ppm"] as? Number)?.toDouble() ?: 0.0,
                afterDustDensity = (map["After dust_density"] as? Number)?.toDouble() ?: 0.0,
                afterNo2Ppm = (map["After no2_ppm"] as? Number)?.toDouble() ?: 0.0,
                motorState = (map["motor_state"] as? Boolean) ?: false,
                userLatitude = (map["User Latitude"] as? Number)?.toDouble() ?: 0.0,
                userLongitude = (map["User Longitude"] as? Number)?.toDouble() ?: 0.0,
                recordedAt = (map["recorded_at"] as? String) ?: ""
            )
        }
    }
    fun toDomain(date: String, time: String): SensorReading {
        return SensorReading(
            beforeAqi = beforeAqi, coPpm = coPpm, dustDensity = dustDensity,
            no2Ppm = no2Ppm, humidity = humidity, temperature = temperature,
            afterAqi = afterAqi, afterCoPpm = afterCoPpm,
            afterDustDensity = afterDustDensity, afterNo2Ppm = afterNo2Ppm,
            motorState = motorState, userLatitude = userLatitude,
            userLongitude = userLongitude, recordedAt = recordedAt,
            date = date, time = time
        )
    }
}

data class Device(
    val id: String = "",
    val name: String = "",
    val modelNumber: String = "",
    val isOnline: Boolean = false,
    val lastSeen: String = "",
    val motorState: Boolean = false,
    val isAutoMode: Boolean = true,
    val filterUsageHours: Int = 0
)

data class PairedDevice(
    val deviceId: String = "",
    val nickname: String = "",
    val productId: String = "",
    val pairedAt: String = "",
    // Named `active` (not `isActive`) so the property name matches Firestore's
    // serialized field name. Firestore strips the "is" prefix on Boolean getters
    // and would otherwise warn at deserialization time.
    val active: Boolean = true
)

data class DeviceStatus(
    val online: Boolean = false,
    val lastSeen: String = ""
)

data class DeviceAlert(
    val threshold: Int = 50,
    val lastCalledAqi: Int = 0,
    val lastCalledAt: String = "",
    val lastCallSid: String = ""
)

enum class AqiStatus(val label: String, val maxValue: Int) {
    GOOD("GOOD", 50),
    NORMAL("NORMAL", 100),
    BAD("BAD", 200),
    HAZARDOUS("HAZARDOUS", 500);
    companion object {
        fun fromAqi(aqi: Int): AqiStatus = when {
            aqi <= GOOD.maxValue -> GOOD
            aqi <= NORMAL.maxValue -> NORMAL
            aqi <= BAD.maxValue -> BAD
            else -> HAZARDOUS
        }
    }
}

data class FilterHealth(
    val usageHours: Int = 0,
    val maxHours: Int = 2000,
    val efficiencyPercent: Int = 100,
    val lastReplacedDate: String = "",
    val estimatedRemainingDays: Int = 0
)

data class AirQualityChartPoint(
    val timestamp: String = "",  // "HH:mm:ss" — time-of-day key from Firebase
    val date: String = "",        // "yyyy-MM-dd" — date folder under history
    val beforeAqi: Int = 0,
    val afterAqi: Double = 0.0,
    val beforeCo: Double = 0.0,
    val afterCo: Double = 0.0,
    val beforeDust: Double = 0.0,
    val afterDust: Double = 0.0,
    val beforeNo2: Double = 0.0,
    val afterNo2: Double = 0.0
)

data class LocationAqi(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val aqi: Int = 0,
    val timestamp: String = ""
)

data class EnvironmentInfo(
    val city: String = "",
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val aqi: Int = 0,
    val aqiStatus: AqiStatus = AqiStatus.GOOD,
    val dustDensity: Double = 0.0,
    val coPpm: Double = 0.0,
    val no2Ppm: Double = 0.0
)
