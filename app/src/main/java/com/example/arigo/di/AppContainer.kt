package com.example.arigo.di

import android.content.Context
import com.example.arigo.data.repository.AuthRepositoryImpl
import com.example.arigo.data.repository.UserRepositoryImpl
import com.example.arigo.domain.repository.AuthRepository
import com.example.arigo.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(private val context: Context) {
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    /** Realtime Database — used for sensor data (airguard_devices/...). */
    val firebaseDatabase: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance().also {
            it.setPersistenceEnabled(true)
        }
    }

    /** Cloud Firestore — used for user profiles and other app data. */
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(firebaseAuth) }
    val userRepository: UserRepository by lazy { UserRepositoryImpl(firestore) }

    // TODO: Add remaining repository implementations in later milestones
    // val deviceRepository: DeviceRepository by lazy { ... }
    // val sensorDataRepository: SensorDataRepository by lazy { ... }
    // val filterRepository: FilterRepository by lazy { ... }
}
