package com.example.arigo.di

import android.content.Context
import com.example.arigo.data.remote.firebase.HardwareApiService
import com.example.arigo.data.repository.AuthRepositoryImpl
import com.example.arigo.data.repository.DeviceRepositoryImpl
import com.example.arigo.data.repository.UserRepositoryImpl
import com.example.arigo.domain.repository.AuthRepository
import com.example.arigo.domain.repository.DeviceRepository
import com.example.arigo.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(private val context: Context) {
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    /**
     * Our Firebase Realtime Database (default project from google-services.json).
     * Reserved for app-owned data that needs streaming reads (notifications, etc.).
     */
    val firebaseDatabase: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance().also {
            it.setPersistenceEnabled(true)
        }
    }

    /** Cloud Firestore — used for user profiles and device pairings. */
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(firebaseAuth) }
    val userRepository: UserRepository by lazy { UserRepositoryImpl(firestore) }
    val deviceRepository: DeviceRepository by lazy { DeviceRepositoryImpl(firestore) }

    /**
     * Reads sensor data from the hardware team's Firebase via REST API.
     * Their database has public read rules but lives in a different Firebase
     * project, so we cannot use the SDK (which would try to authenticate with
     * our project's credentials and fail). Plain HTTPS reads bypass auth.
     */
    val hardwareApiService: HardwareApiService by lazy { HardwareApiService() }

    // TODO: Add remaining repository implementations in later milestones
    // val sensorDataRepository: SensorDataRepository by lazy { ... }
    // val filterRepository: FilterRepository by lazy { ... }
}
