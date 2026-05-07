package com.example.arigo.data.repository

import com.example.arigo.core.common.Resource
import com.example.arigo.domain.model.PairedDevice
import com.example.arigo.domain.repository.DeviceRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DeviceRepositoryImpl(
    private val firestore: FirebaseFirestore
) : DeviceRepository {

    private fun userDevicesRef(userId: String) =
        firestore.collection("user_devices")
            .document(userId)
            .collection("devices")

    override fun observeDevices(userId: String): Flow<List<PairedDevice>> = callbackFlow {
        val listener = userDevicesRef(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val devices = snapshot?.documents
                ?.mapNotNull { it.toObject<PairedDevice>() }
                ?: emptyList()
            trySend(devices)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addDevice(userId: String, device: PairedDevice): Resource<Unit> {
        return try {
            userDevicesRef(userId).document(device.deviceId).set(device).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add device.")
        }
    }

    override suspend fun removeDevice(userId: String, deviceId: String): Resource<Unit> {
        return try {
            userDevicesRef(userId).document(deviceId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to remove device.")
        }
    }
}
