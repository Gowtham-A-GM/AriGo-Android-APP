package com.example.arigo.data.repository

import com.example.arigo.core.common.Resource
import com.example.arigo.domain.model.UserProfile
import com.example.arigo.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun saveUserProfile(profile: UserProfile): Resource<Unit> {
        return try {
            usersCollection.document(profile.uid).set(profile).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save profile")
        }
    }

    override suspend fun getUserProfile(uid: String): Resource<UserProfile> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            val profile = snapshot.toObject<UserProfile>()
            if (profile != null) {
                Resource.Success(profile)
            } else {
                Resource.Error("Profile not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get profile")
        }
    }

    override fun observeUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val listener = usersCollection.document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val profile = snapshot?.toObject<UserProfile>()
                trySend(profile)
            }
        awaitClose { listener.remove() }
    }
}
