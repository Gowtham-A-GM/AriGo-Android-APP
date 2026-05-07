package com.example.arigo.di

import android.content.Context
import com.example.arigo.data.repository.AuthRepositoryImpl
import com.example.arigo.data.repository.UserRepositoryImpl
import com.example.arigo.domain.repository.AuthRepository
import com.example.arigo.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AppContainer(private val context: Context) {
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firebaseDatabase: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance().also {
            it.setPersistenceEnabled(true)
        }
    }

    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(firebaseAuth) }
    val userRepository: UserRepository by lazy { UserRepositoryImpl(firebaseDatabase) }

    // TODO: Add remaining repository implementations in later milestones
    // val deviceRepository: DeviceRepository by lazy { ... }
    // val sensorDataRepository: SensorDataRepository by lazy { ... }
    // val filterRepository: FilterRepository by lazy { ... }
}
