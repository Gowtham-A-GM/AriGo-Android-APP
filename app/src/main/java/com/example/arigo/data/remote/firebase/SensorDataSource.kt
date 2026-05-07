package com.example.arigo.data.remote.firebase

import com.example.arigo.core.common.Constants
import com.example.arigo.domain.model.SensorReading
import com.example.arigo.domain.model.SensorReadingDto
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Reads sensor data from the hardware Firebase project at:
 *   airguard_devices/{deviceId}/history/{date}/{time} -> SensorReading map
 *
 * The latest reading is the lexicographically largest date key, then the
 * lexicographically largest time key inside it.
 */
class SensorDataSource(
    private val firebaseDatabase: FirebaseDatabase
) {

    private fun historyRef(deviceId: String) =
        firebaseDatabase.getReference(Constants.FIREBASE_DEVICES_PATH)
            .child(deviceId)
            .child("history")

    suspend fun getLatestSensorReading(deviceId: String): SensorReading? {
        val snapshot = historyRef(deviceId).get().await()
        return parseLatest(snapshot)
    }

    fun observeLatestReading(deviceId: String): Flow<SensorReading?> = callbackFlow {
        val ref = historyRef(deviceId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(parseLatest(snapshot))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private fun parseLatest(historySnapshot: DataSnapshot): SensorReading? {
        val latestDate = historySnapshot.children
            .maxByOrNull { it.key.orEmpty() } ?: return null
        val date = latestDate.key.orEmpty()

        val latestTime = latestDate.children
            .maxByOrNull { it.key.orEmpty() } ?: return null
        val time = latestTime.key.orEmpty()

        @Suppress("UNCHECKED_CAST")
        val map = latestTime.value as? Map<String, Any?> ?: return null

        return SensorReadingDto.fromFirebaseMap(map).toDomain(date = date, time = time)
    }
}
