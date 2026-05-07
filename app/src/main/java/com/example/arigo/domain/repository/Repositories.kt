package com.example.arigo.domain.repository

import com.example.arigo.core.common.Resource
import com.example.arigo.domain.model.AirQualityChartPoint
import com.example.arigo.domain.model.FilterHealth
import com.example.arigo.domain.model.PairedDevice
import com.example.arigo.domain.model.SensorReading
import com.example.arigo.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isUserLoggedIn: Boolean
    val currentUserId: String?
    suspend fun loginWithEmail(email: String, password: String): Resource<Unit>
    suspend fun signupWithEmail(email: String, password: String): Resource<String>
    suspend fun loginWithGoogle(idToken: String): Resource<Unit>
    suspend fun sendPasswordReset(email: String): Resource<Unit>
    suspend fun logout()
}

interface UserRepository {
    suspend fun saveUserProfile(profile: UserProfile): Resource<Unit>
    suspend fun getUserProfile(uid: String): Resource<UserProfile>
    fun observeUserProfile(uid: String): Flow<UserProfile?>
}

interface DeviceRepository {
    fun observeDevices(userId: String): Flow<List<PairedDevice>>
    suspend fun addDevice(userId: String, device: PairedDevice): Resource<Unit>
    suspend fun removeDevice(userId: String, deviceId: String): Resource<Unit>
}

interface SensorDataRepository {
    fun observeSensorData(deviceId: String): Flow<SensorReading>
    fun observeSensorHistory(
        deviceId: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): Flow<List<AirQualityChartPoint>>
}

interface FilterRepository {
    fun observeFilterHealth(deviceId: String): Flow<FilterHealth>
    suspend fun resetFilter(deviceId: String): Resource<Unit>
}
